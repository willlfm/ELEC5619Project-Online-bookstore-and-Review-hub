package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderUpdateRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminOrderService;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import ELEC5619_Practical2_Group_5.bookstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
// Management API for viewing and updating customer orders from the admin UI.
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<AdminOrderDto>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(adminOrderService.listOrders(page, size, status));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDto> getOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(adminOrderService.getOrder(orderId));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<AdminOrderDto> updateOrder(
            @PathVariable Integer orderId,
            @RequestBody AdminOrderUpdateRequest request
    ) {
        return ResponseEntity.ok(adminOrderService.updateOrder(orderId, request));
    }

    /**
     * Approve refund for a specific order
     */
    @PostMapping("/{orderId}/refund/approve")
    public ResponseEntity<Void> approveRefund(@PathVariable Integer orderId) {
        paymentService.processRefund(orderId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject refund for a specific order
     */
    @PostMapping("/{orderId}/refund/reject")
    public ResponseEntity<Void> rejectRefund(@PathVariable Integer orderId) {
        paymentService.processRefund(orderId, false);
        return ResponseEntity.ok().build();
    }
}
