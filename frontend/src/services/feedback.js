import { apiClient } from "./apiConfig";

const toQueryParams = (params = {}) =>
  Object.fromEntries(
    Object.entries(params).filter(
      ([, value]) => value !== undefined && value !== null && value !== ""
    )
  );

export const submitFeedback = async (description) => {
  const { data } = await apiClient.post("/api/feedback", { description });
  return data;
};

export const fetchAdminFeedbacks = async ({ page = 0, size = 10, status } = {}) => {
  const { data } = await apiClient.get("/api/admin/feedback", {
    params: toQueryParams({ page, size, status }),
  });
  return data;
};

export const deleteAdminFeedback = async (id) => {
  await apiClient.delete(`/api/admin/feedback/${id}`);
};

export const resolveAdminFeedback = async (id) => {
  await apiClient.put(`/api/admin/feedback/${id}/resolve`);
};