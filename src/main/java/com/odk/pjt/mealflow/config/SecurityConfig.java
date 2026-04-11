package com.odk.pjt.mealflow.config;

import com.odk.pjt.mealflow.security.AccountLinkageOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AccountLinkageOidcUserService oidcUserService)
            throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/oauth2/**", "/error", "/css/**", "/favicon.ico")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth -> oauth.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService)))
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
