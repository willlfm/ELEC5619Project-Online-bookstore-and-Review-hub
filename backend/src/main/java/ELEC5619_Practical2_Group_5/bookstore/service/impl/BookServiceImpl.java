package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.Review;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReviewRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.BookRecommendationService;
import ELEC5619_Practical2_Group_5.bookstore.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookFormatRepository bookFormatRepository;
    private final BookRecommendationService bookRecommendationService;

    private final ReviewRepository reviewRepository;
    private final JwtUtils jwtUtils;

    @Override
    public Book addBook(Book book) {
        // save Book
        Book savedBook = bookRepository.save(book);

        // save BookFormat
        if (book.getFormats() != null) {
            for (BookFormat format : book.getFormats()) {
                format.setBook(savedBook);
                bookFormatRepository.save(format);
            }
        }

        return savedBook;
    }

    @Override
    public Book updateBook(Integer bookId, Book bookDetails) {
        Book book = bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        // update basic info
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublicationDate(bookDetails.getPublicationDate());
        book.setEdition(bookDetails.getEdition());
        book.setLanguage(bookDetails.getLanguage());
        book.setPages(bookDetails.getPages());
        book.setCategory(bookDetails.getCategory());
        book.setDescription(bookDetails.getDescription());
        book.setCoverImageUrl(bookDetails.getCoverImageUrl());
        book.setAverageRating(bookDetails.getAverageRating());

        // update BookFormat
        if (bookDetails.getFormats() != null) {
            // delete old formats
            List<BookFormat> existingFormats = book.getFormats();
            if (existingFormats != null && !existingFormats.isEmpty()) {
                bookFormatRepository.deleteAll(existingFormats);
                existingFormats.clear();
            }

            // save new formats
            for (BookFormat format : bookDetails.getFormats()) {
                format.setBook(book);
                bookFormatRepository.save(format);
            }
        }

        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
        bookRepository.delete(book);
        // BookFormat will be deleted by Cascade
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookById(Integer bookId) {
        return bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ISBN: " + isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(int page, int size) {
        return bookRepository.findAll(PageRequest.of(page, size));
    }

//    @Override
//    public Page<Book> getHomePageBooks(int page, int size, HttpServletRequest request) {
//
//        String username = jwtUtils.extractUsernameFromRequest(request);
//
//        // 未登录：原逻辑
//        if (username == null) {
//            return getAllBooks(page, size);
//        }
//
//        // 已登录：推荐
//        List<Integer> bookIds = getRecommendedBookIds(username);
//
//        Pageable pageable = PageRequest.of(page, size);
//        List<Book> books = bookRepository.findAllById(bookIds);
//
//        return new PageImpl<>(books, pageable, books.size());
//    }

    @Override
    public Page<Book> getRecommendedBooks(String username, int page, int size) {
        List<Book> recs = bookRecommendationService.recommendForUser(username);

        int start = page * size;
        int end = Math.min(start + size, recs.size());

        if (start >= recs.size()) {
            return Page.empty();
        }

        return new PageImpl<>(recs.subList(start, end));
    }


//    private List<Integer> getRecommendedBookIds(String username) {
//
//        String redisKey = "rec:books:" + username;
//
//        List<Integer> cached = redisTemplate.opsForValue().get(redisKey);
//        if (cached != null && !cached.isEmpty()) {
//            return cached;
//        }
//
//        Map<Integer, Double> scoreMap = new HashMap<>();
//
//        applyUserProfileScore(username, scoreMap);
//        applyCollaborativeScore(username, scoreMap);
//        applyHighRatingScore(scoreMap);
//
//        List<Integer> result = scoreMap.entrySet().stream()
//                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
//                .limit(50)
//                .map(Map.Entry::getKey)
//                .toList();
//
//        redisTemplate.opsForValue().set(redisKey, result, Duration.ofMinutes(30));
//        return result;
//    }
//
//    private void applyUserProfileScore(String username, Map<Integer, Double> scoreMap) {
//
//        List<Review> reviews = reviewRepository.findByUserUsername(username);
//
//        for (Review r : reviews) {
//            long days = ChronoUnit.DAYS.between(r.getCreatedAt(), LocalDateTime.now());
//            double decay = Math.exp(-0.05 * days);
//            double score = r.getRating() * decay;
//
//            String category = r.getBook().getCategory();
//
//            bookRepository.findByCategory(category, PageRequest.of(0, 10))
//                    .forEach(b ->
//                            scoreMap.merge(b.getBookId(), score, Double::sum)
//                    );
//        }
//    }
//
//    private void applyCollaborativeScore(String username, Map<Integer, Double> scoreMap) {
//
//        List<Review> myReviews = reviewRepository.findByUserUsername(username);
//
//        Set<Integer> myBookIds = myReviews.stream()
//                .map(r -> r.getBook().getBookId())
//                .collect(Collectors.toSet());
//
//        List<Review> similarUserReviews =
//                reviewRepository.findByBookBookIdIn(myBookIds);
//
//        for (Review r : similarUserReviews) {
//            if (r.getRating() >= 4) {
//                scoreMap.merge(r.getBook().getBookId(), 2.0, Double::sum);
//            }
//        }
//    }
//
//    private void applyHighRatingScore(Map<Integer, Double> scoreMap) {
//
//        Page<Book> topBooks =
//                bookRepository.findAll(
//                        PageRequest.of(0, 20, Sort.by("averageRating").descending())
//                );
//
//        for (Book b : topBooks) {
//            scoreMap.merge(b.getBookId(), 1.0, Double::sum);
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String keyword, int page, int size) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByAuthor(String author, int page, int size) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategory(String category, int page, int size) {
        return bookRepository.findByCategory(category, PageRequest.of(page, size));
    }
}
