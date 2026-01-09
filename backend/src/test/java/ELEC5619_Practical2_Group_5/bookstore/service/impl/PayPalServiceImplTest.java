package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayPalServiceImplTest {

    @InjectMocks
    private PayPalServiceImpl payPalService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        // Set up required configuration values
        ReflectionTestUtils.setField(payPalService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(payPalService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(payPalService, "baseUrl", "sandbox");
        ReflectionTestUtils.setField(payPalService, "webClient", webClient);
    }

    @Test
    void testCreateOrderFromCart_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> cart = List.of(
                Map.of("title", "Java Programming", "price", 29.99, "quantity", 2),
                Map.of("title", "Spring Boot Guide", "price", 39.99, "quantity", 1)
        );
        Map<String, Object> body = Map.of("cart", cart);

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-access-token");
        Map<String, Object> orderResponse = Map.of(
                "id", "ORDER123",
                "status", "CREATED",
                "links", List.of(
                        Map.of("rel", "approve", "href", "https://paypal.com/approve")
                )
        );

        // Mock access token call
        mockAccessTokenCall(accessTokenResponse);

        // Mock create order call
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(accessTokenResponse))
                .thenReturn(Mono.just(orderResponse));

        // Act
        Map<String, Object> result = payPalService.createOrderFromCart(body);

        // Assert
        assertNotNull(result);
        assertEquals("ORDER123", result.get("id"));
        assertEquals("CREATED", result.get("status"));
        verify(webClient, atLeast(2)).post();
    }

    @Test
    void testCreateOrderFromCart_CalculatesTotalCorrectly() throws Exception {
        // Arrange
        List<Map<String, Object>> cart = List.of(
                Map.of("title", "Book1", "price", 10.50, "quantity", 3),
                Map.of("title", "Book2", "price", 20.00, "quantity", 2)
        );
        Map<String, Object> body = Map.of("cart", cart);

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> orderResponse = Map.of("id", "ORDER456", "status", "CREATED");

        mockWebClientForSuccessfulOrder(accessTokenResponse, orderResponse);

        // Act
        Map<String, Object> result = payPalService.createOrderFromCart(body);

        // Assert
        assertNotNull(result);
        // Expected total: (10.50 * 3) + (20.00 * 2) = 31.50 + 40.00 = 71.50
    }

    @Test
    void testCreateOrderFromRaw_Success() throws Exception {
        // Arrange
        Map<String, Object> orderRequest = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(
                        Map.of(
                                "amount", Map.of("currency_code", "AUD", "value", "100.00")
                        )
                )
        );

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> orderResponse = Map.of("id", "ORDER789", "status", "CREATED");

        mockWebClientForSuccessfulOrder(accessTokenResponse, orderResponse);

        // Act
        Map<String, Object> result = payPalService.createOrderFromRaw(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ORDER789", result.get("id"));
        assertEquals("CREATED", result.get("status"));
    }

    @Test
    void testCreateOrderFromRaw_AccessTokenFailure() {
        // Arrange
        Map<String, Object> orderRequest = Map.of("intent", "CAPTURE");
        Map<String, Object> invalidTokenResponse = Map.of("error", "invalid_client");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(invalidTokenResponse));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            payPalService.createOrderFromRaw(orderRequest);
        });

        assertTrue(exception.getMessage().contains("Failed to get PayPal access token"));
    }

    @Test
    void testCaptureOrder_Success() throws Exception {
        // Arrange
        String orderId = "ORDER123";
        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> captureResponse = Map.of(
                "id", orderId,
                "status", "COMPLETED",
                "purchase_units", List.of(
                        Map.of("payments", Map.of(
                                "captures", List.of(Map.of("id", "CAPTURE123", "status", "COMPLETED"))
                        ))
                )
        );

        mockWebClientForSuccessfulCapture(accessTokenResponse, captureResponse);

        // Act
        Map<String, Object> result = payPalService.captureOrder(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.get("id"));
        assertEquals("COMPLETED", result.get("status"));
    }

    @Test
    void testCaptureOrder_WithSandboxUrl() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(payPalService, "baseUrl", "sandbox");
        String orderId = "ORDER456";
        Map<String, Object> accessTokenResponse = Map.of("access_token", "sandbox-token");
        Map<String, Object> captureResponse = Map.of("id", orderId, "status", "COMPLETED");

        mockWebClientForSuccessfulCapture(accessTokenResponse, captureResponse);

        // Act
        Map<String, Object> result = payPalService.captureOrder(orderId);

        // Assert
        assertNotNull(result);
        verify(webClient, atLeast(2)).post();
    }

    @Test
    void testCaptureOrder_WithLiveUrl() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(payPalService, "baseUrl", "live");
        String orderId = "ORDER789";
        Map<String, Object> accessTokenResponse = Map.of("access_token", "live-token");
        Map<String, Object> captureResponse = Map.of("id", orderId, "status", "COMPLETED");

        mockWebClientForSuccessfulCapture(accessTokenResponse, captureResponse);

        // Act
        Map<String, Object> result = payPalService.captureOrder(orderId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testRefundPayment_Success() throws Exception {
        // Arrange
        String captureId = "CAPTURE123";
        BigDecimal amount = new BigDecimal("50.00");

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> refundResponse = Map.of(
                "id", "REFUND123",
                "status", "COMPLETED",
                "amount", Map.of("currency_code", "AUD", "value", "50.00")
        );

        mockWebClientForSuccessfulRefund(accessTokenResponse, refundResponse);

        // Act
        Map<String, Object> result = payPalService.refundPayment(captureId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("REFUND123", result.get("id"));
        assertEquals("COMPLETED", result.get("status"));
    }

    @Test
    void testRefundPayment_WithLiveEnvironment() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(payPalService, "baseUrl", "live");
        String captureId = "CAPTURE456";
        BigDecimal amount = new BigDecimal("75.50");

        Map<String, Object> accessTokenResponse = Map.of("access_token", "live-token");
        Map<String, Object> refundResponse = Map.of("id", "REFUND456", "status", "COMPLETED");

        mockWebClientForSuccessfulRefund(accessTokenResponse, refundResponse);

        // Act
        Map<String, Object> result = payPalService.refundPayment(captureId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("REFUND456", result.get("id"));
    }

    @Test
    void testRefundPayment_FormatsAmountCorrectly() throws Exception {
        // Arrange
        String captureId = "CAPTURE789";
        BigDecimal amount = new BigDecimal("123.456"); // Should be formatted to 2 decimal places

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> refundResponse = Map.of("id", "REFUND789", "status", "COMPLETED");

        mockWebClientForSuccessfulRefund(accessTokenResponse, refundResponse);

        // Act
        Map<String, Object> result = payPalService.refundPayment(captureId, amount);

        // Assert
        assertNotNull(result);
        // The service should format 123.456 to "123.46"
    }

    @Test
    void testCreateOrderFromCart_EmptyCart() throws Exception {
        // Arrange
        List<Map<String, Object>> emptyCart = List.of();
        Map<String, Object> body = Map.of("cart", emptyCart);

        Map<String, Object> accessTokenResponse = Map.of("access_token", "mock-token");
        Map<String, Object> orderResponse = Map.of("id", "ORDER_EMPTY", "status", "CREATED");

        mockWebClientForSuccessfulOrder(accessTokenResponse, orderResponse);

        // Act
        Map<String, Object> result = payPalService.createOrderFromCart(body);

        // Assert
        assertNotNull(result);
        // Total amount should be 0.00
    }

    // ========== Helper Methods ==========

    private void mockAccessTokenCall(Map<String, Object> accessTokenResponse) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(accessTokenResponse));
    }

    private void mockWebClientForSuccessfulOrder(Map<String, Object> accessTokenResponse,
                                                 Map<String, Object> orderResponse) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(accessTokenResponse))
                .thenReturn(Mono.just(orderResponse));
    }

    private void mockWebClientForSuccessfulCapture(Map<String, Object> accessTokenResponse,
                                                   Map<String, Object> captureResponse) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(accessTokenResponse))
                .thenReturn(Mono.just(captureResponse));
    }

    private void mockWebClientForSuccessfulRefund(Map<String, Object> accessTokenResponse,
                                                  Map<String, Object> refundResponse) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(accessTokenResponse))
                .thenReturn(Mono.just(refundResponse));
    }
}