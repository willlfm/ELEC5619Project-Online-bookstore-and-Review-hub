package ELEC5619_Practical2_Group_5.bookstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "order_item",
        indexes = {
                @Index(name = "idx_order_id", columnList = "order_id"),
                @Index(name = "idx_book_id", columnList = "book_format_id")
        })
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "book_format_id", nullable = false)
    private Integer bookFormatId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
}
