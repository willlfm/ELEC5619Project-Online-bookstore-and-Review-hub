package ELEC5619_Practical2_Group_5.bookstore.dto.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Item {
    private String name;
    @JsonProperty("unit_amount")
    private Money unitAmount;
    private String quantity;
}