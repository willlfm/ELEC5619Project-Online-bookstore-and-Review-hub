package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderUpdateRequest;
import org.springframework.data.domain.Page;

public interface AdminOrderService {
    Page<AdminOrderDto> listOrders(int page, int size, String status);

    AdminOrderDto getOrder(Integer orderId);

    AdminOrderDto updateOrder(Integer orderId, AdminOrderUpdateRequest request);
}
