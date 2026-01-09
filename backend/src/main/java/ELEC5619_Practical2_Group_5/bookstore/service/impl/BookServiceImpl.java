package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookFormatRepository bookFormatRepository;

    @Override
    public Book addBook(Book book) {
        // save Book
        Book savedBook = bookRepository.save(book);

        // save BookFormat
        if (book.getFormats() != null) {
            for (BookFormat format : book.getFormats()) {
                format.setBook(savedBook);
                bookFormatRepository.save(format);
            }
        }

        return savedBook;
    }

    @Override
    public Book updateBook(Integer bookId, Book bookDetails) {
        Book book = bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        // update basic info
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublicationDate(bookDetails.getPublicationDate());
        book.setEdition(bookDetails.getEdition());
        book.setLanguage(bookDetails.getLanguage());
        book.setPages(bookDetails.getPages());
        book.setCategory(bookDetails.getCategory());
        book.setDescription(bookDetails.getDescription());
        book.setCoverImageUrl(bookDetails.getCoverImageUrl());
        book.setAverageRating(bookDetails.getAverageRating());

        // update BookFormat
        if (bookDetails.getFormats() != null) {
            // delete old formats
            List<BookFormat> existingFormats = book.getFormats();
            if (existingFormats != null && !existingFormats.isEmpty()) {
                bookFormatRepository.deleteAll(existingFormats);
                existingFormats.clear();
            }

            // save new formats
            for (BookFormat format : bookDetails.getFormats()) {
                format.setBook(book);
                bookFormatRepository.save(format);
            }
        }

        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
        bookRepository.delete(book);
        // BookFormat will be deleted by Cascade
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookById(Integer bookId) {
        return bookRepository.findByIdWithFormats(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ISBN: " + isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(int page, int size) {
        return bookRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String keyword, int page, int size) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByAuthor(String author, int page, int size) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategory(String category, int page, int size) {
        return bookRepository.findByCategory(category, PageRequest.of(page, size));
    }
}
