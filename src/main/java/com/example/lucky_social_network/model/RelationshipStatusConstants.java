package com.example.lucky_social_network.model;

import java.util.Arrays;
import java.util.List;

public class RelationshipStatusConstants {
    public static final String SINGLE = "В активном поиске";
    public static final String MARRIED = "Женат/Замужем";
    public static final String DIVORCED = "Разведен(а)";
    public static final String WIDOWED = "Вдовец/Вдова";
    public static final String IN_RELATIONSHIP = "В отношениях";
    public static final String COMPLICATED = "Всё сложно";

    public static List<String> getAllStatuses() {
        return Arrays.asList(SINGLE, MARRIED, DIVORCED, WIDOWED, IN_RELATIONSHIP, COMPLICATED);
    }
}
