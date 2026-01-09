package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.pay.PayPalOrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.impl.PayPalServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PayPalController {

    private final PayPalServiceImpl payPalService;

    public PayPalController(PayPalServiceImpl payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = payPalService.createOrderFromCart(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/capture")
    public ResponseEntity<?> captureOrder(@PathVariable String orderId) {
        try {
            Map<String, Object> response = payPalService.captureOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
