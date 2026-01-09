package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.entity.Payment;
import ELEC5619_Practical2_Group_5.bookstore.service.impl.PaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/save-payment")
    public ResponseEntity<?> savePayPalPayment(@RequestBody Map<String, Object> paypalResponse) {
        try {
            Integer localOrderId = (Integer) paypalResponse.get("localOrderId");
            Map<String, Object> orderData = (Map<String, Object>) paypalResponse.get("order");

            Payment saved = paymentService.savePayPalPayment(orderData, localOrderId);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
