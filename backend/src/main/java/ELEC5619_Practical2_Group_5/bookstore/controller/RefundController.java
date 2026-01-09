package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCreateRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCancelRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/comfirm")
    public ResponseEntity<?> createRefund(@RequestBody RefundCreateRequest req, Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(401).build();
            String username = principal.getName();
            Integer refundId = refundService.createRefund(username, req);
            return ResponseEntity.ok(java.util.Map.of("status", "ok", "refundId", refundId));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(java.util.Map.of("status", "forbidden"));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status","error","message", iae.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("status","error","message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> myRefunds(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var list = refundService.listMyRefunds(principal.getName());
        return ResponseEntity.ok(java.util.Map.of("content", list));
    }


        @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody RefundCancelRequest req, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        try {
            refundService.cancelRefund(principal.getName(), req.getRefundId());
            return ResponseEntity.ok(java.util.Map.of("status", "ok"));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(java.util.Map.of("status","forbidden"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status","error","message", ex.getMessage()));
        }
    }

    @GetMapping("/reason/{orderId}")
    public ResponseEntity<?> getRefundReason(@PathVariable Integer orderId) {
        try {
            // Note: Since this controller is not for admin, a proper admin endpoint
            // should check for ADMIN role here or in the service layer.
            // For now, we assume the service layer handles access control if necessary.

            String reason = refundService.getRefundReasonByOrderId(orderId);

            return ResponseEntity.ok(java.util.Map.of("status", "ok", "reason", reason));

        } catch (IllegalArgumentException iae) {
            // e.g., Refund record not found
            return ResponseEntity.status(404).body(java.util.Map.of("status", "error", "message", iae.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("status", "error", "message", e.getMessage()));
        }
    }

}
