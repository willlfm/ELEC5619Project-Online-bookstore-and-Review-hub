package ELEC5619_Practical2_Group_5.bookstore.dto.order;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistory {
    private Integer orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String shippingName;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostcode;
    private String shippingCountry;
    private String status;
    private List<OrderHistoryItem> items; // includeItems=false 时可为 null
}
