package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistory;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistoryItem;

import java.util.List;

public interface OrderHistoryService {
    List<OrderHistory> listMyOrders(Integer userId, boolean includeItems);

    List<OrderHistoryItem> listOrderItems(Integer orderId, Integer userId);


}
