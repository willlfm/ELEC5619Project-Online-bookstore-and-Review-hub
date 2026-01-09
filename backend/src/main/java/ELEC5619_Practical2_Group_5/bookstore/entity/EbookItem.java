package ELEC5619_Practical2_Group_5.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ebook_item", indexes = {
        @Index(name = "idx_ebook_item_user", columnList = "user_id"),
        @Index(name = "idx_ebook_item_order_date", columnList = "order_date")
})
public class EbookItem {
    /**
     * Primary key is the orderItemId to align with frontend semantics.
     */
    @Id
    @Column(name = "id")
    private Integer id; // orderItemId

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ebook_item_user"))
    private User user;

    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "book_title", nullable = false, length = 300)
    private String bookTitle;

    @Column(name = "author", length = 200)
    private String author;

    @Column(name = "format", length = 50, nullable = false)
    private String format;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    /**
     * If present, controller will 302 redirect to this URL for download.
     */
    @Column(name = "source_url", length = 2000)
    private String sourceUrl;

    /**
     * If present and downloadUrl is absent, controller will stream the file from this path.
     * Example: /var/data/ebooks/12345.pdf
     */
    @Column(name = "source_path", length = 2000)
    private String sourcePath;
}
