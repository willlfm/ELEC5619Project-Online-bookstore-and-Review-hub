import React from "react";
import "./ReviewList.css";

const ReviewList = ({ reviews, isLoading }) => {

    const formatDateTime = (dateTimeString) => {
        if (!dateTimeString) return "";
        return new Date(dateTimeString).toLocaleString();
    };

    if (isLoading) {
        return <div className="review-list-container"><p>Loading reviews...</p></div>;
    }

    return (
        <div className="review-list-container">
            <h3>Customer Reviews</h3>
            {(!reviews || reviews.length === 0) ? (
                <p className="reviews-empty">No reviews yet. Be the first!</p>
            ) : (
                <ul className="review-list">
                    {reviews.map((review) => (
                        <li key={review.reviewId} className="review-list-item">
                            <div className="review-list-header">
                                <span className="review-author-name">
                                    {review.reviewName || "Unknown User"}
                                </span>
                                <span className="review-list-rating">
                                    {"★".repeat(review.rating)}
                                    {"☆".repeat(5 - review.rating)}
                                </span>
                            </div>
                            <p className="review-list-comment">{review.comment}</p>
                            <span className="review-list-date">
                                {formatDateTime(review.createdAt)}
                            </span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default ReviewList;