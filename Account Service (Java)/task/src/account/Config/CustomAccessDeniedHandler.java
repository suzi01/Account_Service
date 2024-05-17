package account.Config;

import account.Service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private SecurityService securityService;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        Map<String, Object> data = new HashMap<>();
        data.put(
                "timestamp",
                String.valueOf(LocalDateTime.now()));
        data.put("status", HttpStatus.FORBIDDEN.value());
        data.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        data.put(
                "message",
                "Access Denied!");
        data.put("path", request.getRequestURI());
        securityService.accessDenied(request.getUserPrincipal().getName(),request.getRequestURI());


        response.getWriter().write(objectMapper.writeValueAsString(data));
    }
}
