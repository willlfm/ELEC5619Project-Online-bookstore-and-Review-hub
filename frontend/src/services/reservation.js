import { apiClient } from './apiConfig';

/**
 * Create a new reservation
 */
export const createReservation = async (reservationData) => {
  const { data } = await apiClient.post("/api/reservations", reservationData);
  return data;
};

/**
 * Get user's active reservations
 */
export const getUserReservations = async () => {
  const { data } = await apiClient.get("/api/reservations");
  return data;
};

/**
 * Cancel a reservation
 */
export const cancelReservation = async (reservationId) => {
  await apiClient.delete(`/api/reservations/${reservationId}`);
};

/**
 * Check if user can reserve a book
 */
export const canReserveBook = async (bookId) => {
  const { data } = await apiClient.get(`/api/reservations/can-reserve/${bookId}`);
  return data;
};

/**
 * Get available time slots for a specific date
 */
export const getAvailableTimeSlots = async (date, bookId) => {
  const { data } = await apiClient.get("/api/reservations/available-slots", {
    params: { date, bookId }
  });
  return data;
};

/**
 * Get available dates (next 7 days)
 */
export const getAvailableDates = async () => {
  const { data } = await apiClient.get("/api/reservations/available-dates");
  return data;
};

