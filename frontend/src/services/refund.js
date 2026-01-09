import { apiClient } from "./apiConfig";

/**
 * Approve refund for a specific order (admin action).
 * POST /api/admin/orders/{orderId}/refund/approve
 * @param {number} orderId - local order id
 * @returns {Promise<boolean>} true on success, false on failure
 */
export const approveRefund = async (orderId) => {
    try {
        const response = await apiClient.post(`/api/admin/orders/${orderId}/refund/approve`);
        // Controller returns 200 OK with empty body; treat 2xx as success
        return response.status >= 200 && response.status < 300;
    } catch (error) {
        console.error(`Error approving refund for order ${orderId}:`, error);
        return false;
    }
};

/**
 * Reject refund for a specific order (admin action).
 * POST /api/admin/orders/{orderId}/refund/reject
 * @param {number} orderId - local order id
 * @returns {Promise<boolean>} true on success, false on failure
 */
export const rejectRefund = async (orderId) => {
    try {
        const response = await apiClient.post(`/api/admin/orders/${orderId}/refund/reject`);
        return response.status >= 200 && response.status < 300;
    } catch (error) {
        console.error(`Error rejecting refund for order ${orderId}:`, error);
        return false;
    }
};

export const getRefundReason = async (orderId) => {
    try {
        const response = await apiClient.get(`/api/refund/reason/${orderId}`);
        return response.data.reason;
    } catch (error) {
        console.error(`Error getting refund reason for order ${orderId}:`, error);
        return false;
    }
};