import { apiClient } from "./apiConfig";

const toQueryParams = (params = {}) =>
  Object.fromEntries(
    Object.entries(params).filter(
      ([, value]) => value !== undefined && value !== null && value !== ""
    )
  );

export const fetchDashboardSummary = async () => {
  const { data } = await apiClient.get("/api/admin/dashboard");
  return data;
};

export const fetchAdminNotifications = async () => {
  const { data } = await apiClient.get("/api/admin/notifications");
  return data;
};

export const createAdminNotification = async (payload) => {
  const { data } = await apiClient.post("/api/admin/notifications", payload);
  return data;
};

export const updateAdminNotification = async (id, payload) => {
  const { data } = await apiClient.put(`/api/admin/notifications/${id}`, payload);
  return data;
};

export const deleteAdminNotification = async (id) => {
  await apiClient.delete(`/api/admin/notifications/${id}`);
};

export const fetchAdminUsers = async ({ page = 0, size = 10, keyword } = {}) => {
  const { data } = await apiClient.get("/api/admin/users", {
    params: toQueryParams({ page, size, keyword }),
  });
  return data;
};

export const createAdminUser = async (payload) => {
  const { data } = await apiClient.post("/api/admin/users", payload);
  return data;
};

export const updateAdminUser = async (id, payload) => {
  const { data } = await apiClient.put(`/api/admin/users/${id}`, payload);
  return data;
};

export const deleteAdminUser = async (id) => {
  await apiClient.delete(`/api/admin/users/${id}`);
};

export const fetchAdminOrders = async ({ page = 0, size = 10, status } = {}) => {
  const { data } = await apiClient.get("/api/admin/orders", {
    params: toQueryParams({ page, size, status }),
  });
  return data;
};

export const updateAdminOrder = async (id, payload) => {
  const { data } = await apiClient.put(`/api/admin/orders/${id}`, payload);
  return data;
};

export const fetchAdminReviews = async ({ page = 0, size = 10, bookId, userId, rating } = {}) => {
  const { data } = await apiClient.get("/api/admin/reviews", {
    params: toQueryParams({ page, size, bookId, userId, rating }),
  });
  return data;
};

export const updateAdminReview = async (id, payload) => {
  const { data } = await apiClient.put(`/api/admin/reviews/${id}`, payload);
  return data;
};

export const deleteAdminReview = async (id) => {
  await apiClient.delete(`/api/admin/reviews/${id}`);
};
