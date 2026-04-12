package com.example.messenger.utils;

import com.example.messenger.entity.UserEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConversationUtil {

    public static String getConversationId(UserEntity user1, UserEntity user2) {
        String id1 = user1.getId().toString();
        String id2 = user2.getId().toString();

        return id1.compareTo(id2) > 0 ? id2 + "_" + id1 : id1 + "_" + id2;
    }
}
