package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final JwtUtils jwtUtils;

//    @GetMapping
//    public ResponseEntity<Page<Book>> getAllBooks(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size) {
//        return ResponseEntity.ok(bookService.getAllBooks(page, size));
//    }

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request
    ) {
        String username = jwtUtils.extractUsernameFromRequest(request);

        if (username == null) {
            return ResponseEntity.ok(bookService.getAllBooks(page, size));
        }

        return ResponseEntity.ok(
                bookService.getRecommendedBooks(username, page, size)
        );
    }




    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        try {
            Book book = bookService.getBookById(id); // 已经通过 findByIdWithFormats 加载 formats
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        try {
            Book book = bookService.getBookByIsbn(isbn);
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookService.addBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<Page<Book>> searchBooksByTitle(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByTitle(keyword, page, size));
    }

    @GetMapping("/search/author")
    public ResponseEntity<Page<Book>> searchBooksByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByAuthor(author, page, size));
    }

    @GetMapping("/category")
    public ResponseEntity<Page<Book>> getBooksByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getBooksByCategory(category, page, size));
    }

}
