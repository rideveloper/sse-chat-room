package com.ridwan.ssechatrooms.service.impl;

import com.ridwan.ssechatrooms.model.ChatMessage;
import com.ridwan.ssechatrooms.model.ChatRequest;
import com.ridwan.ssechatrooms.model.ChatRoom;
import com.ridwan.ssechatrooms.service.ChatService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String[] ADJECTIVES = {"Happy", "Clever", "Swift", "Bright", "Lucky"};
    private static final String[] NOUNS = {"Panda", "Fox", "Eagle", "Tiger", "Dolphin"};

    private final SecureRandom random = new SecureRandom();
    private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<ChatMessage>> sinks = new ConcurrentHashMap<>();

    public ChatServiceImpl() {
        chatRooms.put("general", new ChatRoom("general"));
    }

    private String generateRandomName() {
        var adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        var noun = NOUNS[random.nextInt(NOUNS.length)];
        return "%s%s%d".formatted(adjective, noun, random.nextInt(100));
    }

    private boolean isUsernameTaken(String username) {
        return chatRooms.values().stream()
                .anyMatch(room -> room.hasUser(username));
    }

    @Override
    public Mono<ChatRequest> addUser(ChatRequest request) {
        return Mono.fromCallable(() -> {
            String username = Objects.isNull(request.username()) || request.username().isEmpty() ? generateRandomName() : request.username();

            while (isUsernameTaken(username)) {
                username = generateRandomName();
            }

            var finalUsername = username;
            var room = chatRooms.computeIfAbsent(
                    request.room(),
                    ChatRoom::new
            );

            room.addUser(finalUsername);

            // Create room sink if it doesn't exist
            sinks.computeIfAbsent(
                    request.room(),
                    k -> Sinks.many().multicast().onBackpressureBuffer()
            );

            // Send join message
            var joinMessage = ChatMessage.createSystemMessage(
                    ChatMessage.MessageType.JOIN,
                    finalUsername,
                    request.room(),
                    "%s has joined the chat".formatted(finalUsername)
            );

            sinks.get(request.room()).tryEmitNext(joinMessage);

            return new ChatRequest(finalUsername, request.room());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> removeUser(ChatRequest request) {
        return Mono.fromRunnable(() -> {
            var room = chatRooms.get(request.room());
            if (room != null) {
                room.removeUser(request.username());

                var leaveMessage = ChatMessage.createSystemMessage(
                        ChatMessage.MessageType.LEAVE,
                        request.username(),
                        request.room(),
                        "%s has left the chat".formatted(request.username())
                );

                sinks.get(request.room()).tryEmitNext(leaveMessage);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Flux<ChatMessage> getMessagesStream(ChatRequest request) {
        return Flux.defer(() -> {
            var sink = sinks.computeIfAbsent(
                    request.room(),
                    k -> Sinks.many().multicast().onBackpressureBuffer()
            );
            return sink.asFlux();
        });
    }

    @Override
    public Mono<Void> sendMessage(ChatMessage message) {
        return Mono.fromRunnable(() -> {
            var sink = sinks.get(message.room());
            if (sink != null) {
                var finalMessage = ChatMessage.createUserMessage(
                        message.content(),
                        message.sender(),
                        message.room()
                );
                sink.tryEmitNext(finalMessage);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
