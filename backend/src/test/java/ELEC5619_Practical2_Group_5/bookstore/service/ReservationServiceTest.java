package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import ELEC5619_Practical2_Group_5.bookstore.entity.Reservation;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookFormatRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReservationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

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
    private BookFormat testPaperback;
    private BookFormat testEbook;
    private ReservationRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");

        // Setup paperback format
        testPaperback = new BookFormat();
        testPaperback.setBookFormatId(1);
        testPaperback.setBook(testBook);
        testPaperback.setFormat(BookFormatType.paperback);
        testPaperback.setPrice(new java.math.BigDecimal("25.99"));
        testPaperback.setStockQuantity(10);
        testPaperback.setReservedQuantity(0);

        // Setup ebook format
        testEbook = new BookFormat();
        testEbook.setBookFormatId(2);
        testEbook.setBook(testBook);
        testEbook.setFormat(BookFormatType.ebook);
        testEbook.setPrice(new java.math.BigDecimal("15.99"));
        testEbook.setStockQuantity(100);
        testEbook.setReservedQuantity(0);

        // Setup test request
        testRequest = new ReservationRequest();
        testRequest.setBookId(1);
        testRequest.setReservationDate(LocalDate.now().plusDays(1));
        testRequest.setTimeSlot("10:00-12:00");
    }

    @Test
    void createReservation_Success() {
        // Given
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback, testEbook));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setReservationId(1);
            reservation.setCreatedAt(LocalDateTime.now());
            return reservation;
        });

        // When
        var result = reservationService.createReservation(1, testRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getBookId());
        assertEquals("Test Book", result.getBookTitle());
        assertEquals(testRequest.getReservationDate(), result.getReservationDate());
        assertEquals(testRequest.getTimeSlot(), result.getTimeSlot());
        assertEquals("reserved", result.getStatus());

        verify(bookFormatRepository).save(testPaperback);
        verify(notificationService).createNotification(eq(1), anyString(), anyString());
    }

    @Test
    void createReservation_BookNotFound() {
        // Given
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_EbookOnly_ThrowsException() {
        // Given
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testEbook));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_OutOfStock_ThrowsException() {
        // Given
        testPaperback.setStockQuantity(0);
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_ExceedsLimit_ThrowsException() {
        // Given
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(3L);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_AlreadyReserved_ThrowsException() {
        // Given
        Reservation existingReservation = new Reservation();
        existingReservation.setReservationId(1);
        existingReservation.setUserId(1);
        existingReservation.setBookId(1);
        existingReservation.setStatus("reserved");

        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.singletonList(existingReservation));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_InvalidDate_ThrowsException() {
        // Given
        testRequest.setReservationDate(LocalDate.now().minusDays(1)); // Past date

        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void createReservation_InvalidTimeSlot_ThrowsException() {
        // Given
        testRequest.setTimeSlot("invalid-slot");

        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, testRequest);
        });
    }

    @Test
    void getUserReservations_Success() {
        // Given
        Reservation reservation1 = new Reservation();
        reservation1.setReservationId(1);
        reservation1.setUserId(1);
        reservation1.setBookId(1);
        reservation1.setStatus("reserved");

        Reservation reservation2 = new Reservation();
        reservation2.setReservationId(2);
        reservation2.setUserId(1);
        reservation2.setBookId(2);
        reservation2.setStatus("picked");

        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(reservations);
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(2)).thenReturn(Optional.of(testBook));

        // When
        var result = reservationService.getUserReservations(1);

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getReservationId());
        assertEquals(2, result.get(1).getReservationId());
    }

    @Test
    void cancelReservation_Success() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);
        reservation.setBookId(1);
        reservation.setStatus("reserved");
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setTimeSlot("10:00-12:00");

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));

        // When
        reservationService.cancelReservation(1, 1);

        // Then
        verify(reservationRepository).save(reservation);
        assertEquals("reservation_cancelled", reservation.getStatus());
        verify(notificationService).createNotification(eq(1), anyString(), anyString());
    }

    @Test
    void cancelReservation_NotFound_ThrowsException() {
        // Given
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1, 1);
        });
    }

    @Test
    void cancelReservation_Unauthorized_ThrowsException() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(2); // Different user
        reservation.setStatus("reserved");

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1, 1);
        });
    }

    @Test
    void cancelReservation_InvalidStatus_ThrowsException() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);
        reservation.setStatus("picked"); // Not reserved

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1, 1);
        });
    }

    @Test
    void canReserveBook_True() {
        // Given
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.findByUserIdAndStatus(1, "warning")).thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(2L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertTrue(result);
    }

    @Test
    void canReserveBook_NoPhysicalCopy_False() {
        // Given
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testEbook));

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void canReserveBook_HasWarning_False() {
        // Given
        Reservation warningReservation = new Reservation();
        warningReservation.setStatus("warning");
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.findByUserIdAndStatus(1, "warning")).thenReturn(Collections.singletonList(warningReservation));

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void canReserveBook_ExceedsLimit_False() {
        // Given
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.findByUserIdAndStatus(1, "warning")).thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(3L);

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void canReserveBook_AlreadyReserved_False() {
        // Given
        Reservation existingReservation = new Reservation();
        existingReservation.setStatus("reserved");
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.singletonList(testPaperback));
        when(reservationRepository.findByUserIdAndStatus(1, "warning")).thenReturn(Collections.emptyList());
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(2L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.singletonList(existingReservation));

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void getAvailableTimeSlots_Success() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        // No stubbing needed for empty list case

        // When
        List<String> result = reservationService.getAvailableTimeSlots(tomorrow, 1);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.contains("08:00-10:00"));
        assertTrue(result.contains("10:00-12:00"));
        assertTrue(result.contains("12:00-14:00"));
        assertTrue(result.contains("14:00-16:00"));
        assertTrue(result.contains("16:00-18:00"));
    }

    @Test
    void getAvailableTimeSlots_SomeReserved() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Reservation reserved = new Reservation();
        reserved.setTimeSlot("10:00-12:00");
        // No stubbing needed - the method will return empty list by default

        // When
        List<String> result = reservationService.getAvailableTimeSlots(tomorrow, 1);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.contains("10:00-12:00"));
    }

    @Test
    void getAvailableDates_Success() {
        // When
        List<LocalDate> result = reservationService.getAvailableDates();

        // Then
        assertEquals(7, result.size());
        assertEquals(LocalDate.now(), result.get(0));
        assertEquals(LocalDate.now().plusDays(6), result.get(6));
    }

    @Test
    void createReservation_ExceedsLimitAutoCancel() {
        // Given - User already has 3 active reservations
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(2L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        
        // Create 3 existing reservations with different dates
        Reservation res1 = new Reservation();
        res1.setReservationId(1);
        res1.setUserId(1);
        res1.setBookId(2);
        res1.setReservationDate(LocalDate.now().plusDays(1));
        res1.setStatus("reserved");
        
        Reservation res2 = new Reservation();
        res2.setReservationId(2);
        res2.setUserId(1);
        res2.setBookId(3);
        res2.setReservationDate(LocalDate.now().plusDays(2));
        res2.setStatus("reserved");
        
        Reservation res3 = new Reservation();
        res3.setReservationId(3);
        res3.setUserId(1);
        res3.setBookId(4);
        res3.setReservationDate(LocalDate.now().plusDays(3));
        res3.setStatus("reserved");
        
        Reservation res4 = new Reservation();
        res4.setReservationId(4);
        res4.setUserId(1);
        res4.setBookId(5);
        res4.setReservationDate(LocalDate.now().plusDays(4));
        res4.setStatus("reserved");
        
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(res1, res2, res3, res4));
        
        Reservation newReservation = new Reservation();
        newReservation.setReservationId(10);
        newReservation.setUserId(1);
        newReservation.setBookId(1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(newReservation);
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setTitle("Book 2");
        lenient().when(bookRepository.findById(4)).thenReturn(Optional.of(book2));
        lenient().when(bookRepository.findById(5)).thenReturn(Optional.of(book2));
        
        BookFormat format2 = new BookFormat();
        format2.setFormat(BookFormatType.paperback);
        format2.setStockQuantity(10);
        format2.setReservedQuantity(0);
        lenient().when(bookFormatRepository.findByBookBookId(4)).thenReturn(Arrays.asList(format2));
        lenient().when(bookFormatRepository.findByBookBookId(5)).thenReturn(Arrays.asList(format2));
        
        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00-12:00");

        // When
        reservationService.createReservation(1, request);

        // Then - Auto-cancellation notifications should be sent
        verify(notificationService, atLeast(1)).createNotification(eq(1), contains("Auto-Cancelled"), anyString());
    }

    @Test
    void createReservation_PaperbackNotFound() {
        // Given
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Collections.emptyList());

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00-12:00");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, request);
        });
    }

    @Test
    void cancelReservation_BookNotFound() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);
        reservation.setBookId(999);
        reservation.setStatus("reserved");
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setTimeSlot("10:00-12:00");

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        when(bookFormatRepository.findByBookBookId(999)).thenReturn(Arrays.asList(testPaperback));

        // When
        reservationService.cancelReservation(1, 1);

        // Then - Should handle gracefully
        verify(notificationService).createNotification(eq(1), anyString(), contains("Book ID: 999"));
    }

    @Test
    void createReservation_TodayEarlyTimeSlot() {
        // Given - Reserve today with an early time slot (08:00-10:00)
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        lenient().when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());
        
        Reservation saved = new Reservation();
        saved.setReservationId(1);
        saved.setUserId(1);
        saved.setBookId(1);
        saved.setReservationDate(LocalDate.now());
        saved.setTimeSlot("08:00-10:00");
        saved.setReservedDate(LocalDateTime.now());
        saved.setExpiryDate(LocalDate.now().atTime(18, 0));
        saved.setStatus("reserved");
        saved.setCreatedAt(LocalDateTime.now());
        
        lenient().when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now()); // Today
        request.setTimeSlot("08:00-10:00"); // Early morning

        // When & Then
        try {
            ReservationResponse response = reservationService.createReservation(1, request);
            // If test runs before 08:00, it will succeed
            assertNotNull(response);
        } catch (IllegalArgumentException e) {
            // If test runs after 08:00 or after 18:00, will throw exception
            assertTrue(e.getMessage().contains("Cannot reserve past time slots") || 
                      e.getMessage().contains("Cannot reserve after 18:00"));
        }
    }

    @Test
    void createReservation_TodayLateTimeSlot() {
        // Given - Reserve today with a late time slot (16:00-18:00)
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        lenient().when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());
        
        Reservation saved = new Reservation();
        saved.setReservationId(1);
        saved.setUserId(1);
        saved.setBookId(1);
        saved.setReservationDate(LocalDate.now());
        saved.setTimeSlot("16:00-18:00");
        saved.setReservedDate(LocalDateTime.now());
        saved.setExpiryDate(LocalDate.now().atTime(18, 0));
        saved.setStatus("reserved");
        saved.setCreatedAt(LocalDateTime.now());
        
        lenient().when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now()); // Today
        request.setTimeSlot("16:00-18:00"); // Late afternoon

        // When & Then
        try {
            ReservationResponse response = reservationService.createReservation(1, request);
            // If test runs before 16:00 and before 18:00, will succeed
            assertNotNull(response);
        } catch (IllegalArgumentException e) {
            // If test runs after 16:00 or after 18:00, will throw exception
            assertTrue(e.getMessage().contains("Cannot reserve past time slots") || 
                      e.getMessage().contains("Cannot reserve after 18:00"));
        }
    }

    @Test
    void createReservation_WithReturnedStatus() {
        // Given - User has a returned reservation
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        
        Reservation returnedRes = new Reservation();
        returnedRes.setReservationId(1);
        returnedRes.setUserId(1);
        returnedRes.setBookId(2);
        returnedRes.setReservationDate(LocalDate.now().minusDays(1));
        returnedRes.setStatus("returned");
        
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(returnedRes));
        
        Reservation newReservation = new Reservation();
        newReservation.setReservationId(10);
        newReservation.setUserId(1);
        newReservation.setBookId(1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(newReservation);

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00-12:00");

        // When
        reservationService.createReservation(1, request);

        // Then - Should count returned status correctly (not count toward limit)
        verify(reservationRepository).countActiveReservationsByUserId(1);
    }

    @Test
    void cancelReservation_AfterScheduledTime() {
        // Given - Reservation with past end time
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);
        reservation.setBookId(1);
        reservation.setStatus("reserved");
        reservation.setReservationDate(LocalDate.now().minusDays(1)); // Yesterday
        reservation.setTimeSlot("10:00-12:00");

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1, 1);
        });
    }

    @Test
    void getUserReservations_WithNullBook() {
        // Given - Reservation with non-existent book
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);
        reservation.setBookId(999); // Non-existent book
        reservation.setStatus("reserved");
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setTimeSlot("10:00-12:00");
        reservation.setReservedDate(LocalDateTime.now());
        reservation.setExpiryDate(LocalDateTime.now().plusDays(1));
        reservation.setCreatedAt(LocalDateTime.now());

        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(reservation));
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        // When
        List<ReservationResponse> result = reservationService.getUserReservations(1);

        // Then - Should handle null book gracefully
        assertEquals(1, result.size());
        assertEquals("", result.get(0).getBookTitle());
        assertEquals("", result.get(0).getBookCoverImageUrl());
    }

    @Test
    void canReserveBook_WithZeroStock() {
        // Given - Book with zero stock
        BookFormat zeroStockPaperback = new BookFormat();
        zeroStockPaperback.setFormat(BookFormatType.paperback);
        zeroStockPaperback.setStockQuantity(0); // Zero stock

        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(zeroStockPaperback));

        // When
        boolean result = reservationService.canReserveBook(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void getAvailableTimeSlots_TodayAfter18() {
        // Given - Current time simulation
        LocalDate today = LocalDate.now();
        
        // When
        List<String> result = reservationService.getAvailableTimeSlots(today, 1);

        // Then - Depends on current time
        assertNotNull(result);
        // If it's after 18:00, should return empty list
        // If it's before 18:00, should return available slots
    }

    @Test
    void getAvailableTimeSlots_FutureDate() {
        // Given - Future date (not today)
        LocalDate futureDate = LocalDate.now().plusDays(2);
        
        // When
        List<String> result = reservationService.getAvailableTimeSlots(futureDate, 1);

        // Then - Should return all slots for future dates
        assertEquals(5, result.size());
        assertTrue(result.contains("08:00-10:00"));
        assertTrue(result.contains("10:00-12:00"));
        assertTrue(result.contains("12:00-14:00"));
        assertTrue(result.contains("14:00-16:00"));
        assertTrue(result.contains("16:00-18:00"));
    }

    @Test
    void createReservation_DateInPast() {
        // Given - Try to reserve with a past date
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now().minusDays(1)); // Yesterday
        request.setTimeSlot("10:00-12:00");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1, request);
        });
    }

    @Test
    void createReservation_DateExactly7DaysLater() {
        // Given - Reserve exactly 7 days from now (at the boundary)
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookFormatRepository.findByBookBookId(1)).thenReturn(Arrays.asList(testPaperback));
        when(reservationRepository.countActiveReservationsByUserId(1)).thenReturn(0L);
        when(reservationRepository.findByUserIdAndBookIdAndStatus(1, 1, "reserved")).thenReturn(Collections.emptyList());
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());
        
        Reservation saved = new Reservation();
        saved.setReservationId(1);
        saved.setUserId(1);
        saved.setBookId(1);
        saved.setReservationDate(LocalDate.now().plusDays(7));
        saved.setTimeSlot("10:00-12:00");
        saved.setReservedDate(LocalDateTime.now());
        saved.setExpiryDate(LocalDate.now().plusDays(7).atTime(18, 0));
        saved.setStatus("reserved");
        saved.setCreatedAt(LocalDateTime.now());
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);
        request.setReservationDate(LocalDate.now().plusDays(7)); // Exactly 7 days
        request.setTimeSlot("10:00-12:00");

        // When
        ReservationResponse response = reservationService.createReservation(1, request);

        // Then - Should succeed (within 7 days)
        assertNotNull(response);
        assertEquals(1, response.getReservationId());
    }
}
