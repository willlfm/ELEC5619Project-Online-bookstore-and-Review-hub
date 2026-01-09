import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchAdminOrders, updateAdminOrder } from "../../services/admin";
import "./OrderManagement.css";
// MODIFIED: Import getRefundReason service function
import { approveRefund, rejectRefund, getRefundReason } from "../../services/refund.js";

// MODIFICATION 1: Add 'refunding' to the list of status options
const STATUS_OPTIONS = [
  "pending",
  "processing",
  "shipped",
  "completed",
  "cancelled",
  "refunding", // <-- ADDED REFUNDING STATUS HERE
];

export default function OrderManagement() {
  const [orders, setOrders] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 1 });
  const [statusFilter, setStatusFilter] = useState("");
  const [message, setMessage] = useState("");
  // State to store the refund reason and control the modal
  const [refundReason, setRefundReason] = useState(null);
  const navigate = useNavigate();

  // Separate effect for handling filter changes to reset page
  useEffect(() => {
    // When statusFilter changes, reset page to 0.
    // This will trigger the main load effect below.
    if (pageInfo.page !== 0) {
      setPageInfo((prev) => ({ ...prev, page: 0 }));
    } else {
      // If already on page 0, manually load orders to ensure load occurs
      loadOrders({ page: 0, status: statusFilter });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  useEffect(() => {
    // This is the main load logic, triggered by page change
    loadOrders({ page: pageInfo.page, status: statusFilter });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageInfo.page]);

  const loadOrders = async ({ page, status }) => {
    // Helper function to reload current page orders
    try {
      // NOTE: statusFilter is used directly in loadOrders
      const data = await fetchAdminOrders({ page, size: pageInfo.size, status: status || undefined });
      setOrders(data.content ?? []);
      setPageInfo({ page: data.number ?? 0, size: data.size ?? pageInfo.size, totalPages: data.totalPages ?? 1 });
    } catch (error) {
      setMessage(`Failed to load orders: ${error.message}`);
    }
  };

  const handleStatusChange = async (orderId, nextStatus) => {
    // Handles changing general order status
    try {
      const updated = await updateAdminOrder(orderId, { status: nextStatus });
      setOrders((prev) => prev.map((order) => (order.orderId === orderId ? updated : order)));
      setMessage(`Order #${orderId} updated to ${nextStatus}`);
    } catch (error) {
      setMessage(`Error updating order #${orderId}: ${error.message}`);
    }
  };

  const handleApproveRefund = async (orderId) => {
    // Handles refund approval action
    try {
      await approveRefund(orderId);
      setMessage(`Successfully approved refund for order #${orderId}.`);
      // Reload orders to reflect the status change (e.g., to 'refunded')
      await loadOrders({ page: pageInfo.page, status: statusFilter });
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error';
      setMessage(`Failed to approve refund for order #${orderId}: ${errorMessage}`);
    }
  };

  const handleRejectRefund = async (orderId) => {
    // Handles refund rejection action
    try {
      await rejectRefund(orderId);
      setMessage(`Successfully rejected refund for order #${orderId}.`);
      // Reload orders to reflect any status change
      await loadOrders({ page: pageInfo.page, status: statusFilter });
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error';
      setMessage(`Failed to reject refund for order #${orderId}: ${errorMessage}`);
    }
  };

  // MODIFIED HANDLER: Function to display the refund reason by fetching it
  const handleShowRefundReason = async (orderId) => {
    try {
      // Fetch the reason from the backend using the new service function
      const reason = await getRefundReason(orderId);

      // Set state to show the modal
      setRefundReason({ orderId, reason });

    } catch (error) {
      // Handle error during fetch (e.g., 404 not found if no refund record exists)
      const errorMessage = error.response?.data?.message || error.message || 'Failed to fetch refund reason.';
      setMessage(`Error fetching reason for order #${orderId}: ${errorMessage}`);
    }
  };

  // Function to close the alert box/modal
  const handleCloseReason = () => {
    setRefundReason(null);
  };


  return (
      <div className="order-management" data-testid="order-management">
      <header>
        <div>
          <h1>Order Management</h1>
          <p>Check fulfilment pipelines, update delivery progress, and keep customers informed.</p>
        </div>
        <div className="header-actions">
          <div className="filters">
            <label>
              Status filter
              <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="">All statuses</option>
                {STATUS_OPTIONS.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                ))}
              </select>
            </label>
          </div>
          <button
            type="button"
            className="admin-back-button"
            onClick={() => navigate("/admin")}
          >
            Back to Administration Board
          </button>
        </div>
      </header>

        {/* Message box for success/error alerts */}
        {message && <div className="flash-message">{message}</div>}

        {/* Modal to display the refund reason */}
        {refundReason && (
            <div className="refund-reason-modal">
              <div className="modal-content">
                <h2>Refund Request for Order #{refundReason.orderId}</h2>
                <p>{refundReason.reason}</p>
                <button onClick={handleCloseReason} className="close-btn">
                  Close
                </button>
              </div>
            </div>
        )}

        <section className="order-table">
          <table>
            <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Status</th>
              <th>Total</th>
              <th>Shipping</th>
              <th>Items</th>
              {/* Refund Info Column Header */}
              <th>Refund Info</th>
              <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            {orders.length === 0 && (
                <tr>
                  {/* Updated COLSPAN to 8 */}
                  <td colSpan={8} className="empty">
                    No orders found.
                  </td>
                </tr>
            )}
            {orders.map((order) => (
                <tr key={order.orderId}>
                  <td>
                    <strong>#{order.orderId}</strong>
                    <span className="muted">Placed: {order.orderDate ? new Date(order.orderDate).toLocaleString() : "—"}</span>
                  </td>
                  <td>
                    <strong>{order.username || order.shippingName || "Unknown"}</strong>
                    <span className="muted">User ID: {order.userId}</span>
                  </td>
                  <td>
                    <span className={`status status-${order.status}`}>{order.status}</span>
                  </td>
                  <td>${order.totalAmount?.toFixed ? order.totalAmount.toFixed(2) : order.totalAmount}</td>
                  <td className="shipping">
                    <span>{order.shippingAddress || "—"}</span>
                    <span>{order.shippingCity} {order.shippingPostcode}</span>
                    <span>{order.shippingCountry}</span>
                  </td>
                  <td className="items">
                    {order.items?.map((item) => (
                        <span key={item.orderItemId}>
                      {item.bookTitle} ({item.format}) ×{item.quantity}
                    </span>
                    ))}
                  </td>

                  {/* Refund Info Column */}
                  <td className="refund-info">
                    <button
                        onClick={() => handleShowRefundReason(order.orderId)}
                        // Button is only enabled when status is 'refunding'
                        disabled={order.status !== 'refunding'}
                        className="reason-btn"
                    >
                      Show Reason
                    </button>
                  </td>

                  <td className="actions">
                    <select
                        value={order.status}
                        onChange={(e) => handleStatusChange(order.orderId, e.target.value)}
                    >
                      {/* This select must also include the new status */}
                      {STATUS_OPTIONS.map((option) => (
                          <option key={option} value={option}>
                            {option}
                          </option>
                      ))}
                    </select>

                    <div className="refund-buttons">
                      <button
                          onClick={() => handleApproveRefund(order.orderId)}
                          className="approve-btn"
                          // Button is only enabled when status is 'refunding'
                          disabled={order.status !== 'refunding'}
                      >
                        Approve Refund
                      </button>
                      <button
                          onClick={() => handleRejectRefund(order.orderId)}
                          className="reject-btn"
                          // Button is only enabled when status is 'refunding'
                          disabled={order.status !== 'refunding'}
                      >
                        Reject Refund
                      </button>
                    </div>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>

          <div className="pagination">
            <button
                type="button"
                onClick={() => setPageInfo((prev) => ({ ...prev, page: Math.max(prev.page - 1, 0) }))}
                disabled={pageInfo.page === 0}
            >
              Previous
            </button>
            <span>
            Page {pageInfo.page + 1} of {pageInfo.totalPages}
          </span>
            <button
                type="button"
                onClick={() =>
                    setPageInfo((prev) => ({ ...prev, page: Math.min(prev.page + 1, prev.totalPages - 1) }))
                }
                disabled={pageInfo.page + 1 >= pageInfo.totalPages}
            >
              Next
            </button>
          </div>
        </section>
      </div>
  );
}
