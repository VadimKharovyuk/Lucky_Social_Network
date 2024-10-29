package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDisplayDTO {
    private final String username;
    private final String displayName;

    public static UserDisplayDTO fromUser(User user) {
        if (user == null) {
            return new UserDisplayDTO("Unknown", "Unknown User");
        }

        String display = user.getUsername();
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty() &&
                user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            display = user.getFirstName().trim() + " " + user.getLastName().trim();
        }
        return new UserDisplayDTO(user.getUsername(), display);
    }

}