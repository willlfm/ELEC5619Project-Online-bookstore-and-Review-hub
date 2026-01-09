package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.review.BookDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReviewService {

    ReviewResponse addReview(ReviewRequest request, Integer userId);

    ReviewResponse updateReview(Long reviewId, ReviewRequest request);

    void deleteReview(Long reviewId);

    List<ReviewResponse> getReviewsByBook(Long bookId);

    List<ReviewResponse> getReviewsByUser(Long userId);

    Double getAverageRatingByBook(Long bookId);

    List<ReviewResponse> getRecentReviews();

    List<BookDto> getTopRatedBooks();

    Page<ReviewResponse> listReviews(int page, int size, Long bookId, Long userId, Integer rating);
}
