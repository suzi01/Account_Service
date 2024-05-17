package account.Listeners;

import account.Entity.User;
import account.Repository.UserRepository;
import account.Service.SecurityService;
import account.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthListener {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;


    @EventListener
    public void onAuthenticationFailureLocked(AuthenticationFailureLockedEvent event){
        System.out.println("This is the authentication locked event method in authListener");
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){

        String username = event.getAuthentication().getName();
        User user = userService.getEmail(username);
        if(user.getFailedAttempts() > 0) {
            userService.resetFailedAttempts(user.getEmail());
        }
    }




}
