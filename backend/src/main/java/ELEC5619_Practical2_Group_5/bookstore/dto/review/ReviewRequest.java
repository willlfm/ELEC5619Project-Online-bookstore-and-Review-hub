package ELEC5619_Practical2_Group_5.bookstore.dto.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long bookId;
    private Integer rating;
    private String comment;
}