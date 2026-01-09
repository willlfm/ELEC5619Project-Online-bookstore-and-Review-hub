package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long pendingOrders;
    private long totalUsers;
    private List<SalesTrendPoint> monthlySales;
    private List<OrderStatusSummary> statusBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrendPoint {
        private String month;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusSummary {
        private String status;
        private long count;
    }
}
