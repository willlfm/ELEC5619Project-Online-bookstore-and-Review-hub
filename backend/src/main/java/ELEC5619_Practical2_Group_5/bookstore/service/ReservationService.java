package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for reservation management
 */
public interface ReservationService {

    /**
     * Create a new reservation
     */
    ReservationResponse createReservation(Integer userId, ReservationRequest request);

    /**
     * Get user's active reservations
     */
    List<ReservationResponse> getUserReservations(Integer userId);

    /**
     * Cancel a reservation
     */
    void cancelReservation(Integer reservationId, Integer userId);

    /**
     * Check if user can reserve a book
     */
    boolean canReserveBook(Integer userId, Integer bookId);

    /**
     * Get available time slots for a specific date
     */
    List<String> getAvailableTimeSlots(LocalDate date, Integer bookId);

    /**
     * Get available dates (next 7 days)
     */
    List<LocalDate> getAvailableDates();
}

