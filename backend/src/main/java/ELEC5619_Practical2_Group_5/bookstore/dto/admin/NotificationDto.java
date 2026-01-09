package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Integer notificationId;
    private Integer userId;
    private Integer orderId;
    private String title;
    private String message;
    private String type;
    private boolean pinned;
    
    @JsonProperty("isRead")
    private boolean read;
    
    private String status;
    private LocalDateTime createdAt;
}
