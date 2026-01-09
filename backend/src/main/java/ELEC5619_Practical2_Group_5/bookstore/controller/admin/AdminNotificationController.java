package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
// Administrator tooling for curating announcement banners on the dashboard.
public class AdminNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> listNotifications() {
        return ResponseEntity.ok(notificationService.listNotifications());
    }

    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(@RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationDto> updateNotification(
            @PathVariable Integer notificationId,
            @RequestBody NotificationRequest request
    ) {
        return ResponseEntity.ok(notificationService.updateNotification(notificationId, request));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
