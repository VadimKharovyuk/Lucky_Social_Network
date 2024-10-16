package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Admin admin) {
        this.user = user;
        this.authorities = getAuthoritiesForUser(user, admin);
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesForUser(User user, Admin admin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (admin != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()));
        }

        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Вы можете добавить проверку статуса пользователя здесь в будущем
    }

    public Long getId() {
        return this.user.getId();
    }

    // Дополнительный метод для проверки, является ли пользователь админом
    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN"));
    }

    // Метод для получения роли админа (если есть)
    public String getAdminRole() {
        return authorities.stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_") && !a.getAuthority().equals("ROLE_USER") && !a.getAuthority().equals("ROLE_ADMIN"))
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }
}

//package com.example.lucky_social_network.service;
//
//import com.example.lucky_social_network.model.User;
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//import java.util.Collections;
//
//@Getter
//public class CustomUserDetails implements UserDetails {
//
//    private final User user;
//
//    public CustomUserDetails(User user) {
//        this.user = user;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // Здесь вы можете реализовать логику для ролей пользователя
//        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return user.getUsername();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true; // временно уберите проверку email verification
//    }
//
//    public Long getId() {
//      return   this.user.getId();
//    }
//}