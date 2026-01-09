package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.service.BookRecommendationService;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.Review;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReviewRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
        import java.util.stream.Collectors;

@Service
public class BookRecommendationServiceImpl implements BookRecommendationService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final double USER_PROFILE_WEIGHT = 0.5;
    private static final double COLLABORATIVE_WEIGHT = 0.4;
    private static final double TOP_RATED_WEIGHT = 0.1;

    @Autowired
    public BookRecommendationServiceImpl(
            ReviewRepository reviewRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            RedisTemplate<String, Object> redisTemplate
    ) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Book> recommendForUser(String username) {

        User user = userRepository.findByUsername(username).orElseThrow();
        Integer userId = user.getUserId();

        String cacheKey = "rec:user:" + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> list) {
            return (List<Book>) list;
        }

        Map<Long, Double> bookScoreMap = new HashMap<>();

        // 1️⃣ 用户画像
        List<Review> myReviews = reviewRepository.findByUserId(Long.valueOf(userId));
        for (Review r : myReviews) {
            bookScoreMap.merge(r.getBookId(), USER_PROFILE_WEIGHT * r.getRating(), Double::sum);
        }

        // 2️⃣ 协同过滤
        List<Long> myBookIds = myReviews.stream().map(Review::getBookId).toList();
        List<Review> similarReviews = reviewRepository.findSimilarUsersHighRatingReviews(
                myBookIds, userId, 4
        );
        for (Review r : similarReviews) {
            bookScoreMap.merge(r.getBookId(), COLLABORATIVE_WEIGHT, Double::sum);
        }

        // 3️⃣ 获取数据库里所有书
        List<Book> allBooks = bookRepository.findAll();

        // 4️⃣ 按推荐权重排序，未推荐的书权重为 0
        allBooks.sort((b1, b2) -> {
            double score1 = bookScoreMap.getOrDefault(b1.getBookId().longValue(), 0.0);
            double score2 = bookScoreMap.getOrDefault(b2.getBookId().longValue(), 0.0);
            return Double.compare(score2, score1); // 倒序，推荐分高的排前
        });

        redisTemplate.opsForValue()
                .set(cacheKey, allBooks, Duration.ofMinutes(10));

        return allBooks;
    }


    public List<Book> fallbackTopRated(String cacheKey) {
        List<Long> bookIds =
                reviewRepository.findByRatingGreaterThanEqual(4)
                        .stream()
                        .map(Review::getBookId)
                        .distinct()
                        .limit(12)
                        .toList();

        List<Book> books = bookRepository.findByBookIdIn(
                bookIds.stream().map(Long::intValue).toList()
        );

        redisTemplate.opsForValue()
                .set(cacheKey, books, Duration.ofMinutes(10));

        return books;
    }
}
