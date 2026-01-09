package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.review.BookDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.Review;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReviewRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse addReview(ReviewRequest request, Integer userId) {
        Review review = Review.builder()
                .userId(userId)
                .bookId(request.getBookId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        reviewRepository.save(review);
        return toResponse(review);
    }

    private ReviewResponse toResponse(Review review) {
        Book book = bookRepository.findById(Math.toIntExact(review.getBookId()))
                .orElseThrow(() -> new RuntimeException("Book not found: " + review.getBookId()));

        String username = userRepository.findUsernameByUserId(Math.toIntExact(review.getUserId()))
                .orElse("Unknown User");

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .reviewName(username)
                .bookName(book.getTitle())
                .userId(review.getUserId())
                .bookId(review.getBookId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);
        return toResponse(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public List<ReviewResponse> getReviewsByBook(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingByBook(Long bookId) {
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        return avg != null ? avg : 0.0;
    }

    public List<ReviewResponse> getRecentReviews() {
        return reviewRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookDto> getTopRatedBooks() {
        Map<Long, Double> avgRatings = reviewRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Review::getBookId,
                        Collectors.averagingDouble(Review::getRating)
                ));

        return avgRatings.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    Book book = bookRepository.findById(Math.toIntExact(entry.getKey()))
                            .orElseThrow(() -> new RuntimeException("Book not found: " + entry.getKey()));

                    return BookDto.builder()
                            .id(book.getBookId())
                            .title(book.getTitle())
                            .author(book.getAuthor())
                            .averageRating(entry.getValue())
                            .coverImageUrl(book.getCoverImageUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponse> listReviews(int page, int size, Long bookId, Long userId, Integer rating) {
        PageRequest pageable = PageRequest.of(page, size);
        List<Review> filtered = reviewRepository.findAll().stream()
                .filter(review -> bookId == null || review.getBookId().equals(bookId))
                .filter(review -> userId == null || review.getUserId().equals(userId))
                .filter(review -> rating == null || review.getRating().equals(rating))
                .sorted((a, b) -> {
                    LocalDateTime left = a.getCreatedAt();
                    LocalDateTime right = b.getCreatedAt();
                    if (left == null && right == null) {
                        return 0;
                    } else if (left == null) {
                        return 1;
                    } else if (right == null) {
                        return -1;
                    }
                    return right.compareTo(left);
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ReviewResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end).stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

}
