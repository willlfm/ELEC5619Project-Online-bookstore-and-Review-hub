package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistory;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistoryItem;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderHistoryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<OrderHistory> listMyOrders(Integer userId, boolean includeItems) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);

        final Map<Integer, List<OrderHistoryItem>> itemsByOrderId;
        if (includeItems && !orders.isEmpty()) {
            List<Integer> ids = orders.stream().map(Order::getOrderId).toList();
            itemsByOrderId = fetchItemsForOrderIds(ids);
        } else {
            itemsByOrderId = java.util.Collections.emptyMap();
        }

        return orders.stream().map(o -> OrderHistory.builder()
                .orderId(o.getOrderId())
                .orderDate(o.getOrderDate())
                .totalAmount(o.getTotalAmount())
                .shippingName(o.getShippingName())
                .shippingAddress(o.getShippingAddress())
                .shippingCity(o.getShippingCity())
                .shippingPostcode(o.getShippingPostcode())
                .shippingCountry(o.getShippingCountry())
                .status(o.getStatus())
                .items(includeItems ? itemsByOrderId.getOrDefault(o.getOrderId(), List.of()) : null)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<OrderHistoryItem> listOrderItems(Integer orderId, Integer userId) {
        Order o = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!Objects.equals(o.getUserId(), userId)) throw new SecurityException("Forbidden");

        return fetchItemsForOrderIds(List.of(orderId)).getOrDefault(orderId, List.of());
    }

    private Map<Integer, List<OrderHistoryItem>> fetchItemsForOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return Map.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT 
              oi.order_id,           -- 0
              oi.book_format_id,     -- 1
              oi.quantity,           -- 2
              oi.price,              -- 3
              b.title,               -- 4
              bf.format,             -- 5
              b.book_id              -- 6
            FROM order_item oi
            JOIN book_format bf ON oi.book_format_id = bf.book_format_id
            JOIN book b         ON bf.book_id = b.book_id
            WHERE oi.order_id IN (:ids)
            ORDER BY oi.order_id, oi.order_item_id
        """).setParameter("ids", orderIds).getResultList();

        Map<Integer, List<OrderHistoryItem>> map = new LinkedHashMap<>();
        for (Object[] r : rows) {
            Integer orderId = ((Number) r[0]).intValue();
            Integer bookFormatId = ((Number) r[1]).intValue();
            Integer quantity = ((Number) r[2]).intValue();
            BigDecimal price = (BigDecimal) r[3];
            String bookTitle = (String) r[4];
            String format = (String) r[5];
            Integer bookId = ((Number) r[6]).intValue();

            map.computeIfAbsent(orderId, k -> new ArrayList<>()).add(
                    OrderHistoryItem.builder()
                            .bookFormatId(bookFormatId)
                            .quantity(quantity)
                            .price(price)
                            .bookTitle(bookTitle)
                            .format(format)
                            .bookId(bookId)
                            .build()
            );
        }
        orderIds.forEach(id -> map.putIfAbsent(id, new ArrayList<>()));
        return map;
    }
}
