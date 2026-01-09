package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderItemDto {
    private Integer orderItemId;
    private Integer bookFormatId;
    private String bookTitle;
    private String format;
    private Integer quantity;
    private BigDecimal price;
}
