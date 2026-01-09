package ELEC5619_Practical2_Group_5.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for reservation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Integer reservationId;
    private Integer userId;
    private Integer bookId;
    private String bookTitle;
    private String bookCoverImageUrl;
    private LocalDate reservationDate;
    private String timeSlot;
    private LocalDateTime reservedDate;
    private LocalDateTime expiryDate;
    private String status;
    private LocalDateTime createdAt;
}

