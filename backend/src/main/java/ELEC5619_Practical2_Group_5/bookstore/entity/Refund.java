package ELEC5619_Practical2_Group_5.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer refundId;

    private Integer paymentId;
    private Integer orderId;

    private LocalDateTime refundDate;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDateTime processedAt;
}
