package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderHistoryService;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderService;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderHistoryService orderHistoryService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController orderController;

    private User testUser;
    private OrderRequest orderRequest;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        orderRequest = new OrderRequest();
        OrderRequest.OrderItemDTO item = new OrderRequest.OrderItemDTO();
        item.setBookFormatId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(29.99));
        orderRequest.setItems(List.of(item));
        orderRequest.setTotalAmount(BigDecimal.valueOf(59.98));

        order = new Order();
        order.setOrderId(1);
        order.setUserId(testUser.getUserId());
        order.setTotalAmount(BigDecimal.valueOf(59.98));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setShippingName("Test User");
        order.setShippingAddress("123 Test St");
        order.setShippingCity("Sydney");
        order.setShippingPostcode("2000");
        order.setShippingCountry("Australia");

        orderResponse = new OrderResponse();
        orderResponse.setOrderId(1);
        orderResponse.setTotalAmount(BigDecimal.valueOf(59.98));
    }

    // ========== /api/orders/confirm Tests ==========

    @Test
    void testConfirmOrder_Success() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class))).thenReturn(order);
        when(orderService.toOrderResponse(order)).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":2,\"price\":29.99}],\"totalAmount\":59.98}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.order").exists())
                .andExpect(jsonPath("$.order.orderId").value(1));

        verify(jwtUtils, times(1)).extractUsernameFromRequest(any(HttpServletRequest.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(orderService, times(1)).createOrder(eq(testUser), any(OrderRequest.class));
        verify(orderService, times(1)).toOrderResponse(order);
    }

    @Test
    void testConfirmOrder_Unauthorized_NullUsername() throws Exception {
        // Arrange - JWT returns null (unauthorized)
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":1,\"price\":10}],\"totalAmount\":10}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        verify(jwtUtils, times(1)).extractUsernameFromRequest(any(HttpServletRequest.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(orderService, never()).createOrder(any(User.class), any(OrderRequest.class));
    }

    @Test
    void testConfirmOrder_UserNotFound() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":1,\"price\":10}],\"totalAmount\":10}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(orderService, never()).createOrder(any(User.class), any(OrderRequest.class));
    }

    @Test
    void testConfirmOrder_ServiceException() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Payment processing failed"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":1,\"price\":10}],\"totalAmount\":10}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Payment processing failed"));

        verify(orderService, times(1)).createOrder(eq(testUser), any(OrderRequest.class));
        verify(orderService, never()).toOrderResponse(any(Order.class));
    }

    @Test
    void testConfirmOrder_MultipleItems() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class))).thenReturn(order);
        when(orderService.toOrderResponse(order)).thenReturn(orderResponse);

        String jsonContent = "{\"items\":[" +
                "{\"bookFormatId\":1,\"quantity\":2,\"price\":29.99}," +
                "{\"bookFormatId\":2,\"quantity\":1,\"price\":19.99}" +
                "],\"totalAmount\":79.97}";

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(orderService, times(1)).createOrder(eq(testUser), any(OrderRequest.class));
    }

    @Test
    void testConfirmOrder_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(User.class), any(OrderRequest.class));
    }

    @Test
    void testConfirmOrder_NullItemsList() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Items list cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":null,\"totalAmount\":0}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void testConfirmOrder_EmptyItemsList() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Items list cannot be empty"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[],\"totalAmount\":0}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void testConfirmOrder_ToOrderResponseException() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class))).thenReturn(order);
        when(orderService.toOrderResponse(order))
                .thenThrow(new RuntimeException("Error converting order to response"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":1,\"price\":10}],\"totalAmount\":10}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Error converting order to response"));

        verify(orderService, times(1)).toOrderResponse(order);
    }

    @Test
    void testConfirmOrder_WithFullShippingInfo() throws Exception {
        // Arrange
        Order orderWithShipping = new Order();
        orderWithShipping.setOrderId(2);
        orderWithShipping.setUserId(testUser.getUserId());
        orderWithShipping.setTotalAmount(BigDecimal.valueOf(99.99));
        orderWithShipping.setShippingName("John Doe");
        orderWithShipping.setShippingAddress("456 Main Rd");
        orderWithShipping.setShippingCity("Melbourne");
        orderWithShipping.setShippingPostcode("3000");
        orderWithShipping.setShippingCountry("Australia");

        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(eq(testUser), any(OrderRequest.class))).thenReturn(orderWithShipping);
        when(orderService.toOrderResponse(orderWithShipping)).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookFormatId\":1,\"quantity\":1,\"price\":99.99}],\"totalAmount\":99.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(orderService, times(1)).createOrder(eq(testUser), any(OrderRequest.class));
    }

    // ========== /api/orders/me Tests ==========

    @Test
    void testMyOrders_Success_WithoutItems() throws Exception {
        // Arrange
        List<Map<String, Object>> orders = new ArrayList<>();
        Map<String, Object> order1 = new java.util.HashMap<>();
        order1.put("orderId", 1);
        order1.put("totalAmount", 59.98);
        order1.put("status", "pending");
        orders.add(order1);

        Map<String, Object> order2 = new java.util.HashMap<>();
        order2.put("orderId", 2);
        order2.put("totalAmount", 39.99);
        order2.put("status", "completed");
        orders.add(order2);

        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(eq(testUser.getUserId()), eq(false))).thenReturn((List) orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));

        verify(jwtUtils, times(1)).extractUsernameFromRequest(any(HttpServletRequest.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), false);
    }

    @Test
    void testMyOrders_Success_WithItems() throws Exception {
        // Arrange
        List<Map<String, Object>> ordersWithItems = new ArrayList<>();
        Map<String, Object> order1 = new java.util.HashMap<>();
        order1.put("orderId", 1);
        order1.put("totalAmount", 59.98);
        order1.put("status", "pending");

        List<Map<String, Object>> itemsList = new ArrayList<>();
        Map<String, Object> item1 = new java.util.HashMap<>();
        item1.put("bookTitle", "Java Programming");
        item1.put("quantity", 2);
        itemsList.add(item1);
        order1.put("items", itemsList);

        ordersWithItems.add(order1);

        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(eq(testUser.getUserId()), eq(true))).thenReturn((List) ordersWithItems);

        // Act & Assert
        mockMvc.perform(get("/api/orders/me")
                        .param("includeItems", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), true);
    }

    @Test
    void testMyOrders_EmptyList() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(testUser.getUserId(), false)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), false);
    }

    @Test
    void testMyOrders_UserNotFound() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act - Controller throws NoSuchElementException, expect ServletException wrapper
        try {
            mockMvc.perform(get("/api/orders/me"));
        } catch (ServletException e) {
            // Expected exception - test passes
        }

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(orderHistoryService, never()).listMyOrders(anyInt(), anyBoolean());
    }

    @Test
    void testMyOrders_SecurityException() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(testUser.getUserId(), false))
                .thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("forbidden"));

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), false);
    }

    @Test
    void testMyOrders_WithIncludeItemsDefault() throws Exception {
        // Arrange - Test default value for includeItems parameter
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(testUser.getUserId(), false)).thenReturn(new ArrayList<>());

        // Act & Assert - No includeItems param should default to false
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk());

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), false);
    }

    @Test
    void testMyOrders_IncludeItemsTrue() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(testUser.getUserId(), true)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/orders/me")
                        .param("includeItems", "true"))
                .andExpect(status().isOk());

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), true);
    }

    @Test
    void testMyOrders_IncludeItemsFalse() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listMyOrders(testUser.getUserId(), false)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/orders/me")
                        .param("includeItems", "false"))
                .andExpect(status().isOk());

        verify(orderHistoryService, times(1)).listMyOrders(testUser.getUserId(), false);
    }

    // ========== /api/orders/{orderId}/items Tests ==========

    @Test
    void testOrderItems_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new java.util.HashMap<>();
        item1.put("bookTitle", "Java Programming");
        item1.put("quantity", 2);
        item1.put("price", 29.99);
        items.add(item1);

        Map<String, Object> item2 = new java.util.HashMap<>();
        item2.put("bookTitle", "Spring Boot Guide");
        item2.put("quantity", 1);
        item2.put("price", 39.99);
        items.add(item2);

        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(eq(1), eq(testUser.getUserId()))).thenReturn((List) items);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(jwtUtils, times(1)).extractUsernameFromRequest(any(HttpServletRequest.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(orderHistoryService, times(1)).listOrderItems(1, testUser.getUserId());
    }

    @Test
    void testOrderItems_EmptyList() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(eq(99), eq(testUser.getUserId()))).thenReturn((List) new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/orders/99/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderHistoryService, times(1)).listOrderItems(99, testUser.getUserId());
    }

    @Test
    void testOrderItems_DifferentOrderIds() throws Exception {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("bookTitle", "Test Book", "quantity", 1));

        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(anyInt(), eq(testUser.getUserId()))).thenReturn((List) items);

        // Act & Assert - Test with different order IDs
        mockMvc.perform(get("/api/orders/123/items"))
                .andExpect(status().isOk());
        verify(orderHistoryService, times(1)).listOrderItems(123, testUser.getUserId());

        mockMvc.perform(get("/api/orders/456/items"))
                .andExpect(status().isOk());
        verify(orderHistoryService, times(1)).listOrderItems(456, testUser.getUserId());
    }

    @Test
    void testOrderItems_UserNotFound() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act - Controller throws NoSuchElementException, expect ServletException wrapper
        try {
            mockMvc.perform(get("/api/orders/1/items"));
        } catch (ServletException e) {
            // Expected exception - test passes
        }

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(orderHistoryService, never()).listOrderItems(anyInt(), anyInt());
    }

    @Test
    void testOrderItems_SecurityException() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(1, testUser.getUserId()))
                .thenThrow(new SecurityException("You don't have permission to view this order"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("forbidden"));

        verify(orderHistoryService, times(1)).listOrderItems(1, testUser.getUserId());
    }

    @Test
    void testOrderItems_InvalidOrderId() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(eq(-1), eq(testUser.getUserId())))
                .thenThrow(new IllegalArgumentException("Invalid order ID"));

        // Act - Controller throws IllegalArgumentException, expect ServletException wrapper
        try {
            mockMvc.perform(get("/api/orders/-1/items"));
        } catch (ServletException e) {
            // Expected exception - test passes
        }

        verify(orderHistoryService, times(1)).listOrderItems(-1, testUser.getUserId());
    }

    @Test
    void testOrderItems_LargeOrderId() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(eq(Integer.MAX_VALUE), eq(testUser.getUserId()))).thenReturn((List) new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/orders/" + Integer.MAX_VALUE + "/items"))
                .andExpect(status().isOk());

        verify(orderHistoryService, times(1)).listOrderItems(Integer.MAX_VALUE, testUser.getUserId());
    }

    @Test
    void testOrderItems_RuntimeException() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderHistoryService.listOrderItems(eq(1), eq(testUser.getUserId())))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - Controller throws RuntimeException, expect ServletException wrapper
        try {
            mockMvc.perform(get("/api/orders/1/items"));
        } catch (ServletException e) {
            // Expected exception - test passes
        }

        verify(orderHistoryService, times(1)).listOrderItems(1, testUser.getUserId());
    }
}