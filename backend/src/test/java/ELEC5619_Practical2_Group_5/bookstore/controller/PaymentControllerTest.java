package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.entity.Payment;
import ELEC5619_Practical2_Group_5.bookstore.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentServiceImpl paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private Map<String, Object> paypalResponse;
    private Payment payment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();

        // Setup dummy PayPal response
        Map<String, Object> capture = new HashMap<>();
        capture.put("id", "CAPTURE123");
        capture.put("status", "COMPLETED");

        Map<String, Object> payments = new HashMap<>();
        payments.put("captures", List.of(capture));

        Map<String, Object> amount = new HashMap<>();
        amount.put("value", "100.00");

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amount);
        purchaseUnit.put("payments", payments);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", "PAYPAL123");
        orderData.put("purchase_units", List.of(purchaseUnit));

        paypalResponse = new HashMap<>();
        paypalResponse.put("localOrderId", 1);
        paypalResponse.put("order", orderData);

        payment = new Payment();
        payment.setOrderId(1);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setStatus("COMPLETED");
        payment.setPaymentMethod("PayPal");
        payment.setTransactionId("CAPTURE123");
        payment.setPaypalOrderId("PAYPAL123");
    }

    @Test
    void testSavePayPalPayment_StatusOkAndServiceCalled() throws Exception {
        // Mock service returning a valid payment
        when(paymentService.savePayPalPayment(any(), anyInt())).thenReturn(payment);

        mockMvc.perform(post("/api/payments/save-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localOrderId\":1,\"order\":{\"id\":\"PAYPAL123\",\"purchase_units\":[{\"amount\":{\"value\":\"100.00\"},\"payments\":{\"captures\":[{\"id\":\"CAPTURE123\",\"status\":\"COMPLETED\"}]}}]}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(paymentService, times(1)).savePayPalPayment(any(), anyInt());
    }

    @Test
    void testSavePayPalPayment_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Simulate service throwing exception
        doThrow(new RuntimeException("Service failed"))
                .when(paymentService)
                .savePayPalPayment(any(), anyInt());

        mockMvc.perform(post("/api/payments/save-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localOrderId\":1,\"order\":{\"id\":\"PAYPAL123\"}}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service failed"));
    }

    @Test
    void testSavePayPalPayment_MissingOrder_ThrowsException() throws Exception {
        // Simulate exception when "order" is missing
        doThrow(new RuntimeException("Order data missing"))
                .when(paymentService)
                .savePayPalPayment(any(), anyInt());

        mockMvc.perform(post("/api/payments/save-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localOrderId\":1}")) // "order" key missing
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Order data missing"));
    }

    @Test
    void testSavePayPalPayment_MissingLocalOrderId_ThrowsException() throws Exception {
        // Use any() for both arguments to allow null
        doThrow(new RuntimeException("LocalOrderId missing"))
                .when(paymentService)
                .savePayPalPayment(any(), any());

        mockMvc.perform(post("/api/payments/save-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"order\":{\"id\":\"PAYPAL123\"}}")) // localOrderId missing
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("LocalOrderId missing"));
    }

}
