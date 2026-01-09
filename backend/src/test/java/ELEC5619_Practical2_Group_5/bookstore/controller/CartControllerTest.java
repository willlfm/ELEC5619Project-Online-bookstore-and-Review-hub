package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.dto.cart.CartItemDTO;
import ELEC5619_Practical2_Group_5.bookstore.service.CartService;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private CartItemDTO cartItemDTO;

    @BeforeEach
    void setUp() {
        cartItemDTO = new CartItemDTO();
        cartItemDTO.setBookId(1);
        cartItemDTO.setTitle("Test Book");
        cartItemDTO.setFormat("paperback");
        cartItemDTO.setQuantity(2);
        cartItemDTO.setPrice(29.99);
        cartItemDTO.setCoverImageUrl("test-cover.jpg");
    }

    @Test
    void getCartItems_Success() throws Exception {
        List<CartItemDTO> cartItems = Arrays.asList(cartItemDTO);
        when(cartService.getCartItemsByUserId(1)).thenReturn(cartItems);

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].format").value("paperback"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].price").value(29.99));
    }

    @Test
    void getCartItems_EmptyCart() throws Exception {
        when(cartService.getCartItemsByUserId(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCartItems_ServiceException() throws Exception {
        when(cartService.getCartItemsByUserId(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to get cart items: Database error"));
    }

    @Test
    void addToCart_Success() throws Exception {
        doNothing().when(cartService).addToCart(1, 1, "paperback", 2);

        mockMvc.perform(post("/api/cart/1/add")
                        .param("bookId", "1")
                        .param("format", "paperback")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item added to cart successfully."));

        verify(cartService).addToCart(1, 1, "paperback", 2);
    }

    @Test
    void addToCart_DefaultQuantity() throws Exception {
        doNothing().when(cartService).addToCart(1, 1, "paperback", 1);

        mockMvc.perform(post("/api/cart/1/add")
                        .param("bookId", "1")
                        .param("format", "paperback"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item added to cart successfully."));

        verify(cartService).addToCart(1, 1, "paperback", 1);
    }

    @Test
    void addToCart_InvalidArgument() throws Exception {
        doThrow(new IllegalArgumentException("Invalid book format"))
                .when(cartService).addToCart(1, 1, "invalid", 1);

        mockMvc.perform(post("/api/cart/1/add")
                        .param("bookId", "1")
                        .param("format", "invalid")
                        .param("quantity", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request: Invalid book format"));
    }

    @Test
    void addToCart_ServiceException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(cartService).addToCart(1, 1, "paperback", 1);

        mockMvc.perform(post("/api/cart/1/add")
                        .param("bookId", "1")
                        .param("format", "paperback")
                        .param("quantity", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to add item to cart: Database error"));
    }

    @Test
    void updateCartItem_Success() throws Exception {
        doNothing().when(cartService).updateCartItem(1, 1, "paperback", 3);

        mockMvc.perform(post("/api/cart/1/update")
                        .param("bookId", "1")
                        .param("format", "paperback")
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart item updated successfully."));

        verify(cartService).updateCartItem(1, 1, "paperback", 3);
    }

    @Test
    void updateCartItem_InvalidArgument() throws Exception {
        doThrow(new IllegalArgumentException("Item not found in cart"))
                .when(cartService).updateCartItem(1, 1, "paperback", 3);

        mockMvc.perform(post("/api/cart/1/update")
                        .param("bookId", "1")
                        .param("format", "paperback")
                        .param("quantity", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request: Item not found in cart"));
    }

    @Test
    void updateCartItem_ServiceException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(cartService).updateCartItem(1, 1, "paperback", 3);

        mockMvc.perform(post("/api/cart/1/update")
                        .param("bookId", "1")
                        .param("format", "paperback")
                        .param("quantity", "3"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to update cart item: Database error"));
    }

    @Test
    void removeFromCart_Success() throws Exception {
        doNothing().when(cartService).removeFromCart(1, 1, "paperback");

        mockMvc.perform(delete("/api/cart/1/remove")
                        .param("bookId", "1")
                        .param("format", "paperback"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item removed from cart successfully."));

        verify(cartService).removeFromCart(1, 1, "paperback");
    }

    @Test
    void removeFromCart_InvalidArgument() throws Exception {
        doThrow(new IllegalArgumentException("Item not found in cart"))
                .when(cartService).removeFromCart(1, 1, "paperback");

        mockMvc.perform(delete("/api/cart/1/remove")
                        .param("bookId", "1")
                        .param("format", "paperback"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request: Item not found in cart"));
    }

    @Test
    void removeFromCart_ServiceException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(cartService).removeFromCart(1, 1, "paperback");

        mockMvc.perform(delete("/api/cart/1/remove")
                        .param("bookId", "1")
                        .param("format", "paperback"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to remove item from cart: Database error"));
    }

    @Test
    void clearCart_Success() throws Exception {
        doNothing().when(cartService).clearCart(1);

        mockMvc.perform(delete("/api/cart/1/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart cleared successfully."));

        verify(cartService).clearCart(1);
    }

    @Test
    void clearCart_ServiceException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(cartService).clearCart(1);

        mockMvc.perform(delete("/api/cart/1/clear"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to clear cart: Database error"));
    }
}