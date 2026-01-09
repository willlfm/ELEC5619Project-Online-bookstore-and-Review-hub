import { apiClient } from './apiConfig';

/**
 * Get all notifications for current user
 */
export const getUserNotifications = async () => {
  const response = await apiClient.get('/api/notifications');
  return response.data;
};

/**
 * Get unread notifications for current user
 */
export const getUnreadNotifications = async () => {
  const response = await apiClient.get('/api/notifications/unread');
  return response.data;
};

/**
 * Mark a notification as read
 */
export const markNotificationAsRead = async (notificationId) => {
  await apiClient.put(`/api/notifications/${notificationId}/read`);
};

/**
 * Mark all notifications as read
 */
export const markAllNotificationsAsRead = async () => {
  await apiClient.put('/api/notifications/read-all');
};

