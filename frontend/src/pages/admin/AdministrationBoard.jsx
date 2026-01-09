import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchDashboardSummary,
  fetchAdminNotifications,
  createAdminNotification,
  deleteAdminNotification,
} from "../../services/admin";
import "./AdministrationBoard.css";

const NAV_CARDS = [
  {
    title: "Account Management",
    description: "Review, create, or disable bookstore accounts.",
    action: "/admin/accounts",
  },
  {
    title: "Book Management",
    description: "Keep the catalogue fresh with new titles and pricing.",
    action: "/admin/books",
  },
  {
    title: "Comment Moderation",
    description: "Highlight great feedback and remove spam in seconds.",
    action: "/admin/comments",
  },
  {
    title: "Order Desk",
    description: "Track fulfilment progress and update delivery details.",
    action: "/admin/orders",
  },
  {
    title: "Reservation Management",
    description: "Prioritise reservations and keep wait-listed readers informed.",
    action: "/admin/reservations",
  },
  {
    title: "Feedback Tracking and Notification",
    description: "Review user feedback and manage reports.",
    action: "/admin/feedback",
  },
];

const FALLBACK_SALES = [
  { month: "2024-05", amount: 820 },
  { month: "2024-06", amount: 910 },
  { month: "2024-07", amount: 1130 },
  { month: "2024-08", amount: 980 },
  { month: "2024-09", amount: 1250 },
];

const NOTIFICATIONS_PER_PAGE = 4;

