package ELEC5619_Practical2_Group_5.bookstore.dto.order;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryItem {
    private Integer bookFormatId;
    private Integer bookId;
    private String  bookTitle;
    private String  format;
    private Integer quantity;
    private BigDecimal price;
}
