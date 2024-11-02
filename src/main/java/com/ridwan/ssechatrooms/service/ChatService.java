package com.ridwan.ssechatrooms.service;

import com.ridwan.ssechatrooms.model.ChatMessage;
import com.ridwan.ssechatrooms.model.ChatRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {
    Mono<ChatRequest> addUser(ChatRequest request);
    Mono<Void> removeUser(ChatRequest request);
    Flux<ChatMessage> getMessagesStream(ChatRequest request);
    Mono<Void> sendMessage(ChatMessage message);
}
