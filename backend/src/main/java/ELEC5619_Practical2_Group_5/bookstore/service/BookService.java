package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

public interface BookService {

    Book addBook(Book book);

    Book updateBook(Integer bookId, Book book);

    void deleteBook(Integer bookId);

    Book getBookById(Integer bookId);

    Book getBookByIsbn(String isbn);

    Page<Book> getAllBooks(int page, int size);

    Page<Book> getRecommendedBooks(String username, int page, int size);

    Page<Book> searchBooksByTitle(String keyword, int page, int size);

    Page<Book> searchBooksByAuthor(String author, int page, int size);

    Page<Book> getBooksByCategory(String category, int page, int size);
}
