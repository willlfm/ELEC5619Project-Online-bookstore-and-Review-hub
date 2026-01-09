package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.review.ReviewResponse;
import ELEC5619_Practical2_Group_5.bookstore.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminReviewController.class)
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtUtils jwtUtils;

    private ReviewResponse sampleReview() {
        return ReviewResponse.builder()
                .reviewId(100L)
                .userId(5)
                .bookId(7L)
                .rating(4)
                .comment("Great read")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listReviews_returnsPage() throws Exception {
        Page<ReviewResponse> page = new PageImpl<>(List.of(sampleReview()));
        when(reviewService.listReviews(0, 10, null, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reviewId").value(100));
    }

    @Test
    void updateReview_returnsPayload() throws Exception {
        ReviewResponse response = sampleReview();
        when(reviewService.updateReview(eq(100L), any(ReviewRequest.class))).thenReturn(response);

        ReviewRequest request = new ReviewRequest();
        request.setComment("Updated comment");
        request.setRating(5);

        mockMvc.perform(put("/api/admin/reviews/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(100));
    }

    @Test
    void deleteReview_returnsNoContent() throws Exception {
        doNothing().when(reviewService).deleteReview(100L);

        mockMvc.perform(delete("/api/admin/reviews/100"))
                .andExpect(status().isNoContent());
    }
}
