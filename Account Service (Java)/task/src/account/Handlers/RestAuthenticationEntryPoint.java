package account.Handlers;

import account.Entity.Role;
import account.Entity.User;
import account.Service.SecurityService;
import account.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws ServletException, IOException {

        String path = (String) request.getAttribute("path");
        String message = "unauthorized";

        String headerName = request.getHeader("authorization");
        if(headerName == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "headername is null");

        } else {
            byte [] decodedBytes = Base64.getDecoder().decode(headerName.split(" ")[1]);
            String decodedString = new String(decodedBytes);
            String[] credentials = decodedString.split(":");
            String username = credentials[0];


            User user = userService.getEmail(username);
            Set<String> userRoles = new HashSet<>();


            if(user != null){
                for(Role role: user.getRoles()){
                    userRoles.add(role.getName());
                }
                if(!userRoles.contains("ROLE_ADMINISTRATOR")){
                    if(user.isAccountNonLocked()){
                        if(user.getFailedAttempts() < UserService.MAX_FAILED_ATTEMPTS - 1  ){
                            userService.increaseFailedAttempts(user);
                            securityService.loginFailed(username, path);
                        } else {
                            securityService.loginFailed(username, path);
                            if(user.getFailedAttempts() == 4){
                                userService.lockUser(user);
                                securityService.bruteForce(username, path);
                                securityService.lockUser(username, path);
                            }
                            userService.increaseFailedAttempts(user);
                            throw new LockedException("Your account has been locked due to 5 failed attempts. It will be unlocked after 24 hours");
                        }
                    } else {
                        message = "User account is locked";
                        if(userService.unlockWhenTimeExpired(user)){
                            throw new LockedException("Your account has been unlocked. Please try again");
                        }
                    }
                } else {
                    userService.increaseFailedAttempts(user);
                    securityService.loginFailed(username, path);
                }

            }

            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            Map<String, Object> data = new HashMap<>();
            data.put(
                    "timestamp",
                    String.valueOf(LocalDateTime.now()));
            data.put("status", HttpStatus.UNAUTHORIZED.value());
            data.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
            data.put(
                    "message",
                    message);
            data.put("path", path);

            response.getWriter().write(objectMapper.writeValueAsString(data));
        }
    }

}
