package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.Data;

@Data
public class NotificationRequest {
    private String title;
    private String message;
    private String type;
    private Boolean pinned;
    private String status;
}
