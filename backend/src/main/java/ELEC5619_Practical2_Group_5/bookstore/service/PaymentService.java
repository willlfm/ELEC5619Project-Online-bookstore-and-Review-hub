package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.entity.Payment;

import java.util.Map;

public interface PaymentService {

    Payment savePayPalPayment(Map<String, Object> orderData, Integer localOrderId);

    /**
     * Process refund approval or rejection for an order.
     * @param orderId The order ID.
     * @param approved true if refund approved, false if rejected.
     */
    void processRefund(Integer orderId, boolean approved);
}
