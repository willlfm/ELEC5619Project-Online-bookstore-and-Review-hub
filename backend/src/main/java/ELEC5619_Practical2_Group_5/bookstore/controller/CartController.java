package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.cart.CartItemDTO;
import ELEC5619_Practical2_Group_5.bookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartItems(@PathVariable Integer userId) {
        try {
            List<CartItemDTO> cartItems = cartService.getCartItemsByUserId(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get cart items: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(
            @PathVariable Integer userId,
            @RequestParam Integer bookId,
            @RequestParam String format,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            cartService.addToCart(userId, bookId, format, quantity);
            return ResponseEntity.ok("Item added to cart successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add item to cart: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer userId,
            @RequestParam Integer bookId,
            @RequestParam String format,
            @RequestParam Integer quantity) {
        try {
            cartService.updateCartItem(userId, bookId, format, quantity);
            return ResponseEntity.ok("Cart item updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update cart item: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Integer userId,
            @RequestParam Integer bookId,
            @RequestParam String format) {
        try {
            cartService.removeFromCart(userId, bookId, format);
            return ResponseEntity.ok("Item removed from cart successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove item from cart: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok("Cart cleared successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear cart: " + e.getMessage());
        }
    }
}
