package com.ridwan.ssechatrooms.controller;

import com.ridwan.ssechatrooms.model.ChatMessage;
import com.ridwan.ssechatrooms.model.ChatRequest;
import com.ridwan.ssechatrooms.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/join")
    public Mono<ChatRequest> joinChat(@RequestBody ChatRequest request) {
        return chatService.addUser(request);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> streamMessages(@RequestParam String username,
                                            @RequestParam(defaultValue = "general") String room) {
        return chatService.getMessagesStream(new ChatRequest(username, room));
    }

    @PostMapping("/message")
    public Mono<Void> sendMessage(@RequestBody ChatMessage message) {
        return chatService.sendMessage(message);
    }

    @PostMapping("/leave")
    public Mono<Void> leaveChat(@RequestBody ChatRequest request) {
        return chatService.removeUser(request);
    }
}
