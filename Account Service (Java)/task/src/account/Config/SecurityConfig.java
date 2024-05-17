package account.Config;

import account.Handlers.RestAuthenticationEntryPoint;
//import account.Handlers.CustomAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private CustomAccessDeniedHandler customAccessDeniedHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(getEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .csrf(AbstractHttpConfigurer::disable)// or POST requests via Postman
                .headers(headers -> headers.frameOptions().disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()

                        .requestMatchers(HttpMethod.POST, "api/auth/changepass").hasAnyAuthority("ROLE_USER","ROLE_ACCOUNTANT","ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasRole("ACCOUNTANT")


                        .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority("ROLE_USER","ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/security/events/").hasAuthority("ROLE_AUDITOR")

                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasAuthority("ROLE_ADMINISTRATOR")

                        .requestMatchers(toH2Console()).permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()
                        .anyRequest().authenticated()
                )


                .csrf(AbstractHttpConfigurer::disable).headers(headers -> headers.frameOptions().disable())// for POST requests via Postman
                .build();
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint getEntryPoint(){
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}
