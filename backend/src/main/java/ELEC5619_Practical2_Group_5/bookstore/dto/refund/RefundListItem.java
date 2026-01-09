package ELEC5619_Practical2_Group_5.bookstore.dto.refund;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundListItem {
    private Integer refundId;
    private Integer orderId;
    private LocalDateTime refundDate;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDateTime processedAt;
}
