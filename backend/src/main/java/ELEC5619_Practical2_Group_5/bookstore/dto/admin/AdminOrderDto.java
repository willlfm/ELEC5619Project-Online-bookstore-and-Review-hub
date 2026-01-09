package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDto {
    private Integer orderId;
    private Integer userId;
    private String username;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String shippingName;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostcode;
    private String shippingCountry;
    private List<AdminOrderItemDto> items;
}
