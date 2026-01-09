package ELEC5619_Practical2_Group_5.bookstore.dto.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Amount {
    @JsonProperty("currency_code")
    private String currencyCode;
    private String value;
    private Breakdown breakdown;
}