package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.UnauthorizedAccessException;
import com.example.lucky_social_network.exception.UserNotFoundException;
import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.AdminRepository;
import com.example.lucky_social_network.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    @Transactional
    public Admin convertUserToAdmin(Long userId, Admin.AdminRole adminRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверяем, не является ли пользователь уже администратором
        if (adminRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User is already an admin");
        }

        // Создаем нового администратора
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setRole(adminRole);
        admin.setTwoFactorSecret(generateTwoFactorSecret());
        admin.setLastLogin(LocalDateTime.now());

        // Сохраняем администратора
        Admin savedAdmin = adminRepository.save(admin);

        // Добавляем роль ADMIN пользователю
        user.addRole(User.Role.ADMIN);
        userRepository.save(user);

        return savedAdmin;
    }

    private String generateTwoFactorSecret() {
        Random random = new Random();
        int secretNumber = random.nextInt(10000);
        return String.format("%04d", secretNumber);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional
    public void updateAdminRole(Long adminId, Admin.AdminRole newRole) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));

        // Проверяем, изменилась ли роль
        if (admin.getRole() != newRole) {
            admin.setRole(newRole);
            adminRepository.save(admin);

            // Обновляем роль пользователя, если это необходимо
            User user = admin.getUser();
            User.Role userRole = Admin.mapAdminRoleToUserRole(newRole);
            if (!user.getRoles().contains(userRole)) {
                user.getRoles().clear();
                user.addRole(userRole);
                userRepository.save(user);
            }

            // Здесь можно добавить логирование изменения роли
            log.info("Updated admin role for admin id: {}. New role: {}", adminId, newRole);
        }
    }

    public void removeAdmin(Long adminId) {
        adminRepository.deleteById(adminId);
    }

    public Admin getAdminByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return adminRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Admin not found for user: " + username));
    }

    public boolean isUserAdmin(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return adminRepository.findByUser(user).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void deleteUserProfile(Long adminId, Long targetUserId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedAccessException("Администратор не найден"));

        // Проверяем права администратора
        if (!canDeleteUser(admin.getRole())) {
            throw new UnauthorizedAccessException("Недостаточно прав для удаления пользователя");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Проверяем, не пытается ли админ удалить другого админа
        if (isUserAdmin(targetUser)) {
            if (!canManageAdmins(admin.getRole())) {
                throw new UnauthorizedAccessException("Недостаточно прав для удаления администратора");
            }
        }

        try {
            // Удаляем связанные данные
            removeUserAssociations(targetUser);

            // Удаляем пользователя
            userRepository.delete(targetUser);

            log.info("User {} successfully deleted by admin {}", targetUserId, adminId);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", targetUserId, e.getMessage());
            throw new RuntimeException("Ошибка при удалении пользователя", e);
        }
    }

    private boolean canDeleteUser(Admin.AdminRole role) {
        return role == Admin.AdminRole.SENIOR_ADMIN || role == Admin.AdminRole.SUPER_ADMIN;
    }

    private boolean canManageAdmins(Admin.AdminRole role) {
        return role == Admin.AdminRole.SUPER_ADMIN;
    }

    private boolean isUserAdmin(User user) {
        return user.getRoles().contains(User.Role.ADMIN);
    }

    /**
     * Удаляет все связи пользователя перед удалением профиля
     */
    private void removeUserAssociations(User user) {
        // Удаляем из друзей
        for (User friend : new HashSet<>(user.getFriends())) {
            user.removeFriend(friend);
        }

        // Удаляем подписки
        user.getSubscriptions().clear();
        user.getFollowers().clear();

        // Удаляем фотографии и альбомы
        user.getPhotos().clear();
        user.getAlbums().clear();

        // Если пользователь был партнером для кого-то, удаляем эту связь
        if (user.getPartner() != null) {
            User partner = user.getPartner();
            partner.setPartner(null);
            user.setPartner(null);
            userRepository.save(partner);
        }

        userRepository.save(user);
    }

    public Admin getAdminByUserId(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + id));
    }
}