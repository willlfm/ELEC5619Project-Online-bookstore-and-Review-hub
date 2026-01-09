package ELEC5619_Practical2_Group_5.bookstore.dto.cart;

import lombok.Data;

@Data
public class CartItemDTO {
    private Integer bookId;
    private Integer bookFormatId;  // 新增字段
    private String title;
    private String coverImageUrl;
    private Double price;
    private Integer quantity;
    private String format;
}
