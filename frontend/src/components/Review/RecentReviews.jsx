import React, { useEffect, useState } from "react";
import { getRecentReviews } from "../../services/books";
import "../../pages/ReviewsPage.css";

const RecentReviews = () => {
    const [reviews, setReviews] = useState([]);

    useEffect(() => {
        getRecentReviews().then(setReviews);
    }, []);

    return (
        /* * MODIFIED: Replaced 'reviews-section' with 'content-panel'
         * This is CRITICAL for matching the height of the
         * TopRatedBooks panel.
         */
        <div className="content-panel">
            {/* * This was already correct! 'section-title'
             * provides the correct font and border.
             */
            }
            <h2 className="section-title">Latest Comments</h2>

            {/* * ADDED: 'content-scroll-wrapper'
             * This div handles internal scrolling if the
             * content is taller than the panel.
             */
            }
            <div className="content-scroll-wrapper">
                <ul className="review-list">
                    {reviews.map((review) => (
                        <li key={review.reviewId} className="review-item">
                            <div className="review-header">
                                {/* Your data fields are used here */}
                                <span className="review-book">{review.bookName}</span>
                                <span className="review-rating">{review.rating} ★</span>
                            </div>
                            <p className="review-comment">{review.comment}</p>
                            <p className="review-author">Reviewer: {review.reviewName}</p>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default RecentReviews;