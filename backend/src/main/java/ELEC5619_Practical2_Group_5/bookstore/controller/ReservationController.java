package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for reservation management
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    /**
     * Create a new reservation
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication
    ) {
        try {
            Integer userId = getUserIdFromAuthentication(authentication);
            ReservationResponse response = reservationService.createReservation(userId, request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get user's active reservations
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getUserReservations(Authentication authentication) {
        Integer userId = getUserIdFromAuthentication(authentication);
        List<ReservationResponse> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Cancel a reservation
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Integer reservationId,
            Authentication authentication
    ) {
        try {
            Integer userId = getUserIdFromAuthentication(authentication);
            reservationService.cancelReservation(reservationId, userId);
            return ResponseEntity.ok("Reservation cancelled successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Check if user can reserve a specific book
     */
    @GetMapping("/can-reserve/{bookId}")
    public ResponseEntity<java.util.Map<String, Boolean>> canReserveBook(
            @PathVariable Integer bookId,
            Authentication authentication
    ) {
        Integer userId = getUserIdFromAuthentication(authentication);
        boolean canReserve = reservationService.canReserveBook(userId, bookId);
        return ResponseEntity.ok(java.util.Map.of("canReserve", canReserve));
    }

    /**
     * Get available time slots for a specific date
     */
    @GetMapping("/available-slots")
    public ResponseEntity<List<String>> getAvailableTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer bookId
    ) {
        List<String> slots = reservationService.getAvailableTimeSlots(date, bookId);
        return ResponseEntity.ok(slots);
    }

    /**
     * Get available dates (next 7 days)
     */
    @GetMapping("/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates() {
        List<LocalDate> dates = reservationService.getAvailableDates();
        return ResponseEntity.ok(dates);
    }

    /**
     * Extract user ID from authentication
     */
    private Integer getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return user.getUserId();
    }
}

