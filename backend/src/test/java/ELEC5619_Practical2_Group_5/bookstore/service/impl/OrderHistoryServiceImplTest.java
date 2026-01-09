package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistory;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderHistoryItem;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EntityManager em;

    @Mock
    private Query nativeQuery;

    private OrderHistoryServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new OrderHistoryServiceImpl(orderRepository);
        Field f = OrderHistoryServiceImpl.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(service, em);
    }

    private Order newOrder(int id, int userId, BigDecimal total) {
        Order o = new Order();
        o.setOrderId(id);
        o.setUserId(userId);
        o.setOrderDate(LocalDateTime.of(2024, 1, Math.min(id, 28), 10, 0));
        o.setTotalAmount(total);
        o.setShippingName("Alice");
        o.setShippingAddress("1 Street");
        o.setShippingCity("City");
        o.setShippingPostcode("2000");
        o.setShippingCountry("AU");
        o.setStatus("PAID");
        return o;
    }

    // includeItems = false → items = null
    @Test
    void listMyOrders_ReturnsSummaries_WhenIncludeItemsFalse() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(7))
                .thenReturn(List.of(
                        newOrder(1, 7, new BigDecimal("12.34")),
                        newOrder(2, 7, new BigDecimal("56.78"))
                ));

        List<OrderHistory> out = service.listMyOrders(7, false);

        assertEquals(2, out.size());
        assertEquals(1, out.get(0).getOrderId());
        assertNull(out.get(0).getItems());
        assertEquals(2, out.get(1).getOrderId());
        assertNull(out.get(1).getItems());
    }

    @Test
    void listMyOrders_ReturnsItemsGrouped_WhenIncludeItemsTrue() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(7))
                .thenReturn(List.of(
                        newOrder(1, 7, new BigDecimal("10.00")),
                        newOrder(2, 7, new BigDecimal("20.00"))
                ));

        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("ids"), any())).thenReturn(nativeQuery);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1, 101, 2, new BigDecimal("5.00"), "Book A", "Paperback", 1001});
        rows.add(new Object[]{1, 102, 1, new BigDecimal("5.00"), "Book B", "Hardcover", 1002});
        when(nativeQuery.getResultList()).thenReturn(rows);

        List<OrderHistory> out = service.listMyOrders(7, true);

        assertEquals(2, out.size());

        OrderHistory h1 = out.get(0);
        assertEquals(1, h1.getOrderId());
        assertNotNull(h1.getItems());
        assertEquals(2, h1.getItems().size());
        assertEquals(101, h1.getItems().get(0).getBookFormatId());
        assertEquals("Book A", h1.getItems().get(0).getBookTitle());

        OrderHistory h2 = out.get(1);
        assertEquals(2, h2.getOrderId());
        assertNotNull(h2.getItems());
        assertTrue(h2.getItems().isEmpty(), "An empty list should be returned if no details are available not null");
    }

    @Test
    void listOrderItems_ReturnsItems_WhenUserOwnsOrder() {
        Order o = newOrder(9, 7, new BigDecimal("30.00"));
        when(orderRepository.findByOrderId(9)).thenReturn(Optional.of(o));

        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("ids"), any())).thenReturn(nativeQuery);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{9, 201, 3, new BigDecimal("10.00"), "Book C", "eBook", 2001});
        when(nativeQuery.getResultList()).thenReturn(rows);

        List<OrderHistoryItem> items = service.listOrderItems(9, 7);

        assertEquals(1, items.size());
        OrderHistoryItem it = items.get(0);
        assertEquals(201, it.getBookFormatId());
        assertEquals(3, it.getQuantity());
        assertEquals(new BigDecimal("10.00"), it.getPrice());
        assertEquals("Book C", it.getBookTitle());
        assertEquals("eBook", it.getFormat());
        assertEquals(2001, it.getBookId());
    }

    // listOrderItems：order do not exist
    @Test
    void listOrderItems_ThrowsIllegalArgument_WhenOrderNotFound() {
        when(orderRepository.findByOrderId(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.listOrderItems(999, 7));
        assertTrue(ex.getMessage().contains("Order not found"));
    }

    // listOrderItems：can not access
    @Test
    void listOrderItems_ThrowsSecurityException_WhenUserMismatch() {
        Order o = newOrder(9, 77, new BigDecimal("30.00"));
        when(orderRepository.findByOrderId(9)).thenReturn(Optional.of(o));

        assertThrows(SecurityException.class, () -> service.listOrderItems(9, 7));
        verifyNoInteractions(em);
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<Integer, java.util.List<OrderHistoryItem>> invokeFetch(java.util.List<Integer> ids) throws Exception {
        var m = OrderHistoryServiceImpl.class.getDeclaredMethod("fetchItemsForOrderIds", java.util.List.class);
        m.setAccessible(true);
        return (java.util.Map<Integer, java.util.List<OrderHistoryItem>>) m.invoke(service, ids);
    }

    @org.junit.jupiter.api.Test
    void fetchItemsForOrderIds_ReturnsEmptyMap_WhenNull() throws Exception {
        var map = invokeFetch(null);
        org.junit.jupiter.api.Assertions.assertNotNull(map);
        org.junit.jupiter.api.Assertions.assertTrue(map.isEmpty());
        org.mockito.Mockito.verifyNoInteractions(em);
    }

    @org.junit.jupiter.api.Test
    void fetchItemsForOrderIds_ReturnsEmptyMap_WhenEmpty() throws Exception {
        var map = invokeFetch(java.util.Collections.emptyList());
        org.junit.jupiter.api.Assertions.assertNotNull(map);
        org.junit.jupiter.api.Assertions.assertTrue(map.isEmpty());
        org.mockito.Mockito.verifyNoInteractions(em);
    }

    @org.junit.jupiter.api.Test
    void fetchItemsForOrderIds_MapsRows_AndFillsMissingOrders() throws Exception {
        org.mockito.Mockito.when(em.createNativeQuery(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(nativeQuery);
        org.mockito.Mockito.when(nativeQuery.setParameter(org.mockito.ArgumentMatchers.eq("ids"),
                org.mockito.ArgumentMatchers.any())).thenReturn(nativeQuery);

        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(new Object[]{
                java.lang.Long.valueOf(5),   // order_id
                java.lang.Long.valueOf(301), // book_format_id
                java.lang.Long.valueOf(4),   // quantity
                new java.math.BigDecimal("2.00"), // price
                "Title X",                   // title
                "Kindle",                    // format
                java.lang.Long.valueOf(7001) // book_id
        });
        org.mockito.Mockito.when(nativeQuery.getResultList()).thenReturn(rows);

        var map = invokeFetch(java.util.List.of(5, 6));
        org.junit.jupiter.api.Assertions.assertEquals(2, map.size());
        org.junit.jupiter.api.Assertions.assertTrue(map.containsKey(5));
        org.junit.jupiter.api.Assertions.assertTrue(map.containsKey(6));

        var list5 = map.get(5);
        org.junit.jupiter.api.Assertions.assertEquals(1, list5.size());
        var it = list5.get(0);
        org.junit.jupiter.api.Assertions.assertEquals(301, it.getBookFormatId());
        org.junit.jupiter.api.Assertions.assertEquals(4, it.getQuantity());
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("2.00"), it.getPrice());
        org.junit.jupiter.api.Assertions.assertEquals("Title X", it.getBookTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Kindle", it.getFormat());
        org.junit.jupiter.api.Assertions.assertEquals(7001, it.getBookId());

        org.junit.jupiter.api.Assertions.assertTrue(map.get(6).isEmpty());
    }

    @org.junit.jupiter.api.Test
    void listOrderItems_ReturnsEmptyList_WhenNoItems() {
        Order o = newOrder(10, 7, new java.math.BigDecimal("0.00"));
        org.mockito.Mockito.when(orderRepository.findByOrderId(10))
                .thenReturn(java.util.Optional.of(o));

        org.mockito.Mockito.when(em.createNativeQuery(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(nativeQuery);
        org.mockito.Mockito.when(nativeQuery.setParameter(org.mockito.ArgumentMatchers.eq("ids"),
                org.mockito.ArgumentMatchers.any())).thenReturn(nativeQuery);
        org.mockito.Mockito.when(nativeQuery.getResultList())
                .thenReturn(java.util.Collections.emptyList());

        java.util.List<OrderHistoryItem> items = service.listOrderItems(10, 7);
        org.junit.jupiter.api.Assertions.assertNotNull(items);
        org.junit.jupiter.api.Assertions.assertTrue(items.isEmpty(), "An empty list should be returned if no details are available.");
    }
}
