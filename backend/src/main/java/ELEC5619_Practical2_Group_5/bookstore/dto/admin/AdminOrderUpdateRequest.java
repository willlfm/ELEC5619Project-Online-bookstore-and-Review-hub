package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.Data;

@Data
public class AdminOrderUpdateRequest {
    private String status;
    private String shippingName;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostcode;
    private String shippingCountry;
}
