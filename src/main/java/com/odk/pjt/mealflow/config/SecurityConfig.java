package com.odk.pjt.mealflow.config;

import com.odk.pjt.mealflow.security.AccountLinkageOidcUserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AccountLinkageOidcUserService oidcUserService,
            @Value("${mealflow.frontend.login-url}") String frontendLoginUrl)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/error",
                                "/css/**",
                                "/favicon.ico")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .loginPage(frontendLoginUrl)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService)))
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
