import React, { useState } from "react";
import { addReview } from "../../services/review";
import "./AddReviewForm.css";

const AddReviewForm = ({ bookId, onReviewAdded, onCancel }) => {
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (comment.trim() === "") {
            setError("Comment cannot be empty.");
            return;
        }
        setError(null);
        setSubmitting(true);
        try {
            const newReview = await addReview({
                bookId,
                rating,
                comment,
            });
            onReviewAdded(newReview);
        } catch (err) {
            setError("Failed to submit review. Please try again.");
            console.error(err);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="review-form">
            <h4 className="review-form-title">Write your review</h4>
            {error && <p className="review-form-error">{error}</p>}
            <div className="review-form-group">
                <label>Rating:</label>
                <div className="star-rating">
                    {[5, 4, 3, 2, 1].map((star) => (
                        <React.Fragment key={star}>
                            <input
                                type="radio"
                                id={`star${star}`}
                                name="rating"
                                value={star}
                                checked={rating === star}
                                onChange={() => setRating(star)}
                            />
                            <label htmlFor={`star${star}`}>★</label>
                        </React.Fragment>
                    ))}
                </div>
            </div>
            <div className="review-form-group">
                <label htmlFor="comment">Comment:</label>
                <textarea
                    id="comment"
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    placeholder="What did you think of the book?"
                    rows="4"
                    required
                />
            </div>
            <div className="review-form-actions">
                <button
                    type="submit"
                    className="review-submit-button"
                    disabled={submitting}
                >
                    {submitting ? "Submitting..." : "Submit Review"}
                </button>
                <button
                    type="button"
                    className="review-cancel-button"
                    onClick={onCancel}
                >
                    Cancel
                </button>
            </div>
        </form>
    );
};

export default AddReviewForm;