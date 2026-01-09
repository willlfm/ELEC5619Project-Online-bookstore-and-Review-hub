import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { getBookById } from "../../services/books";
import { useCart } from "../../contexts/CartContext";
import { canReserveBook } from "../../services/reservation";
import "./BookDetail.css";

import { getReviewsByBook } from "../../services/review";
import ReviewList from "../Review/ReviewList";
import AddReviewForm from "../Review/AddReviewForm";

const BookDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    // get user state from useCart
    const { user, addToCart } = useCart();
    const [book, setBook] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedFormat, setSelectedFormat] = useState("");
    const [canReserve, setCanReserve] = useState(false);

    const [reviews, setReviews] = useState([]);
    const [reviewLoading, setReviewLoading] = useState(true);
    const [showReviewForm, setShowReviewForm] = useState(false);

    const handleBack = () => {
        if (location.state?.from) {
            navigate(location.state.from);
        } else {
            navigate(-1);
        }
    };

    const handlePrevBook = () => {
        const { allBookIds, globalIndex } = location.state || {};
        if (allBookIds && globalIndex > 0) {
            const prevBookId = allBookIds[globalIndex - 1];
            navigate(`/book/${prevBookId}`, {
                state: {
                    ...location.state,
                    globalIndex: globalIndex - 1
                },
                replace: true
            });
            window.scrollTo(0, 0);
        }
    };

    const handleNextBook = () => {
        const { allBookIds, globalIndex } = location.state || {};
        if (allBookIds && globalIndex < allBookIds.length - 1) {
            const nextBookId = allBookIds[globalIndex + 1];
            navigate(`/book/${nextBookId}`, {
                state: {
                    ...location.state,
                    globalIndex: globalIndex + 1
                },
                replace: true
            });
            window.scrollTo(0, 0);
        }
    };

    const hasPrevBook = location.state?.allBookIds && location.state?.globalIndex > 0;
    const hasNextBook = location.state?.allBookIds && location.state?.globalIndex < location.state.allBookIds.length - 1;

    useEffect(() => {
        const fetchBookAndReviews = async () => {
            if (!id) return;

            // reset state in case seeing old data when switching
            setLoading(true);
            setReviewLoading(true);
            setError(null);
            setBook(null);
            setReviews([]);
            setShowReviewForm(false);

            // fetch book details
            try {
                const bookData = await getBookById(id);
                if (bookData) {
                    setBook(bookData);
                    if (bookData.formats && bookData.formats.length > 0) {
                        setSelectedFormat(bookData.formats[0].format);
                    }
                    // check reservation
                    try {
                        const reserveStatus = await canReserveBook(id);
                        setCanReserve(reserveStatus);
                    } catch (err) {
                        console.error("Error checking reservation status:", err);
                        setCanReserve(false);
                    }
                } else {
                    setError("Book not found");
                }
            } catch (err) {
                setError("Failed to load book details");
                console.error("Error fetching book:", err);
            } finally {
                setLoading(false);
            }

            // fetch comment(load not sync book)
            try {
                const reviewsData = await getReviewsByBook(id);
                setReviews(reviewsData || []);
            } catch (err) {
                console.error("Failed to fetch reviews:", err);
            } finally {
                setReviewLoading(false);
            }
        };

        fetchBookAndReviews();
    }, [id]);

    // function after posting comment-
    const handleReviewAdded = (newReview) => {
        // add new comment to the top
        setReviews([newReview, ...reviews]);
        setShowReviewForm(false); // 隐藏表单

        // update average score of the book
        if (book && book.rating !== undefined) {
            const currentTotalRating = book.rating * reviews.length;
            const newAvgRating = (currentTotalRating + newReview.rating) / (reviews.length + 1);
            setBook({ ...book, rating: newAvgRating.toFixed(2) });
        } else if (book) {
            setBook({ ...book, rating: newReview.rating.toFixed(2) });
        }
    };

    // post comment button
    // button banned when not signed in
    const handleWriteReviewClick = () => {
        setShowReviewForm(true);
    };

    const handleAddToCart = () => {
        if (!book || !selectedFormat) return;

        // fetch price basing on chosen format
        const formatObj = book.formats.find(f => f.format === selectedFormat);
        const price = formatObj ? formatObj.price : 0;

        // Check stock
        if (formatObj && formatObj.stockQuantity === 0) {
            alert("Sorry, this item is out of stock.");
            return;
        }

        const cartItem = {
            bookId: book.bookId || id,
            title: book.title,
            author: book.author,
            coverImageUrl: book.coverImageUrl,
            format: selectedFormat,
            price,
            quantity: 1
        };

        try {
            addToCart(cartItem);
            alert(`Added "${book.title}" (${selectedFormat}) to cart!`);
        } catch (error) {
            console.error("Error adding to cart:", error);
            alert("Failed to add item to cart. Please try again.");
        }
    };

    const handleReserve = () => {
        // Check if paperback is available
        const paperback = book.formats.find(f => f.format === 'paperback');
        if (!paperback) {
            alert("Only physical books can be reserved.");
            return;
        }
        if (paperback.stockQuantity === 0) {
            alert("Sorry, this book is out of stock and cannot be reserved.");
            return;
        }

        // Navigate to reservation page with book ID
        navigate(`/reserve/${id}`);
    };

    // Helper function to check if current format has stock
    const getCurrentFormatStock = () => {
        if (!book || !selectedFormat) return 0;
        const formatObj = book.formats.find(f => f.format === selectedFormat);
        return formatObj ? formatObj.stockQuantity : 0;
    };

    // Helper function to check if current selected format is paperback and its stock
    const shouldShowReserveButton = () => {
        if (!book || !selectedFormat) return false;
        // Only show reserve button when paperback is selected
        if (selectedFormat !== 'paperback') return false;

        const paperback = book.formats.find(f => f.format === 'paperback');
        return !!paperback; // Return true if paperback exists
    };

    // Helper function to check if selected paperback has stock
    const getSelectedPaperbackStock = () => {
        if (!book || selectedFormat !== 'paperback') return 0;
        const paperback = book.formats.find(f => f.format === 'paperback');
        return paperback ? paperback.stockQuantity : 0;
    };

    if (loading) {
        return (
            <div className="book-detail-container">
                <div className="loading">Loading book details...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="book-detail-container">
                <div className="error">{error}</div>
                <button onClick={handleBack} className="back-button">
                    Go Back
                </button>
            </div>
        );
    }

    if (!book) {
        return (
            <div className="book-detail-container">
                <div className="error">Book not found</div>
                <button onClick={handleBack} className="back-button">
                    Go Back
                </button>
            </div>
        );
    }

    return (
        <div className="book-detail-container">
            <div className="book-detail-header">
                <button onClick={handleBack} className="back-button">
                    ← Back
                </button>
                <h1>Book Details</h1>
            </div>

            {/* Navigation arrows */}
            {hasPrevBook && (
                <button className="nav-arrow nav-arrow-left" onClick={handlePrevBook} title="Previous Book">
                    ‹
                </button>
            )}
            {hasNextBook && (
                <button className="nav-arrow nav-arrow-right" onClick={handleNextBook} title="Next Book">
                    ›
                </button>
            )}

            <div className="book-detail-content">
                <div className="book-cover-section">
                    <img
                        src={`/${book.coverImageUrl}`}
                        alt={book.title}
                        className="book-cover-large"
                    />

                    <ReviewList reviews={reviews} isLoading={reviewLoading} />
                </div>

                <div className="book-info-section">
                    <h2 className="book-title">{book.title}</h2>
                    <p className="book-author">by {book.author}</p>
                    <p className="book-category">Category: {book.category}</p>

                    {(book.rating !== undefined && book.rating !== null) && (
                        <div className="book-rating">
                            <span className="rating-value">★ {book.rating}</span>
                            <span className="rating-text">Average Rating</span>
                        </div>
                    )}

                    <div className="book-price-section">
                        <div className="format-selection">
                            <label>Format:</label>
                            <select
                                value={selectedFormat}
                                onChange={(e) => setSelectedFormat(e.target.value)}
                                className="format-select"
                            >
                                {book.formats && book.formats.length > 0 ? (
                                    book.formats.map(f => (
                                        <option key={f.format} value={f.format}>
                                            {f.format.charAt(0).toUpperCase() + f.format.slice(1)} - ${f.price.toFixed(2)}
                                        </option>
                                    ))
                                ) : (
                                    <option value="">No formats available</option>
                                )}
                            </select>
                        </div>

                        <div className="button-group">
                            <button
                                onClick={handleAddToCart}
                                className="add-to-cart-button"
                                disabled={getCurrentFormatStock() === 0}
                            >
                                Add to Cart {getCurrentFormatStock() === 0 && <span className="out-of-stock-label">(Out of Stock)</span>}
                            </button>

                            {shouldShowReserveButton() && (
                                <button
                                    onClick={handleReserve}
                                    className="reserve-button"
                                    disabled={getSelectedPaperbackStock() === 0}
                                >
                                    Reserve {getSelectedPaperbackStock() === 0 && <span className="out-of-stock-label">(Out of Stock)</span>}
                                </button>
                            )}
                        </div>
                    </div>

                    {book.description && (
                        <div className="book-description">
                            <h3>Description</h3>
                            <p>{book.description}</p>
                        </div>
                    )}

                    {/* --- (修改) 评论按钮和表单 --- */}
                    <div className="review-form-container">
                        {!showReviewForm ? (
                            <button
                                onClick={handleWriteReviewClick}
                                className="add-to-cart-button"
                                // if !user is true, ban the button
                                disabled={!user}
                            >
                                {user ? "Write a Review" : "please sign to post comments"}
                            </button>
                        ) : (
                            <AddReviewForm
                                bookId={Number(id)}
                                onReviewAdded={handleReviewAdded}
                                onCancel={() => setShowReviewForm(false)}
                            />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BookDetail;