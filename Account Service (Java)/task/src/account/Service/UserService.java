package account.Service;

import account.Entity.User;
import account.Models.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    public static final int MAX_FAILED_ATTEMPTS = 5;

    User getEmail(String email);

    UserDTO verifyUser(UserDTO userDto);

    UserDTO getUserByEmail(String email);

    SuccessDTO changePassword(String password,String userPassword, String username);

    List<UserDTO> getAllUsers();

    void deleteUserByEmail(String userEmail, String adminEmail);

    void increaseFailedAttempts (User user);

    void lockUser(User user);

    void resetFailedAttempts(String email);

    boolean unlockWhenTimeExpired(User user);

    String changeAccess(ChangeAccess access, String adminEmail);
}


