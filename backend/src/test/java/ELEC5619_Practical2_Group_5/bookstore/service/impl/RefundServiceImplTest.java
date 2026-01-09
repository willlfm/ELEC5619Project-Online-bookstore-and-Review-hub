package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundCreateRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.refund.RefundListItem;
import ELEC5619_Practical2_Group_5.bookstore.entity.Notification;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.Refund;
import ELEC5619_Practical2_Group_5.bookstore.repository.NotificationRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.RefundRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EntityManager em;

    @InjectMocks
    private RefundServiceImpl refundService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refundService, "em", em);
    }

    // ================== createRefund ==================
    @Test
    void createRefund_ReturnsRefundId_WhenValid() {
        String username = "testUser";
        RefundCreateRequest req = new RefundCreateRequest();
        req.setOrderId(1);
        req.setAmount(BigDecimal.valueOf(50));
        req.setReason("Reason");

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(123);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus("pending");
        lenient().doReturn(Optional.of(order)).when(orderRepository).findByOrderId(1);

        RefundRepository.PaymentRow payRow = mock(RefundRepository.PaymentRow.class);
        lenient().doReturn(1).when(payRow).getPaymentId();
        lenient().doReturn(BigDecimal.valueOf(100)).when(payRow).getAmount();
        lenient().doReturn(Optional.of(payRow)).when(refundRepository).findLatestPaymentByOrderId(1);

        Refund savedRefund = new Refund();
        savedRefund.setRefundId(99);
        lenient().doReturn(savedRefund).when(refundRepository).save(any(Refund.class));

        Integer refundId = refundService.createRefund(username, req);

        assertEquals(99, refundId);
        verify(refundRepository, times(1)).save(any(Refund.class));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createRefund_OrderNotFound_Throws() {
        String username = "testUser";
        RefundCreateRequest req = new RefundCreateRequest();
        req.setOrderId(1);

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        lenient().doReturn(Optional.empty()).when(orderRepository).findByOrderId(1);

        assertThrows(IllegalArgumentException.class, () -> refundService.createRefund(username, req));
    }

    @Test
    void createRefund_SecurityException_WhenUserMismatch() {
        String username = "testUser";
        RefundCreateRequest req = new RefundCreateRequest();
        req.setOrderId(1);

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(999).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(123);
        order.setTotalAmount(BigDecimal.valueOf(100));
        lenient().doReturn(Optional.of(order)).when(orderRepository).findByOrderId(1);

        RefundRepository.PaymentRow payRow = mock(RefundRepository.PaymentRow.class);
        lenient().doReturn(Optional.of(payRow)).when(refundRepository).findLatestPaymentByOrderId(1);

        assertThrows(SecurityException.class, () -> refundService.createRefund(username, req));
    }

    @Test
    void createRefund_AmountZeroOrNegative_Throws() {
        String username = "testUser";
        RefundCreateRequest req = new RefundCreateRequest();
        req.setOrderId(1);
        req.setAmount(BigDecimal.ZERO);

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(123);
        order.setTotalAmount(BigDecimal.valueOf(100));
        lenient().doReturn(Optional.of(order)).when(orderRepository).findByOrderId(1);

        RefundRepository.PaymentRow payRow = mock(RefundRepository.PaymentRow.class);
        lenient().doReturn(1).when(payRow).getPaymentId();
        lenient().doReturn(BigDecimal.valueOf(100)).when(payRow).getAmount();
        lenient().doReturn(Optional.of(payRow)).when(refundRepository).findLatestPaymentByOrderId(1);

        assertThrows(IllegalArgumentException.class, () -> refundService.createRefund(username, req));
    }

    @Test
    void createRefund_AmountExceedsPayment_Throws() {
        String username = "testUser";
        RefundCreateRequest req = new RefundCreateRequest();
        req.setOrderId(1);
        req.setAmount(BigDecimal.valueOf(200));

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(123);
        order.setTotalAmount(BigDecimal.valueOf(100));
        lenient().doReturn(Optional.of(order)).when(orderRepository).findByOrderId(1);

        RefundRepository.PaymentRow payRow = mock(RefundRepository.PaymentRow.class);
        lenient().doReturn(1).when(payRow).getPaymentId();
        lenient().doReturn(BigDecimal.valueOf(100)).when(payRow).getAmount();
        lenient().doReturn(Optional.of(payRow)).when(refundRepository).findLatestPaymentByOrderId(1);

        assertThrows(IllegalArgumentException.class, () -> refundService.createRefund(username, req));
    }

    // ================== listMyRefunds ==================
    @Test
    void listMyRefunds_ReturnsRefundList() {
        String username = "testUser";
        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Object[] row1 = {1, 101, java.sql.Timestamp.valueOf(LocalDateTime.now()), BigDecimal.valueOf(50), "reason1", "pending", null};
        Object[] row2 = {2, 102, java.sql.Timestamp.valueOf(LocalDateTime.now()), BigDecimal.valueOf(70), "reason2", "completed", java.sql.Timestamp.valueOf(LocalDateTime.now())};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        Query query = mock(Query.class);
        lenient().doReturn(query).when(em).createNativeQuery(anyString());
        lenient().doReturn(query).when(query).setParameter(anyString(), any());
        lenient().doReturn(rows).when(query).getResultList();

        List<RefundListItem> list = refundService.listMyRefunds(username);
        assertEquals(2, list.size());
    }

    // ================== cancelRefund ==================
    @Test
    void cancelRefund_CancelsSuccessfully_WhenPending() {
        String username = "testUser";
        Integer refundId = 1;

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        Object[] row = {1, 123, "pending"};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);

        Query query = mock(Query.class);
        lenient().doReturn(query).when(em).createNativeQuery(anyString());
        lenient().doReturn(query).when(query).setParameter(anyString(), any());
        lenient().doReturn(rows).when(query).getResultList();

        Refund refund = new Refund();
        refund.setRefundId(refundId);
        refund.setOrderId(10);
        lenient().doReturn(Optional.of(refund)).when(refundRepository).findById(refundId);

        Order order = new Order();
        order.setOrderId(10);
        order.setUserId(123);
        order.setStatus("refunding");
        lenient().doReturn(Optional.of(order)).when(orderRepository).findByOrderId(10);

        refundService.cancelRefund(username, refundId);

        assertEquals("cancelled", refund.getStatus());
        assertEquals("pending", order.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(refundRepository, times(1)).save(refund);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelRefund_RefundNotFound_Throws() {
        String username = "testUser";
        Integer refundId = 1;

        var user = mock(ELEC5619_Practical2_Group_5.bookstore.entity.User.class);
        lenient().doReturn(123).when(user).getUserId();
        lenient().doReturn(Optional.of(user)).when(userRepository).findByUsername(username);

        List<Object[]> rows = new ArrayList<>(); // empty -> refund not found
        Query query = mock(Query.class);
        lenient().doReturn(query).when(em).createNativeQuery(anyString());
        lenient().doReturn(query).when(query).setParameter(anyString(), any());
        lenient().doReturn(rows).when(query).getResultList();

        assertThrows(IllegalArgumentException.class, () -> refundService.cancelRefund(username, refundId));
    }

    // ================== getRefundReasonByOrderId ==================
    @Test
    void getRefundReasonByOrderId_ReturnsReason() {
        Refund refund = new Refund();
        refund.setReason("Some reason");
        lenient().doReturn(Optional.of(refund)).when(refundRepository).findByOrderId(1);

        String reason = refundService.getRefundReasonByOrderId(1);
        assertEquals("Some reason", reason);
    }

    @Test
    void getRefundReasonByOrderId_NoReason_ReturnsDefault() {
        Refund refund = new Refund();
        refund.setReason(null);
        lenient().doReturn(Optional.of(refund)).when(refundRepository).findByOrderId(1);

        String reason = refundService.getRefundReasonByOrderId(1);
        assertEquals("No reason provided by user.", reason);
    }
}
