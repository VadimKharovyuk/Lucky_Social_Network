package com.example.lucky_social_network.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
    @Autowired
    private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;

    @Bean
    public SessionEventListener sessionEventListener() {
        return new SessionEventListener();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Базовые настройки безопасности
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/**", "/chat/send")  // Игнорируем CSRF для определенных URL
                        .disable()
                )

                // Настройка авторизации запросов
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/",
                                "/luda",
                                "/chat/send",
                                "/register",
                                "/login",
                                "/css/**",
                                "/auth/**",
                                "/auth/forgot-password",
                                "/profile-list",
                                "/about",
                                "/news",
                                "/js/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/moderator/**").hasAnyRole("JUNIOR_ADMIN", "SENIOR_ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )

                // Настройка формы логина
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/posts", true)
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Настройка выхода из системы
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll()
                )

                // Настройка управления сессиями
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)  // Ограничение на количество активных сессий пользователя
                        .maxSessionsPreventsLogin(false)  // Новый вход завершает предыдущую сессию
                        .expiredUrl("/login?expired")  // URL при истечении сессии
                )

                // Настройка обработки исключений
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (isAjaxRequest(request)) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Вспомогательный метод для определения AJAX-запросов
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

//    // Настройка CORS если необходимо
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}


//
//package com.example.lucky_social_network.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    @Autowired
//    private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
//
//
//    @Bean
//    public SessionEventListener sessionEventListener() {
//        return new SessionEventListener();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeHttpRequests((requests) -> requests
//                                .requestMatchers("/", "/chat/send", "/register",
//                                        "/login", "/css/**", "/auth/**", "/auth/forgot-password", "/profile-list", "/about", "/news",
//                                        "/js/**")
//                                .permitAll()
////                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
//                                .requestMatchers("/moderator/**").hasAnyRole("JUNIOR_ADMIN", "SENIOR_ADMIN", "SUPER_ADMIN")
//                                .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/posts", true)
//                        .successHandler(authenticationSuccessHandler)
//                        .failureUrl("/login?error")
//                        .permitAll()
//                )
//                .logout((logout) -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
//        return authConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
