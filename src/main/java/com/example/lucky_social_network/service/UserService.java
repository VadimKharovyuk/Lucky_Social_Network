package com.example.lucky_social_network.service;

import com.example.lucky_social_network.config.TimeUtils;
import com.example.lucky_social_network.exception.EmailAlreadyExistsException;
import com.example.lucky_social_network.exception.FriendshipNotFoundException;
import com.example.lucky_social_network.exception.UserNotFoundException;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserActivityEvent;
import com.example.lucky_social_network.redis.UserCacheDTO;
import com.example.lucky_social_network.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final ActivityPublisher activityPublisher;
    private final UserActivityService userActivityService;

    @EventListener
    @Transactional
    public void onUserActivity(UserActivityEvent event) {
        log.info("Обработка события активности пользователя: ID={}, тип={}",
                event.getUserId(), event.getActivityType());


        updateLastLogin(event.getUserId());
    }


    @Cacheable(value = "user_profiles", key = "#userId", unless = "#result == null")
    public UserCacheDTO getUserProfileById(Long userId) {
        log.info("Fetching user profile from database for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // Проверяем роли перед конвертацией
        log.info("User {} roles before conversion: {}", userId, user.getRoles());

        UserCacheDTO dto = UserCacheDTO.fromUser(user);


        return dto;
    }

    @CachePut(value = "user_profiles", key = "#user.id")
    public UserCacheDTO updateUserProfile(User user) {
        log.info("Updating user profile in cache for user ID: {}", user.getId());
        log.info("Last login time before saving: {}", user.getLastLogin());

        User savedUser = userRepository.save(user);
        UserCacheDTO dto = UserCacheDTO.fromUser(savedUser);


        return dto;
    }

    public User getUserFullProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }


    @CacheEvict(value = "user_profiles", key = "#userId")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean areFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        return user1.getFriends().stream().anyMatch(friend -> friend.getId().equals(userId2));
    }


    @Transactional
    public User updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Обновляем основные поля
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setBio(user.getBio());
            existingUser.setDateOfBirth(user.getDateOfBirth());
            existingUser.setLocation(user.getLocation());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setStatusMessage(user.getStatusMessage());

            // Сохраняем изменения
            User savedUser = userRepository.save(existingUser);

            // Отслеживаем действие
            userActivityService.trackUserAction(savedUser);

            log.info("Updated user profile and tracked activity for user ID: {}", user.getId());

            return savedUser;
        } catch (Exception e) {
            log.error("Error updating user profile for ID {}: {}", user.getId(), e.getMessage());
            throw e;
        }
    }

    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDate.now());
        user.setEmailVerified(false);
        user.setIsPrivate(false);
        user.setFollowersCount(0);
        user.setFriendsCount(0);
        return userRepository.save(user);
    }

    @Transactional
    public User findByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public User getUserById(Long senderId) {
//        activityPublisher.publishActivity(senderId, UserActivityEvent.ActivityType.PROFILE_UPDATED);
        return userRepository.findById(senderId).orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friend)) {
            throw new IllegalStateException("Users are already friends");
        }

        user.getFriends().add(friend);
        friend.getFriends().add(user);

        user.setFriendsCount(user.getFriendsCount() == null ? 1 : user.getFriendsCount() + 1);
        friend.setFriendsCount(friend.getFriendsCount() == null ? 1 : friend.getFriendsCount() + 1);

        userRepository.save(user);
        userRepository.save(friend);

        // Автоматически подписываем друзей друг на друга
        subscriptionService.subscribe(userId, friendId);
        subscriptionService.subscribe(friendId, userId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        log.info("Attempting to remove friend relationship between users: {} and {}", userId, friendId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getFriends().contains(friend)) {
            log.warn("Friend relationship not found between users: {} and {}", userId, friendId);
            throw new FriendshipNotFoundException("Дружба не найдена между пользователями");
        }

        // Удаляем друга из списка друзей
        user.removeFriend(friend);

        // Уменьшаем количество друзей у пользователя
        user.setFriendsCount(Math.max(0, user.getFriendsCount() - 1));
        // Уменьшаем количество подписчиков у друга
        friend.setFollowersCount(Math.max(0, friend.getFollowersCount() - 1));

        userRepository.save(user);
        userRepository.save(friend);

        log.info("Friend relationship successfully removed between users: {} and {}", userId, friendId);
    }


    @Transactional(readOnly = true)
    public Set<User> getFriendsByUser(Long userId) {
        User user = getUserById(userId);
        return user.getFriends();
    }


    public String getUserAvatarUrl(User user) {
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            return user.getAvatarUrl();
        }
        activityPublisher.publishActivity(user.getId(), UserActivityEvent.ActivityType.PROFILE_UPDATED);
        return getDefaultAvatarUrl();
    }

    private String getDefaultAvatarUrl() {
        // Возвращает URL аватара по умолчанию
        return "https://example.com/default-avatar.png";
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Attempt to access getCurrentUser() with no authenticated user");
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }


        log.error("Unexpected principal type: " + principal.getClass());
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm);
    }

    // Можно также добавить отслеживание при обновлении аватара
    @Transactional
    public void updateAvatarUrl(Long userId, String imageUrl) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user.setAvatarUrl(imageUrl);
            User savedUser = userRepository.save(user);

            // Отслеживаем действие
            userActivityService.trackUserAction(savedUser);

            log.info("Updated avatar and tracked activity for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error updating avatar for user ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public Optional<User> findById(Long userId) {
        activityPublisher.publishActivity(userId, UserActivityEvent.ActivityType.PROFILE_UPDATED);
        return userRepository.findById(userId);
    }


    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();

    }


    public boolean isBirthdayToday(Long userId) {
        User user = getUserFullProfile(userId);
        if (user.getDateOfBirth() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate birthDate = user.getDateOfBirth();

        return birthDate.getMonth() == today.getMonth() &&
                birthDate.getDayOfMonth() == today.getDayOfMonth();
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Текущий пароль неверен");
        }

        // Обновляем пароль
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }


    @Transactional(readOnly = true)
    public String getUserOnlineStatus(Long userId) {
        log.debug("Получение статуса пользователя: {}", userId);

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        // Проверяем время последнего входа
        if (user.getLastLogin() == null) {
            log.debug("Пользователь {} еще не входил в систему", userId);
            return "не в сети";
        }

        if (wasOnlineWithinLastMinutes(userId, 5)) {
            return "онлайн";
        }
        return getLastLoginFormatted(userId);
    }


    @Transactional(readOnly = true)
    public boolean wasOnlineWithinLastMinutes(Long userId, int minutes) {
        log.debug("Проверка онлайн статуса для пользователя {} в течение {} минут", userId, minutes);

        LocalDateTime lastLogin = userRepository.findLastLoginById(userId)
                .orElse(null);

        // Если lastLogin null, значит пользователь еще ни разу не входил
        if (lastLogin == null) {
            log.debug("Пользователь {} еще не входил в систему", userId);
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return lastLogin.isAfter(threshold);
    }

    @Transactional(readOnly = true)
    public String getLastLoginFormatted(Long userId) {
        log.debug("Получение форматированного времени последнего входа для пользователя {}", userId);

        LocalDateTime lastLogin = userRepository.findLastLoginById(userId)
                .orElse(null);

        if (lastLogin == null) {
            return "никогда не был в сети";
        }

        return TimeUtils.getTimeAgo(lastLogin);
    }


    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + userId));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.debug("Обновлено время последней активности для пользователя {}: {}",
                userId, user.getLastLogin());
    }

    public boolean canAccessProfile(Long currentUserId, Long targetUserId) {
        // Свой профиль всегда доступен
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        User targetUser = getUserById(targetUserId);

        // Проверяем, является ли профиль приватным
        if (targetUser.getIsPrivate()) {
            // Проверяем, являются ли пользователи друзьями
            return areFriends(currentUserId, targetUserId);
        }

        return true;
    }


    private void validateUserDetails(User user, User existingUser) {
        if (user.getFirstName() != null && user.getFirstName().length() > 50) {
            throw new ValidationException("First name too long");
        }
        if (user.getLastName() != null && user.getLastName().length() > 50) {
            throw new ValidationException("Last name too long");
        }
        if (user.getBio() != null && user.getBio().length() > 500) {
            throw new ValidationException("Bio too long");
        }

        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
                throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already in use");
            }
        }
    }

    @Transactional
    public void updateUserDetails(@Valid User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + user.getId()));

            validateUserDetails(user, existingUser);

            // Обновляем данные пользователя
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setBio(user.getBio());

            // Сохраняем обновления
            User savedUser = userRepository.save(existingUser);

            // Отслеживаем действие пользователя
            userActivityService.trackUserAction(savedUser);

            // Публикуем событие обновления профиля
            activityPublisher.publishActivity(user.getId(), UserActivityEvent.ActivityType.PROFILE_UPDATED);

            log.info("Updated user details and tracked activity for user ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Error updating user details for ID {}: {}", user.getId(), e.getMessage());
            throw e;
        }
    }


    @Transactional
    public void updateUserStatus(Long userId, String newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setStatusMessage(newStatus);
        userRepository.save(user);
        log.info("Status updated for user {}: {}", userId, newStatus);
    }

    public String getUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return user.getStatusMessage();
    }

    public User getUserWithWorkExperience(Long id) {
        return userRepository.findUserWithWorkExperience(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}