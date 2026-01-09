package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;

public interface OrderService {
    Order createOrder(User user, OrderRequest orderRequest);

    OrderResponse toOrderResponse(Order order);
}
