package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.*;
import ELEC5619_Practical2_Group_5.bookstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private BookFormatRepository bookFormatRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1);

        orderRequest = new OrderRequest();
        OrderRequest.OrderItemDTO itemDTO = new OrderRequest.OrderItemDTO();
        itemDTO.setBookFormatId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setPrice(BigDecimal.valueOf(15));
        orderRequest.setItems(List.of(itemDTO));
        orderRequest.setTotalAmount(BigDecimal.valueOf(30));

        order = new Order();
        order.setOrderId(1);
    }

    @Test
    void testCreateOrder_Success() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        Order created = orderService.createOrder(testUser, orderRequest);

        assertEquals(order.getOrderId(), created.getOrderId());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(orderItemRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void testCreateOrder_WithShippingInfo() {
        orderRequest.setShippingInfo(new OrderRequest.ShippingInfoDTO());
        orderRequest.getShippingInfo().setName("John");
        orderRequest.getShippingInfo().setAddress("123 Street");
        orderRequest.getShippingInfo().setCity("City");
        orderRequest.getShippingInfo().setPostcode("0000");
        orderRequest.getShippingInfo().setCountry("Country");

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        Order created = orderService.createOrder(testUser, orderRequest);

        assertEquals(order.getOrderId(), created.getOrderId());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_NullOrderRequest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(testUser, null));
        assertEquals("Order must contain at least one item", ex.getMessage());
    }

    @Test
    void testCreateOrder_EmptyItems() {
        orderRequest.setItems(List.of());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(testUser, orderRequest));
        assertEquals("Order must contain at least one item", ex.getMessage());
    }

    @Test
    void testCreateOrder_NullBookFormatId() {
        orderRequest.getItems().get(0).setBookFormatId(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(testUser, orderRequest));
        assertEquals("bookFormatId cannot be null", ex.getMessage());
    }

    @Test
    void testToOrderResponse_Success() {
        OrderItem orderItem = new OrderItem();
        orderItem.setBookFormatId(1);
        orderItem.setQuantity(2);
        orderItem.setPrice(BigDecimal.valueOf(15));

        Book book = new Book();
        book.setTitle("Test Book");

        BookFormat bf = new BookFormat();
        bf.setBookFormatId(1);
        bf.setBook(book);
        bf.setFormat(BookFormatType.paperback);

        when(orderItemRepository.findByOrderId(order.getOrderId())).thenReturn(List.of(orderItem));
        when(bookFormatRepository.findById(orderItem.getBookFormatId())).thenReturn(Optional.of(bf));

        OrderResponse response = orderService.toOrderResponse(order);

        assertEquals(1, response.getItems().size());
        assertEquals("Test Book", response.getItems().get(0).getBookTitle());
        verify(orderItemRepository, times(1)).findByOrderId(order.getOrderId());
        verify(bookFormatRepository, times(1)).findById(orderItem.getBookFormatId());
    }

    @Test
    void testToOrderResponse_BookFormatNotFound() {
        OrderItem orderItem = new OrderItem();
        orderItem.setBookFormatId(999);
        orderItem.setQuantity(1);
        orderItem.setPrice(BigDecimal.valueOf(10));

        when(orderItemRepository.findByOrderId(order.getOrderId())).thenReturn(List.of(orderItem));
        when(bookFormatRepository.findById(orderItem.getBookFormatId())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.toOrderResponse(order));
        assertTrue(ex.getMessage().contains("BookFormat not found"));
    }
}
