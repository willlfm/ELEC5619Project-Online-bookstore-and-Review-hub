package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Integer orderId);

    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId);

    Page<Order> findByStatusIgnoreCase(String status, Pageable pageable);
}
