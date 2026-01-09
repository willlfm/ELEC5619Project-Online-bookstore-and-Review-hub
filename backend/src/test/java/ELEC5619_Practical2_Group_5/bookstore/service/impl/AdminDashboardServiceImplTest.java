package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.DashboardSummary;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    void loadSummary_aggregatesMetrics() {
        Order pending = new Order();
        pending.setOrderId(1);
        pending.setStatus("pending");
        pending.setOrderDate(LocalDateTime.of(2024, 9, 1, 12, 0));
        pending.setTotalAmount(BigDecimal.valueOf(100));

        Order shipped = new Order();
        shipped.setOrderId(2);
        shipped.setStatus("shipped");
        shipped.setOrderDate(LocalDateTime.of(2024, 9, 15, 12, 0));
        shipped.setTotalAmount(BigDecimal.valueOf(50));

        Order old = new Order();
        old.setOrderId(3);
        old.setStatus("completed");
        old.setOrderDate(LocalDateTime.of(2024, 8, 10, 12, 0));
        old.setTotalAmount(BigDecimal.valueOf(25));

        when(orderRepository.findAll()).thenReturn(List.of(pending, shipped, old));
        when(userRepository.count()).thenReturn(4L);

        DashboardSummary summary = adminDashboardService.loadSummary();

        assertThat(summary.getTotalRevenue()).isEqualByComparingTo("175");
        assertThat(summary.getTotalOrders()).isEqualTo(3);
        assertThat(summary.getPendingOrders()).isEqualTo(1); // only "pending" counts
        assertThat(summary.getTotalUsers()).isEqualTo(4);
        assertThat(summary.getMonthlySales()).extracting(DashboardSummary.SalesTrendPoint::getMonth)
                .containsExactly("2024-08", "2024-09");
        assertThat(summary.getStatusBreakdown()).extracting(DashboardSummary.OrderStatusSummary::getStatus)
                .contains("pending", "shipped", "completed");
    }

    @Test
    void loadSummary_handlesNullValues() {
        Order noAmount = new Order();
        noAmount.setOrderId(4);
        noAmount.setStatus(null);
        noAmount.setOrderDate(null);
        noAmount.setTotalAmount(null); // should be excluded from revenue and monthly sales

        when(orderRepository.findAll()).thenReturn(List.of(noAmount));
        when(userRepository.count()).thenReturn(0L);

        DashboardSummary summary = adminDashboardService.loadSummary();

        assertThat(summary.getTotalRevenue()).isEqualByComparingTo("0");
        assertThat(summary.getMonthlySales()).isEmpty();
        assertThat(summary.getStatusBreakdown()).extracting(DashboardSummary.OrderStatusSummary::getStatus)
                .containsExactly("unknown");
        assertThat(summary.getPendingOrders()).isZero();
    }
}
