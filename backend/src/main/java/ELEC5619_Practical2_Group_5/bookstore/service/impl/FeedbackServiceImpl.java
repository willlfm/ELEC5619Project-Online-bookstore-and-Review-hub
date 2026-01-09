package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Feedback;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.FeedbackRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Override
    public Integer createFeedback(String username, String description) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Feedback feedback = Feedback.builder()
                .userId(user.getUserId())
                .description(description)
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();
        feedback = feedbackRepository.save(feedback);
        return feedback.getFeedbackId();
    }

    @Override
    public Page<FeedbackResponse> listFeedbacks(int page, int size, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<Feedback> source = (status == null || status.isBlank())
                ? feedbackRepository.findAll(pageable)
                : feedbackRepository.findByStatusIgnoreCase(status, pageable);

        return source.map(item -> {
            String username = userRepository.findUsernameByUserId(item.getUserId())
                    .orElse("User #" + item.getUserId());
            return FeedbackResponse.builder()
                    .feedbackId(item.getFeedbackId())
                    .userId(item.getUserId())
                    .username(username)
                    .description(item.getDescription())
                    .status(item.getStatus())
                    .createdAt(item.getCreatedAt())
                    .build();
        });
    }

    @Override
    public void deleteFeedback(Integer id) {
        feedbackRepository.deleteById(id);
    }

    @Override
    public void resolveFeedback(Integer id) {
        Feedback fb = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + id));
        fb.setStatus("resolved");
        fb.setRespondedAt(LocalDateTime.now());
        feedbackRepository.save(fb);
    }
}