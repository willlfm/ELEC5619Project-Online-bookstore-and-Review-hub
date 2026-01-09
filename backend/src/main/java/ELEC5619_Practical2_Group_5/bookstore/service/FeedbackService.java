package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackResponse;
import org.springframework.data.domain.Page;

public interface FeedbackService {
    Integer createFeedback(String username, String description);
    Page<FeedbackResponse> listFeedbacks(int page, int size, String status);
    void deleteFeedback(Integer id);
    void resolveFeedback(Integer id);
}