package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import ELEC5619_Practical2_Group_5.bookstore.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void listNotifications_Success() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .title("Test Notification")
                .message("Test message")
                .type("info")
                .pinned(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdIsNullOrderByPinnedDescCreatedAtDesc())
                .thenReturn(List.of(notification));

        List<NotificationDto> result = notificationService.listNotifications();

        assertEquals(1, result.size());
        assertEquals("Test Notification", result.get(0).getTitle());
    }

    @Test
    void createNotification_Success() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("New Notification");
        request.setMessage("New message");
        request.setType("info");

        Notification notification = Notification.builder()
                .notificationId(1)
                .title("New Notification")
                .message("New message")
                .type("info")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDto result = notificationService.createNotification(request);

        assertEquals("New Notification", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void updateNotification_Success() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Updated Title");
        request.setMessage("Updated message");

        Notification notification = Notification.builder()
                .notificationId(1)
                .title("Original Title")
                .message("Original message")
                .build();

        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDto result = notificationService.updateNotification(1, request);

        assertEquals("Updated Title", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void updateNotification_NotFound() {
        NotificationRequest request = new NotificationRequest();
        when(notificationRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
                () -> notificationService.updateNotification(1, request));
    }

    @Test
    void deleteNotification_Success() {
        when(notificationRepository.existsById(1)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(1);

        notificationService.deleteNotification(1);

        verify(notificationRepository).deleteById(1);
    }

    @Test
    void deleteNotification_NotFound() {
        when(notificationRepository.existsById(1)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, 
                () -> notificationService.deleteNotification(1));
    }

    @Test
    void createOrderNotification_Shipped() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        notificationService.createOrderNotification(1, 123, "shipped");

        verify(notificationRepository).save(argThat(notification -> 
                "Order Shipped".equals(notification.getTitle()) &&
                notification.getMessage().contains("123")));
    }

    @Test
    void getUserNotifications_Success() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .userId(1)
                .title("User Notification")
                .message("User message")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(List.of(notification));

        List<NotificationDto> result = notificationService.getUserNotifications(1);

        assertEquals(1, result.size());
        assertEquals("User Notification", result.get(0).getTitle());
    }

    @Test
    void getUserUnreadNotifications_Success() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .userId(1)
                .title("Unread Notification")
                .isRead(false)
                .build();

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1))
                .thenReturn(List.of(notification));

        List<NotificationDto> result = notificationService.getUserUnreadNotifications(1);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void markAsRead_Success() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .userId(1)
                .isRead(false)
                .build();

        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1, 1);

        verify(notificationRepository).save(argThat(n -> n.isRead()));
    }

    @Test
    void markAsRead_WrongUser() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .userId(2)
                .isRead(false)
                .build();

        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));

        assertThrows(IllegalArgumentException.class, 
                () -> notificationService.markAsRead(1, 1));
    }

    @Test
    void markAllAsRead_Success() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .userId(1)
                .isRead(false)
                .build();

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1))
                .thenReturn(List.of(notification));
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(notification));

        notificationService.markAllAsRead(1);

        verify(notificationRepository).saveAll(anyList());
    }
}