package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(BookController.class)
@Import(TestSecurityConfig.class) // Use test security configuration
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc; // Performs simulated HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // Serializes objects to JSON strings

    @MockitoBean // Mock Service layer
    private BookService bookService;

    // 3. (Fix) Mock JwtUtils to satisfy SecurityConfig/Filter dependencies
    @MockitoBean
    private JwtUtils jwtUtils;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        // Prepare common Book objects for tests
        book1 = new Book();
        book1.setBookId(1);
        book1.setTitle("Test Book 1");
        book1.setAuthor("Author 1");
        book1.setIsbn("1234567890");

        book2 = new Book();
        book2.setBookId(2);
        book2.setTitle("Test Book 2");
        book2.setAuthor("Author 2");
        book2.setIsbn("0987654321");
    }

    @Test
    @DisplayName("GET /api/books - should return paginated books")
    void shouldGetAllBooks() throws Exception {
        // Prepare paginated data
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book2));

        // Mock service: when getAllBooks(0, 12) is called, return prepared page
        when(bookService.getAllBooks(0, 12)).thenReturn(bookPage);

        // Execute request and verify
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk()) // Expect status 200
                .andExpect(jsonPath("$.content", hasSize(2))) // Expect content array has 2 items
                .andExpect(jsonPath("$.content[0].title", is("Test Book 1"))) // Expect first item's title matches
                .andExpect(jsonPath("$.totalPages", is(1))); // Expect totalPages equals 1
    }

    @Test
    @DisplayName("GET /api/books/{id} - should return a single book")
    void shouldGetBookById() throws Exception {
        // Mock service
        when(bookService.getBookById(1)).thenReturn(book1);

        // Execute request and verify
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId", is(1)))
                .andExpect(jsonPath("$.title", is("Test Book 1")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - returns 404 when ID not found")
    void shouldReturn404WhenBookByIdNotFound() throws Exception {
        // Mock service throws exception
        when(bookService.getBookById(99)).thenThrow(new RuntimeException("Book not found"));

        // Execute request and verify
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound()); // Expect status 404
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - should return a book by ISBN")
    void shouldGetBookByIsbn() throws Exception {
        // Mock service
        when(bookService.getBookByIsbn("1234567890")).thenReturn(book1);

        // Execute request and verify
        mockMvc.perform(get("/api/books/isbn/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("1234567890")));
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - returns 404 when ISBN not found")
    void shouldReturn404WhenBookByIsbnNotFound() throws Exception {
        // Mock service throws exception
        when(bookService.getBookByIsbn("000")).thenThrow(new RuntimeException("Book not found"));

        // Execute request and verify
        mockMvc.perform(get("/api/books/isbn/000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/books - should create a new book")
    void shouldAddBook() throws Exception {
        // Mock service: when addBook(any(Book.class)) is called, return book1
        when(bookService.addBook(any(Book.class))).thenReturn(book1);

        // Execute request and verify
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON) // Set Content-Type header
                        .content(objectMapper.writeValueAsString(book1))) // Set JSON request body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId", is(1)))
                .andExpect(jsonPath("$.title", is("Test Book 1")));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - should update a book")
    void shouldUpdateBook() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setBookId(1);
        updatedBook.setTitle("Updated Title");

        // Mock service
        when(bookService.updateBook(eq(1), any(Book.class))).thenReturn(updatedBook);

        // Execute request and verify
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - returns 404 when updating non-existent book")
    void shouldReturn404WhenUpdatingNonExistentBook() throws Exception {
        // Mock service throws exception
        when(bookService.updateBook(eq(99), any(Book.class))).thenThrow(new RuntimeException("Book not found"));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - should delete a book and return 204")
    void shouldDeleteBook() throws Exception {
        // Mock service (void method)
        doNothing().when(bookService).deleteBook(1);

        // Execute request and verify
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent()); // Expect status 204
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - returns 404 when deleting non-existent book")
    void shouldReturn404WhenDeletingNonExistentBook() throws Exception {
        // Mock service (void method) throws exception
        doThrow(new RuntimeException("Book not found")).when(bookService).deleteBook(99);

        // Execute request and verify
        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/books/search/title - should return paginated books by title")
    void shouldSearchBooksByTitle() throws Exception {
        Page<Book> bookPage = new PageImpl<>(List.of(book1));

        // Mock service
        when(bookService.searchBooksByTitle(eq("Test"), eq(0), eq(10))).thenReturn(bookPage);

        // Execute request and verify
        mockMvc.perform(get("/api/books/search/title")
                        .param("keyword", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test Book 1")));
    }

    @Test
    @DisplayName("GET /api/books/search/author - should return paginated books by author")
    void shouldSearchBooksByAuthor() throws Exception {
        Page<Book> bookPage = new PageImpl<>(List.of(book1));

        // Mock service
        when(bookService.searchBooksByAuthor(eq("Author"), eq(0), eq(10))).thenReturn(bookPage);

        // Execute request and verify
        mockMvc.perform(get("/api/books/search/author")
                        .param("author", "Author")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].author", is("Author 1")));
    }

    @Test
    @DisplayName("GET /api/books/category - should return paginated books by category")
    void shouldGetBooksByCategory() throws Exception {
        Page<Book> bookPage = new PageImpl<>(List.of(book1));

        // Mock service
        when(bookService.getBooksByCategory(eq("Fiction"), eq(0), eq(10))).thenReturn(bookPage);

        // Execute request and verify
        mockMvc.perform(get("/api/books/category")
                        .param("category", "Fiction")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}