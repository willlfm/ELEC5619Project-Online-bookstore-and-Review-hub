package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminOrderUpdateRequest;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminOrderService;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import ELEC5619_Practical2_Group_5.bookstore.service.PaymentService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminOrderService adminOrderService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtils jwtUtils;

    private AdminOrderDto sampleOrder() {
        return AdminOrderDto.builder()
                .orderId(10)
                .userId(2)
                .status("pending")
                .build();
    }

    @Test
    void listOrders_returnsPage() throws Exception {
        Page<AdminOrderDto> page = new PageImpl<>(List.of(sampleOrder()));
        when(adminOrderService.listOrders(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(10));
    }

    @Test
    void getOrder_returnsDto() throws Exception {
        when(adminOrderService.getOrder(10)).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/admin/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10));
    }

    @Test
    void updateOrder_returnsDto() throws Exception {
        when(adminOrderService.updateOrder(eq(10), any(AdminOrderUpdateRequest.class))).thenReturn(sampleOrder());

        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setStatus("shipped");

        mockMvc.perform(put("/api/admin/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10));
    }

    @Test
    void approveRefund_callsPaymentService() throws Exception {
        mockMvc.perform(post("/api/admin/orders/10/refund/approve"))
                .andExpect(status().isOk());

        verify(paymentService).processRefund(10, true);
    }

    @Test
    void rejectRefund_callsPaymentService() throws Exception {
        mockMvc.perform(post("/api/admin/orders/10/refund/reject"))
                .andExpect(status().isOk());

        verify(paymentService).processRefund(10, false);
    }
}
