package com.example.lucky_social_network.service;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.Subscription;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.PostRepository;
import com.example.lucky_social_network.repository.SubscriptionRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    @Transactional
    public void subscribe(Long followerId, Long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("Followee not found"));

        if (subscriptionRepository.findByFollowerAndFollowee(follower, followee).isPresent()) {
            throw new IllegalStateException("Already subscribed");
        }

        Subscription subscription = new Subscription();
        subscription.setFollower(follower);
        subscription.setFollowee(followee);
        subscription.setCreatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
        followee.setFollowersCount(followee.getFollowersCount() == null ? 1 : followee.getFollowersCount() + 1);
        userRepository.save(followee);
    }

    @Transactional
    public void unsubscribe(Long followerId, Long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("Followee not found"));

        Subscription subscription = subscriptionRepository.findByFollowerAndFollowee(follower, followee)
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));

        subscriptionRepository.delete(subscription);
        followee.setFollowersCount(followee.getFollowersCount() - 1);
        userRepository.save(followee);
    }

    public Page<Post> getNewsFeed(Long userId, Pageable pageable) {
        List<Long> followeeIds = subscriptionRepository.findFolloweeIdsByFollowerId(userId);
        log.debug("User {} is following {} users", userId, followeeIds.size());

        if (followeeIds.isEmpty()) {
            log.debug("User {} has no subscriptions, returning empty page", userId);
            return Page.empty(pageable);
        }

        Page<Post> newsFeed = postRepository.findByAuthorIdInOrderByTimestampDesc(followeeIds, pageable);
        log.debug("Fetched {} posts for user {}", newsFeed.getTotalElements(), userId);

        return newsFeed;
    }
}