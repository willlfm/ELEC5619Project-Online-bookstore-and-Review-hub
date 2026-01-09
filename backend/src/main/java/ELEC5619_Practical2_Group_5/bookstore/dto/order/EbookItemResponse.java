package ELEC5619_Practical2_Group_5.bookstore.dto.order;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EbookItemResponse {
    private Integer orderId;
    private Integer orderItemId;
    private String orderDate;
    private String status;
    private Integer bookId;
    private String bookTitle;
    private String author;
    private String format;
    private String sourceUrl;
}
