package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Reservation entity
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    /**
     * Find active reservations by user ID
     */
    List<Reservation> findByUserIdAndStatus(Integer userId, String status);

    /**
     * Find active reservations by user ID and book ID
     */
    List<Reservation> findByUserIdAndBookIdAndStatus(Integer userId, Integer bookId, String status);

    /**
     * Count active reservations by user ID (excluding cancelled and returned reservations)
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status != 'reservation_cancelled' AND r.status != 'returned'")
    long countActiveReservationsByUserId(@Param("userId") Integer userId);

    /**
     * Find reservations by date and time slot (for checking availability)
     */
    List<Reservation> findByReservationDateAndTimeSlotAndStatus(LocalDate date, String timeSlot, String status);

    /**
     * Find reservations by book ID and date
     */
    List<Reservation> findByBookIdAndReservationDateAndStatus(Integer bookId, LocalDate date, String status);

    /**
     * Find all reservations by status
     */
    List<Reservation> findByStatus(String status);

    /**
     * Find all reservations by user ID ordered by created date
     */
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Integer userId);

    /**
     * Count reservations by user ID and status
     */
    long countByUserIdAndStatus(Integer userId, String status);

    /**
     * Find reserved (active) reservations by book ID, date and time slot
     */
    @Query("SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.reservationDate = :date AND r.timeSlot = :timeSlot AND r.status = 'reserved'")
    List<Reservation> findActiveReservationsByBookIdAndDateAndTimeSlot(
            @Param("bookId") Integer bookId,
            @Param("date") LocalDate date,
            @Param("timeSlot") String timeSlot
    );
}

