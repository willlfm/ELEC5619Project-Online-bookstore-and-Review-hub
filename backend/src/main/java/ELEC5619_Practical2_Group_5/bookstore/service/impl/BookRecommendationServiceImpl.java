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

    public List<Book> recommendForUser(String username) {

        User user = userRepository.findByUsername(username).orElseThrow();
        Integer userId = user.getUserId();

        String cacheKey = "rec:user:" + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> list) {
            return (List<Book>) list;
        }

        // 1. 用户画像
        List<Review> myReviews = reviewRepository.findByUserId(Long.valueOf(userId));
        if (myReviews.isEmpty()) {
            return fallbackTopRated(cacheKey);
        }

        List<Long> myBookIds = myReviews.stream()
                .map(Review::getBookId)
                .toList();

        // 2. 协同过滤
        List<Review> similarReviews =
                reviewRepository.findSimilarUsersHighRatingReviews(
                        myBookIds, userId, 4
                );

        if (similarReviews.isEmpty()) {
            return fallbackTopRated(cacheKey);
        }

        // 3. 聚合评分
        Map<Long, Long> scoreMap =
                similarReviews.stream()
                        .collect(Collectors.groupingBy(
                                Review::getBookId,
                                Collectors.counting()
                        ));

        List<Integer> bookIds =
                scoreMap.entrySet().stream()
                        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                        .limit(12)
                        .map(e -> e.getKey().intValue())
                        .toList();

        List<Book> books = bookRepository.findByBookIdIn(bookIds);

        redisTemplate.opsForValue()
                .set(cacheKey, books, Duration.ofMinutes(10));

        return books;
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
