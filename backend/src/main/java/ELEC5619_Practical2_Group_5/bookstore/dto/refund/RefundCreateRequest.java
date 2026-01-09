package ELEC5619_Practical2_Group_5.bookstore.dto.refund;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundCreateRequest {
    private Integer orderId;
    private BigDecimal amount;
    private String reason;
}
