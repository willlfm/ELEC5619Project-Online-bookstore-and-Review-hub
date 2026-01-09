package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    List<Review> findByUserId(Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId")
    Double findAverageRatingByBookId(Long bookId);

    List<Review> findTop10ByOrderByCreatedAtDesc();

    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    List<Review> findByRatingGreaterThanEqual(Integer rating);

    /**
     * 3️⃣ 协同过滤核心：
     *     找「和我看过相同 bookId 的其他用户」的高分记录
     */
    @Query("""
        SELECT r
        FROM Review r
        WHERE r.bookId IN :bookIds
          AND r.rating >= :minRating
          AND r.userId <> :userId
    """)
    List<Review> findSimilarUsersHighRatingReviews(
            @Param("bookIds") Collection<Long> bookIds,
            @Param("userId") Integer userId,
            @Param("minRating") Integer minRating
    );

    /**
     * 4️⃣ 防止推荐我已经看过的书
     */
    @Query("""
        SELECT r.bookId
        FROM Review r
        WHERE r.userId = :userId
    """)
    List<Long> findReviewedBookIdsByUser(
            @Param("userId") Integer userId
    );

}
