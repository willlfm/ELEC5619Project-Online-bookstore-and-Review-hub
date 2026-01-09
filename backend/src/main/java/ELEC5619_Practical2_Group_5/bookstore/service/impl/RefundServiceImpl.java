package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCreateRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import ELEC5619_Practical2_Group_5.bookstore.repository.NotificationRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.RefundRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.RefundService;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final NotificationRepository notificationRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Integer createRefund(String username, RefundCreateRequest req) {
        var user = userRepository.findByUsername(username).orElseThrow();
        Order order = orderRepository.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!Objects.equals(order.getUserId(), user.getUserId()))
            throw new SecurityException("Forbidden");

        RefundRepository.PaymentRow pay = refundRepository.findLatestPaymentByOrderId(order.getOrderId())
                .orElseThrow(() -> new IllegalStateException("No payment found for order"));

        BigDecimal amount = req.getAmount() != null ? req.getAmount() : order.getTotalAmount();
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be > 0");
        if (amount.compareTo(pay.getAmount()) > 0)
            throw new IllegalArgumentException("Amount exceeds payment");

        Refund refund = Refund.builder()
                .paymentId(pay.getPaymentId())
                .orderId(order.getOrderId())
                .amount(amount)
                .reason(req.getReason())
                .status("pending")
                .refundDate(java.time.LocalDateTime.now())
                .build();

        Refund saved = refundRepository.save(refund);

        if (!"cancelled".equalsIgnoreCase(order.getStatus()) && !"refunding".equalsIgnoreCase(order.getStatus()) && !"completed".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("refunding");
            orderRepository.save(order);
        }

        return saved.getRefundId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundListItem> listMyRefunds(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
        SELECT r.refund_id, r.order_id, r.refund_date, r.amount, r.reason, r.status, r.processed_at
        FROM refund r
        JOIN `order` o ON r.order_id = o.order_id
        WHERE o.user_id = :uid
        ORDER BY r.refund_date DESC, r.refund_id DESC
    """).setParameter("uid", user.getUserId()).getResultList();

        List<RefundListItem> list = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            list.add(RefundListItem.builder()
                    .refundId(((Number) r[0]).intValue())
                    .orderId(((Number) r[1]).intValue())
                    .refundDate(r[2] == null ? null : ((java.sql.Timestamp) r[2]).toLocalDateTime())
                    .amount((java.math.BigDecimal) r[3])
                    .reason((String) r[4])
                    .status((String) r[5])
                    .processedAt(r[6] == null ? null : ((java.sql.Timestamp) r[6]).toLocalDateTime())
                    .build());
        }
        return list;
    }

    @Override
    @Transactional
    public void cancelRefund(String username, Integer refundId) {
        var user = userRepository.findByUsername(username).orElseThrow();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
        SELECT r.refund_id, o.user_id, r.status
        FROM refund r
        JOIN `order` o ON r.order_id = o.order_id
        WHERE r.refund_id = :rid
    """).setParameter("rid", refundId).getResultList();

        if (rows.isEmpty()) throw new IllegalArgumentException("Refund not found");
        Integer ownerId = ((Number) rows.get(0)[1]).intValue();
        String status = (String) rows.get(0)[2];
        if (!Objects.equals(ownerId, user.getUserId())) throw new SecurityException("Forbidden");
        if (!"pending".equalsIgnoreCase(status)) throw new IllegalStateException("Only pending refund can be cancelled");

        var refund = refundRepository.findById(refundId).orElseThrow();
        refund.setStatus("cancelled");
        refundRepository.save(refund);

        var order = orderRepository.findByOrderId(refund.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("pending");
            orderRepository.save(order);
        }

        String title = "Refund cancelled";
        String message = String.format(
                "Your refund request #%d for order #%d has been cancelled. Order status is now pending.",
                refund.getRefundId(), order.getOrderId()
        );


        notificationRepository.save(Notification.builder()
                .userId(order.getUserId() != null ? order.getUserId() : user.getUserId())
                .orderId(order.getOrderId())
                .title(title)
                .message(message)
                .type("refund")
                .pinned(false)
                .isRead(false)
                .status("active")
                .createdAt(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public String getRefundReasonByOrderId(Integer orderId) {
        Refund refund = refundRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Refund record not found for order ID: " + orderId));

        return refund.getReason() != null ? refund.getReason() : "No reason provided by user.";
    }
}
