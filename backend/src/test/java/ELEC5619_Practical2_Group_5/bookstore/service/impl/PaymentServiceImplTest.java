package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.Payment;
import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.PaymentRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.RefundRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import ELEC5619_Practical2_Group_5.bookstore.service.PayPalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PayPalService payPalService;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order order;
    private Refund refund;
    private Payment payment;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1);
        order.setUserId(10);
        order.setStatus("paid");

        refund = new Refund();
        refund.setOrderId(1);
        refund.setStatus("pending");

        payment = new Payment();
        payment.setOrderId(1);
        payment.setTransactionId("txn123");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setStatus("COMPLETED");
    }

    @Test
    void testSavePayPalPayment_Success() {
        Map<String, Object> captureMap = Map.of("id", "txn123", "status", "COMPLETED");
        Map<String, Object> paymentsMap = Map.of("captures", List.of(captureMap));
        Map<String, Object> amountMap = Map.of("value", "100.00");
        Map<String, Object> purchaseUnitMap = Map.of("amount", amountMap, "payments", paymentsMap);
        Map<String, Object> orderData = Map.of(
                "id", "paypalOrder123",
                "purchase_units", List.of(purchaseUnitMap)
        );

        when(orderRepository.findByOrderId(1)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.savePayPalPayment(orderData, 1);

        assertNotNull(result);
        assertEquals("PayPal", result.getPaymentMethod());
        assertEquals("COMPLETED", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).findByOrderId(1);
    }

    @Test
    void testProcessRefund_Approved() throws Exception {
        when(refundRepository.findByOrderId(1)).thenReturn(Optional.of(refund));
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(orderRepository.findByOrderId(1)).thenReturn(Optional.of(order));
        when(payPalService.refundPayment(anyString(), any())).thenReturn(Map.of("id", "refund123"));

        paymentService.processRefund(1, true);

        assertEquals("cancelled", order.getStatus());
        assertEquals("completed", refund.getStatus());

        verify(notificationService).createNotification(eq(order.getUserId()), eq("Refund Approved"), anyString());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        verify(refundRepository).save(refund);
    }

    @Test
    void testProcessRefund_Rejected() {
        when(refundRepository.findByOrderId(1)).thenReturn(Optional.of(refund));
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(orderRepository.findByOrderId(1)).thenReturn(Optional.of(order));

        paymentService.processRefund(1, false);

        assertEquals("pending", order.getStatus());
        assertEquals("rejected", refund.getStatus());

        verify(notificationService).createNotification(eq(order.getUserId()), eq("Refund Rejected"), anyString());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        verify(refundRepository).save(refund);
    }

    @Test
    void testProcessRefund_RefundNotFound_ThrowsException() {
        when(refundRepository.findByOrderId(1)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processRefund(1, true);
        });

        assertTrue(exception.getMessage().contains("No pending refund request found"));
        verifyNoInteractions(paymentRepository, orderRepository, notificationService);
    }

    @Test
    void testProcessRefund_PaymentNotFound_ThrowsException() {
        when(refundRepository.findByOrderId(1)).thenReturn(Optional.of(refund));
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processRefund(1, true);
        });

        assertTrue(exception.getMessage().contains("Payment not found"));
        verify(orderRepository, never()).save(any());
        verify(refundRepository, never()).save(any());
        verify(notificationService, never()).createNotification(anyInt(), anyString(), anyString());
    }
}
