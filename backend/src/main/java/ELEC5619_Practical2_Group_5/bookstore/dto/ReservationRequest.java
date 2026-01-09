package ELEC5619_Practical2_Group_5.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new reservation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    @NotNull(message = "Book ID is required")
    private Integer bookId;
    
    @NotNull(message = "Reservation date is required")
    private LocalDate reservationDate;
    
    @NotNull(message = "Time slot is required")
    private String timeSlot; // e.g., "08:00-10:00"
}

