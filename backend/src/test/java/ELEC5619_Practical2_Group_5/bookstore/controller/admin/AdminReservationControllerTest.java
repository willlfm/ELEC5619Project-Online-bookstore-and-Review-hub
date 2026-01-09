package ELEC5619_Practical2_Group_5.bookstore.controller.admin;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.config.TestSecurityConfig;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import ELEC5619_Practical2_Group_5.bookstore.entity.Reservation;
import ELEC5619_Practical2_Group_5.bookstore.repository.BookRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.ReservationRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AdminReservationController.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private Reservation testReservation;
    private Book testBook;
    private ReservationResponse testResponse;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setCoverImageUrl("test-cover.jpg");

        testReservation = new Reservation();
        testReservation.setReservationId(1);
        testReservation.setUserId(1);
        testReservation.setBookId(1);
        testReservation.setReservationDate(LocalDate.now().plusDays(1));
        testReservation.setTimeSlot("10:00-12:00");
        testReservation.setReservedDate(LocalDateTime.now());
        testReservation.setExpiryDate(LocalDate.now().plusDays(1).atTime(18, 0));
        testReservation.setStatus("reserved");
        testReservation.setCreatedAt(LocalDateTime.now());

        testResponse = ReservationResponse.builder()
                .reservationId(1)
                .userId(1)
                .bookId(1)
                .bookTitle("Test Book")
                .bookCoverImageUrl("test-cover.jpg")
                .reservationDate(LocalDate.now().plusDays(1))
                .timeSlot("10:00-12:00")
                .reservedDate(LocalDateTime.now())
                .expiryDate(LocalDate.now().plusDays(1).atTime(18, 0))
                .status("reserved")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllReservations_Success() throws Exception {
        // Given
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationRepository.findAll()).thenReturn(reservations);
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("Test Book"))
                .andExpect(jsonPath("$[0].status").value("reserved"));

        verify(reservationRepository).findAll();
        verify(bookRepository).findById(1);
    }

    @Test
    void getAllReservations_WithStatusFilter() throws Exception {
        // Given
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationRepository.findByStatus("reserved")).thenReturn(reservations);
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", "reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("reserved"));

        verify(reservationRepository).findByStatus("reserved");
        verify(bookRepository).findById(1);
    }

    @Test
    void getAllReservations_EmptyList() throws Exception {
        // Given
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(reservationRepository).findAll();
    }

    @Test
    void updateReservationStatus_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "picked");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.status").value("picked"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
        verify(notificationService).createNotification(eq(1), anyString(), anyString());
    }

    @Test
    void updateReservationStatus_NotFound_ReturnsBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "picked");
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Reservation not found"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository, never()).save(any());
        verify(notificationService, never()).createNotification(anyInt(), anyString(), anyString());
    }

    @Test
    void updateReservationStatus_EmptyStatus_ReturnsBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "");

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status cannot be empty"));

        verify(reservationRepository, never()).findById(anyInt());
    }

    @Test
    void updateReservationStatus_WarningBeforeDueTime_ReturnsBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "warning");
        testReservation.setExpiryDate(LocalDateTime.now().plusHours(2)); // Future expiry
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot set warning status before due time"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservationStatus_WarningAfterDueTime_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "warning");
        testReservation.setExpiryDate(LocalDateTime.now().minusHours(1)); // Past expiry
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("warning"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
        verify(notificationService).createNotification(eq(1), anyString(), anyString());
    }

    @Test
    void updateReservationStatus_ReservedToPicked_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "picked");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("picked"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
        verify(notificationService).createNotification(eq(1), eq("Book Picked Up"), anyString());
    }

    @Test
    void updateReservationStatus_PickedToReturned_Success() throws Exception {
        // Given
        testReservation.setStatus("picked");
        Map<String, String> request = Map.of("status", "returned");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("returned"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
        verify(notificationService).createNotification(eq(1), eq("Book Returned Successfully"), anyString());
    }

    @Test
    void updateReservationStatus_ReservedToCancelled_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "reservation_cancelled");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("reservation_cancelled"));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
        verify(notificationService).createNotification(eq(1), eq("Reservation Cancelled"), anyString());
    }

    @Test
    void updateReservationStatus_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        Map<String, String> request = Map.of("status", "picked");
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error updating status")));

        verify(reservationRepository).findById(1);
        verify(reservationRepository).save(testReservation);
    }

    @Test
    void updateReservationStatus_InvalidJson_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(reservationRepository, never()).findById(anyInt());
    }

    @Test
    void updateReservationStatus_WarningStatusSet_AutoCancelsReservedOrders() throws Exception {
        // Given - User has warning status and some reserved orders
        Reservation warningReservation = new Reservation();
        warningReservation.setReservationId(1);
        warningReservation.setUserId(1);
        warningReservation.setBookId(1);
        warningReservation.setStatus("picked");
        warningReservation.setReservationDate(LocalDate.now());
        warningReservation.setTimeSlot("08:00-10:00");
        warningReservation.setReservedDate(LocalDateTime.now().minusDays(7));
        warningReservation.setExpiryDate(LocalDateTime.now().minusDays(1));
        warningReservation.setCreatedAt(LocalDateTime.now().minusDays(7));

        Reservation reservedOrder1 = new Reservation();
        reservedOrder1.setReservationId(2);
        reservedOrder1.setUserId(1);
        reservedOrder1.setBookId(2);
        reservedOrder1.setStatus("reserved");
        reservedOrder1.setReservationDate(LocalDate.now().plusDays(1));
        reservedOrder1.setTimeSlot("10:00-12:00");
        reservedOrder1.setReservedDate(LocalDateTime.now());
        reservedOrder1.setExpiryDate(LocalDateTime.now().plusDays(1));
        reservedOrder1.setCreatedAt(LocalDateTime.now());

        Reservation reservedOrder2 = new Reservation();
        reservedOrder2.setReservationId(3);
        reservedOrder2.setUserId(1);
        reservedOrder2.setBookId(3);
        reservedOrder2.setStatus("reserved");
        reservedOrder2.setReservationDate(LocalDate.now().plusDays(2));
        reservedOrder2.setTimeSlot("14:00-16:00");
        reservedOrder2.setReservedDate(LocalDateTime.now());
        reservedOrder2.setExpiryDate(LocalDateTime.now().plusDays(2));
        reservedOrder2.setCreatedAt(LocalDateTime.now());

        Map<String, String> request = Map.of("status", "warning");
        
        when(reservationRepository.findById(1)).thenReturn(Optional.of(warningReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(warningReservation);
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(Arrays.asList(reservedOrder2, reservedOrder1, warningReservation));

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify that reserved orders were auto-cancelled
        verify(reservationRepository).findByUserIdOrderByCreatedAtDesc(1);
        verify(reservationRepository, atLeast(3)).save(any(Reservation.class));
        verify(notificationService, atLeast(3)).createNotification(anyInt(), anyString(), anyString());
    }

    @Test
    void updateReservationStatus_WarningToReturned_ClearsWarningStatus() throws Exception {
        // Given - User returns a warning status book
        Reservation warningReservation = new Reservation();
        warningReservation.setReservationId(1);
        warningReservation.setUserId(1);
        warningReservation.setBookId(1);
        warningReservation.setStatus("warning");
        warningReservation.setReservationDate(LocalDate.now().minusDays(7));
        warningReservation.setTimeSlot("08:00-10:00");
        warningReservation.setReservedDate(LocalDateTime.now().minusDays(7));
        warningReservation.setExpiryDate(LocalDateTime.now().minusDays(1));
        warningReservation.setCreatedAt(LocalDateTime.now().minusDays(7));

        Map<String, String> request = Map.of("status", "returned");
        
        when(reservationRepository.findById(1)).thenReturn(Optional.of(warningReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(warningReservation);
        when(reservationRepository.findByUserIdAndStatus(1, "warning")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(put("/api/admin/reservations/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reservationRepository).findByUserIdAndStatus(1, "warning");
        verify(notificationService).createNotification(eq(1), anyString(), anyString());
    }

    @Test
    void getAllReservations_WithStatusFilter_ReturnsFilteredReservations() throws Exception {
        // Given
        Reservation pickedReservation = new Reservation();
        pickedReservation.setReservationId(1);
        pickedReservation.setUserId(1);
        pickedReservation.setBookId(1);
        pickedReservation.setReservationDate(LocalDate.now().plusDays(1));
        pickedReservation.setTimeSlot("10:00-12:00");
        pickedReservation.setReservedDate(LocalDateTime.now());
        pickedReservation.setExpiryDate(LocalDate.now().plusDays(1).atTime(18, 0));
        pickedReservation.setStatus("picked");
        pickedReservation.setCreatedAt(LocalDateTime.now());
        
        when(reservationRepository.findByStatus("picked")).thenReturn(Arrays.asList(pickedReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", "picked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].status").value("picked"));

        verify(reservationRepository).findByStatus("picked");
        verify(reservationRepository, never()).findAll();
    }

    @Test
    void getAllReservations_WithoutStatusFilter_ReturnsAllReservations() throws Exception {
        // Given
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1));

        verify(reservationRepository).findAll();
        verify(reservationRepository, never()).findByStatus(anyString());
    }

    @Test
    void getAllReservations_WithEmptyStatusFilter_ReturnsAllReservations() throws Exception {
        // Given
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(testReservation));
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1));

        verify(reservationRepository).findAll();
        verify(reservationRepository, never()).findByStatus(anyString());
    }
}
