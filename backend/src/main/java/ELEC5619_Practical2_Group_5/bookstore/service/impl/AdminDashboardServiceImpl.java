package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.DashboardSummary;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// Gathers simple aggregates so the dashboard can remain responsive without heavy SQL.
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public DashboardSummary loadSummary() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();
        long pendingOrders = orders.stream()
                .filter(order -> "pending".equalsIgnoreCase(order.getStatus()) ||
                        "processing".equalsIgnoreCase(order.getStatus()))
                .count();

        Map<String, BigDecimal> monthlySales = orders.stream()
                .filter(order -> order.getOrderDate() != null && order.getTotalAmount() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().format(MONTH_FORMATTER),
                        TreeMap::new,
                        Collectors.mapping(Order::getTotalAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<DashboardSummary.SalesTrendPoint> trend = monthlySales.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> DashboardSummary.SalesTrendPoint.builder()
                        .month(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .toList();

        List<DashboardSummary.OrderStatusSummary> statusBreakdown = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown",
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(entry -> DashboardSummary.OrderStatusSummary.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();

        return DashboardSummary.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .totalUsers(userRepository.count())
                .monthlySales(trend)
                .statusBreakdown(statusBreakdown)
                .build();
    }
}
