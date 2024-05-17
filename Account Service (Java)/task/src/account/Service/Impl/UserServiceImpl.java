package account.Service.Impl;

import account.Entity.Role;
import account.Exceptions.*;
import account.Models.*;
import account.Repository.RoleRepository;
import account.Repository.UserRepository;
import account.Service.SecurityService;
import account.Service.UserService;
import account.UserAdaptor.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import account.Entity.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
public class UserServiceImpl implements UserDetailsService, UserService {


    private static final long LOCK_TIME_DURATION =  24 * 60 * 60 * 1000;

    private final UserRepository repository;


    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final SecurityService securityService;


    private final List<String> blacklistPasswords = List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");



    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder,

                           RoleRepository roleRepository,
                           SecurityService securityService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    public boolean isPasswordBreached(String password){
        return blacklistPasswords.contains(password);
    }


    public User getEmail(String email){
        return repository.findUserByEmailIgnoreCase(email);
    }


    @Override
    public UserDTO verifyUser(UserDTO userDto) {
        if (repository.existsUserByEmailIgnoreCase(userDto.getEmail())) {
            throw new UserAlreadyExistsException("User exist!");
        }

        if(isPasswordBreached(userDto.getPassword())){
            throw new BreachedPasswordException("The password is in the hacker's database!");
        }
        if(userDto.getPassword().length() < 12){
            throw new PasswordLengthException("Password length must be 12 chars minimum!");
        }
        Set<Role> roleSet = new HashSet<>();
        Set<String> roleString = new TreeSet<>();
        boolean isDatabaseEmpty = repository.existsAnyUser();
        User newUser = new User();
        newUser.setEmail(userDto.getEmail().toLowerCase());
        newUser.setName(userDto.getName());
        newUser.setLastname(userDto.getLastname());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if(isDatabaseEmpty){
            Role role = roleRepository.findByName("ROLE_USER");
            roleSet.add(role);
            roleString.add("ROLE_USER");
            newUser.setRoles(roleSet);
        }else {
            Role role = roleRepository.findByName("ROLE_ADMINISTRATOR");
            roleString.add("ROLE_ADMINISTRATOR");
            roleSet.add(role);
            newUser.setRoles(roleSet);
        }
        newUser.setAccountNonLocked(true);
        repository.save(newUser);
        userDto.setId(newUser.getId());
        userDto.addRoles(roleString);
        securityService.createUser(userDto.getEmail());
        return userDto;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        UserDTO userDTO = new UserDTO();
        User user = getEmail(email);
        if(user != null){
            Set<String> roleString = new TreeSet<>();
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            userDTO.setLastname(user.getLastname());
            userDTO.setPassword(user.getPassword());
            userDTO.setId(user.getId());
            for(Role role: user.getRoles()){
                roleString.add(role.getName());
            }
            userDTO.addRoles(roleString);
        }
        else{
            throw new UnauthorizedRequestException("");
        }
        return userDTO;
    }

    @Override
    public SuccessDTO changePassword(String password, String userPassword, String email) {
        if(passwordEncoder.matches(password, userPassword)){
            throw new SamePasswordException("The passwords must be different!");
        }
        if(isPasswordBreached(password)){
            throw new BreachedPasswordException("The password is in the hacker's database!");
        }
        if(password.length() < 12){
            throw new PasswordLengthException("Password length must be 12 chars minimum!");
        }
        User user = repository
                .findUserByEmailIgnoreCase(email);
        if(user == null){
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        user.setPassword(passwordEncoder.encode(password));
        securityService.changePassword(email);
        repository.save(user);
        return new SuccessDTO(email, "The password has been updated successfully");
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserDTO> foundUsers = new ArrayList<>();
        List<User> users = repository.findAllByOrderByIdAsc();
        for(User user: users){
            Set<String> roleString = new TreeSet<>();
            UserDTO userDTO = new UserDTO(
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getPassword()
            );
            userDTO.setId(user.getId());
            for(Role role: user.getRoles()){
                roleString.add(role.getName());
            }

            userDTO.addRoles(roleString);
            foundUsers.add(userDTO);
        }
        return foundUsers;
    }

    @Transactional
    @Override
    public void deleteUserByEmail(String userEmail, String adminEmail) {
        User user = repository.findUserByEmailIgnoreCase(userEmail);
        if(user == null){
            throw new NotFoundUserException("User not found!");
        }
        repository.deleteUserById(user.getId());
        securityService.deleteUser(userEmail, adminEmail);
    }

    @Override
    public String changeAccess(ChangeAccess access, String adminEmail) {
        User user = getEmail(access.getUser());
        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        if(user == null){
            throw new NotFoundUserException("User not found!");
        }
        if(roleNames.contains("ROLE_ADMINISTRATOR") && access.getOperation().equals("LOCK")){
            throw new InvalidUserException("Can't lock the ADMINISTRATOR!");
        }
        if(access.getOperation().equals("LOCK")){
            user.setAccountNonLocked(false);
            securityService.lockUser(access.getUser(),"/api/admin/user/access");
            return String.format("User %s locked!", access.getUser().toLowerCase());
        }
        user.setAccountNonLocked(true);
        securityService.unlockUser(adminEmail,access.getUser());
        return String.format("User %s unlocked!", access.getUser().toLowerCase());
    }

    @Transactional
    @Override
    public void increaseFailedAttempts(User user){
        int newFailedAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailedAttempts);
        repository.save(user);
    }

    @Transactional
    @Override
    public void resetFailedAttempts(String email){
        repository.updateFailedAttempts(0, email);
    }

    @Override
    public void lockUser(User user){
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());

        repository.save(user);
    }

    @Override
    public boolean unlockWhenTimeExpired(User user){
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if(lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis){
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempts(0);
            repository.save(user);
            return true;
        }
        return false;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String path = request.getRequestURI();

        request.setAttribute("path", path);
        try {
            User user = getEmail(email);
            if(user == null){
                securityService.loginFailed(email, path);
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

            if(!user.isAccountNonLocked()){
                throw new UsernameNotFoundException("Yea, you're blocked");
            }
            user.setFailedAttempts(0);


            return new CustomUserDetails(user);

        }catch (Exception e){
            throw new UsernameNotFoundException("This is in the loadUser method");
        }

    }


}
