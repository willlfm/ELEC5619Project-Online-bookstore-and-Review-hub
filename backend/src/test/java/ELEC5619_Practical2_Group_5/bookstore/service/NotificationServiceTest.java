package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import ELEC5619_Practical2_Group_5.bookstore.repository.NotificationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private NotificationRequest testRequest;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .notificationId(1)
                .userId(1)
                .orderId(100)
                .title("Test Notification")
                .message("This is a test notification")
                .type("order")
                .isRead(false)
                .pinned(false)
                .createdAt(LocalDateTime.now())
                .build();

        testRequest = new NotificationRequest();
        testRequest.setTitle("Test Notification");
        testRequest.setMessage("This is a test notification");
        testRequest.setType("order");
        testRequest.setPinned(false);
    }

    @Test
    void listNotifications_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        // Service lists global notifications (userId is null) ordered by pinned and createdAt
        when(notificationRepository.findByUserIdIsNullOrderByPinnedDescCreatedAtDesc()).thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.listNotifications();

        // Then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getNotificationId());
        assertEquals("Test Notification", result.get(0).getTitle());
        assertEquals("This is a test notification", result.get(0).getMessage());
    }

    @Test
    void createNotification_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        NotificationDto result = notificationService.createNotification(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getNotificationId());
        assertEquals("Test Notification", result.getTitle());
        assertEquals("This is a test notification", result.getMessage());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void updateNotification_Success() {
        // Given
        NotificationRequest updateRequest = new NotificationRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setMessage("Updated message");
        updateRequest.setType("system");
        updateRequest.setPinned(true);

        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        NotificationDto result = notificationService.updateNotification(1, updateRequest);

        // Then
        assertNotNull(result);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void updateNotification_NotFound_ThrowsException() {
        // Given
        when(notificationRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.updateNotification(1, testRequest);
        });
    }

    @Test
    void deleteNotification_Success() {
        // Given
        when(notificationRepository.existsById(1)).thenReturn(true);

        // When
        notificationService.deleteNotification(1);

        // Then
        verify(notificationRepository).deleteById(1);
    }

    @Test
    void createOrderNotification_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "shipped");

        // Then
        verify(notificationRepository).save(argThat(notification -> 
            notification.getUserId().equals(1) &&
            notification.getOrderId().equals(100) &&
            notification.getTitle().equals("Order Shipped") &&
            notification.getMessage().contains("order #100") &&
            notification.getType().equals("order")
        ));
    }

    @Test
    void createOrderNotification_Cancelled() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "cancelled");

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createOrderNotification_Completed() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "completed");

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createOrderNotification_Default() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "processing");

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.getUserNotifications(1);

        // Then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getNotificationId());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1);
    }

    @Test
    void getUserUnreadNotifications_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1)).thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.getUserUnreadNotifications(1);

        // Then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getNotificationId());
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1);
    }

    @Test
    void markAsRead_Success() {
        // Given
        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(1, 1);

        // Then
        assertTrue(testNotification.isRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_NotFound_ThrowsException() {
        // Given
        when(notificationRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(1, 1);
        });
    }

    @Test
    void markAsRead_WrongUser_ThrowsException() {
        // Given
        testNotification.setUserId(2); // Different user
        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(1, 1);
        });
    }

    @Test
    void markAllAsRead_Success() {
        // Given
        List<Notification> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1)).thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(unreadNotifications);

        // When
        notificationService.markAllAsRead(1);

        // Then
        assertTrue(testNotification.isRead());
        verify(notificationRepository).saveAll(unreadNotifications);
    }

    @Test
    void createNotification_Generic_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createNotification(1, "Test Title", "Test Message");

        // Then
        verify(notificationRepository).save(argThat(notification -> 
            notification.getUserId().equals(1) &&
            notification.getTitle().equals("Test Title") &&
            notification.getMessage().equals("Test Message") &&
            notification.getType().equals("reservation") &&
            !notification.isRead()
        ));
    }

    @Test
    void createNotification_Generic_WithNullValues() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createNotification(1, null, null);

        // Then
        verify(notificationRepository).save(argThat(notification -> 
            notification.getUserId().equals(1) &&
            notification.getTitle() == null &&
            notification.getMessage() == null &&
            notification.getType().equals("reservation")
        ));
    }

    @Test
    void updateNotification_WithAllFields() {
        // Given
        Notification existing = Notification.builder()
                .notificationId(1)
                .title("Old Title")
                .message("Old Message")
                .type("general")
                .pinned(false)
                .build();
        
        when(notificationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenReturn(existing);

        NotificationRequest request = new NotificationRequest();
        request.setTitle("New Title");
        request.setMessage("New Message");
        request.setType("announcement");
        request.setPinned(true);

        // When
        NotificationDto result = notificationService.updateNotification(1, request);

        // Then
        verify(notificationRepository).save(existing);
        assertNotNull(result);
    }

    @Test
    void updateNotification_WithEmptyFields() {
        // Given
        Notification existing = Notification.builder()
                .notificationId(1)
                .title("Old Title")
                .message("Old Message")
                .type("general")
                .pinned(false)
                .build();
        
        when(notificationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenReturn(existing);

        NotificationRequest request = new NotificationRequest();
        request.setTitle("   "); // Empty after trim
        request.setMessage("   "); // Empty after trim

        // When
        NotificationDto result = notificationService.updateNotification(1, request);

        // Then - Should not update empty fields
        verify(notificationRepository).save(existing);
        assertNotNull(result);
    }

    @Test
    void createOrderNotification_Confirmed() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "confirmed");

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createOrderNotification_Processing() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createOrderNotification(1, 100, "processing");

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void deleteNotification_NotFound_ThrowsException() {
        // Given
        when(notificationRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.deleteNotification(999);
        });
    }
}
