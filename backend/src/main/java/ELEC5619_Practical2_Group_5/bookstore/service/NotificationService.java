package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> listNotifications();

    NotificationDto createNotification(NotificationRequest request);

    NotificationDto updateNotification(Integer notificationId, NotificationRequest request);

    void deleteNotification(Integer notificationId);
    
    /**
     * Create order status notification for user
     */
    void createOrderNotification(Integer userId, Integer orderId, String orderStatus);
    
    /**
     * Get user's notifications
     */
    List<NotificationDto> getUserNotifications(Integer userId);
    
    /**
     * Get user's unread notifications
     */
    List<NotificationDto> getUserUnreadNotifications(Integer userId);
    
    /**
     * Mark notification as read
     */
    void markAsRead(Integer notificationId, Integer userId);
    
    /**
     * Mark all user's notifications as read
     */
    void markAllAsRead(Integer userId);
    
    /**
     * Create notification for user
     */
    void createNotification(Integer userId, String title, String message);
}
