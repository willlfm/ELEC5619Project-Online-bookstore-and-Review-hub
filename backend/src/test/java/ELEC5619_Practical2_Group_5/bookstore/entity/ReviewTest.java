package ELEC5619_Practical2_Group_5.bookstore.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void testLombokGettersSettersAndBuilder() {
        // Using setter methods
        Review review = new Review();
        review.setReviewId(1L);
        review.setUserId(100);
        review.setBookId(200L);
        review.setRating(5);
        review.setComment("Great book!");
        LocalDateTime now = LocalDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        // Using getter methods
        assertEquals(1L, review.getReviewId());
        assertEquals(100, review.getUserId());
        assertEquals(200L, review.getBookId());
        assertEquals(5, review.getRating());
        assertEquals("Great book!", review.getComment());
        assertEquals(now, review.getCreatedAt());
        assertEquals(now, review.getUpdatedAt());

        // Using builder
        Review builtReview = Review.builder()
                .reviewId(2L)
                .userId(101)
                .bookId(201L)
                .rating(4)
                .comment("Nice read")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(2L, builtReview.getReviewId());
        assertEquals(101, builtReview.getUserId());
        assertEquals(201L, builtReview.getBookId());
        assertEquals(4, builtReview.getRating());
        assertEquals("Nice read", builtReview.getComment());
        assertEquals(now, builtReview.getCreatedAt());
        assertEquals(now, builtReview.getUpdatedAt());
    }

    @Test
    void testPrePersistAndPreUpdate() {
        Review review = new Review();

        // Trigger @PrePersist
        review.onCreate();
        assertNotNull(review.getCreatedAt());
        assertNotNull(review.getUpdatedAt());

        // Trigger @PreUpdate
        LocalDateTime beforeUpdate = review.getUpdatedAt();
        review.onUpdate();
        assertTrue(review.getUpdatedAt().isAfter(beforeUpdate) || review.getUpdatedAt().isEqual(beforeUpdate));
    }
}
