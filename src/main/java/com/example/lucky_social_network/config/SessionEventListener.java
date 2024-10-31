package com.example.lucky_social_network.config;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AuthenticationService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionEventListener implements HttpSessionListener {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (securityContext != null && securityContext.getAuthentication() != null) {
            try {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (principal instanceof User) {
                    User user = (User) principal;
                    authenticationService.handleLogout(user);
                    log.info("Session destroyed for user: {}", user.getUsername());
                }
            } catch (Exception e) {
                log.error("Error handling session destruction: {}", e.getMessage());
            }
        }
    }


    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Можно добавить логику при создании сессии, если нужно
        log.debug("New session created: {}", se.getSession().getId());
    }
}
