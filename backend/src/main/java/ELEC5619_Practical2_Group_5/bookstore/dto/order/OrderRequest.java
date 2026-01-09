package ELEC5619_Practical2_Group_5.bookstore.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class OrderRequest {

    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String paypalOrderId;
    private ShippingInfoDTO shippingInfo;

    @Setter
    @Getter
    public static class OrderItemDTO {
        private Long bookFormatId;
        private Integer quantity;
        private BigDecimal price;
    }

    @Setter
    @Getter
    public static class ShippingInfoDTO {
        private String name;
        private String address;
        private String city;
        private String postcode;
        private String country;
    }
}
