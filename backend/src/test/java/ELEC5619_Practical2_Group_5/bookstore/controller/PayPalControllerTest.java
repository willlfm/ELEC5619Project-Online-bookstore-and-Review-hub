package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.service.impl.PayPalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayPalControllerTest {

    @Mock
    private PayPalServiceImpl payPalService;

    @InjectMocks
    private PayPalController payPalController;

    private Map<String, Object> requestBody;

    @BeforeEach
    void setUp() {
        requestBody = Map.of(
                "cartId", 123,
                "currency", "USD"
        );
    }

    @Test
    void createOrder_ReturnsOk_WhenSuccess() throws Exception {
        Map<String, Object> serviceResponse = Map.of(
                "orderId", "ABC123",
                "status", "CREATED"
        );

        when(payPalService.createOrderFromCart(requestBody)).thenReturn(serviceResponse);

        ResponseEntity<?> response = payPalController.createOrder(requestBody);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(serviceResponse, response.getBody());
        verify(payPalService, times(1)).createOrderFromCart(requestBody);
    }

    @Test
    void createOrder_ReturnsInternalServerError_WhenException() throws Exception {
        when(payPalService.createOrderFromCart(requestBody)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = payPalController.createOrder(requestBody);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("Service error"));
        verify(payPalService, times(1)).createOrderFromCart(requestBody);
    }

    @Test
    void captureOrder_ReturnsOk_WhenSuccess() throws Exception {
        String orderId = "ABC123";
        Map<String, Object> serviceResponse = Map.of(
                "orderId", orderId,
                "status", "COMPLETED"
        );

        when(payPalService.captureOrder(orderId)).thenReturn(serviceResponse);

        ResponseEntity<?> response = payPalController.captureOrder(orderId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(serviceResponse, response.getBody());
        verify(payPalService, times(1)).captureOrder(orderId);
    }

    @Test
    void captureOrder_ReturnsInternalServerError_WhenException() throws Exception {
        String orderId = "ABC123";
        when(payPalService.captureOrder(orderId)).thenThrow(new RuntimeException("Capture failed"));

        ResponseEntity<?> response = payPalController.captureOrder(orderId);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("Capture failed"));
        verify(payPalService, times(1)).captureOrder(orderId);
    }
}
