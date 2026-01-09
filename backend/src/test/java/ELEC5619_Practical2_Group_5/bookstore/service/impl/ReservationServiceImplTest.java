package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.entity.Reservation;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReservationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookFormatRepository bookFormatRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Book testBook;
    private BookFormat testBookFormat;
    private ReservationRequest testRequest;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setTitle("Test Book");
        testBook.setCoverImageUrl("test-cover.jpg");

        testBookFormat = new BookFormat();
        testBookFormat.setBookFormatId(1);
        testBookFormat.setBook(testBook);
        testBookFormat.setFormat(BookFormatType.paperback);
        testBookFormat.setStockQuantity(5);
        testBookFormat.setReservedQuantity(0);

        testRequest = new ReservationRequest();
        testRequest.setBookId(1);
        testRequest.setReservationDate(LocalDate.now().plusDays(1));
        testRequest.setTimeSlot("10:00-12:00");

        testReservation = new Reservation();
        testReservation.setReservationId(1);
        testReservation.setUserId(1);
        testReservation.setBookId(1);
        testReservation.setReservationDate(LocalDate.now().plusDays(1));
        testReservation.setTimeSlot("10:00-12:00");
        testReservation.setReservedDate(LocalDateTime.now());
        testReservation.setExpiryDate(LocalDate.now().plusDays(1).atTime(18, 0));
        testReservation.setStatus("reserved");
        testReservation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReservation_Success() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(testReservation));

        ReservationResponse result = reservationService.createReservation(1, testRequest);

        assertNotNull(result);
        assertEquals(1, result.getReservationId());
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getBookId());
        assertEquals("Test Book", result.getBookTitle());
        assertEquals("reserved", result.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
        verify(bookFormatRepository).save(testBookFormat);
        verify(notificationService).createNotification(eq(1), eq("Reservation Confirmed"), anyString());
    }

    @Test
    void createReservation_BookNotFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Book not found", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_BookFormatNotFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Book format not found", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_EbookCannotBeReserved() {
        BookFormat ebookFormat = new BookFormat();
        ebookFormat.setFormat(BookFormatType.ebook);
        
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(ebookFormat));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("E-books cannot be reserved", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_OutOfStock() {
        testBookFormat.setStockQuantity(0);
        
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Book is out of stock", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_MaxReservationLimitReached() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(3L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Maximum 3 active reservations allowed", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_BookAlreadyReserved() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Arrays.asList(testReservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("You have already reserved this book", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_InvalidReservationDate_Past() {
        testRequest.setReservationDate(LocalDate.now().minusDays(1));
        
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Reservation date must be within next 7 days", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_InvalidReservationDate_TooFar() {
        testRequest.setReservationDate(LocalDate.now().plusDays(8));
        
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Reservation date must be within next 7 days", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_InvalidTimeSlot() {
        testRequest.setTimeSlot("20:00-22:00");
        
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1, testRequest));

        assertEquals("Invalid time slot", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getUserReservations_Success() {
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        List<ReservationResponse> result = reservationService.getUserReservations(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        ReservationResponse response = result.get(0);
        assertEquals(1, response.getReservationId());
        assertEquals("Test Book", response.getBookTitle());
        verify(reservationRepository).findByUserIdOrderByCreatedAtDesc(1);
    }

    @Test
    void getUserReservations_EmptyList() {
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Collections.emptyList());

        List<ReservationResponse> result = reservationService.getUserReservations(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reservationRepository).findByUserIdOrderByCreatedAtDesc(1);
    }

    @Test
    void cancelReservation_Success() {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        reservationService.cancelReservation(1, 1);

        assertEquals("reservation_cancelled", testReservation.getStatus());
        verify(reservationRepository).save(testReservation);
        verify(bookFormatRepository).save(testBookFormat);
        verify(notificationService).createNotification(eq(1), eq("Reservation Cancelled"), anyString());
    }

    @Test
    void cancelReservation_ReservationNotFound() {
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(1, 1));

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_Unauthorized() {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(1, 2));

        assertEquals("Unauthorized to cancel this reservation", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_InvalidStatus() {
        testReservation.setStatus("completed");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(1, 1));

        assertEquals("Only reserved reservations can be cancelled", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void canReserveBook_Success() {
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.findByUserIdAndStatus(1, "warning"))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Collections.emptyList());

        boolean result = reservationService.canReserveBook(1, 1);

        assertTrue(result);
    }

    @Test
    void canReserveBook_NoPhysicalCopy() {
        BookFormat ebookFormat = new BookFormat();
        ebookFormat.setFormat(BookFormatType.ebook);
        ebookFormat.setStockQuantity(5);
        
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(ebookFormat));

        boolean result = reservationService.canReserveBook(1, 1);

        assertFalse(result);
    }

    @Test
    void canReserveBook_OutOfStock() {
        testBookFormat.setStockQuantity(0);
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));

        boolean result = reservationService.canReserveBook(1, 1);

        assertFalse(result);
    }

    @Test
    void canReserveBook_UserHasWarning() {
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.findByUserIdAndStatus(1, "warning"))
                .thenReturn(Arrays.asList(testReservation));

        boolean result = reservationService.canReserveBook(1, 1);

        assertFalse(result);
    }

    @Test
    void canReserveBook_MaxLimitReached() {
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.findByUserIdAndStatus(1, "warning"))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(3L);

        boolean result = reservationService.canReserveBook(1, 1);

        assertFalse(result);
    }

    @Test
    void canReserveBook_AlreadyReserved() {
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testBookFormat));
        when(reservationRepository.findByUserIdAndStatus(1, "warning"))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved"))
                .thenReturn(Arrays.asList(testReservation));

        boolean result = reservationService.canReserveBook(1, 1);

        assertFalse(result);
    }

    @Test
    void getAvailableTimeSlots_FutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(2);

        List<String> result = reservationService.getAvailableTimeSlots(futureDate, 1);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.contains("08:00-10:00"));
        assertTrue(result.contains("10:00-12:00"));
        assertTrue(result.contains("12:00-14:00"));
        assertTrue(result.contains("14:00-16:00"));
        assertTrue(result.contains("16:00-18:00"));
    }

    @Test
    void getAvailableDates_Success() {
        List<LocalDate> result = reservationService.getAvailableDates();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals(LocalDate.now(), result.get(0));
        assertEquals(LocalDate.now().plusDays(6), result.get(6));
    }
}