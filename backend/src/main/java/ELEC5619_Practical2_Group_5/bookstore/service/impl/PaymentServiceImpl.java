package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.Payment;
import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.PaymentRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.RefundRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import ELEC5619_Practical2_Group_5.bookstore.service.PayPalService;
import ELEC5619_Practical2_Group_5.bookstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PayPalService payPalService;
    private final RefundRepository refundRepository;


    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, NotificationService notificationService, PayPalService payPalService, RefundRepository refundRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.payPalService = payPalService;
        this.refundRepository = refundRepository;
    }

    @Transactional
    @Override
    public Payment savePayPalPayment(Map<String, Object> orderData, Integer localOrderId) {
        try {
            Order order = orderRepository.findByOrderId(localOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order with ID " + localOrderId + " not found"));

            String paypalOrderId = (String) orderData.get("id");

            Map<String, Object> purchaseUnit = ((java.util.List<Map<String, Object>>) orderData.get("purchase_units")).get(0);
            Map<String, Object> amountObj = (Map<String, Object>) purchaseUnit.get("amount");
            Map<String, Object> paymentsObj = (Map<String, Object>) purchaseUnit.get("payments");
            Map<String, Object> capture = ((java.util.List<Map<String, Object>>) paymentsObj.get("captures")).get(0);

            String captureId = (String) capture.get("id");
            String status = (String) capture.get("status");
            String value = (String) amountObj.get("value");

            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setPaymentMethod("PayPal");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(new BigDecimal(value));
            payment.setStatus(status);
            payment.setTransactionId(captureId);
            payment.setPaypalOrderId(paypalOrderId);

            return paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse and save PayPal payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void processRefund(Integer orderId, boolean approved) {
        Optional<Refund> refundOpt = refundRepository.findByOrderId(orderId);

        if (refundOpt.isEmpty()) {
            throw new IllegalArgumentException("No pending refund request found for order: " + orderId);
        }

        Refund refund = refundOpt.get();
        if (!"pending".equalsIgnoreCase(refund.getStatus())) {
            throw new IllegalStateException("Refund request for order " + orderId + " is not in a pending state. Current status: " + refund.getStatus());
        }

        Optional<Payment> paymentOpt = paymentRepository.findAll()
                .stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst();

        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found for order: " + orderId);
        }

        Payment payment = paymentOpt.get();
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (approved) {
            try {
                Map<String, Object> refundResult = payPalService.refundPayment(
                        payment.getTransactionId(),
                        payment.getAmount()
                );

                order.setStatus("cancelled");

                refund.setStatus("completed");
                refund.setProcessedAt(LocalDateTime.now());

                notificationService.createNotification(
                        order.getUserId(),
                        "Refund Approved",
                        "Your refund for order #" + orderId + " has been approved and processed. Refund ID: " +
                                refundResult.get("id")
                );

            } catch (Exception e) {
                throw new RuntimeException("PayPal refund failed: " + e.getMessage(), e);
            }
        } else {
            order.setStatus("pending");

            refund.setStatus("rejected");
            refund.setProcessedAt(LocalDateTime.now());

            notificationService.createNotification(
                    order.getUserId(),
                    "Refund Rejected",
                    "Your refund request for order #" + orderId + " has been rejected by the administrator."
            );
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
        refundRepository.save(refund);
    }

}
