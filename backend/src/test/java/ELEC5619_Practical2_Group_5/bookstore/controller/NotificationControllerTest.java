package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@WithMockUser(username = "testuser")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationDto testNotification;

    @BeforeEach
    void setUp() {
        // Mock user for authentication
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        testNotification = NotificationDto.builder()
                .notificationId(1)
                .userId(1)
                .orderId(100)
                .title("Test Notification")
                .message("This is a test notification")
                .type("order")
                .read(false)
                .pinned(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserNotifications_Success() throws Exception {
        // Given
        List<NotificationDto> notifications = Arrays.asList(testNotification);
        when(notificationService.getUserNotifications(1)).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Notification"))
                .andExpect(jsonPath("$[0].message").value("This is a test notification"))
                .andExpect(jsonPath("$[0].type").value("order"))
                .andExpect(jsonPath("$[0].isRead").value(false));

        verify(notificationService).getUserNotifications(1);
    }

    @Test
    void getUserNotifications_EmptyList() throws Exception {
        // Given
        when(notificationService.getUserNotifications(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(notificationService).getUserNotifications(1);
    }

    @Test
    void getUserUnreadNotifications_Success() throws Exception {
        // Given
        List<NotificationDto> notifications = Arrays.asList(testNotification);
        when(notificationService.getUserUnreadNotifications(1)).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].isRead").value(false));

        verify(notificationService).getUserUnreadNotifications(1);
    }

    @Test
    void getUserUnreadNotifications_EmptyList() throws Exception {
        // Given
        when(notificationService.getUserUnreadNotifications(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(notificationService).getUserUnreadNotifications(1);
    }

    @Test
    void markAsRead_Success() throws Exception {
        // Given
        doNothing().when(notificationService).markAsRead(1, 1);

        // When & Then
        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification marked as read"));

        verify(notificationService).markAsRead(1, 1);
    }

    @Test
    void markAsRead_NotFound_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Notification not found"))
                .when(notificationService).markAsRead(1, 1);

        // When & Then
        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Notification not found"));

        verify(notificationService).markAsRead(1, 1);
    }

    @Test
    void markAsRead_Unauthorized_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Notification does not belong to user"))
                .when(notificationService).markAsRead(1, 1);

        // When & Then
        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Notification does not belong to user"));

        verify(notificationService).markAsRead(1, 1);
    }

    @Test
    void markAllAsRead_Success() throws Exception {
        // Given
        doNothing().when(notificationService).markAllAsRead(1);

        // When & Then
        mockMvc.perform(put("/api/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("All notifications marked as read"));

        verify(notificationService).markAllAsRead(1);
    }

    @Test
    void markAllAsRead_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(notificationService).markAllAsRead(1);

        // When & Then
        mockMvc.perform(put("/api/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to mark all notifications as read"));

        verify(notificationService).markAllAsRead(1);
    }

    @Test
    void markAsRead_GenericException_ReturnsInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Unexpected error"))
                .when(notificationService).markAsRead(1, 1);

        // When & Then
        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to mark notification as read"));

        verify(notificationService).markAsRead(1, 1);
    }

    @Test
    void getUserNotifications_UserNotFound_ThrowsException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUnreadNotifications_ServiceReturnsData() throws Exception {
        // Given
        List<NotificationDto> notifications = Arrays.asList(testNotification);
        when(notificationService.getUserUnreadNotifications(1)).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value(1));

        verify(notificationService).getUserUnreadNotifications(1);
    }
}

