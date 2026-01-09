package ELEC5619_Practical2_Group_5.bookstore.dto.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Breakdown {
    @JsonProperty("item_total")
    private Money itemTotal;
}