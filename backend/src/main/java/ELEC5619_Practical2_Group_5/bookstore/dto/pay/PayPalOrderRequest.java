package ELEC5619_Practical2_Group_5.bookstore.dto.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PayPalOrderRequest {
    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;
}