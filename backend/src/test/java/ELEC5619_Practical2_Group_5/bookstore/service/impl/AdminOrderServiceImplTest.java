package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderItemDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderUpdateRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.OrderItem;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderItemRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private BookFormatRepository bookFormatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    private Order order;
    private OrderItem item;
    private BookFormat format;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(10);
        order.setUserId(3);
        order.setStatus("pending");
        order.setTotalAmount(BigDecimal.valueOf(50));
        order.setOrderDate(LocalDateTime.now());

        item = new OrderItem();
        item.setOrderItemId(5);
        item.setOrderId(10);
        item.setBookFormatId(7);
        item.setQuantity(2);
        item.setPrice(BigDecimal.TEN);

        Book book = Book.builder().title("Sample Book").build();
        format = BookFormat.builder()
                .bookFormatId(7)
                .book(book)
                .format(BookFormatType.paperback)
                .build();

        lenient().when(orderItemRepository.findByOrderId(10)).thenReturn(List.of(item));
        lenient().when(bookFormatRepository.findById(7)).thenReturn(Optional.of(format));
        lenient().when(userRepository.findById(3)).thenReturn(Optional.of(User.builder().username("alice").build()));
    }

    @Test
    void listOrders_withoutStatus_returnsAll() {
        when(orderRepository.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(order)));

        Page<AdminOrderDto> result = adminOrderService.listOrders(0, 5, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("alice");
        verify(orderRepository).findAll(PageRequest.of(0, 5));
    }

    @Test
    void listOrders_withStatus_filters() {
        when(orderRepository.findByStatusIgnoreCase(eq("pending"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        Page<AdminOrderDto> result = adminOrderService.listOrders(1, 10, "pending");

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByStatusIgnoreCase("pending", PageRequest.of(1, 10));
    }

    @Test
    void getOrder_found_returnsDto() {
        when(orderRepository.findByOrderId(10)).thenReturn(Optional.of(order));

        AdminOrderDto dto = adminOrderService.getOrder(10);

        assertThat(dto.getOrderId()).isEqualTo(10);
        assertThat(dto.getItems()).hasSize(1);
    }

    @Test
    void getOrder_notFound_throws() {
        when(orderRepository.findByOrderId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminOrderService.getOrder(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void updateOrder_statusChanges_triggersNotification() {
        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setStatus("shipped");
        when(orderRepository.findByOrderId(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        AdminOrderDto dto = adminOrderService.updateOrder(10, request);

        assertThat(dto.getStatus()).isEqualTo("shipped");
        verify(notificationService).createOrderNotification(3, 10, "shipped");
    }

    @Test
    void updateOrder_statusUnchanged_doesNotNotify() {
        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setStatus("pending");
        when(orderRepository.findByOrderId(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        adminOrderService.updateOrder(10, request);

        verify(notificationService, never()).createOrderNotification(any(), any(), any());
    }

    @Test
    void updateOrder_updatesShippingFields() {
        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setShippingName("Alice");
        request.setShippingAddress("123 Road");
        request.setShippingCity("Sydney");
        request.setShippingPostcode("2000");
        request.setShippingCountry("AU");

        when(orderRepository.findByOrderId(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        AdminOrderDto dto = adminOrderService.updateOrder(10, request);

        assertThat(dto.getShippingName()).isEqualTo("Alice");
        assertThat(dto.getShippingAddress()).isEqualTo("123 Road");
        assertThat(dto.getShippingCity()).isEqualTo("Sydney");
        assertThat(dto.getShippingPostcode()).isEqualTo("2000");
        assertThat(dto.getShippingCountry()).isEqualTo("AU");
        verify(notificationService, never()).createOrderNotification(any(), any(), any());
    }

    @Test
    void getOrder_whenUserMissingAndFormatMissing_usesFallbackValues() {
        Order orphanOrder = new Order();
        orphanOrder.setOrderId(20);
        orphanOrder.setUserId(9);
        orphanOrder.setStatus("processing");
        orphanOrder.setTotalAmount(BigDecimal.TEN);

        OrderItem orphanItem = new OrderItem();
        orphanItem.setOrderItemId(8);
        orphanItem.setOrderId(20);
        orphanItem.setBookFormatId(999);
        orphanItem.setQuantity(1);
        orphanItem.setPrice(BigDecimal.ONE);

        when(orderRepository.findByOrderId(20)).thenReturn(Optional.of(orphanOrder));
        when(orderItemRepository.findByOrderId(20)).thenReturn(List.of(orphanItem));
        when(bookFormatRepository.findById(999)).thenReturn(Optional.empty());
        when(userRepository.findById(9)).thenReturn(Optional.empty());

        AdminOrderDto dto = adminOrderService.getOrder(20);

        assertThat(dto.getUsername()).isEqualTo("Unknown");
        assertThat(dto.getItems()).hasSize(1);
        AdminOrderItemDto itemDto = dto.getItems().get(0);
        assertThat(itemDto.getBookTitle()).isEqualTo("Unknown");
        assertThat(itemDto.getFormat()).isEqualTo("UNKNOWN");
    }

    @Test
    void getOrder_whenFormatWithoutBookOrType_usesFallbacks() {
        Order orderWithNullFormat = new Order();
        orderWithNullFormat.setOrderId(30);
        orderWithNullFormat.setUserId(3);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(9);
        orderItem.setOrderId(30);
        orderItem.setBookFormatId(70);
        orderItem.setQuantity(1);

        BookFormat noBookFormat = BookFormat.builder()
                .book(null)
                .format(null)
                .build();

        when(orderRepository.findByOrderId(30)).thenReturn(Optional.of(orderWithNullFormat));
        when(orderItemRepository.findByOrderId(30)).thenReturn(List.of(orderItem));
        when(bookFormatRepository.findById(70)).thenReturn(Optional.of(noBookFormat));

        AdminOrderDto dto = adminOrderService.getOrder(30);

        AdminOrderItemDto itemDto = dto.getItems().get(0);
        assertThat(itemDto.getBookTitle()).isEqualTo("Unknown");
        assertThat(itemDto.getFormat()).isEqualTo("UNKNOWN");
    }
}
