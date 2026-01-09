package ELEC5619_Practical2_Group_5.bookstore.dto.feedback;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Integer feedbackId;
    private Integer userId;
    private String username;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}