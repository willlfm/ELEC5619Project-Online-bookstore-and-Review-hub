package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtUtils jwtUtils;

    private AdminUserDto dto;

    @BeforeEach
    void setUp() {
        dto = AdminUserDto.builder()
                .userId(1)
                .username("alice")
                .email("alice@example.com")
                .role("admin")
                .build();
    }

    @Test
    void listUsers_returnsPage() throws Exception {
        Page<AdminUserDto> page = new PageImpl<>(List.of(dto));
        when(adminUserService.listUsers(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    @Test
    void createUser_returnsDto() throws Exception {
        when(adminUserService.createUser(any(AdminUserRequest.class))).thenReturn(dto);

        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("pass");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void updateUser_returnsDto() throws Exception {
        when(adminUserService.updateUser(eq(1), any(AdminUserRequest.class))).thenReturn(dto);

        AdminUserRequest request = new AdminUserRequest();
        request.setEmail("new@example.com");

        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(adminUserService).deleteUser(1);
    }
}
