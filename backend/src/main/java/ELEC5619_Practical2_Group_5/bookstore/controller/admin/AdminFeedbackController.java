package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackResponse;
import ELEC5619_Practical2_Group_5.bookstore.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(feedbackService.listFeedbacks(page, size, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Integer id) {
        feedbackService.resolveFeedback(id);
        return ResponseEntity.ok().build();
    }
}