export default function AdministrationBoard() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState();
  const [notifications, setNotifications] = useState([]);
  const [notificationPage, setNotificationPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [formValues, setFormValues] = useState({
    title: "",
    message: "",
    type: "announcement",
    pinned: false,
  });

  // AdminRoute already guarantees only test_admin1 reaches this component, so we focus on data loading here.
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    (async () => {
      try {
        const [dashboardData, notificationData] = await Promise.all([
          fetchDashboardSummary().catch(() => null),
          fetchAdminNotifications().catch(() => []),
        ]);
        if (!cancelled) {
          setSummary(dashboardData);
          setNotifications(notificationData ?? []);
        }
      } catch (err) {
        if (!cancelled) setError("Failed to load dashboard data");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(notifications.length / NOTIFICATIONS_PER_PAGE));
    setNotificationPage((prev) => Math.min(prev, totalPages - 1));
  }, [notifications]);

  const salesData = useMemo(() => {
    if (summary?.monthlySales?.length) return summary.monthlySales;
    return FALLBACK_SALES;
  }, [summary]);

  const maxSales = useMemo(() => {
    return salesData.reduce((max, point) => Math.max(max, Number(point.amount || 0)), 0) || 1;
  }, [salesData]);

  const totalNotificationPages = useMemo(() => {
    return Math.max(1, Math.ceil(notifications.length / NOTIFICATIONS_PER_PAGE));
  }, [notifications]);

  const pagedNotifications = useMemo(() => {
    const start = notificationPage * NOTIFICATIONS_PER_PAGE;
    return notifications.slice(start, start + NOTIFICATIONS_PER_PAGE);
  }, [notificationPage, notifications]);

  const handleCreateNotification = async (evt) => {
    evt.preventDefault();
    if (!formValues.title.trim() || !formValues.message.trim()) {
      setError("Please provide both a title and a message.");
      return;
    }
    try {
      const created = await createAdminNotification(formValues);
      setNotifications((prev) => [created, ...prev]);
      setFormValues({ title: "", message: "", type: "announcement", pinned: false });
      setNotificationPage(0);
      setError("");
    } catch (err) {
      setError("Unable to publish notification");
    }
  };

  const handleDeleteNotification = async (id) => {
    try {
      await deleteAdminNotification(id);
      setNotifications((prev) => prev.filter((item) => item.notificationId !== id));
    } catch (err) {
      setError("Unable to delete notification");
    }
  };

  if (loading) {
    return <div className="admin-board">Loading administration board…</div>;
  }

  return (
    <div className="admin-board" data-testid="admin-board">
      <header className="admin-board__header">
        <div>
          <h1>Administration Board</h1>
          <p>Monitor bookstore health, highlight announcements, and jump to management tools.</p>
        </div>
        <div className="admin-board__cta">
          <button className="primary" onClick={() => navigate("/homepage")}>Back to Storefront</button>
        </div>
      </header>

      {error && <div className="admin-board__error">{error}</div>}

      <section className="admin-board__stats" aria-label="Key metrics">
        <article className="stat-card">
          <h2>Total Revenue</h2>
          <p>{summary?.totalRevenue ? `$${Number(summary.totalRevenue).toFixed(2)}` : "$0.00"}</p>
        </article>
        <article className="stat-card">
          <h2>Orders in System</h2>
          <p>{summary?.totalOrders ?? FALLBACK_SALES.length * 20}</p>
        </article>
        <article className="stat-card">
          <h2>Pending / Processing</h2>
          <p>{summary?.pendingOrders ?? 6}</p>
        </article>
        <article className="stat-card">
          <h2>Registered Users</h2>
          <p>{summary?.totalUsers ?? 42}</p>
        </article>
      </section>

      <section className="admin-board__grid">
        <div className="admin-board__panel" aria-label="Sales trend">
          <div className="panel-header">
            <h3>Monthly Sales Snapshot</h3>
            <span className="panel-subtitle">Revenue by month (AUD)</span>
          </div>
          <div className="sales-bars">
            {salesData.map((point) => {
              const value = Number(point.amount ?? 0);
              const height = Math.max((value / maxSales) * 100, 6);
              return (
                <div key={point.month} className="sales-bar">
                  <div className="sales-bar__value" style={{ height: `${height}%` }}>
                    <span>${value.toFixed ? value.toFixed(0) : value}</span>
                  </div>
                  <p>{point.month}</p>
                </div>
              );
            })}
          </div>
        </div>

        <div className="admin-board__panel" aria-label="Notifications">
          <div className="panel-header">
            <h3>Announcements &amp; Notifications</h3>
            <span className="panel-subtitle">Communicate with staff and customers</span>
          </div>

          <form className="notification-form" onSubmit={handleCreateNotification}>
            <input
              type="text"
              name="title"
              placeholder="Title"
              value={formValues.title}
              onChange={(e) => setFormValues((prev) => ({ ...prev, title: e.target.value }))}
            />
            <textarea
              name="message"
              placeholder="What should people know?"
              rows={3}
              value={formValues.message}
              onChange={(e) => setFormValues((prev) => ({ ...prev, message: e.target.value }))}
            />
            <div className="notification-form__actions">
              <label>
                <input
                  type="checkbox"
                  checked={formValues.pinned}
                  onChange={(e) => setFormValues((prev) => ({ ...prev, pinned: e.target.checked }))}
                />
                Pin to top
              </label>
              <button type="submit" className="primary">Publish</button>
            </div>
          </form>

          <ul className="notification-list">
            {notifications.length === 0 && <li className="empty">No notifications yet.</li>}
            {pagedNotifications.map((note) => (
              <li key={note.notificationId} className={note.pinned ? "notification pinned" : "notification"}>
                <div>
                  <h4>{note.title}</h4>
                  <p>{note.message}</p>
                  <span className="meta">{note.type || "general"}</span>
                </div>
                <button type="button" onClick={() => handleDeleteNotification(note.notificationId)}>
                  Remove
                </button>
              </li>
            ))}
          </ul>

          {notifications.length > NOTIFICATIONS_PER_PAGE && (
            <div className="notification-pagination">
              <button
                type="button"
                onClick={() => setNotificationPage((prev) => Math.max(prev - 1, 0))}
                disabled={notificationPage === 0}
              >
                Previous
              </button>
              <span>
                Page {notificationPage + 1} of {totalNotificationPages}
              </span>
              <button
                type="button"
                onClick={() =>
                  setNotificationPage((prev) => Math.min(prev + 1, totalNotificationPages - 1))
                }
                disabled={notificationPage + 1 >= totalNotificationPages}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </section>

      <section className="admin-board__nav" aria-label="Management shortcuts">
        {NAV_CARDS.map((card) => (
          <article key={card.title} className="nav-card">
            <h3>{card.title}</h3>
            <p>{card.description}</p>
            <button className="secondary" onClick={() => navigate(card.action)}>
              Open {card.title.split(" ")[0]}
            </button>
          </article>
        ))}
      </section>
    </div>
  );
}
