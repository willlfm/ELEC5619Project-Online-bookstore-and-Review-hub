package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Integer> {
    interface PaymentRow {
        Integer getPaymentId();
        BigDecimal getAmount();
    }

    @Query(value = """
        SELECT p.payment_id AS paymentId, p.amount AS amount
        FROM payment p
        WHERE p.order_id = :orderId
        ORDER BY p.payment_date DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<PaymentRow> findLatestPaymentByOrderId(@Param("orderId") Integer orderId);

    Optional<Refund> findByOrderId(Integer orderId);
}
