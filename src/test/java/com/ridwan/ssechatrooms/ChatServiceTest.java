package com.ridwan.ssechatrooms;

import com.ridwan.ssechatrooms.model.ChatMessage;
import com.ridwan.ssechatrooms.model.ChatRequest;
import com.ridwan.ssechatrooms.service.ChatService;
import com.ridwan.ssechatrooms.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl();
    }

    @Test
    @DisplayName("Should generate unique username when empty username is provided")
    void shouldGenerateUniqueUsername() {
        var request = new ChatRequest("", "general");

        StepVerifier.create(chatService.addUser(request))
                .assertNext(response -> {
                    assertNotNull(response.username());
                    assertFalse(response.username().isEmpty());
                    assertEquals("general", response.room());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should not allow duplicate usernames")
    void shouldNotAllowDuplicateUsernames() {
        var request1 = new ChatRequest("testUser", "general");
        var request2 = new ChatRequest("testUser", "general");

        StepVerifier.create(chatService.addUser(request1))
                .assertNext(response -> assertEquals("testUser", response.username()))
                .verifyComplete();

        StepVerifier.create(chatService.addUser(request2))
                .assertNext(response -> assertNotEquals("testUser", response.username()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should broadcast messages to all users in room")
    void shouldBroadcastMessagesToRoom() {
        var room = "testRoom";
        var user1 = new ChatRequest("user1", room);
        var user2 = new ChatRequest("user2", room);

        // Add users and collect their messages
        chatService.addUser(user1).block();
        chatService.addUser(user2).block();

        var messages1 = chatService.getMessagesStream(user1);
        var messages2 = chatService.getMessagesStream(user2);

        // Send a test message
        var testMessage = ChatMessage.createUserMessage("Hello", "user1", room);
        chatService.sendMessage(testMessage).block();

        // Verify both users receive the message
        StepVerifier.create(messages1.take(2)) // Join message + test message
                .assertNext(msg -> assertEquals(ChatMessage.MessageType.JOIN, msg.type()))
                .assertNext(msg -> {
                    assertEquals("user2 has joined the chat", msg.content());
                    assertEquals("user2", msg.sender());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle user leaving chat room")
    void shouldHandleUserLeaving() {
        var room = "testRoom";
        var user = new ChatRequest("testUser", room);

        // Add user
        chatService.addUser(user).block();

        // Subscribe to messages
        var messages = chatService.getMessagesStream(user);

        // Remove user
        chatService.removeUser(user).block();

        // Verify join and leave messages
        StepVerifier.create(messages.take(2))
                .assertNext(msg -> {
                    assertEquals(ChatMessage.MessageType.JOIN, msg.type());
                    assertEquals("testUser", msg.sender());
                })
                .assertNext(msg -> {
                    assertEquals(ChatMessage.MessageType.LEAVE, msg.type());
                    assertEquals("testUser", msg.sender());
                })
                .verifyComplete();
    }
}
