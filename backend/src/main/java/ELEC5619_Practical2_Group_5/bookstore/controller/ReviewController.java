package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.BookDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request, HttpServletRequest httpServletRequest) {
        try {
            String username = jwtUtils.extractUsernameFromRequest(httpServletRequest);
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "status", "error",
                        "message", "Unauthorized"
                ));
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ReviewResponse reviewResponse = reviewService.addReview(request, user.getUserId());
            return ResponseEntity.ok(reviewResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return reviewService.updateReview(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/book/{bookId}")
    public List<ReviewResponse> getReviewsByBook(@PathVariable Long bookId) {
        return reviewService.getReviewsByBook(bookId);
    }

    @GetMapping("/user/{userId}")
    public List<ReviewResponse> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    @GetMapping("/book/{bookId}/average-rating")
    public Double getAverageRatingByBook(@PathVariable Long bookId) {
        return reviewService.getAverageRatingByBook(bookId);
    }

    @GetMapping("/recent")
    public List<ReviewResponse> getRecentReviews() {
        return reviewService.getRecentReviews();
    }

    @GetMapping("/books/top-rated")
    public List<BookDto> getTopRatedBooks() {
        return reviewService.getTopRatedBooks();
    }

}
