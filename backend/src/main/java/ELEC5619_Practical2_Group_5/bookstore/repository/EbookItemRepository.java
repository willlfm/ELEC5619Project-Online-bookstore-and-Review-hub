package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.repository.EbookRow;
import ELEC5619_Practical2_Group_5.bookstore.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EbookItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query(value = """
        SELECT
          o.order_id       AS orderId,
          oi.order_item_id AS orderItemId,
          o.order_date     AS orderDate,
          o.status         AS status,
          b.book_id        AS bookId,
          b.title          AS bookTitle,
          b.author         AS author,
          bf.format        AS format,
          bf.source_url    AS sourceUrl
        FROM `order` o
        JOIN order_item   oi ON oi.order_id       = o.order_id
        JOIN book_format  bf ON bf.book_format_id = oi.book_format_id
        JOIN book         b  ON b.book_id         = bf.book_id
        JOIN user         u  ON u.user_id         = o.user_id
        WHERE u.username = :username
          AND bf.format  = 'ebook'
        ORDER BY o.order_date DESC
        """, nativeQuery = true)
    List<EbookRow> findMyEbooks(@Param("username") String username);

    @Query(value = """
        SELECT
          o.order_id       AS orderId,
          oi.order_item_id AS orderItemId,
          o.order_date     AS orderDate,
          o.status         AS status,
          b.book_id        AS bookId,
          b.title          AS bookTitle,
          b.author         AS author,
          bf.format        AS format,
          bf.source_url    AS sourceUrl
        FROM `order` o
        JOIN order_item   oi ON oi.order_id       = o.order_id
        JOIN book_format  bf ON bf.book_format_id = oi.book_format_id
        JOIN book         b  ON b.book_id         = bf.book_id
        JOIN user         u  ON u.user_id         = o.user_id
        WHERE u.username      = :username
          AND oi.order_item_id = :orderItemId
          AND bf.format       = 'ebook'
        LIMIT 1
        """, nativeQuery = true)
    Optional<EbookRow> findMyEbookByOrderItemId(@Param("username") String username,
                                                @Param("orderItemId") Integer orderItemId);
}
