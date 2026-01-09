package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.cart.CartItemDTO;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.entity.CartItem;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookFormatRepository bookFormatRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItem testCartItem;
    private BookFormat testBookFormat;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setTitle("Test Book");
        testBook.setCoverImageUrl("test-cover.jpg");

        testBookFormat = new BookFormat();
        testBookFormat.setBookFormatId(1);
        testBookFormat.setBook(testBook);
        testBookFormat.setFormat(BookFormatType.paperback);
        testBookFormat.setPrice(BigDecimal.valueOf(29.99));

        testCartItem = new CartItem();
        testCartItem.setCartItemId(1);
        testCartItem.setUserId(1);
        testCartItem.setBookId(1);
        testCartItem.setFormat(BookFormatType.paperback);
        testCartItem.setQuantity(2);
        testCartItem.setAddedAt(LocalDateTime.now());
    }

    @Test
    void getCartItemsByUserId_Success() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));
        when(bookFormatRepository.findByBookBookIdAndFormat(1, BookFormatType.paperback))
                .thenReturn(Optional.of(testBookFormat));

        List<CartItemDTO> result = cartService.getCartItemsByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        CartItemDTO dto = result.get(0);
        assertEquals(1, dto.getBookId());
        assertEquals(2, dto.getQuantity());
        assertEquals("paperback", dto.getFormat());
        assertEquals(1, dto.getBookFormatId());
        assertEquals("Test Book", dto.getTitle());
        assertEquals("test-cover.jpg", dto.getCoverImageUrl());
        assertEquals(29.99, dto.getPrice());
        verify(cartItemRepository).findByUserId(1);
        verify(bookFormatRepository).findByBookBookIdAndFormat(1, BookFormatType.paperback);
    }

    @Test
    void getCartItemsByUserId_EmptyCart() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());

        List<CartItemDTO> result = cartService.getCartItemsByUserId(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findByUserId(1);
    }

    @Test
    void getCartItemsByUserId_BookFormatNotFound() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));
        when(bookFormatRepository.findByBookBookIdAndFormat(1, BookFormatType.paperback))
                .thenReturn(Optional.empty());

        List<CartItemDTO> result = cartService.getCartItemsByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        CartItemDTO dto = result.get(0);
        assertEquals(1, dto.getBookId());
        assertEquals(2, dto.getQuantity());
        assertEquals("paperback", dto.getFormat());
        assertNull(dto.getBookFormatId());
        assertNull(dto.getTitle());
        assertNull(dto.getCoverImageUrl());
        assertNull(dto.getPrice());
        verify(cartItemRepository).findByUserId(1);
        verify(bookFormatRepository).findByBookBookIdAndFormat(1, BookFormatType.paperback);
    }

    @Test
    void addToCart_NewItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.addToCart(1, 1, "paperback", 2);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.addToCart(1, 1, "paperback", 3);

        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    void addToCart_InvalidFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1, 1, "invalid", 2));

        assertEquals("Invalid format: invalid", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_NullFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1, 1, null, 2));

        assertEquals("Format cannot be null", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_EbookFormat() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.addToCart(1, 1, "ebook", 1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_ExistingItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.updateCartItem(1, 1, "paperback", 5);

        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    void updateCartItem_NewItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.updateCartItem(1, 1, "paperback", 3);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_ZeroQuantity() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));

        cartService.updateCartItem(1, 1, "paperback", 0);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void updateCartItem_NegativeQuantity() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));

        cartService.updateCartItem(1, 1, "paperback", -1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void updateCartItem_InvalidFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.updateCartItem(1, 1, "invalid", 2));

        assertEquals("Invalid format: invalid", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeFromCart_ExistingItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));

        cartService.removeFromCart(1, 1, "paperback");

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void removeFromCart_NonExistingItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());

        cartService.removeFromCart(1, 1, "paperback");

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void removeFromCart_InvalidFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.removeFromCart(1, 1, "invalid"));

        assertEquals("Invalid format: invalid", exception.getMessage());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void clearCart_Success() {
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartItemRepository.findByUserId(1)).thenReturn(cartItems);

        cartService.clearCart(1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).deleteAll(cartItems);
    }

    @Test
    void clearCart_EmptyCart() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());

        cartService.clearCart(1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).deleteAll(Collections.emptyList());
    }

    @Test
    void parseFormat_ValidFormats() {
        // Test case-insensitive format parsing
        cartService.addToCart(1, 1, "PAPERBACK", 1);
        cartService.addToCart(1, 2, "Ebook", 1);
        cartService.addToCart(1, 3, "  paperback  ", 1);

        // Verify that the methods were called (format parsing succeeded)
        verify(cartItemRepository, times(3)).findByUserId(1);
    }

    @Test
    void addToCart_MultipleItemsDifferentFormats() {
        CartItem ebookItem = new CartItem();
        ebookItem.setCartItemId(2);
        ebookItem.setUserId(1);
        ebookItem.setBookId(1);
        ebookItem.setFormat(BookFormatType.ebook);
        ebookItem.setQuantity(1);

        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(ebookItem);

        // Add ebook format of the same book
        cartService.addToCart(1, 1, "ebook", 1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_DifferentBookSameFormat() {
        CartItem anotherBookItem = new CartItem();
        anotherBookItem.setCartItemId(2);
        anotherBookItem.setUserId(1);
        anotherBookItem.setBookId(2);
        anotherBookItem.setFormat(BookFormatType.paperback);
        anotherBookItem.setQuantity(1);

        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem, anotherBookItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(anotherBookItem);

        cartService.updateCartItem(1, 2, "paperback", 3);

        assertEquals(3, anotherBookItem.getQuantity());
        assertEquals(2, testCartItem.getQuantity()); // Should remain unchanged
        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(anotherBookItem);
    }

    // Add test cases to improve branch coverage
    @Test
    void updateCartItem_ZeroQuantity_ShouldRemoveItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));

        cartService.updateCartItem(1, 1, "paperback", 0);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void updateCartItem_NegativeQuantity_ShouldRemoveItem() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(testCartItem));

        cartService.updateCartItem(1, 1, "paperback", -1);

        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void parseFormat_CaseInsensitive() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Test various case combinations
        cartService.addToCart(1, 1, "PAPERBACK", 1);
        cartService.addToCart(1, 2, "paperback", 1);
        cartService.addToCart(1, 3, "PaperBack", 1);
        cartService.addToCart(1, 4, "EBOOK", 1);
        cartService.addToCart(1, 5, "ebook", 1);

        verify(cartItemRepository, times(5)).findByUserId(1);
        verify(cartItemRepository, times(5)).save(any(CartItem.class));
    }

    @Test
    void parseFormat_WithWhitespace() {
        when(cartItemRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Test formats with whitespace
        cartService.addToCart(1, 1, "  paperback  ", 1);
        cartService.addToCart(1, 2, "\tebook\t", 1);
        cartService.addToCart(1, 3, " PAPERBACK ", 1);

        verify(cartItemRepository, times(3)).findByUserId(1);
        verify(cartItemRepository, times(3)).save(any(CartItem.class));
    }

    @Test
    void addToCart_InvalidFormat_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1, 1, "invalid_format", 2));

        assertEquals("Invalid format: invalid_format", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateCartItem_InvalidFormat_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.updateCartItem(1, 1, "invalid_format", 2));

        assertEquals("Invalid format: invalid_format", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeFromCart_NullFormat_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.removeFromCart(1, 1, null));

        assertEquals("Format cannot be null", exception.getMessage());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void updateCartItem_NullFormat_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.updateCartItem(1, 1, null, 2));

        assertEquals("Format cannot be null", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_EmptyFormat_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1, 1, "", 2));

        assertEquals("Invalid format: ", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_MultipleExistingItems_ShouldUpdateCorrectOne() {
        CartItem paperbackItem = new CartItem();
        paperbackItem.setCartItemId(1);
        paperbackItem.setUserId(1);
        paperbackItem.setBookId(1);
        paperbackItem.setFormat(BookFormatType.paperback);
        paperbackItem.setQuantity(2);

        CartItem ebookItem = new CartItem();
        ebookItem.setCartItemId(2);
        ebookItem.setUserId(1);
        ebookItem.setBookId(1);
        ebookItem.setFormat(BookFormatType.ebook);
        ebookItem.setQuantity(1);

        when(cartItemRepository.findByUserId(1)).thenReturn(Arrays.asList(paperbackItem, ebookItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(paperbackItem);

        cartService.addToCart(1, 1, "paperback", 3);

        assertEquals(5, paperbackItem.getQuantity()); // 2 + 3
        assertEquals(1, ebookItem.getQuantity()); // Should remain unchanged
        verify(cartItemRepository).findByUserId(1);
        verify(cartItemRepository).save(paperbackItem);
    }
}