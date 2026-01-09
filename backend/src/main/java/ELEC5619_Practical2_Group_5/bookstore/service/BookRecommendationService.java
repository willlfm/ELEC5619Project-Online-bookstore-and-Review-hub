package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;

import java.util.List;

public interface BookRecommendationService {

    List<Book> recommendForUser(String username);

    List<Book> fallbackTopRated(String cacheKey);


}
