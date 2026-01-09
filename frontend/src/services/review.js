import { apiClient } from "./apiConfig";

/**
 * Get all reviews for a specific book
 * @param {number|string} bookId
 * @returns {Promise<Array|null>}
 */
export const getReviewsByBook = async (bookId) => {
    try {
        const response = await apiClient.get(`/api/reviews/book/${bookId}`);
        // data sorted by createdAt at back-end
        return response.data;
    } catch (error) {
        console.error(`Error fetching reviews for book ${bookId}:`, error);
        return null;
    }
};

/**
 * Add a new review
 * @param {object} reviewData - { bookId, rating, comment }
 * @returns {Promise<object|null>}
 */
export const addReview = async (reviewData) => {
    try {
        // backend get userId from Token, front-end doesn't need to send
        const response = await apiClient.post("/api/reviews", reviewData);
        return response.data;
    } catch (error) {
        console.error("Error adding review:", error);
        throw error;
    }
};