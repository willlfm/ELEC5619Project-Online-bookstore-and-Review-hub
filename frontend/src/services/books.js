import { apiClient } from "./apiConfig";

/**
 * get all book list，return Book + formats
 */
export const getAllBooks = async (page = 0, size = 12) => {
    try {
        const response = await apiClient.get("/api/books", { params: { page, size } });
        return response.data; // 每个 book 对象包含 formats 数组
    } catch (error) {
        console.error("Error fetching all books:", error);
        return null;
    }
};

/**
 * get single book info from bookId including formats
 */
export const getBookById = async (id) => {
    try {
        const response = await apiClient.get(`/api/books/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching book with id ${id}:`, error);
        return null;
    }
};

/**
 * search a book using title
 */
export const searchBooksByTitle = async (keyword, page = 0, size = 10) => {
    try {
        const response = await apiClient.get("/api/books/search/title", {
            params: { keyword, page, size },
        });
        return response.data;
    } catch (error) {
        console.error(`Error searching books by title "${keyword}":`, error);
        return null;
    }
};

/**
 * search a book using author
 */
export const searchBooksByAuthor = async (author, page = 0, size = 10) => {
    try {
        const response = await apiClient.get("/api/books/search/author", {
            params: { author, page, size },
        });
        return response.data;
    } catch (error) {
        console.error(`Error searching books by author "${author}":`, error);
        return null;
    }
};

/**
 * search a book using category
 */
export const getBooksByCategory = async (category, page = 0, size = 10) => {
    try {
        const response = await apiClient.get("/api/books/category", {
            params: { category, page, size },
        });
        return response.data;
    } catch (error) {
        console.error(`Error fetching books by category "${category}":`, error);
        return null;
    }
};

/**
 * add a book including formats
 * bookData: {
 *   isbn, title, author, publisher, publicationDate, edition, language, pages, category, description, coverImageUrl,
 *   formats: [{ format: 'paperback', price, stockQuantity, reservedQuantity }, { format: 'ebook', price, stockQuantity }]
 * }
 */
export const addBook = async (bookData) => {
    try {
        const response = await apiClient.post("/api/books", bookData);
        return response.data;
    } catch (error) {
        console.error("Error adding book:", error);
        return null;
    }
};

/**
 * update a book
 */
export const updateBook = async (id, bookData) => {
    try {
        const response = await apiClient.put(`/api/books/${id}`, bookData);
        return response.data;
    } catch (error) {
        console.error(`Error updating book with id ${id}:`, error);
        return null;
    }
};

/**
 * delete a book
 */
export const deleteBook = async (id) => {
    try {
        await apiClient.delete(`/api/books/${id}`);
        return true;
    } catch (error) {
        console.error(`Error deleting book with id ${id}:`, error);
        return false;
    }
};

/**
 * get the highest rating book
 */
export const getTopRatedBooks = async (limit = 5) => {
    try {
        const response = await apiClient.get("/api/reviews/books/top-rated", { params: { limit } });
        return response.data;
    } catch (error) {
        console.error("Error fetching top-rated books:", error);
        return [];
    }
};

/**
 * fetch recent reviews
 */
export const getRecentReviews = async (limit = 10) => {
    try {
        const response = await apiClient.get("/api/reviews/recent", { params: { limit } });
        return response.data;
    } catch (error) {
        console.error("Error fetching recent reviews:", error);
        return [];
    }
};
