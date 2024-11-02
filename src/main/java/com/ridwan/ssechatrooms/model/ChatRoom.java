package com.ridwan.ssechatrooms.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public record ChatRoom(
        String name,
        Set<String> users
) {
    public ChatRoom(String name) {
        this(name, ConcurrentHashMap.newKeySet());
    }

    public void addUser(String username) {
        users.add(username);
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public boolean hasUser(String username) {
        return users.contains(username);
    }
}
