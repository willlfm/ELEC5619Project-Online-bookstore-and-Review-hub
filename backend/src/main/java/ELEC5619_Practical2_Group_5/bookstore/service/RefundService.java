package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCreateRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundListItem;

import java.util.List;

public interface RefundService {
    Integer createRefund(String username, RefundCreateRequest req);

    List<RefundListItem> listMyRefunds(String username);

    void cancelRefund(String username, Integer refundId);

    String getRefundReasonByOrderId(Integer orderId);
}