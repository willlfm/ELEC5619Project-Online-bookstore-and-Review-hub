package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.review.BookDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.Review;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReviewRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Book book;

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .reviewId(1L)
                .bookId(100L)
                .userId(1)
                .rating(5)
                .comment("Great book")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        book = new Book();
        book.setBookId(100);
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setCoverImageUrl("url");
    }

    @Test
    void addReview_ReturnsReviewResponse() {
        // Test normal addReview path
        ReviewRequest request = new ReviewRequest();
        request.setBookId(100L);
        request.setRating(5);
        request.setComment("Great book");

        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));
        when(reviewRepository.save(any())).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setReviewId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        ReviewResponse response = reviewService.addReview(request, 1);

        assertEquals(1L, response.getReviewId());
        assertEquals("TestUser", response.getReviewName());
        assertEquals("Test Book", response.getBookName());
        assertEquals(5, response.getRating());
        assertEquals("Great book", response.getComment());
    }

    @Test
    void toResponse_BookNotFound_ThrowsException() {
        // Test bookRepository returns empty
        ReviewRequest request = new ReviewRequest();
        request.setBookId(100L);
        request.setRating(5);
        request.setComment("Test");

        when(bookRepository.findById(100)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.addReview(request, 1)
        );
        assertTrue(ex.getMessage().contains("Book not found"));
    }

    @Test
    void toResponse_UserNotFound_ReturnsUnknownUser() {
        // Test userRepository returns empty
        ReviewRequest request = new ReviewRequest();
        request.setBookId(100L);
        request.setRating(5);
        request.setComment("Test");

        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.empty());
        when(reviewRepository.save(any())).thenReturn(review);

        ReviewResponse resp = reviewService.addReview(request, 1);

        assertEquals("Unknown User", resp.getReviewName());
    }

    @Test
    void updateReview_UpdatesAndReturnsResponse() {
        // Test updateReview normal path
        ReviewRequest request = new ReviewRequest();
        request.setRating(4);
        request.setComment("Updated comment");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));
        when(reviewRepository.save(any())).thenReturn(review);

        ReviewResponse response = reviewService.updateReview(1L, request);

        assertEquals(4, response.getRating());
        assertEquals("Updated comment", response.getComment());
    }

    @Test
    void deleteReview_CallsRepository() {
        // Test deleteReview method
        reviewService.deleteReview(1L);
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void getReviewsByBook_ReturnsList() {
        // Test getReviewsByBook normal path
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(review));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));

        List<ReviewResponse> list = reviewService.getReviewsByBook(100L);

        assertEquals(1, list.size());
        assertEquals("TestUser", list.get(0).getReviewName());
    }

    @Test
    void getReviewsByUser_ReturnsList() {
        // Test getReviewsByUser normal path
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(review));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));

        List<ReviewResponse> list = reviewService.getReviewsByUser(1L);

        assertEquals(1, list.size());
        assertEquals("Test Book", list.get(0).getBookName());
    }

    @Test
    void getAverageRatingByBook_ReturnsCorrectValue() {
        // Test average rating non-null
        when(reviewRepository.findAverageRatingByBookId(100L)).thenReturn(4.5);

        Double avg = reviewService.getAverageRatingByBook(100L);

        assertEquals(4.5, avg);
    }

    @Test
    void getAverageRatingByBook_Null_ReturnsZero() {
        // Test average rating null branch
        when(reviewRepository.findAverageRatingByBookId(100L)).thenReturn(null);

        Double avg = reviewService.getAverageRatingByBook(100L);

        assertEquals(0.0, avg);
    }

    @Test
    void getRecentReviews_ReturnsList() {
        // Test getRecentReviews normal path
        when(reviewRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(review));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));

        List<ReviewResponse> list = reviewService.getRecentReviews();

        assertEquals(1, list.size());
        assertEquals("Test Book", list.get(0).getBookName());
    }

    @Test
    void getTopRatedBooks_ReturnsTopBooks() {
        // Test getTopRatedBooks normal path
        Review r1 = Review.builder().bookId(100L).rating(4).build();
        Review r2 = Review.builder().bookId(101L).rating(5).build();

        Book book1 = new Book();
        book1.setBookId(100);
        book1.setTitle("Test Book");
        book1.setAuthor("Author");
        book1.setCoverImageUrl("url");

        Book book2 = new Book();
        book2.setBookId(101);
        book2.setTitle("Book2");
        book2.setAuthor("Author2");
        book2.setCoverImageUrl("url2");

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(101)).thenReturn(Optional.of(book2));

        List<BookDto> topBooks = reviewService.getTopRatedBooks();

        assertEquals(2, topBooks.size());
        assertEquals("Book2", topBooks.get(0).getTitle());
        assertEquals("Test Book", topBooks.get(1).getTitle());
    }

    @Test
    void getTopRatedBooks_BookNotFound_ThrowsException() {
        // Test getTopRatedBooks when book not found
        Review r = Review.builder().bookId(999L).rating(5).build();
        when(reviewRepository.findAll()).thenReturn(List.of(r));
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.getTopRatedBooks());
        assertTrue(ex.getMessage().contains("Book not found"));
    }

    @Test
    void listReviews_PaginationAndFiltering() {
        // Test listReviews normal filtering and pagination
        Review r1 = Review.builder().reviewId(1L).bookId(100L).userId(1).rating(5).createdAt(LocalDateTime.now()).build();
        Review r2 = Review.builder().reviewId(2L).bookId(101L).userId(2).rating(4).createdAt(LocalDateTime.now()).build();

        lenient().when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));
        lenient().when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        lenient().when(bookRepository.findById(101)).thenReturn(Optional.of(book2()));
        lenient().when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("TestUser"));
        lenient().when(userRepository.findUsernameByUserId(2)).thenReturn(Optional.of("User2"));

        Page<ReviewResponse> page = reviewService.listReviews(0, 1, null, null, null);

        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void listReviews_NullCreatedAt_SortsCorrectly() {
        // Test sorting when createdAt is null
        Review r1 = Review.builder().reviewId(1L).bookId(100L).userId(1).rating(5).createdAt(null).build();
        Review r2 = Review.builder().reviewId(2L).bookId(100L).userId(1).rating(4).createdAt(LocalDateTime.now()).build();

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));
        when(bookRepository.findById(100)).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(anyInt())).thenReturn(Optional.of("TestUser"));

        Page<ReviewResponse> page = reviewService.listReviews(0, 10, null, null, null);

        assertEquals(2, page.getContent().size());
    }

    @Test
    void listReviews_NoMatches_ReturnsEmptyPage() {
        // Test filtering results in empty page
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        Page<ReviewResponse> page = reviewService.listReviews(0, 10, 999L, null, null);

        assertEquals(0, page.getContent().size());
    }


    @Test
    void listReviews_NullSortingBranches() {
        // Test sorting branches with both createdAt null
        Review r1 = Review.builder().reviewId(1L).bookId(100L).userId(1).rating(5).createdAt(null).build();
        Review r2 = Review.builder().reviewId(2L).bookId(100L).userId(1).rating(4).createdAt(null).build();

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        when(userRepository.findUsernameByUserId(anyInt())).thenReturn(Optional.of("TestUser"));

        Page<ReviewResponse> page = reviewService.listReviews(0, 10, null, null, null);
        assertEquals(2, page.getContent().size());
    }

    private Book book2() {
        Book b = new Book();
        b.setBookId(101);
        b.setTitle("Book2");
        b.setAuthor("Author2");
        b.setCoverImageUrl("url2");
        return b;
    }
}
