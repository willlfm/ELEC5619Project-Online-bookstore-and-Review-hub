package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.EbookItemResponse;
import ELEC5619_Practical2_Group_5.bookstore.service.EbookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(EbookController.class)
class EbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EbookService ebookService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "testuser")
    void getMyEbooks_Success() throws Exception {
        EbookItemResponse ebook = EbookItemResponse.builder()
                .orderItemId(1)
                .bookTitle("Test Ebook")
                .author("Test Author")
                .build();

        when(ebookService.getMyEbooks("testuser")).thenReturn(List.of(ebook));

        mockMvc.perform(get("/api/ebooks/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getMyEbooks_EmptyList() throws Exception {
        when(ebookService.getMyEbooks("testuser")).thenReturn(List.of());

        mockMvc.perform(get("/api/ebooks/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void downloadEbook_Success() throws Exception {
        Resource resource = new ByteArrayResource("test content".getBytes());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .body(resource);

        when(ebookService.download("testuser", 1)).thenReturn(response);

        mockMvc.perform(get("/api/ebooks/1/download"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void downloadEbook_NotFound() throws Exception {
        when(ebookService.download("testuser", 999))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/api/ebooks/999/download"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyEbooks_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/ebooks/me"))
                .andExpect(status().isOk());
    }

    @Test
    void downloadEbook_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/ebooks/1/download"))
                .andExpect(status().isOk());
    }
}