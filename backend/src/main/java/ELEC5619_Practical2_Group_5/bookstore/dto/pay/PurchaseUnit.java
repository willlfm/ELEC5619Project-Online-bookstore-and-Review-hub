package ELEC5619_Practical2_Group_5.bookstore.dto.pay;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseUnit {
    private String reference_id;
    private String description;
    private Amount amount;
    private List<Item> items;
}