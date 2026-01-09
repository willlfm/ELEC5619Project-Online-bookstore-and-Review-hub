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
import ELEC5619_Practical2_Group_5.bookstore.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for reservation management
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookFormatRepository bookFormatRepository;
    private final NotificationService notificationService;

    private static final List<String> ALL_TIME_SLOTS = List.of(
            "08:00-10:00",
            "10:00-12:00",
            "12:00-14:00",
            "14:00-16:00",
            "16:00-18:00"
    );

    @Override
    @Transactional
    public ReservationResponse createReservation(Integer userId, ReservationRequest request) {
        // Validate book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Check if book has only ebook format
        List<BookFormat> formats = bookFormatRepository.findByBookBookId(request.getBookId());
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("Book format not found");
        }
        
        boolean hasPhysicalCopy = formats.stream()
                .anyMatch(f -> BookFormatType.paperback.equals(f.getFormat()));
        
        if (!hasPhysicalCopy) {
            throw new IllegalArgumentException("E-books cannot be reserved");
        }

        // Check stock quantity
        BookFormat paperback = formats.stream()
                .filter(f -> BookFormatType.paperback.equals(f.getFormat()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Paperback format not found"));

        if (paperback.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Book is out of stock");
        }

        // Check user reservation limit (max 3 active reservations)
        long activeCount = reservationRepository.countActiveReservationsByUserId(userId);
        if (activeCount >= 3) {
            throw new IllegalArgumentException("Maximum 3 active reservations allowed");
        }

        // Check if user already reserved this book
        List<Reservation> existingReservations = reservationRepository
                .findByUserIdAndBookIdAndStatus(userId, request.getBookId(), "reserved");
        if (!existingReservations.isEmpty()) {
            throw new IllegalArgumentException("You have already reserved this book");
        }

        // Validate reservation date (must be within 7 days)
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7);
        if (request.getReservationDate().isBefore(today) || request.getReservationDate().isAfter(maxDate)) {
            throw new IllegalArgumentException("Reservation date must be within next 7 days");
        }

        // Validate time slot
        if (!ALL_TIME_SLOTS.contains(request.getTimeSlot())) {
            throw new IllegalArgumentException("Invalid time slot");
        }

        // Check if it's today and time slot is in the past or 18:00-20:00
        if (request.getReservationDate().equals(today)) {
            LocalTime now = LocalTime.now();
            String[] times = request.getTimeSlot().split("-");
            LocalTime slotStart = LocalTime.parse(times[0]);
            
            if (now.isAfter(slotStart)) {
                throw new IllegalArgumentException("Cannot reserve past time slots");
            }
            
            if (now.isAfter(LocalTime.of(18, 0))) {
                throw new IllegalArgumentException("Cannot reserve after 18:00 on the same day");
            }
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(request.getBookId());
        reservation.setReservationDate(request.getReservationDate());
        reservation.setTimeSlot(request.getTimeSlot());
        reservation.setReservedDate(LocalDateTime.now());
        
        // Set expiry date: reservation date at 18:00
        LocalDateTime expiryDateTime = request.getReservationDate().atTime(18, 0);
        reservation.setExpiryDate(expiryDateTime);
        reservation.setStatus("reserved"); // Initial status when user successfully reserves

        Reservation saved = reservationRepository.save(reservation);

        // Update reserved quantity
        paperback.setReservedQuantity(paperback.getReservedQuantity() + 1);
        bookFormatRepository.save(paperback);

        // Check if user has more than 3 active reservations (excluding cancelled and returned)
        List<Reservation> userReservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Reservation> activeReservations = userReservations.stream()
                .filter(r -> !"reservation_cancelled".equals(r.getStatus()) && !"returned".equals(r.getStatus()))
                .sorted((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate())) // Sort by reservation date
                .collect(Collectors.toList());

        // If more than 3, cancel the extras (after the first 3)
        if (activeReservations.size() > 3) {
            for (int i = 3; i < activeReservations.size(); i++) {
                Reservation toCancel = activeReservations.get(i);
                toCancel.setStatus("reservation_cancelled");
                reservationRepository.save(toCancel);

                // Update reserved quantity for cancelled reservation
                List<BookFormat> cancelFormats = bookFormatRepository.findByBookBookId(toCancel.getBookId());
                cancelFormats.stream()
                        .filter(f -> BookFormatType.paperback.equals(f.getFormat()))
                        .findFirst()
                        .ifPresent(pb -> {
                            pb.setReservedQuantity(Math.max(0, pb.getReservedQuantity() - 1));
                            bookFormatRepository.save(pb);
                        });

                // Send notification about auto-cancellation
                Book cancelledBook = bookRepository.findById(toCancel.getBookId()).orElse(null);
                String cancelledBookTitle = cancelledBook != null ? cancelledBook.getTitle() : "Book ID: " + toCancel.getBookId();
                String autoCancelMessage = String.format(
                    "Your reservation for '%s' (scheduled for %s at %s) has been automatically cancelled because you have exceeded the maximum limit of 3 active reservations.",
                    cancelledBookTitle,
                    toCancel.getReservationDate(),
                    toCancel.getTimeSlot()
                );
                notificationService.createNotification(userId, "Reservation Auto-Cancelled", autoCancelMessage);
            }
        }

        // Send notification to user for successful reservation
        String title = "Reservation Confirmed";
        String message = String.format(
            "Your reservation for '%s' has been confirmed. " +
            "Reservation date: %s at %s. " +
            "Please return the book by %s at 18:00.",
            book.getTitle(),
            saved.getReservationDate(),
            saved.getTimeSlot(),
            saved.getReservationDate()
        );
        notificationService.createNotification(userId, title, message);

        return toResponse(saved, book);
    }

    @Override
    public List<ReservationResponse> getUserReservations(Integer userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return reservations.stream()
                .map(r -> {
                    Book book = bookRepository.findById(r.getBookId()).orElse(null);
                    return toResponse(r, book);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelReservation(Integer reservationId, Integer userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this reservation");
        }

        // Only RESERVED status can be cancelled by user
        if (!"reserved".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Only reserved reservations can be cancelled");
        }

        // Can only cancel before reservation end time
        LocalDateTime reservationEndTime = reservation.getReservationDate()
                .atTime(LocalTime.parse(reservation.getTimeSlot().split("-")[1]));
        if (LocalDateTime.now().isAfter(reservationEndTime)) {
            throw new IllegalArgumentException("Cannot cancel reservation after scheduled time");
        }

        // Update status
        reservation.setStatus("reservation_cancelled");
        reservationRepository.save(reservation);

        // Update reserved quantity
        List<BookFormat> formats = bookFormatRepository.findByBookBookId(reservation.getBookId());
        formats.stream()
                .filter(f -> BookFormatType.paperback.equals(f.getFormat()))
                .findFirst()
                .ifPresent(paperback -> {
                    paperback.setReservedQuantity(Math.max(0, paperback.getReservedQuantity() - 1));
                    bookFormatRepository.save(paperback);
                });

        // Get book title for notification
        Book book = bookRepository.findById(reservation.getBookId()).orElse(null);
        String bookTitle = book != null ? book.getTitle() : "Book ID: " + reservation.getBookId();
        
        // Send notification
        String message = String.format(
            "Your reservation for '%s' (scheduled for %s at %s) has been cancelled by your request.",
            bookTitle,
            reservation.getReservationDate(),
            reservation.getTimeSlot()
        );
        notificationService.createNotification(userId, "Reservation Cancelled", message);
    }

    @Override
    public boolean canReserveBook(Integer userId, Integer bookId) {
        // Check if book exists and has paperback format with stock
        List<BookFormat> formats = bookFormatRepository.findByBookBookId(bookId);
        boolean hasPhysicalCopy = formats.stream()
                .anyMatch(f -> BookFormatType.paperback.equals(f.getFormat()) && f.getStockQuantity() > 0);

        if (!hasPhysicalCopy) {
            return false;
        }

        // Check if user has any warning status - if yes, cannot reserve
        List<Reservation> warningReservations = reservationRepository.findByUserIdAndStatus(userId, "warning");
        if (!warningReservations.isEmpty()) {
            return false;
        }

        // Check user hasn't reached limit (excluding cancelled and returned)
        long activeCount = reservationRepository.countActiveReservationsByUserId(userId);
        if (activeCount >= 3) {
            return false;
        }

        // Check user hasn't already reserved this book
        List<Reservation> existing = reservationRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, "reserved");
        return existing.isEmpty();
    }

    @Override
    public List<String> getAvailableTimeSlots(LocalDate date, Integer bookId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<String> availableSlots = new ArrayList<>(ALL_TIME_SLOTS);

        // If it's today, filter out past time slots and after 18:00
        if (date.equals(today)) {
            availableSlots = availableSlots.stream()
                    .filter(slot -> {
                        String[] times = slot.split("-");
                        LocalTime slotStart = LocalTime.parse(times[0]);
                        return now.isBefore(slotStart) && now.isBefore(LocalTime.of(18, 0));
                    })
                    .collect(Collectors.toList());
        }

        return availableSlots;
    }

    @Override
    public List<LocalDate> getAvailableDates() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = new ArrayList<>();
        
        for (int i = 0; i <= 6; i++) {
            dates.add(today.plusDays(i));
        }
        
        return dates;
    }

    private ReservationResponse toResponse(Reservation reservation, Book book) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUserId())
                .bookId(reservation.getBookId())
                .bookTitle(book != null ? book.getTitle() : "")
                .bookCoverImageUrl(book != null ? book.getCoverImageUrl() : "")
                .reservationDate(reservation.getReservationDate())
                .timeSlot(reservation.getTimeSlot())
                .reservedDate(reservation.getReservedDate())
                .expiryDate(reservation.getExpiryDate())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

