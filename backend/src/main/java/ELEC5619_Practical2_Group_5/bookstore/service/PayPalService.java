package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.pay.PayPalOrderRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface PayPalService {
    Map<String, Object> createOrderFromCart(Map<String, Object> body) throws Exception;
    Map<String, Object> captureOrder(String orderId) throws Exception;

    /**
     * Issue a refund for a captured PayPal payment.
     * @param captureId PayPal capture_id (transaction ID).
     * @param amount Refund amount.
     * @return PayPal refund API response.
     * @throws Exception if refund fails.
     */
    Map<String, Object> refundPayment(String captureId, BigDecimal amount) throws Exception;
}
