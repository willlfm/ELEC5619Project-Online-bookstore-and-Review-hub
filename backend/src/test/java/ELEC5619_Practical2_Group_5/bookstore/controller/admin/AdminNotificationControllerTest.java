package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.NotificationRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminNotificationController.class)
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void listNotifications_Success() throws Exception {
        NotificationDto notification = NotificationDto.builder()
                .notificationId(1)
                .userId(1)
                .title("Test Notification")
                .message("Test message")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(notificationService.listNotifications()).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/admin/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Notification"));
    }

    @Test
    void createNotification_Success() throws Exception {
        NotificationDto notification = NotificationDto.builder()
                .notificationId(1)
                .title("Test Title")
                .message("Test Message")
                .build();
        
        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(notification);

        mockMvc.perform(post("/api/admin/notifications")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Test Title\", \"message\": \"Test Message\", \"type\": \"INFO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void updateNotification_Success() throws Exception {
        NotificationDto notification = NotificationDto.builder()
                .notificationId(1)
                .title("Updated Title")
                .message("Updated Message")
                .build();
        
        when(notificationService.updateNotification(eq(1), any(NotificationRequest.class))).thenReturn(notification);

        mockMvc.perform(put("/api/admin/notifications/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Title\", \"message\": \"Updated Message\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(1);

        mockMvc.perform(delete("/api/admin/notifications/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(notificationService).deleteNotification(1);
    }
}