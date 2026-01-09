import React, { useEffect, useState } from "react";
import { getTopRatedBooks } from "../../services/books";
// We don't need to import the CSS here if ReviewsPage.js already imports it.

const TopRatedBooks = () => {
    const [books, setBooks] = useState([]);

    useEffect(() => {
        getTopRatedBooks().then(setBooks);
    }, []);

    return (
        /* * MODIFIED: Replaced Tailwind classes with 'content-panel'
         * This class is defined in ReviewsPage.css for styling
         * and height alignment.
         */
        <div className="content-panel">
            {/* * MODIFIED: Replaced Tailwind classes with 'section-title'
             * This ensures the title matches "Latest Comments"
             * (font, size, and bottom border).
             */
            }
            <h2 className="section-title">Highest Rated Books</h2>

            {/* * ADDED: 'content-scroll-wrapper'
             * This div handles internal scrolling if the
             * content is taller than the panel.
             */
            }
            <div className="content-scroll-wrapper">
                {/* * The 'review-list' class might be for reviews,
                 * but we can reuse it if it just removes list styles,
                 * or create a new 'book-list-panel' if needed.
                 * Let's assume 'review-list' is fine for now.
                 */}
                <ul className="review-list">
                    {books.map((book, idx) => (
                        /* * The styles for 'book-item' are already
                         * defined in ReviewsPage.css to be a card.
                         */
                        <li key={book.id} className="book-item">
                            {book.coverImageUrl && <img src={book.coverImageUrl} alt={book.title} />}
                            <div className="book-info">
                                {/* * The CSS file styles 'book-title' and 'book-rating'
                                 * inside a 'book-item'
                                 */}
                                <span className="book-title">{idx + 1}. {book.title}</span>
                                <span className="book-rating">{book.averageRating.toFixed(1)} ★</span>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>

        </div>
    );
};

export default TopRatedBooks;