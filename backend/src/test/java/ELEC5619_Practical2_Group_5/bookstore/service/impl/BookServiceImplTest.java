package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookFormatRepository bookFormatRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookFormat testFormat;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setPublisher("Test Publisher");
        testBook.setPublicationDate(LocalDate.now());
        testBook.setEdition(1);
        testBook.setLanguage("English");
        testBook.setPages(200);
        testBook.setCategory("FICTION");
        testBook.setDescription("Test Description");
        testBook.setCoverImageUrl("test-cover.jpg");
        testBook.setAverageRating(new BigDecimal("4.5"));
        testBook.setIsbn("978-0123456789");

        testFormat = new BookFormat();
        testFormat.setBookFormatId(1);
        testFormat.setFormat(BookFormatType.paperback);
        testFormat.setPrice(new BigDecimal("29.99"));
        testFormat.setStockQuantity(10);
        testFormat.setBook(testBook);

        testBook.setFormats(new ArrayList<>(Arrays.asList(testFormat)));
    }

    @Test
    void addBook_Success() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(testFormat);

        Book result = bookService.addBook(testBook);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).save(testBook);
        verify(bookFormatRepository).save(testFormat);
    }

    @Test
    void addBook_WithoutFormats() {
        testBook.setFormats(null);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.addBook(testBook);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).save(testBook);
        verify(bookFormatRepository, never()).save(any(BookFormat.class));
    }

    @Test
    void updateBook_Success() {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationDate(LocalDate.now().minusYears(1));
        updatedDetails.setEdition(2);
        updatedDetails.setLanguage("Spanish");
        updatedDetails.setPages(250);
        updatedDetails.setCategory("Non-Fiction");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCoverImageUrl("updated-cover.jpg");
        updatedDetails.setAverageRating(new BigDecimal("4.8"));

        BookFormat newFormat = new BookFormat();
        newFormat.setFormat(BookFormatType.ebook);
        newFormat.setPrice(BigDecimal.valueOf(29.99));
        newFormat.setStockQuantity(5);
        updatedDetails.setFormats(new ArrayList<>(Arrays.asList(newFormat)));

        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(newFormat);

        Book result = bookService.updateBook(1, updatedDetails);

        assertNotNull(result);
        assertEquals("Updated Title", testBook.getTitle());
        assertEquals("Updated Author", testBook.getAuthor());
        assertEquals(2, testBook.getEdition());
        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository).save(testBook);
        verify(bookFormatRepository).deleteAll(anyList());
        verify(bookFormatRepository).save(newFormat);
    }

    @Test
    void updateBook_BookNotFound() {
        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookService.updateBook(1, testBook);
        });

        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(testBook));

        bookService.deleteBook(1);

        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository).delete(testBook);
    }

    @Test
    void deleteBook_BookNotFound() {
        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookService.deleteBook(1);
        });

        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(testBook));

        Book result = bookService.getBookById(1);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).findByIdWithFormats(1);
    }

    @Test
    void getBookById_BookNotFound() {
        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookService.getBookById(1);
        });

        verify(bookRepository).findByIdWithFormats(1);
    }

    @Test
    void getBookByIsbn_Success() {
        when(bookRepository.findByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));

        Book result = bookService.getBookByIsbn("978-0123456789");

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).findByIsbn("978-0123456789");
    }

    @Test
    void getBookByIsbn_BookNotFound() {
        when(bookRepository.findByIsbn("978-0123456789")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookService.getBookByIsbn("978-0123456789");
        });

        verify(bookRepository).findByIsbn("978-0123456789");
    }

    @Test
    void getAllBooks_Success() {
        List<Book> books = Arrays.asList(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(bookPage);

        Page<Book> result = bookService.getAllBooks(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Book", result.getContent().get(0).getTitle());
        verify(bookRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void searchBooksByTitle_Success() {
        List<Book> books = Arrays.asList(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        when(bookRepository.findByTitleContainingIgnoreCase("Test", PageRequest.of(0, 10)))
                .thenReturn(bookPage);

        Page<Book> result = bookService.searchBooksByTitle("Test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Book", result.getContent().get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("Test", PageRequest.of(0, 10));
    }

    @Test
    void searchBooksByAuthor_Success() {
        List<Book> books = Arrays.asList(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        when(bookRepository.findByAuthorContainingIgnoreCase("Test Author", PageRequest.of(0, 10)))
                .thenReturn(bookPage);

        Page<Book> result = bookService.searchBooksByAuthor("Test Author", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Author", result.getContent().get(0).getAuthor());
        verify(bookRepository).findByAuthorContainingIgnoreCase("Test Author", PageRequest.of(0, 10));
    }

    @Test
    void getBooksByCategory_Success() {
        List<Book> books = Arrays.asList(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        when(bookRepository.findByCategory("FICTION", PageRequest.of(0, 10)))
                .thenReturn(bookPage);

        Page<Book> result = bookService.getBooksByCategory("FICTION", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("FICTION", result.getContent().get(0).getCategory());
        verify(bookRepository).findByCategory("FICTION", PageRequest.of(0, 10));
    }

    @Test
    void addBook_WithNullFormats() {
        Book bookWithoutFormats = new Book();
        bookWithoutFormats.setTitle("Book Without Formats");
        bookWithoutFormats.setFormats(null);

        when(bookRepository.save(any(Book.class))).thenReturn(bookWithoutFormats);

        Book result = bookService.addBook(bookWithoutFormats);

        assertNotNull(result);
        assertEquals("Book Without Formats", result.getTitle());
        verify(bookRepository).save(bookWithoutFormats);
        verify(bookFormatRepository, never()).save(any(BookFormat.class));
    }

    @Test
    void addBook_WithEmptyFormats() {
        Book bookWithEmptyFormats = new Book();
        bookWithEmptyFormats.setTitle("Book With Empty Formats");
        bookWithEmptyFormats.setFormats(new ArrayList<>());

        when(bookRepository.save(any(Book.class))).thenReturn(bookWithEmptyFormats);

        Book result = bookService.addBook(bookWithEmptyFormats);

        assertNotNull(result);
        assertEquals("Book With Empty Formats", result.getTitle());
        verify(bookRepository).save(bookWithEmptyFormats);
        verify(bookFormatRepository, never()).save(any(BookFormat.class));
    }

    @Test
    void updateBook_WithNullFormats() {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationDate(LocalDate.now().minusYears(1));
        updatedDetails.setEdition(2);
        updatedDetails.setLanguage("Spanish");
        updatedDetails.setPages(250);
        updatedDetails.setCategory("Non-Fiction");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCoverImageUrl("updated-cover.jpg");
        updatedDetails.setAverageRating(new BigDecimal("4.8"));
        updatedDetails.setFormats(null);

        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.updateBook(1, updatedDetails);

        assertNotNull(result);
        assertEquals("Updated Title", testBook.getTitle());
        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository).save(testBook);
        verify(bookFormatRepository, never()).deleteAll(anyList());
        verify(bookFormatRepository, never()).save(any(BookFormat.class));
    }

    @Test
    void updateBook_WithExistingFormatsNull() {
        Book bookWithNullFormats = new Book();
        bookWithNullFormats.setBookId(1);
        bookWithNullFormats.setTitle("Test Book");
        bookWithNullFormats.setFormats(null);

        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationDate(LocalDate.now().minusYears(1));
        updatedDetails.setEdition(2);
        updatedDetails.setLanguage("Spanish");
        updatedDetails.setPages(250);
        updatedDetails.setCategory("Non-Fiction");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCoverImageUrl("updated-cover.jpg");
        updatedDetails.setAverageRating(new BigDecimal("4.8"));

        BookFormat newFormat = new BookFormat();
        newFormat.setFormat(BookFormatType.ebook);
        newFormat.setPrice(BigDecimal.valueOf(29.99));
        newFormat.setStockQuantity(5);
        updatedDetails.setFormats(new ArrayList<>(Arrays.asList(newFormat)));

        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(bookWithNullFormats));
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithNullFormats);
        when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(newFormat);

        Book result = bookService.updateBook(1, updatedDetails);

        assertNotNull(result);
        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository).save(bookWithNullFormats);
        verify(bookFormatRepository, never()).deleteAll(anyList());
        verify(bookFormatRepository).save(newFormat);
    }

    @Test
    void updateBook_WithExistingFormatsEmpty() {
        Book bookWithEmptyFormats = new Book();
        bookWithEmptyFormats.setBookId(1);
        bookWithEmptyFormats.setTitle("Test Book");
        bookWithEmptyFormats.setFormats(new ArrayList<>());

        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationDate(LocalDate.now().minusYears(1));
        updatedDetails.setEdition(2);
        updatedDetails.setLanguage("Spanish");
        updatedDetails.setPages(250);
        updatedDetails.setCategory("Non-Fiction");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCoverImageUrl("updated-cover.jpg");
        updatedDetails.setAverageRating(new BigDecimal("4.8"));

        BookFormat newFormat = new BookFormat();
        newFormat.setFormat(BookFormatType.ebook);
        newFormat.setPrice(BigDecimal.valueOf(29.99));
        newFormat.setStockQuantity(5);
        updatedDetails.setFormats(new ArrayList<>(Arrays.asList(newFormat)));

        when(bookRepository.findByIdWithFormats(1)).thenReturn(Optional.of(bookWithEmptyFormats));
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithEmptyFormats);
        when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(newFormat);

        Book result = bookService.updateBook(1, updatedDetails);

        assertNotNull(result);
        verify(bookRepository).findByIdWithFormats(1);
        verify(bookRepository).save(bookWithEmptyFormats);
        verify(bookFormatRepository, never()).deleteAll(anyList());
        verify(bookFormatRepository).save(newFormat);
    }



    @Test
    void searchBooksByTitle_EmptyResult() {
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookRepository.findByTitleContainingIgnoreCase("NonExistent", PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        Page<Book> result = bookService.searchBooksByTitle("NonExistent", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findByTitleContainingIgnoreCase("NonExistent", PageRequest.of(0, 10));
    }

    @Test
    void searchBooksByAuthor_EmptyResult() {
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookRepository.findByAuthorContainingIgnoreCase("NonExistent", PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        Page<Book> result = bookService.searchBooksByAuthor("NonExistent", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findByAuthorContainingIgnoreCase("NonExistent", PageRequest.of(0, 10));
    }

    @Test
    void getBooksByCategory_EmptyResult() {
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookRepository.findByCategory("NonExistent", PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        Page<Book> result = bookService.getBooksByCategory("NonExistent", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findByCategory("NonExistent", PageRequest.of(0, 10));
    }

    @Test
    void addBook_MultipleFormats() {
        Book bookWithMultipleFormats = new Book();
        bookWithMultipleFormats.setTitle("Multi Format Book");
        bookWithMultipleFormats.setAuthor("Test Author");
        bookWithMultipleFormats.setIsbn("978-0123456789");
        bookWithMultipleFormats.setCategory("Fiction");
        bookWithMultipleFormats.setPublicationDate(LocalDate.now());
        bookWithMultipleFormats.setDescription("Test Description");

        BookFormat format1 = new BookFormat();
        format1.setFormat(BookFormatType.paperback);
        format1.setPrice(new BigDecimal("29.99"));
        format1.setStockQuantity(10);

        BookFormat format2 = new BookFormat();
        format2.setFormat(BookFormatType.ebook);
        format2.setPrice(new BigDecimal("19.99"));
        format2.setStockQuantity(20);

        bookWithMultipleFormats.setFormats(Arrays.asList(format1, format2));

        when(bookRepository.save(any(Book.class))).thenReturn(bookWithMultipleFormats);

        Book result = bookService.addBook(bookWithMultipleFormats);

        assertNotNull(result);
        assertEquals("Multi Format Book", result.getTitle());
        assertEquals(2, result.getFormats().size());
        verify(bookRepository).save(bookWithMultipleFormats);
    }
}