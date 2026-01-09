package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    /**
     * Find all notifications for a specific user, ordered by creation date descending
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    /**
     * Find unread notifications for a specific user
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
    
    /**
     * Find all global notifications (userId is null), ordered by pinned and creation date
     */
    List<Notification> findByUserIdIsNullOrderByPinnedDescCreatedAtDesc();
}
