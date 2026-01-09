package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        String description = request == null ? null : request.getDescription();
        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Description required"
            ));
        }
        Integer id = feedbackService.createFeedback(principal.getName(), description.trim());
        return ResponseEntity.ok(Map.of("status", "ok", "feedbackId", id));
    }
}