package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.cart.CartItemDTO;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.entity.CartItem;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.CartItemRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookFormatRepository bookFormatRepository;

    // --------------------- GET CART ---------------------
    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItemsByUserId(Integer userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(item -> {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setBookId(item.getBookId());
                    dto.setQuantity(item.getQuantity());
                    dto.setFormat(item.getFormat().name()); // enum to String

                    // find BookFormat corresponding to format
                    Optional<BookFormat> formatOptional = bookFormatRepository
                            .findByBookBookIdAndFormat(item.getBookId(), item.getFormat());

                    formatOptional.ifPresent(format -> {
                        dto.setBookFormatId(format.getBookFormatId()); // 新增
                        dto.setTitle(format.getBook().getTitle());
                        dto.setCoverImageUrl(format.getBook().getCoverImageUrl());
                        dto.setPrice(format.getPrice().doubleValue());
                    });

                    return dto;
                }).collect(Collectors.toList());
    }

    // --------------------- ADD TO CART ---------------------
    @Transactional
    @Override
    public void addToCart(Integer userId, Integer bookId, String formatStr, Integer quantity) {
        BookFormatType format = parseFormat(formatStr);

        CartItem existing = cartItemRepository.findByUserId(userId)
                .stream()
                .filter(item -> item.getBookId().equals(bookId) && item.getFormat() == format)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setBookId(bookId);
            newItem.setFormat(format);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(newItem);
        }
    }

    // --------------------- UPDATE CART ---------------------
    @Transactional
    @Override
    public void updateCartItem(Integer userId, Integer bookId, String formatStr, Integer quantity) {
        BookFormatType format = parseFormat(formatStr);

        if (quantity <= 0) {
            removeFromCart(userId, bookId, formatStr);
            return;
        }

        CartItem existing = cartItemRepository.findByUserId(userId)
                .stream()
                .filter(item -> item.getBookId().equals(bookId) && item.getFormat() == format)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(quantity);
            existing.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setBookId(bookId);
            newItem.setFormat(format);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(newItem);
        }
    }

    // --------------------- REMOVE CART ITEM ---------------------
    @Transactional
    @Override
    public void removeFromCart(Integer userId, Integer bookId, String formatStr) {
        BookFormatType format = parseFormat(formatStr);

        cartItemRepository.findByUserId(userId)
                .stream()
                .filter(item -> item.getBookId().equals(bookId) && item.getFormat() == format)
                .findFirst()
                .ifPresent(cartItemRepository::delete);
    }

    // --------------------- CLEAR CART ---------------------
    @Transactional
    @Override
    public void clearCart(Integer userId) {
        cartItemRepository.deleteAll(cartItemRepository.findByUserId(userId));
    }

    // --------------------- HELPER ---------------------
    private BookFormatType parseFormat(String formatStr) {
        if (formatStr == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        try {
            // turn into lower case to match enum
            String lower = formatStr.trim().toLowerCase();
            return BookFormatType.valueOf(lower);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + formatStr);
        }
    }
}
