package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCancelRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCreateRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import ELEC5619_Practical2_Group_5.bookstore.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundControllerTest {

    @Mock
    private RefundService refundService;

    private RefundController refundController;

    @BeforeEach
    void setUp() {
        refundController = new RefundController(refundService);
    }

    // ========== createRefund ==========
    @Test
    void createRefund_ReturnsOk_WhenPrincipalPresent() {
        RefundCreateRequest req = new RefundCreateRequest();
        Principal principal = () -> "testUser";
        when(refundService.createRefund("testUser", req)).thenReturn(123);

        ResponseEntity<?> response = refundController.createRefund(req, principal);

        assertEquals(200, response.getStatusCodeValue());
        verify(refundService).createRefund("testUser", req);
    }

    @Test
    void createRefund_ReturnsUnauthorized_WhenPrincipalNull() {
        RefundCreateRequest req = new RefundCreateRequest();
        ResponseEntity<?> response = refundController.createRefund(req, null);

        assertEquals(401, response.getStatusCodeValue());
        verifyNoInteractions(refundService);
    }

    @Test
    void createRefund_ReturnsForbidden_WhenSecurityException() {
        RefundCreateRequest req = new RefundCreateRequest();
        Principal principal = () -> "user";
        when(refundService.createRefund("user", req)).thenThrow(new SecurityException());

        ResponseEntity<?> response = refundController.createRefund(req, principal);

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void createRefund_ReturnsBadRequest_WhenIllegalArgumentException() {
        RefundCreateRequest req = new RefundCreateRequest();
        Principal principal = () -> "user";
        when(refundService.createRefund("user", req)).thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<?> response = refundController.createRefund(req, principal);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void createRefund_ReturnsInternalServerError_WhenOtherException() {
        RefundCreateRequest req = new RefundCreateRequest();
        Principal principal = () -> "user";
        when(refundService.createRefund("user", req)).thenThrow(new RuntimeException("Oops"));

        ResponseEntity<?> response = refundController.createRefund(req, principal);

        assertEquals(500, response.getStatusCodeValue());
    }

    // ========== myRefunds ==========
    @Test
    void myRefunds_ReturnsOk_WhenPrincipalPresent() {
        Principal principal = () -> "testUser";
        Refund refund1 = new Refund();
        Refund refund2 = new Refund();
        List<Refund> refundList = Arrays.asList(refund1, refund2);
        doReturn(refundList).when(refundService).listMyRefunds(anyString());

        ResponseEntity<?> response = refundController.myRefunds(principal);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void myRefunds_ReturnsUnauthorized_WhenPrincipalNull() {
        ResponseEntity<?> response = refundController.myRefunds(null);
        assertEquals(401, response.getStatusCodeValue());
    }

    // ========== cancel ==========
    @Test
    void cancel_ReturnsOk_WhenPrincipalPresent() {
        RefundCancelRequest req = new RefundCancelRequest();
        req.setRefundId(1);
        Principal principal = () -> "user";

        doNothing().when(refundService).cancelRefund("user", 1);

        ResponseEntity<?> response = refundController.cancel(req, principal);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void cancel_ReturnsUnauthorized_WhenPrincipalNull() {
        RefundCancelRequest req = new RefundCancelRequest();
        req.setRefundId(1);

        ResponseEntity<?> response = refundController.cancel(req, null);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void cancel_ReturnsForbidden_WhenSecurityException() {
        RefundCancelRequest req = new RefundCancelRequest();
        req.setRefundId(1);
        Principal principal = () -> "user";

        doThrow(new SecurityException()).when(refundService).cancelRefund("user", 1);

        ResponseEntity<?> response = refundController.cancel(req, principal);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void cancel_ReturnsBadRequest_WhenIllegalArgumentOrStateException() {
        RefundCancelRequest req = new RefundCancelRequest();
        req.setRefundId(1);
        Principal principal = () -> "user";

        doThrow(new IllegalArgumentException("Invalid")).when(refundService).cancelRefund("user", 1);

        ResponseEntity<?> response = refundController.cancel(req, principal);
        assertEquals(400, response.getStatusCodeValue());

        doThrow(new IllegalStateException("State error")).when(refundService).cancelRefund("user", 1);
        response = refundController.cancel(req, principal);
        assertEquals(400, response.getStatusCodeValue());
    }

    // ========== getRefundReason ==========
    @Test
    void getRefundReason_ReturnsOk_WhenReasonExists() {
        Integer orderId = 10;
        when(refundService.getRefundReasonByOrderId(orderId)).thenReturn("Some reason");

        ResponseEntity<?> response = refundController.getRefundReason(orderId);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getRefundReason_ReturnsNotFound_WhenIllegalArgumentException() {
        Integer orderId = 10;
        when(refundService.getRefundReasonByOrderId(orderId)).thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<?> response = refundController.getRefundReason(orderId);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getRefundReason_ReturnsInternalServerError_WhenOtherException() {
        Integer orderId = 10;
        when(refundService.getRefundReasonByOrderId(orderId)).thenThrow(new RuntimeException("Oops"));

        ResponseEntity<?> response = refundController.getRefundReason(orderId);
        assertEquals(500, response.getStatusCodeValue());
    }
}
