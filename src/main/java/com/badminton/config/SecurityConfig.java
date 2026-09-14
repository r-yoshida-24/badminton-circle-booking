package com.badminton.config;

import com.badminton.security.RoleBasedAuthenticationFailureHandler;
import com.badminton.security.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final RoleBasedAuthenticationSuccessHandler successHandler;
    private final RoleBasedAuthenticationFailureHandler failureHandler;

    public SecurityConfig(RoleBasedAuthenticationSuccessHandler successHandler,
                          RoleBasedAuthenticationFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/participant/login", "/participant/register", "/login").permitAll()
                .requestMatchers("/admin/register", "/admin/dashboard").hasRole("ADMIN")
                .requestMatchers("/participant/dashboard").hasRole("PARTICIPANT")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/participant/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/participant/login?logout"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
