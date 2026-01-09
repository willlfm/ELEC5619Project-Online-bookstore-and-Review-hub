package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.BookDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ReviewController reviewController;

    private User testUser;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();

        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        reviewRequest = new ReviewRequest();
        reviewRequest.setBookId(100L);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great book!");

        reviewResponse = ReviewResponse.builder()
                .reviewId(10L)
                .reviewName("My Review")
                .userId(1)
                .bookId(100L)
                .bookName("Book Title")
                .rating(5)
                .comment("Great book!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== POST /api/reviews Tests ==========

    @Test
    void testAddReview_Success() throws Exception {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(any(HttpServletRequest.class))).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(reviewService.addReview(any(ReviewRequest.class), eq(1))).thenReturn(reviewResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":100,\"rating\":5,\"comment\":\"Great book!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(10))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great book!"));

        verify(jwtUtils, times(1)).extractUsernameFromRequest(any(HttpServletRequest.class));
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(reviewService, times(1)).addReview(any(ReviewRequest.class), eq(1));
    }

    @Test
    void testAddReview_Unauthorized_NullUsername() {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn(null);

        // Act
        ResponseEntity<?> response = reviewController.addReview(reviewRequest, httpServletRequest);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Unauthorized", body.get("message"));

        verify(userRepository, never()).findByUsername(anyString());
        verify(reviewService, never()).addReview(any(), anyInt());
    }

    @Test
    void testAddReview_UserNotFound() {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reviewController.addReview(reviewRequest, httpServletRequest);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("User not found", body.get("message"));

        verify(reviewService, never()).addReview(any(), anyInt());
    }

    @Test
    void testAddReview_ServiceException() {
        // Arrange
        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(reviewService.addReview(any(ReviewRequest.class), eq(1)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = reviewController.addReview(reviewRequest, httpServletRequest);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Database error", body.get("message"));
    }

    @Test
    void testAddReview_WithAllFields() {
        // Arrange
        ReviewRequest fullRequest = new ReviewRequest();
        fullRequest.setBookId(200L);
        fullRequest.setRating(4);
        fullRequest.setComment("Good read with detailed explanations");

        ReviewResponse fullResponse = ReviewResponse.builder()
                .reviewId(20L)
                .userId(1)
                .bookId(200L)
                .rating(4)
                .comment("Good read with detailed explanations")
                .createdAt(LocalDateTime.now())
                .build();

        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(reviewService.addReview(any(ReviewRequest.class), eq(1))).thenReturn(fullResponse);

        // Act
        ResponseEntity<?> response = reviewController.addReview(fullRequest, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        ReviewResponse body = (ReviewResponse) response.getBody();
        assertNotNull(body);
        assertEquals(20L, body.getReviewId());
        assertEquals(4, body.getRating());
    }

    @Test
    void testAddReview_MinimumRating() {
        // Arrange
        reviewRequest.setRating(1);
        reviewResponse.setRating(1);

        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(reviewService.addReview(any(ReviewRequest.class), eq(1))).thenReturn(reviewResponse);

        // Act
        ResponseEntity<?> response = reviewController.addReview(reviewRequest, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        ReviewResponse body = (ReviewResponse) response.getBody();
        assertEquals(1, body.getRating());
    }

    @Test
    void testAddReview_MaximumRating() {
        // Arrange
        reviewRequest.setRating(5);
        reviewResponse.setRating(5);

        when(jwtUtils.extractUsernameFromRequest(httpServletRequest)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(reviewService.addReview(any(ReviewRequest.class), eq(1))).thenReturn(reviewResponse);

        // Act
        ResponseEntity<?> response = reviewController.addReview(reviewRequest, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        ReviewResponse body = (ReviewResponse) response.getBody();
        assertEquals(5, body.getRating());
    }

    // ========== PUT /api/reviews/{id} Tests ==========

    @Test
    void testUpdateReview_Success() throws Exception {
        // Arrange
        ReviewResponse updatedResponse = ReviewResponse.builder()
                .reviewId(5L)
                .comment("Updated comment")
                .rating(4)
                .build();

        when(reviewService.updateReview(eq(5L), any(ReviewRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/reviews/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(5))
                .andExpect(jsonPath("$.comment").value("Updated comment"));

        verify(reviewService, times(1)).updateReview(eq(5L), any(ReviewRequest.class));
    }

    @Test
    void testUpdateReview_DifferentReviewIds() throws Exception {
        // Arrange
        ReviewResponse response1 = ReviewResponse.builder().reviewId(10L).build();
        ReviewResponse response2 = ReviewResponse.builder().reviewId(20L).build();

        when(reviewService.updateReview(eq(10L), any(ReviewRequest.class))).thenReturn(response1);
        when(reviewService.updateReview(eq(20L), any(ReviewRequest.class))).thenReturn(response2);

        // Act & Assert
        mockMvc.perform(put("/api/reviews/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(10));

        mockMvc.perform(put("/api/reviews/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(20));

        verify(reviewService, times(1)).updateReview(eq(10L), any(ReviewRequest.class));
        verify(reviewService, times(1)).updateReview(eq(20L), any(ReviewRequest.class));
    }

    @Test
    void testUpdateReview_CallsServiceCorrectly() {
        // Arrange
        ReviewResponse mockResponse = ReviewResponse.builder()
                .reviewId(5L)
                .comment("Updated comment")
                .build();

        when(reviewService.updateReview(5L, reviewRequest)).thenReturn(mockResponse);

        // Act
        ReviewResponse response = reviewController.updateReview(5L, reviewRequest);

        // Assert
        assertEquals(5L, response.getReviewId());
        assertEquals("Updated comment", response.getComment());
        verify(reviewService, times(1)).updateReview(5L, reviewRequest);
    }

    // ========== DELETE /api/reviews/{id} Tests ==========

    @Test
    void testDeleteReview_Success() throws Exception {
        // Arrange
        doNothing().when(reviewService).deleteReview(7L);

        // Act & Assert
        mockMvc.perform(delete("/api/reviews/7"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).deleteReview(7L);
    }

    @Test
    void testDeleteReview_DifferentIds() throws Exception {
        // Arrange
        doNothing().when(reviewService).deleteReview(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isOk());
        verify(reviewService, times(1)).deleteReview(1L);

        mockMvc.perform(delete("/api/reviews/999"))
                .andExpect(status().isOk());
        verify(reviewService, times(1)).deleteReview(999L);
    }

    @Test
    void testDeleteReview_CallsService() {
        // Act
        reviewController.deleteReview(7L);

        // Assert
        verify(reviewService, times(1)).deleteReview(7L);
    }

    // ========== GET /api/reviews/book/{bookId} Tests ==========

    @Test
    void testGetReviewsByBook_Success() throws Exception {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder().reviewId(1L).rating(5).build();
        ReviewResponse r2 = ReviewResponse.builder().reviewId(2L).rating(4).build();
        when(reviewService.getReviewsByBook(100L)).thenReturn(List.of(r1, r2));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/book/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reviewId").value(1))
                .andExpect(jsonPath("$[1].reviewId").value(2));

        verify(reviewService, times(1)).getReviewsByBook(100L);
    }

    @Test
    void testGetReviewsByBook_EmptyList() throws Exception {
        // Arrange
        when(reviewService.getReviewsByBook(999L)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/book/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getReviewsByBook(999L);
    }

    @Test
    void testGetReviewsByBook_ReturnsList() {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder().reviewId(1L).build();
        ReviewResponse r2 = ReviewResponse.builder().reviewId(2L).build();
        when(reviewService.getReviewsByBook(100L)).thenReturn(List.of(r1, r2));

        // Act
        List<ReviewResponse> list = reviewController.getReviewsByBook(100L);

        // Assert
        assertEquals(2, list.size());
        verify(reviewService, times(1)).getReviewsByBook(100L);
    }

    // ========== GET /api/reviews/user/{userId} Tests ==========

    @Test
    void testGetReviewsByUser_Success() throws Exception {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder().reviewId(3L).userId(1).build();
        when(reviewService.getReviewsByUser(1L)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reviewId").value(3));

        verify(reviewService, times(1)).getReviewsByUser(1L);
    }

    @Test
    void testGetReviewsByUser_EmptyList() throws Exception {
        // Arrange
        when(reviewService.getReviewsByUser(999L)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getReviewsByUser(999L);
    }

    @Test
    void testGetReviewsByUser_ReturnsList() {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder().reviewId(3L).build();
        when(reviewService.getReviewsByUser(1L)).thenReturn(List.of(r1));

        // Act
        List<ReviewResponse> list = reviewController.getReviewsByUser(1L);

        // Assert
        assertEquals(1, list.size());
        verify(reviewService, times(1)).getReviewsByUser(1L);
    }

    @Test
    void testGetReviewsByUser_MultipleReviews() throws Exception {
        // Arrange
        List<ReviewResponse> reviews = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            reviews.add(ReviewResponse.builder().reviewId((long) i).build());
        }
        when(reviewService.getReviewsByUser(1L)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        verify(reviewService, times(1)).getReviewsByUser(1L);
    }

    // ========== GET /api/reviews/book/{bookId}/average-rating Tests ==========

    @Test
    void testGetAverageRatingByBook_Success() throws Exception {
        // Arrange
        when(reviewService.getAverageRatingByBook(10L)).thenReturn(4.5);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/book/10/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.5));

        verify(reviewService, times(1)).getAverageRatingByBook(10L);
    }

    @Test
    void testGetAverageRatingByBook_ZeroRating() throws Exception {
        // Arrange
        when(reviewService.getAverageRatingByBook(99L)).thenReturn(0.0);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/book/99/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0.0));

        verify(reviewService, times(1)).getAverageRatingByBook(99L);
    }

    @Test
    void testGetAverageRatingByBook_ReturnsValue() {
        // Arrange
        when(reviewService.getAverageRatingByBook(10L)).thenReturn(4.5);

        // Act
        Double avg = reviewController.getAverageRatingByBook(10L);

        // Assert
        assertEquals(4.5, avg);
        verify(reviewService, times(1)).getAverageRatingByBook(10L);
    }

    @Test
    void testGetAverageRatingByBook_PerfectRating() throws Exception {
        // Arrange
        when(reviewService.getAverageRatingByBook(5L)).thenReturn(5.0);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/book/5/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5.0));

        verify(reviewService, times(1)).getAverageRatingByBook(5L);
    }

    // ========== GET /api/reviews/recent Tests ==========

    @Test
    void testGetRecentReviews_Success() throws Exception {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder()
                .reviewId(11L)
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewService.getRecentReviews()).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reviewId").value(11));

        verify(reviewService, times(1)).getRecentReviews();
    }

    @Test
    void testGetRecentReviews_EmptyList() throws Exception {
        // Arrange
        when(reviewService.getRecentReviews()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getRecentReviews();
    }

    @Test
    void testGetRecentReviews_ReturnsList() {
        // Arrange
        ReviewResponse r1 = ReviewResponse.builder().reviewId(11L).build();
        when(reviewService.getRecentReviews()).thenReturn(List.of(r1));

        // Act
        List<ReviewResponse> list = reviewController.getRecentReviews();

        // Assert
        assertEquals(1, list.size());
        verify(reviewService, times(1)).getRecentReviews();
    }

    @Test
    void testGetRecentReviews_MultipleReviews() throws Exception {
        // Arrange
        List<ReviewResponse> reviews = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            reviews.add(ReviewResponse.builder()
                    .reviewId((long) i)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build());
        }
        when(reviewService.getRecentReviews()).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));

        verify(reviewService, times(1)).getRecentReviews();
    }

    // ========== GET /api/reviews/books/top-rated Tests ==========

    @Test
    void testGetTopRatedBooks_Success() throws Exception {
        // Arrange
        BookDto b1 = new BookDto();
        b1.setId(101);
        b1.setTitle("Top Book");
        when(reviewService.getTopRatedBooks()).thenReturn(List.of(b1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/books/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(101));

        verify(reviewService, times(1)).getTopRatedBooks();
    }

    @Test
    void testGetTopRatedBooks_EmptyList() throws Exception {
        // Arrange
        when(reviewService.getTopRatedBooks()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/books/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getTopRatedBooks();
    }

    @Test
    void testGetTopRatedBooks_ReturnsList() {
        // Arrange
        BookDto b1 = new BookDto();
        b1.setId(101);
        when(reviewService.getTopRatedBooks()).thenReturn(List.of(b1));

        // Act
        List<BookDto> list = reviewController.getTopRatedBooks();

        // Assert
        assertEquals(1, list.size());
        verify(reviewService, times(1)).getTopRatedBooks();
    }

    @Test
    void testGetTopRatedBooks_MultipleBooks() throws Exception {
        // Arrange
        List<BookDto> books = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            BookDto book = new BookDto();
            book.setId(100 + i);
            book.setTitle("Book " + i);
            books.add(book);
        }
        when(reviewService.getTopRatedBooks()).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/books/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        verify(reviewService, times(1)).getTopRatedBooks();
    }
}