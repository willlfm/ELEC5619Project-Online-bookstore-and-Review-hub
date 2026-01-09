import React, { useState, useEffect } from "react";
import {
    searchBooksByTitle,
    searchBooksByAuthor,
    getBooksByCategory,
    getAllBooks,
} from "../../services/books";
import "./Homepage.css";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import CartIcon from "../Cart/CartIcon";
import NotificationIcon from "../Notification/NotificationIcon";
import ReservationIcon from "../Reservation/ReservationIcon";
// removed FeedbackForm inline usage; use link to /feedback instead


const HomePage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [books, setBooks] = useState([]);
    const [searchType, setSearchType] = useState("title");
    const [query, setQuery] = useState("");
    const [page, setPage] = useState(parseInt(searchParams.get("page") || "0"));
    const [totalPages, setTotalPages] = useState(0);
    const [hovered, setHovered] = useState(false);

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [jumpPage, setJumpPage] = useState("");
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const navigate = useNavigate();

    const handleBookClick = async (bookId) => {
        // Get all book IDs from all pages
        try {
            const allBooksResult = await getAllBooks(0, 1000); // Get up to 1000 books
            const allBookIds = allBooksResult?.content?.map(b => b.bookId) || [];
            const globalIndex = allBookIds.indexOf(bookId);

            navigate(`/book/${bookId}`, {
                state: {
                    from: `/homepage?page=${page}`,
                    allBookIds: allBookIds,
                    globalIndex: globalIndex
                }
            });
        } catch (err) {
            console.error('Failed to get all books:', err);
            // Fallback to current page books
            const bookIds = books.map(b => b.bookId);
            navigate(`/book/${bookId}`, {
                state: {
                    from: `/homepage?page=${page}`,
                    allBookIds: bookIds,
                    globalIndex: bookIds.indexOf(bookId)
                }
            });
        }
    };

    const updatePage = (newPage) => {
        setPage(newPage);
        setSearchParams({ page: newPage.toString() });
    };

    useEffect(() => {
        if (!sidebarOpen) return;
        const onKey = (e) => e.key === "Escape" && setSidebarOpen(false);
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [sidebarOpen]);

    const handleSearch = async () => {
        if (!query.trim()) return;
        let result = null;

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
                return;
        }

        setBooks(result?.content || []);
        setTotalPages(result?.totalPages || 0);
    };

    const fetchAllBooks = async () => {
        const result = await getAllBooks(page);
        setBooks(result?.content || []);
        setTotalPages(result?.totalPages || 0);
    };

    useEffect(() => {
        const timeout = setTimeout(() => {
            if (query.trim()) handleSearch();
            else fetchAllBooks();
        }, 500);

        return () => clearTimeout(timeout);
    }, [query, page]);

    useEffect(() => {
        let cancelled = false;
        (async () => {
            try {
                const res = await fetch("/api/auth/me", { credentials: "include" });
                if (!cancelled) {
                    if (res.ok) {
                        const data = await res.json();
                        setUser(data);
                    } else {
                        setUser(null);
                    }
                }
            } catch {
                if (!cancelled) setUser(null);
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, []);

    const handleLogout = async () => {
        try {
            await fetch("/api/auth/signout", {
                method: "POST",
                credentials: "include",
            });
        } catch {}
        setUser(null);
    };

    const displayName = user?.name || "";
    const initial = displayName ? displayName.trim().charAt(0).toUpperCase() : "";
    const isAdmin = user?.username === "test_admin1";

    return (
        <div className="container">
            <h1 className="title">Bookstore</h1>

            <p className="subtitle-link">
                Check out the latest <Link to="/reviews">reviews and top-rated books</Link>
            </p>

            {isAdmin && (
                <div className="admin-banner">
                    <div>
                        <strong>Administration Board</strong>
                        <p>Manage accounts, inventory, comments, and orders with a single dashboard.</p>
                    </div>
                    <button type="button" onClick={() => navigate("/admin")}>
                        Enter Board
                    </button>
                </div>
            )}

            <div className="topRight">
                {!loading && !user && (
                    <div className="authLinks">
                        <a href="/signin" className="link">Sign in</a>
                        <a href="/signup" className="link">Sign up</a>
                    </div>
                )}

                {!loading && user && (
                    <div className="userBarVertical">
                        <button
                            aria-label="Open user menu"
                            className="avatar clickable"
                            onClick={() => setSidebarOpen(true)}
                        >
                            {initial}
                        </button>
                        <span className="displayName">{displayName}</span>
                        <NotificationIcon />
                        <CartIcon />
                        <ReservationIcon />
                        {/* Feedback button moved to search bar */}
                    </div>
                )}
            </div>

            <aside
                className={`sidebar ${sidebarOpen ? "open" : ""}`}
                role="dialog"
                aria-modal="true"
                aria-label="User menu"
            >
                <div className="sidebar-header">
                    <div aria-label="avatar" className="avatar lg">{initial}</div>
                    <div className="sidebar-name">{displayName}</div>
                    <button
                        className="sidebar-close"
                        onClick={() => setSidebarOpen(false)}
                        aria-label="Close"
                    >
                        ×
                    </button>
                </div>

                <nav className="sidebar-nav">
                    <Link
                        to="/userprofile"
                        className="sidebar-link"
                        onClick={() => setSidebarOpen(false)}
                    >
                        User Profile
                    </Link>

                    <button
                        className="sidebar-link as-button"
                        onClick={() => { setSidebarOpen(false); handleLogout(); }}
                    >
                        Log out
                    </button>

                    <div className="sidebar-divider" />

                    <Link
                        to="/cart"
                        className="sidebar-link"
                        onClick={() => setSidebarOpen(false)}
                    >
                        <CartIcon />
                        <span>Cart</span>
                    </Link>
                </nav>
            </aside>

            <div
                className={`sidebar-overlay ${sidebarOpen ? "show" : ""}`}
                onClick={() => setSidebarOpen(false)}
            />

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
                        updatePage(0);
                        handleSearch();
                    }}
                    onMouseEnter={() => setHovered(true)}
                    onMouseLeave={() => setHovered(false)}
                >
                    Search
                </button>
                <Link to="/feedback" className="button">Feedback</Link>
            </div>

            {!query.trim() && (
                <div className="book-grid">
                    {books.map((book) => (
                        <div
                            key={book.bookId}
                            className="book-card"
                            onClick={() => handleBookClick(book.bookId)}
                            style={{ cursor: 'pointer' }}
                        >
                            <img
                                src={`/${book.coverImageUrl}`}
                                alt={book.title}
                                className="book-cover"
                            />

                        </div>
                    ))}
                </div>
            )}

            {query.trim() && (
                <div className="book-search-grid">
                    {books.map((book) => (
                        <div
                            key={book.bookId}
                            className="book-search-card"
                            onClick={() => handleBookClick(book.bookId)}
                        >
                            <img
                                src={`/${book.coverImageUrl}`}
                                alt={book.title}
                                className="book-search-cover"
                            />

                            <div className="book-search-info">
                                <h3 className="book-search-title">{book.title}</h3>
                                <p className="book-search-detail"><strong>Author:</strong> {book.author}</p>
                                <p className="book-search-detail"><strong>Category:</strong> {book.category}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <div className="pagination">
                <button disabled={page <= 0} onClick={() => updatePage(page - 1)}>
                    Previous
                </button>

                {Array.from({ length: totalPages }, (_, i) => i).map((i) => {
                    if (i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 1) {
                        return (
                            <button
                                key={i}
                                className={i === page ? "active" : ""}
                                onClick={() => updatePage(i)}
                            >
                                {i + 1}
                            </button>
                        );
                    } else if (
                        i === page - 2 ||
                        i === page + 2
                    ) {
                        return <span key={i}>···</span>;
                    } else {
                        return null;
                    }
                })}

                <button
                    disabled={page >= totalPages - 1}
                    onClick={() => updatePage(page + 1)}
                >
                    Next
                </button>

                <input
                    type="number"
                    min={1}
                    max={totalPages}
                    value={jumpPage}
                    onChange={(e) => setJumpPage(e.target.value)}
                    placeholder="page number"
                    className="jump-input"
                    style={{ width: "120px", marginLeft: "8px" }}
                />
                <button
                    onClick={() => {
                        const p = parseInt(jumpPage);
                        if (!isNaN(p) && p >= 1 && p <= totalPages) {
                            updatePage(p - 1);
                            setJumpPage("");
                        }
                    }}
                >
                    Go
                </button>
            </div>

        </div>
    );
};

export default HomePage;