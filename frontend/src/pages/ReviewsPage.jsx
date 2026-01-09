import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import TopRatedBooks from "../components/Review/TopRatedBooks";
import RecentReviews from "../components/Review/RecentReviews";
import {
    searchBooksByTitle,
    searchBooksByAuthor,
    getBooksByCategory,
    getAllBooks,
} from "../services/books";
import "./ReviewsPage.css";
import CartIcon from "../components/Cart/CartIcon";

const ReviewsPage = () => {
    // --- State Hooks ---
    const [books, setBooks] = useState([]); // Stores the list of books (from search or all books)
    const [searchType, setSearchType] = useState("title"); // Stores the current search filter (title, author, category)
    const [query, setQuery] = useState(""); // Stores the user's search input
    const [page, setPage] = useState(0); // Stores the current page number for pagination
    const [totalPages, setTotalPages] = useState(0); // Stores the total number of pages from the API response
    const [hovered, setHovered] = useState(false); // State for button hover effect

    const navigate = useNavigate();

    // --- Event Handlers ---

    /**
     * Navigates to the detailed book page when a book is clicked.
     * @param {string} bookId - The ID of the book to navigate to.
     */
    const handleBookClick = (bookId) => {
        navigate(`/book/${bookId}`);
    };

    /**
     * Performs a search based on the current query and searchType.
     * Resets if the query is empty.
     */
    const handleSearch = async () => {
        if (!query.trim()) return; // Do nothing if query is just whitespace
        let result = null;

        // Call the appropriate API service based on search type
        switch (searchType) {
            case "title":
                result = await searchBooksByTitle(query, page);
                break;
            case "author":
                result = await searchBooksByAuthor(query, page);
                break;
            case "category":
                result = await getBooksByCategory(query, page);
                break;
            default:
                return; // Should not happen
        }
        // Update state with search results
        setBooks(result?.content || []);
        setTotalPages(result?.totalPages || 0);
    };

    /**
     * Fetches the initial list of all books (paginated).
     */
    const fetchAllBooks = async () => {
        const result = await getAllBooks(page);
        setBooks(result?.content || []);
        setTotalPages(result?.totalPages || 0);
    };

    // --- Effects ---

    /**
     * Effect hook to trigger search or fetchAllBooks.
     * It runs when 'query' or 'page' changes.
     * Includes a 500ms debounce to prevent excessive API calls on typing.
     */
    useEffect(() => {
        const timeout = setTimeout(() => {
            if (query.trim()) {
                handleSearch(); // Perform search if there is a query
            } else {
                fetchAllBooks(); // Fetch all books if query is empty
            }
        }, 500); // 500ms debounce

        // Cleanup function to clear the timeout if component unmounts or dependencies change
        return () => clearTimeout(timeout);
    }, [query, page]); // Dependencies

    // --- Render ---
    return (
        <div className="container">
            {/* Main Title (Unchanged) */}
            <h1 className="title">
                <Link to="/" className="title-link">
                    Welcome to the Bookstore Reviews!
                </Link>
            </h1>

            {/* Cart Icon (Unchanged) */}
            <div style={{ position: 'absolute', top: '0', right: '0' }}>
                <CartIcon />
            </div>

            {/* Search Bar (Unchanged) */}
            <div className="search-card">
                <select
                    value={searchType}
                    onChange={(e) => setSearchType(e.target.value)}
                    className="select"
                >
                    <option value="title">Search by Title</option>
                    <option value="author">Search by Author</option>
                    <option value="category">Search by Category</option>
                </select>

                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Enter keyword"
                    className="input"
                />

                <button
                    className={`button ${hovered ? "button-hover" : ""}`}
                    onClick={() => {
                        setPage(0); // Reset to first page on new search
                        handleSearch();
                    }}
                    onMouseEnter={() => setHovered(true)}
                    onMouseLeave={() => setHovered(false)}
                >
                    Search
                </button>
            </div>

            {/* Conditional Content: Search Results or Default View */}
            {query.trim() && (
                // Display search results if there is a query
                <ul className="book-list">
                    {books.map((book) => (
                        <li
                            key={book.bookId || book.id}
                            className="book-list-item"
                            onClick={() => handleBookClick(book.bookId || book.id)}
                            style={{ cursor: 'pointer' }}
                        >
                            <div className="book-info">
                                <span className="book-title">{book.title}</span>
                                <span className="book-rating">Rating: {book.rating || "N/A"}</span>
                            </div>
                            <img
                                src={book.coverImageUrl}
                                alt={book.title}
                                className="book-list-cover"
                            />
                        </li>
                    ))}
                </ul>
            )}

            {!query.trim() && (
                // Display default content (Top Rated & Recent) if no query
                <div className="reviews-content">
                    <TopRatedBooks />
                    <RecentReviews />
                </div>
            )}

            {/* Pagination Controls (Only show if query exists and totalPages > 1) */}
            {query.trim() && totalPages > 1 && (
                <div className="pagination">
                    <button disabled={page <= 0} onClick={() => setPage(page - 1)}>
                        Previous
                    </button>
                    <span>{page + 1} / {totalPages}</span>
                    <button
                        disabled={page >= totalPages - 1}
                        onClick={() => setPage(page + 1)}
                    >
                        Next
                    </button>
                </div>
            )}

        </div>
    );
};

export default ReviewsPage;