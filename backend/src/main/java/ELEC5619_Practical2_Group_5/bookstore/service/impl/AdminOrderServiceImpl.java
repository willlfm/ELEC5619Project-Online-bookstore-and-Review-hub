package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderItemDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderUpdateRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.OrderItem;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderItemRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminOrderService;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
// Provides the admin UI with enriched order details (customer + items) in a single call.
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookFormatRepository bookFormatRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminOrderDto> listOrders(int page, int size, String status) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders;
        if (StringUtils.hasText(status)) {
            orders = orderRepository.findByStatusIgnoreCase(status.trim(), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDto getOrder(Integer orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return toDto(order);
    }

    @Override
    public AdminOrderDto updateOrder(Integer orderId, AdminOrderUpdateRequest request) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String oldStatus = order.getStatus();
        boolean statusChanged = false;
        
        if (StringUtils.hasText(request.getStatus())) {
            String newStatus = request.getStatus().trim();
            if (!newStatus.equals(oldStatus)) {
                order.setStatus(newStatus);
                statusChanged = true;
            }
        }
        if (StringUtils.hasText(request.getShippingName())) {
            order.setShippingName(request.getShippingName());
        }
        if (StringUtils.hasText(request.getShippingAddress())) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (StringUtils.hasText(request.getShippingCity())) {
            order.setShippingCity(request.getShippingCity());
        }
        if (StringUtils.hasText(request.getShippingPostcode())) {
            order.setShippingPostcode(request.getShippingPostcode());
        }
        if (StringUtils.hasText(request.getShippingCountry())) {
            order.setShippingCountry(request.getShippingCountry());
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Create notification if status changed
        if (statusChanged) {
            notificationService.createOrderNotification(order.getUserId(), orderId, order.getStatus());
        }
        
        return toDto(savedOrder);
    }

    private AdminOrderDto toDto(Order order) {
        String username = userRepository.findById(order.getUserId())
                .map(User::getUsername)
                .orElse("Unknown");

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
        List<AdminOrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    BookFormat format = bookFormatRepository.findById(item.getBookFormatId())
                            .orElse(null);
                    String bookTitle = format != null && format.getBook() != null
                            ? format.getBook().getTitle()
                            : "Unknown";
                    String formatName = format != null && format.getFormat() != null
                            ? format.getFormat().name()
                            : "UNKNOWN";
                    return AdminOrderItemDto.builder()
                            .orderItemId(item.getOrderItemId())
                            .bookFormatId(item.getBookFormatId())
                            .bookTitle(bookTitle)
                            .format(formatName)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build();
                })
                .toList();

        return AdminOrderDto.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .username(username)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .shippingName(order.getShippingName())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingPostcode(order.getShippingPostcode())
                .shippingCountry(order.getShippingCountry())
                .items(itemDtos)
                .build();
    }
}
