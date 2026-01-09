package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private FeedbackController feedbackController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackController).build();
    }

    @Test
    void submitFeedback_Unauthorized_WhenPrincipalNull() throws Exception {
        String json = "{\"description\":\"Hello\"}";

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submitFeedback_BadRequest_WhenDescriptionMissing() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/feedback")
                        .principal(() -> "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Description required"));
    }

    @Test
    void submitFeedback_BadRequest_WhenDescriptionBlank() throws Exception {
        String json = "{\"description\":\"   \\t\\n\"}";

        mockMvc.perform(post("/api/feedback")
                        .principal(() -> "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Description required"));
    }

    @Test
    void submitFeedback_Success_ReturnsFeedbackId_AndTrimsDescription() throws Exception {
        String json = "{\"description\":\"  Hello world  \"}";

        when(feedbackService.createFeedback(eq("testuser"), eq("Hello world"))).thenReturn(123);

        mockMvc.perform(post("/api/feedback")
                        .principal(() -> "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.feedbackId").value(123));

        verify(feedbackService).createFeedback(eq("testuser"), eq("Hello world"));
    }

    @Test
    void submitFeedback_BadRequest_WhenRequestIsNull() {
        var response = feedbackController.submitFeedback(null, (java.security.Principal) () -> "testuser");
        org.junit.jupiter.api.Assertions.assertEquals(400, response.getStatusCodeValue());
    }
}