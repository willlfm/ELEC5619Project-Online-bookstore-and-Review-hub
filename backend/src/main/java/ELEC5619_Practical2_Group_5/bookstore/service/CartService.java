package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.cart.CartItemDTO;
import java.util.List;

public interface CartService {
    List<CartItemDTO> getCartItemsByUserId(Integer userId);

    void addToCart(Integer userId, Integer bookId, String format, Integer quantity);

    void updateCartItem(Integer userId, Integer bookId, String format, Integer quantity);

    void removeFromCart(Integer userId, Integer bookId, String format);

    void clearCart(Integer userId);
}
