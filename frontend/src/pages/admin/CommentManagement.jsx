import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchAdminReviews,
  updateAdminReview,
  deleteAdminReview,
} from "../../services/admin";
import "./CommentManagement.css";

const DEFAULT_FILTER = { rating: "" };

export default function CommentManagement() {
  const [reviews, setReviews] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 1 });
  const [filter, setFilter] = useState(DEFAULT_FILTER);
  const [message, setMessage] = useState("");
  const [editing, setEditing] = useState(null);
  const [editForm, setEditForm] = useState({ comment: "", rating: 5 });
  const navigate = useNavigate();

  useEffect(() => {
    loadReviews({ page: pageInfo.page, rating: filter.rating });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageInfo.page, filter.rating]);

  const loadReviews = async ({ page, rating }) => {
    const data = await fetchAdminReviews({ page, size: pageInfo.size, rating: rating || undefined });
    setReviews(data.content ?? []);
    setPageInfo({ page: data.number ?? 0, size: data.size ?? pageInfo.size, totalPages: data.totalPages ?? 1 });
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm("Delete this comment?")) return;
    await deleteAdminReview(reviewId);
    setReviews((prev) => prev.filter((review) => review.reviewId !== reviewId));
    setMessage("Comment removed");
  };

  const handleEdit = (review) => {
    setEditing(review.reviewId);
    setEditForm({ comment: review.comment ?? "", rating: review.rating ?? 5 });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSubmit = async (evt) => {
    evt.preventDefault();
    if (!editing) return;
    const targetReview = reviews.find((item) => item.reviewId === editing);
    const payload = {
      comment: editForm.comment,
      rating: Number(editForm.rating) || 1,
      userId: targetReview?.userId,
      bookId: targetReview?.bookId,
    };
    const updated = await updateAdminReview(editing, payload);
    setReviews((prev) => prev.map((review) => (review.reviewId === editing ? updated : review)));
    setEditing(null);
    setMessage("Comment updated successfully");
  };

  return (
    <div className="comment-management" data-testid="comment-management">
      <header>
        <div>
          <h1>Comment Management</h1>
          <p>Celebrate insightful feedback while keeping your community respectful and on-topic.</p>
        </div>
        <div className="filters">
          <label>
            Rating filter
            <select value={filter.rating} onChange={(e) => setFilter({ rating: e.target.value })}>
              <option value="">All ratings</option>
              {[5, 4, 3, 2, 1].map((value) => (
                <option key={value} value={value}>
                  {value} stars
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
      </header>

      {message && <div className="flash-message">{message}</div>}

      {editing && (
        <section className="comment-editor">
          <h2>Edit comment</h2>
          <form onSubmit={handleSubmit}>
            <label>
              Rating
              <select
                value={editForm.rating}
                onChange={(e) => setEditForm((prev) => ({ ...prev, rating: Number(e.target.value) }))}
              >
                {[1, 2, 3, 4, 5].map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </select>
            </label>
            <label className="textarea">
              Comment
              <textarea
                rows={4}
                value={editForm.comment}
                onChange={(e) => setEditForm((prev) => ({ ...prev, comment: e.target.value }))}
              />
            </label>
            <div className="form-actions">
              <button className="primary" type="submit">
                Save changes
              </button>
              <button type="button" className="link" onClick={() => setEditing(null)}>
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="comment-table">
        <table>
          <thead>
            <tr>
              <th>Reader</th>
              <th>Book</th>
              <th>Rating</th>
              <th>Comment</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reviews.length === 0 && (
              <tr>
                <td colSpan={6} className="empty">
                  No comments found.
                </td>
              </tr>
            )}
            {reviews.map((review) => (
              <tr key={review.reviewId}>
                <td>
                  <strong>{review.reviewName || `User #${review.userId}`}</strong>
                  <span className="muted">ID: {review.userId}</span>
                </td>
                <td>
                  <strong>{review.bookName || `Book #${review.bookId}`}</strong>
                  <span className="muted">ID: {review.bookId}</span>
                </td>
                <td>
                  <span className="rating">{"★".repeat(review.rating ?? 0)}</span>
                </td>
                <td className="comment-cell">{review.comment}</td>
                <td>{review.createdAt ? new Date(review.createdAt).toLocaleDateString() : "—"}</td>
                <td className="actions">
                  <button type="button" onClick={() => handleEdit(review)}>
                    Edit
                  </button>
                  <button type="button" className="danger" onClick={() => handleDelete(review.reviewId)}>
                    Delete
                  </button>
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
