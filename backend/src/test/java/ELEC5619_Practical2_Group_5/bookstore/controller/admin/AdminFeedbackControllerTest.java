package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.feedback.FeedbackResponse;
import ELEC5619_Practical2_Group_5.bookstore.service.FeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminFeedbackController.class)
class AdminFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void list_DefaultParams() throws Exception {
        FeedbackResponse feedback = FeedbackResponse.builder()
                .feedbackId(1)
                .userId(1)
                .username("testuser")
                .description("Test feedback")
                .status("open")
                .createdAt(LocalDateTime.now())
                .build();
        
        Page<FeedbackResponse> page = new PageImpl<>(List.of(feedback));
        when(feedbackService.listFeedbacks(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].feedbackId").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    void list_WithStatusFilter() throws Exception {
        Page<FeedbackResponse> page = new PageImpl<>(List.of());
        when(feedbackService.listFeedbacks(0, 10, "resolved")).thenReturn(page);

        mockMvc.perform(get("/api/admin/feedback")
                .param("status", "resolved"))
                .andExpect(status().isOk());
        
        verify(feedbackService).listFeedbacks(0, 10, "resolved");
    }

    @Test
    void delete_Success() throws Exception {
        doNothing().when(feedbackService).deleteFeedback(1);

        mockMvc.perform(delete("/api/admin/feedback/1"))
                .andExpect(status().isNoContent());
        
        verify(feedbackService).deleteFeedback(1);
    }

    @Test
    void resolve_Success() throws Exception {
        doNothing().when(feedbackService).resolveFeedback(1);

        mockMvc.perform(put("/api/admin/feedback/1/resolve"))
                .andExpect(status().isOk());
        
        verify(feedbackService).resolveFeedback(1);
    }
}