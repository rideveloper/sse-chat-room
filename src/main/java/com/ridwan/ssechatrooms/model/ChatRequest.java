package com.ridwan.ssechatrooms.model;

public record ChatRequest(
        String username,
        String room
) {
    public ChatRequest {
        room = room == null ? "general" : room;
    }
}
