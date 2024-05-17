package account.Service;

import account.Models.SecurityDto;

import java.util.List;

public interface SecurityService {
    void createUser(String email);

    void loginFailed(String email, String url );

    void changePassword(String email);

    void accessDenied(String email, String url);

    void grantRole(String email, String adminEmail, String role);

    void removeRole(String email, String adminEmail, String role);

    void lockUser(String email, String pathUrl);

    void unlockUser(String adminEmail, String userEmail);

    void deleteUser(String email,  String adminEmail);

    void bruteForce(String email, String url);

    List<SecurityDto> getAllEvents();
}
