package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Feedback;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.FeedbackRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @Test
    void createFeedback_Success() {
        User user = User.builder().userId(1).username("testuser").build();
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        Integer result = feedbackService.createFeedback("testuser", "Test feedback");

        assertEquals(1, result);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    void createFeedback_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
                () -> feedbackService.createFeedback("nonexistent", "Test feedback"));
    }

    @Test
    void listFeedbacks_WithoutStatus() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findAll(any(PageRequest.class))).thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("testuser"));

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(0, 10, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void listFeedbacks_WithStatus() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("resolved")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findByStatusIgnoreCase("resolved", PageRequest.of(0, 10)))
                .thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("testuser"));

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(0, 10, "resolved");

        assertEquals(1, result.getTotalElements());
        assertEquals("resolved", result.getContent().get(0).getStatus());
    }

    @Test
    void deleteFeedback_Success() {
        doNothing().when(feedbackRepository).deleteById(1);

        feedbackService.deleteFeedback(1);

        verify(feedbackRepository).deleteById(1);
    }

    @Test
    void resolveFeedback_Success() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(1)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        feedbackService.resolveFeedback(1);

        verify(feedbackRepository).save(argThat(fb -> "resolved".equals(fb.getStatus())));
    }

    @Test
    void resolveFeedback_NotFound() {
        when(feedbackRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> feedbackService.resolveFeedback(1));
    }

    @Test
    void listFeedbacks_WithBlankStatus() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findAll(any(PageRequest.class))).thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("testuser"));

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(0, 10, "   ");

        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void listFeedbacks_UserNotFound() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findAll(any(PageRequest.class))).thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.empty());

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(0, 10, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("User #1", result.getContent().get(0).getUsername());
    }

    @Test
    void listFeedbacks_NegativePageAndSize() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findAll(any(PageRequest.class))).thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.of("testuser"));

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(-1, -1, null);

        assertEquals(1, result.getTotalElements());
        verify(feedbackRepository).findAll(PageRequest.of(0, 1));
    }

    @Test
    void listFeedbacks_WithStatusUserNotFound() {
        Feedback feedback = Feedback.builder()
                .feedbackId(1)
                .userId(1)
                .description("Test feedback")
                .status("resolved")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Feedback> feedbackPage = new PageImpl<>(List.of(feedback));
        when(feedbackRepository.findByStatusIgnoreCase("resolved", PageRequest.of(0, 10)))
                .thenReturn(feedbackPage);
        when(userRepository.findUsernameByUserId(1)).thenReturn(Optional.empty());

        Page<FeedbackResponse> result = feedbackService.listFeedbacks(0, 10, "resolved");

        assertEquals(1, result.getTotalElements());
        assertEquals("User #1", result.getContent().get(0).getUsername());
    }
}