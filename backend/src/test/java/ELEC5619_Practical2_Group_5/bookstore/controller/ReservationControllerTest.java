package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.ReservationResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@WithMockUser(username = "testuser")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationRequest testRequest;
    private ReservationResponse testResponse;

    @BeforeEach
    void setUp() {
        // Mock user for authentication
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        testRequest = new ReservationRequest();
        testRequest.setBookId(1);
        testRequest.setReservationDate(LocalDate.now().plusDays(1));
        testRequest.setTimeSlot("10:00-12:00");

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

    private Authentication createMockAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(auth.isAuthenticated()).thenReturn(true);
        return auth;
    }

    @Test
    void createReservation_Success() throws Exception {
        // Given
        when(reservationService.createReservation(1, testRequest)).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.status").value("reserved"));

        verify(reservationService).createReservation(1, testRequest);
    }

    @Test
    void createReservation_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        ReservationRequest invalidRequest = new ReservationRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).createReservation(anyInt(), any());
    }

    @Test
    void createReservation_ServiceException_ReturnsBadRequest() throws Exception {
        // Given
        when(reservationService.createReservation(1, testRequest))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Book not found"));

        verify(reservationService).createReservation(1, testRequest);
    }

    @Test
    void getUserReservations_Success() throws Exception {
        // Given
        List<ReservationResponse> reservations = Arrays.asList(testResponse);
        when(reservationService.getUserReservations(1)).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("Test Book"));

        verify(reservationService).getUserReservations(1);
    }

    @Test
    void getUserReservations_EmptyList() throws Exception {
        // Given
        when(reservationService.getUserReservations(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(reservationService).getUserReservations(1);
    }

    @Test
    void cancelReservation_Success() throws Exception {
        // Given
        doNothing().when(reservationService).cancelReservation(1, 1);

        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation cancelled successfully"));

        verify(reservationService).cancelReservation(1, 1);
    }

    @Test
    void cancelReservation_NotFound_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Reservation not found"))
                .when(reservationService).cancelReservation(1, 1);

        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Reservation not found"));

        verify(reservationService).cancelReservation(1, 1);
    }

    @Test
    void cancelReservation_Unauthorized_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Unauthorized to cancel this reservation"))
                .when(reservationService).cancelReservation(1, 1);

        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unauthorized to cancel this reservation"));

        verify(reservationService).cancelReservation(1, 1);
    }

    @Test
    void canReserveBook_True() throws Exception {
        // Given
        when(reservationService.canReserveBook(1, 1)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/reservations/can-reserve/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canReserve").value(true));

        verify(reservationService).canReserveBook(1, 1);
    }

    @Test
    void canReserveBook_False() throws Exception {
        // Given
        when(reservationService.canReserveBook(1, 1)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/reservations/can-reserve/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canReserve").value(false));

        verify(reservationService).canReserveBook(1, 1);
    }

    @Test
    void getAvailableTimeSlots_Success() throws Exception {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        List<String> timeSlots = Arrays.asList("08:00-10:00", "10:00-12:00", "12:00-14:00");
        when(reservationService.getAvailableTimeSlots(testDate, 1)).thenReturn(timeSlots);

        // When & Then
        mockMvc.perform(get("/api/reservations/available-slots")
                        .param("date", testDate.toString())
                        .param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("08:00-10:00"))
                .andExpect(jsonPath("$[1]").value("10:00-12:00"))
                .andExpect(jsonPath("$[2]").value("12:00-14:00"));

        verify(reservationService).getAvailableTimeSlots(testDate, 1);
    }

    @Test
    void getAvailableTimeSlots_EmptyList() throws Exception {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(reservationService.getAvailableTimeSlots(testDate, 1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/reservations/available-slots")
                        .param("date", testDate.toString())
                        .param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(reservationService).getAvailableTimeSlots(testDate, 1);
    }

    @Test
    void getAvailableDates_Success() throws Exception {
        // Given
        List<LocalDate> dates = Arrays.asList(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );
        when(reservationService.getAvailableDates()).thenReturn(dates);

        // When & Then
        mockMvc.perform(get("/api/reservations/available-dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());

        verify(reservationService).getAvailableDates();
    }

    @Test
    void getAvailableDates_EmptyList() throws Exception {
        // Given
        when(reservationService.getAvailableDates()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/reservations/available-dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(reservationService).getAvailableDates();
    }

    @Test
    void getUserReservations_UserNotFound_ThrowsException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void canReserveBook_UserNotFound_ThrowsException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/reservations/can-reserve/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelReservation_GenericException_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Cannot cancel reservation"))
                .when(reservationService).cancelReservation(999, 1);

        // When & Then
        mockMvc.perform(delete("/api/reservations/999")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot cancel reservation"));

        verify(reservationService).cancelReservation(999, 1);
    }

    @Test
    void createReservation_UserNotFound_ThrowsException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }
}
