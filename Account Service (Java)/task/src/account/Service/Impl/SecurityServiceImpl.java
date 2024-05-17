package account.Service.Impl;

import account.Entity.SecurityEvents;
import account.InformationEvents.SecurityEventsLogs;
import account.Models.SecurityDto;
import account.Repository.SecurityServiceRepository;
import account.Service.SecurityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {


    private final SecurityServiceRepository securityServiceRepository;

    public SecurityServiceImpl(SecurityServiceRepository securityServiceRepository) {
        this.securityServiceRepository = securityServiceRepository;
    }

    @Override
    public void createUser(String email) {
        SecurityEvents securityEvents = new SecurityEvents();
        securityEvents.setDate(String.valueOf(LocalDateTime.now()));
        securityEvents.setAction(SecurityEventsLogs.createUser);
        securityEvents.setSubject("Anonymous");
        securityEvents.setObject(email.toLowerCase());
        securityEvents.setPath("api/auth/signup");
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void loginFailed(String email, String url) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.loginFailed,
                email.toLowerCase(),
                url,
                url
        );

        securityServiceRepository.save(securityEvents);
    }
    @Override
    public void changePassword(String email) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.changePassword,
                email.toLowerCase(),
                email.toLowerCase(),
                "/api/auth/changepass"
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void accessDenied(String email, String url) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.accessDenied,
                email.toLowerCase(),
                url,
                url
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void grantRole(String email, String adminEmail, String role) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.grantRole,
                adminEmail.toLowerCase(),
                String.format("Grant role %s to %s", role, email.toLowerCase()),
                "/api/admin/user/role"
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void removeRole(String email, String adminEmail, String role) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.removeRole,
                adminEmail.toLowerCase(),
                String.format("Remove role %s from %s",role, email.toLowerCase()),
                "/api/admin/user/access"
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void lockUser(String email, String pathUrl) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.lockUser,
                email.toLowerCase(),
                String.format("Lock user %s",email),
                pathUrl
        );

        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void unlockUser(String adminEmail, String userEmail) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.unlockUser,
                adminEmail.toLowerCase(),
                String.format("Unlock user %s", userEmail.toLowerCase()),
                "/api/admin/user/access"
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void deleteUser(String email, String adminEmail) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.deleteUser,
                adminEmail.toLowerCase(),
                email.toLowerCase(),
                "/api/admin/user"
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public void bruteForce(String email, String url) {
        SecurityEvents securityEvents = new SecurityEvents(
                String.valueOf(LocalDateTime.now()),
                SecurityEventsLogs.bruteForce,
                email.toLowerCase(),
                url,
                url
        );
        securityServiceRepository.save(securityEvents);
    }

    @Override
    public List<SecurityDto> getAllEvents() {
        List<SecurityDto> securityDtos = new ArrayList<>();
        List<SecurityEvents>  securityEventsList = securityServiceRepository.findAllByOrderByIdAsc();
        for (SecurityEvents eventLog : securityEventsList){
            SecurityDto newLog = new SecurityDto(
                    eventLog.getDate(),
                    eventLog.getAction(),
                    eventLog.getSubject(),
                    eventLog.getObject(),
                    eventLog.getPath()
            );
            securityDtos.add(newLog);
        }
        return securityDtos;
    }
}
