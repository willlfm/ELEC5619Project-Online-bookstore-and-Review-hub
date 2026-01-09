package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import ELEC5619_Practical2_Group_5.bookstore.repository.NotificationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
// Small helper around the Notification repository so controllers stay thin.
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> listNotifications() {
        return notificationRepository.findByUserIdIsNullOrderByPinnedDescCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public NotificationDto createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        updateEntity(notification, request);
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Override
    public NotificationDto updateNotification(Integer notificationId, NotificationRequest request) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        updateEntity(notification, request);
        return toDto(notificationRepository.save(notification));
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("Notification not found: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void createOrderNotification(Integer userId, Integer orderId, String orderStatus) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setType("order");
        notification.setStatus("active");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        // Set title and message based on order status
        switch (orderStatus.toLowerCase()) {
            case "shipped":
                notification.setTitle("Order Shipped");
                notification.setMessage("Your order #" + orderId + " has been shipped and is on its way!");
                break;
            case "delivered":
                notification.setTitle("Order Delivered");
                notification.setMessage("Your order #" + orderId + " has been delivered successfully!");
                break;
            case "canceled":
            case "cancelled":
                notification.setTitle("Order Canceled");
                notification.setMessage("Your order #" + orderId + " has been canceled.");
                break;
            case "processing":
                notification.setTitle("Order Processing");
                notification.setMessage("Your order #" + orderId + " is being processed.");
                break;
            case "confirmed":
                notification.setTitle("Order Confirmed");
                notification.setMessage("Your order #" + orderId + " has been confirmed!");
                break;
            default:
                notification.setTitle("Order Status Updated");
                notification.setMessage("Your order #" + orderId + " status has been updated to: " + orderStatus);
                break;
        }
        
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void markAsRead(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        // Verify notification belongs to user
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void createNotification(Integer userId, String title, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .isRead(false)
                .type("reservation")  // Changed to 'reservation' for better categorization
                .createdAt(LocalDateTime.now())  // Explicitly set createdAt
                .build();
        notificationRepository.save(notification);
    }

    private void updateEntity(Notification notification, NotificationRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            notification.setTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getMessage())) {
            notification.setMessage(request.getMessage().trim());
        }
        if (StringUtils.hasText(request.getType())) {
            notification.setType(request.getType().trim());
        }
        if (request.getPinned() != null) {
            notification.setPinned(request.getPinned());
        }
        if (StringUtils.hasText(request.getStatus())) {
            notification.setStatus(request.getStatus().trim());
        }
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .pinned(notification.isPinned())
                .read(notification.isRead())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
