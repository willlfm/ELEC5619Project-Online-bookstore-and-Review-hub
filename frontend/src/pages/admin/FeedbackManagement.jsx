import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchAdminFeedbacks, resolveAdminFeedback } from "../../services/feedback";
import "./ReservationManagement.css";

export default function FeedbackManagement() {
  const [items, setItems] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 1 });
  const [statusFilter, setStatusFilter] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageInfo.page, statusFilter]);

  const load = async () => {
    try {
      const data = await fetchAdminFeedbacks({ page: pageInfo.page, size: pageInfo.size, status: statusFilter || undefined });
      setItems(data.content ?? []);
      setPageInfo({ page: data.number ?? 0, size: data.size ?? pageInfo.size, totalPages: data.totalPages ?? 1 });
    } catch (err) {
      setMessage("Failed to load feedback");
    }
  };

  const handleResolve = async (id) => {
    if (!window.confirm("Mark this feedback as resolved?")) return;
    try {
      await resolveAdminFeedback(id);
      setItems((prev) => prev.map((it) => it.feedbackId === id ? { ...it, status: "resolved" } : it));
      setMessage("Feedback resolved");
    } catch (err) {
      setMessage("Failed to resolve");
    }
  };

  return (
    <div className="reservation-management" data-testid="feedback-management">
      <header>
        <div>
          <h1>Feedback Tracking and Notification</h1>
          <p>View and manage user feedback and issues (resolve supported).</p>
        </div>
        <div className="header-actions">
          <div className="filters">
            <label>
              Status Filter
              <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                <option value="">All</option>
                <option value="open">open</option>
                <option value="resolved">resolved</option>
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

      {message && <div className="flash-message">{message}</div>}

      <section className="reservation-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>User</th>
              <th>Content</th>
              <th>Status</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 && (
              <tr>
                <td colSpan={6} className="empty">No feedback</td>
              </tr>
            )}
            {items.map((item) => (
              <tr key={item.feedbackId}>
                <td>#{item.feedbackId}</td>
                <td><strong>{item.username || `User #${item.userId}`}</strong></td>
                <td style={{ maxWidth: "520px" }}>{item.description}</td>
                <td><span className={`status status-${item.status}`}>{item.status}</span></td>
                <td>{item.createdAt ? new Date(item.createdAt).toLocaleString() : "—"}</td>
                <td>
                  <button onClick={() => handleResolve(item.feedbackId)} disabled={item.status === "resolved"}>Resolve</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="pagination" style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 12 }}>
          <button disabled={pageInfo.page <= 0} onClick={() => setPageInfo((prev) => ({ ...prev, page: Math.max(prev.page - 1, 0) }))}>Previous</button>
          <span>{pageInfo.page + 1} / {pageInfo.totalPages}</span>
          <button disabled={pageInfo.page + 1 >= pageInfo.totalPages} onClick={() => setPageInfo((prev) => ({ ...prev, page: Math.min(prev.page + 1, prev.totalPages - 1) }))}>Next</button>
        </div>
      </section>
    </div>
  );
}
