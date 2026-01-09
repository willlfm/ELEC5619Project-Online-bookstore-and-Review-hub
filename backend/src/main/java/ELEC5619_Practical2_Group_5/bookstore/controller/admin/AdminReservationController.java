package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.Reservation;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReservationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller for reservation management
 */
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    /**
     * Get all reservations with optional status filter
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) String status
    ) {
        List<Reservation> reservations = status != null && !status.isEmpty()
                ? reservationRepository.findByStatus(status)
                : reservationRepository.findAll();
        
        List<ReservationResponse> responses = reservations.stream()
                .map(r -> {
                    Book book = bookRepository.findById(r.getBookId()).orElse(null);
                    return toResponse(r, book);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update reservation status and send notification
     */
    @PutMapping("/{reservationId}/status")
    @Transactional
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Integer reservationId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String newStatus = request.get("status");
            
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("Status cannot be empty");
            }
            
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
            
            // Validate warning status can only be set after expiry date
            if ("warning".equals(newStatus)) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(reservation.getExpiryDate())) {
                    return ResponseEntity.badRequest().body("Cannot set warning status before due time");
                }
            }
            
            Book book = bookRepository.findById(reservation.getBookId()).orElse(null);
            String oldStatus = reservation.getStatus();
            
            // Update status
            reservation.setStatus(newStatus);
            Reservation saved = reservationRepository.save(reservation);
            
            // If changing from warning to returned, check if user has any other warnings
            // and auto-cancel reserved orders if warning exists
            if ("warning".equals(oldStatus) && "returned".equals(newStatus)) {
                handleWarningStatusCleared(reservation.getUserId());
            } else if ("warning".equals(newStatus)) {
                handleWarningStatusSet(reservation.getUserId(), reservationId);
            }
            
            // Send notification to user based on status
            sendStatusNotification(saved, book, newStatus);
            
            return ResponseEntity.ok(toResponse(saved, book));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }

    private void handleWarningStatusSet(Integer userId, Integer currentReservationId) {
        // Get all user's reservations sorted by reservation date
        List<Reservation> userReservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Find all reserved status orders after this warning order
        userReservations.stream()
                .filter(r -> "reserved".equals(r.getStatus()))
                .forEach(r -> {
                    r.setStatus("reservation_cancelled");
                    reservationRepository.save(r);
                    
                    // Send notification
                    Book book = bookRepository.findById(r.getBookId()).orElse(null);
                    String bookTitle = book != null ? book.getTitle() : "Book ID: " + r.getBookId();
                    String message = String.format(
                        "Your reservation for '%s' has been automatically cancelled because you have an overdue book (warning status).",
                        bookTitle
                    );
                    notificationService.createNotification(userId, "Reservation Auto-Cancelled", message);
                });
    }
    
    private void handleWarningStatusCleared(Integer userId) {
        // Check if user still has any warning status reservations
        List<Reservation> warningReservations = reservationRepository.findByUserIdAndStatus(userId, "warning");
        
        // If no warnings left, user can reserve normally again (no action needed here)
        // The createReservation logic will handle the 3-book limit
    }

    private void sendStatusNotification(Reservation reservation, Book book, String status) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        String bookTitle = book != null ? book.getTitle() : "Book ID: " + reservation.getBookId();
        String reservationDate = reservation.getReservationDate().format(dateFormatter);
        String reservedDate = reservation.getReservedDate().format(dateTimeFormatter);
        
        String title;
        String message;
        
        switch (status) {
            case "reserved":
                title = "Reservation Confirmed";
                message = String.format(
                    "Your reservation for '%s' has been confirmed. " +
                    "Reservation date: %s at %s. " +
                    "Please return the book by %s at 18:00.",
                    bookTitle,
                    reservationDate,
                    reservation.getTimeSlot(),
                    reservationDate
                );
                break;
                
            case "reservation_cancelled":
                title = "Reservation Cancelled";
                message = String.format(
                    "Your reservation for '%s' (scheduled for %s at %s) has been cancelled.",
                    bookTitle,
                    reservationDate,
                    reservation.getTimeSlot()
                );
                break;
                
            case "warning":
                title = "Late Return Warning";
                message = String.format(
                    "WARNING: You have not returned '%s' on time. " +
                    "If the book is not returned within 24 hours, you will be added to the bookstore blacklist.",
                    bookTitle
                );
                break;
                
            case "returned":
                title = "Book Returned Successfully";
                message = String.format(
                    "Your reserved book '%s' (borrowed on %s) has been successfully returned. Thank you!",
                    bookTitle,
                    reservedDate
                );
                break;
                
            case "picked":
                title = "Book Picked Up";
                message = String.format(
                    "Your reserved book '%s' has been picked up. " +
                    "Please return the book by %s at 18:00.",
                    bookTitle,
                    reservationDate
                );
                break;
                
            default:
                return; // No notification for other statuses
        }
        
        notificationService.createNotification(
            reservation.getUserId(),
            title,
            message
        );
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
