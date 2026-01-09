package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.OrderResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Order;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.OrderRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderService;
import ELEC5619_Practical2_Group_5.bookstore.service.OrderHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final OrderHistoryService orderHistoryService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, JwtUtils jwtUtils, UserRepository userRepository, OrderRepository orderRepository, OrderHistoryService orderHistoryService) {
        this.orderService = orderService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderHistoryService = orderHistoryService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        try {
            String username = jwtUtils.extractUsernameFromRequest(request);
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "status", "error",
                        "message", "Unauthorized"
                ));
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderService.createOrder(user, orderRequest);

            OrderResponse response = orderService.toOrderResponse(order);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "order", response
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> myOrders(@RequestParam(name="includeItems", defaultValue="false") boolean includeItems,
                                      HttpServletRequest request) {
        try {
            String username = jwtUtils.extractUsernameFromRequest(request);
            var user = userRepository.findByUsername(username).orElseThrow();
            var list = orderHistoryService.listMyOrders(user.getUserId(), includeItems);
            return ResponseEntity.ok(Map.of("content", list));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("status","forbidden"));
        }
    }

    // GET /api/orders/{id}/items
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> orderItems(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            String username = jwtUtils.extractUsernameFromRequest(request);
            var user = userRepository.findByUsername(username).orElseThrow();
            var items = orderHistoryService.listOrderItems(orderId, user.getUserId());
            return ResponseEntity.ok(items);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("status","forbidden"));
        }
    }
}
