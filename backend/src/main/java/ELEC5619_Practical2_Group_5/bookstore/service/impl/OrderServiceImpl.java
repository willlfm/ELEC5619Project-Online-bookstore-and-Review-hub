package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.*;
import ELEC5619_Practical2_Group_5.bookstore.repository.*;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookFormatRepository bookFormatRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            BookFormatRepository bookFormatRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookFormatRepository = bookFormatRepository;
    }

    @Override
    @Transactional
    public Order createOrder(User user, OrderRequest orderRequest) {
        if (orderRequest == null || orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // create order
        Order order = new Order();
        order.setUserId(user.getUserId());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setStatus("paid");
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (orderRequest.getShippingInfo() != null) {
            order.setShippingName(orderRequest.getShippingInfo().getName());
            order.setShippingAddress(orderRequest.getShippingInfo().getAddress());
            order.setShippingCity(orderRequest.getShippingInfo().getCity());
            order.setShippingPostcode(orderRequest.getShippingInfo().getPostcode());
            order.setShippingCountry(orderRequest.getShippingInfo().getCountry());
        }

        order = orderRepository.save(order);

        // create order item
        List<OrderItem> items = new ArrayList<>();
        for (OrderRequest.OrderItemDTO itemDTO : orderRequest.getItems()) {
            if (itemDTO.getBookFormatId() == null) {
                throw new IllegalArgumentException("bookFormatId cannot be null");
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setBookFormatId(itemDTO.getBookFormatId().intValue());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            items.add(item);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(items);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse toOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

        List<OrderResponse.OrderItemResponse> items = new ArrayList<>();
        for (OrderItem item : orderItems) {
            OrderResponse.OrderItemResponse r = new OrderResponse.OrderItemResponse();
            r.setBookFormatId(item.getBookFormatId());

            // search BookFormat to get title and format
            BookFormat bf = bookFormatRepository.findById(item.getBookFormatId())
                    .orElseThrow(() -> new RuntimeException("BookFormat not found: " + item.getBookFormatId()));
            r.setBookTitle(bf.getBook().getTitle());
            r.setFormat(bf.getFormat().name());

            r.setQuantity(item.getQuantity());
            r.setPrice(item.getPrice());
            items.add(r);
        }

        return new OrderResponse(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                items
        );
    }
}
