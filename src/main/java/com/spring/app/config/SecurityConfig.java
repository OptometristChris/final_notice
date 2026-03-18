package com.spring.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.spring.app.jwt.JwtAuthenticationFilter;
import com.spring.app.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/bootstrap-4.6.2-dist/**",
                    "/jquery-ui-1.13.1.custom/**",
                    "/smarteditor/**"
                ).permitAll()

                .requestMatchers("/notice/list", "/notice", "/notice/detail/**").permitAll()

                .requestMatchers("/notice/write", "/notice/edit/**", "/notice/delete")
                    .hasRole("ADMIN_BRANCH")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form.disable())

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                             UsernamePasswordAuthenticationFilter.class)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }
}