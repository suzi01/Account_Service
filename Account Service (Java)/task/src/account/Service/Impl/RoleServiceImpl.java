package account.Service.Impl;

import account.Entity.Role;
import account.Entity.User;
import account.Exceptions.InvalidRoleException;
import account.Exceptions.NotFoundUserException;
import account.Models.ChangeRole;
import account.Models.UserDTO;
import account.Repository.RoleRepository;
import account.Repository.UserRepository;
import account.Service.RoleService;
import account.Service.SecurityService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleServiceImpl implements RoleService {

    private final UserRepository repository;

    private final RoleRepository roleRepository;
    private final SecurityService securityService;

    private final List<String> rolesList = List.of("ROLE_ADMINISTRATOR", "ROLE_USER","ROLE_ACCOUNTANT", "ROLE_AUDITOR");

    public RoleServiceImpl(UserRepository repository, RoleRepository roleRepository, SecurityService securityService) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    @Override
    public UserDTO changeUserRole(ChangeRole changeRole, String adminEmail) {
        User user = repository.findUserByEmailIgnoreCase(changeRole.getUser());
        if(user == null){
            // check user exists (else send error not found)
            throw new NotFoundUserException("User not found!");
        }
        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        // check role exists (else send error not found)
        String prefixRole = "ROLE_" + changeRole.getRole();

        if(!rolesList.contains(prefixRole)){
            throw new NotFoundUserException("Role not found!");
        }

        if(!roleNames.contains(prefixRole) && changeRole.getOperation().equals("REMOVE")){
            // if action is delete but user doesn't have role crete bad request error
            throw new InvalidRoleException("The user does not have a role!");
        }
        if(roleNames.contains("ROLE_ADMINISTRATOR") && prefixRole.equals("ROLE_ADMINISTRATOR")){
            // try to remove the ROLE_ADMINISTRATOR
            throw new InvalidRoleException("Can't remove ADMINISTRATOR role!");
        }

        if(roleNames.size() == 1 && changeRole.getOperation().equals("REMOVE")){
            // if action is delete and user only has one role
            throw new InvalidRoleException("The user must have at least one role!");
        }


        if(roleNames.contains("ROLE_ADMINISTRATOR") ||
                roleNames.contains("ROLE_ACCOUNTANT") && prefixRole.equals("ROLE_ADMINISTRATOR")){
            throw new InvalidRoleException("The user cannot combine administrative and business roles!");
        }
        Role newRole = roleRepository.findByName(prefixRole);
        if(changeRole.getOperation().equals("GRANT")){
            user.addRole(newRole);
            securityService.grantRole(changeRole.getUser(),adminEmail, changeRole.getRole() );
        }
        else {
            user.removeRole(newRole);
            securityService.removeRole(changeRole.getUser(),adminEmail, changeRole.getRole() );
        }

        repository.save(user);
        UserDTO returnDto =  new UserDTO(
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getPassword()
        );
        returnDto.setId(user.getId());
        Set<String> roleStrings = new TreeSet<>();
        for (Role role : user.getRoles()){
            roleStrings.add(role.getName());
        }
        returnDto.setRoles(roleStrings);
        return returnDto;

    }
}
