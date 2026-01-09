import { apiRequest } from './apiConfig';

/**
 * get user cart data
 */
export const getCart = async (userId) => {
    try {
        const response = await apiRequest(`/api/cart/${userId}`, { method: 'GET', credentials: 'include' });
        return response;
    } catch (error) {
        console.error('Failed to get cart items:', error);
        throw error;
    }
};

/**
 * add item to cart
 * @param {number} userId
 * @param {number} bookId
 * @param {string} format - paperback / ebook
 * @param {number} quantity
 */
export const addToCart = async (userId, bookId, format, quantity = 1) => {
    try {
        const formatEnum = format ? format.toLowerCase() : null;  // 改为小写
        const query = new URLSearchParams({ bookId, quantity });
        if (formatEnum) query.append('format', formatEnum);

        const response = await apiRequest(
            `/api/cart/${userId}/add?${query.toString()}`,
            { method: 'POST', credentials: 'include' }
        );
        return response;
    } catch (error) {
        console.error('Error adding to cart:', error);
        throw error;
    }
};

export const updateCartItem = async (userId, bookId, quantity, format) => {
    try {
        const formatEnum = format ? format.toLowerCase() : null;  // 改为小写
        const query = new URLSearchParams({ bookId, quantity });
        if (formatEnum) query.append('format', formatEnum);

        const response = await apiRequest(
            `/api/cart/${userId}/update?${query.toString()}`,
            { method: 'POST', credentials: 'include' }
        );
        return response;
    } catch (error) {
        console.error('Error updating cart item:', error);
        throw error;
    }
};

export const removeFromCart = async (userId, bookId, format) => {
    try {
        const query = new URLSearchParams();
        if (bookId != null) query.append('bookId', bookId);
        if (format) query.append('format', format.toLowerCase());  // 改为小写

        const url = `/api/cart/${userId}/remove?${query.toString()}`;

        const response = await apiRequest(url, {
            method: 'DELETE',
            credentials: 'include'
        });
        return response;
    } catch (error) {
        console.error('Error removing from cart:', error);
        throw error;
    }
};

export const clearCart = async (userId) => {
    try {
        const response = await apiRequest(`/api/cart/${userId}/clear`, { method: 'DELETE', credentials: 'include' });
        return response;
    } catch (error) {
        console.error('Error clearing cart:', error);
        throw error;
    }
};

export const getCartItemCount = async () => {
    try {
        const response = await apiRequest('/api/cart/count', { method: 'GET' });
        return response;
    } catch (error) {
        console.error('Error fetching cart item count:', error);
        throw error;
    }
};

/**
 * sync local cart to server
 */
export const syncCartToServer = async (cartItems) => {
    try {
        const response = await apiRequest('/api/cart/sync', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ items: cartItems })
        });
        return response;
    } catch (error) {
        console.error('Error syncing cart to server:', error);
        throw error;
    }
};

/**
 * check storage
 */
export const checkStock = async (bookId, format, quantity) => {
    try {
        const response = await apiRequest('/api/cart/check-stock', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookId, format: format?.toLowerCase(), quantity })  // 改为小写
        });
        return response;
    } catch (error) {
        console.error('Error checking stock:', error);
        throw error;
    }
};

/**
 * calculate the total price of cart items
 */
export const calculateCartTotal = async () => {
    try {
        const response = await apiRequest('/api/cart/calculate-total', { method: 'GET' });
        return response;
    } catch (error) {
        console.error('Error calculating cart total:', error);
        throw error;
    }
};

export const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 2 }).format(price);
};

export const validateCartItem = (item) => {
    return item && typeof item.bookId === 'number' && typeof item.quantity === 'number' &&
        item.quantity > 0 && typeof item.price === 'number' && item.price >= 0;
};

export const calculateItemSubtotal = (item) => {
    if (!validateCartItem(item)) return 0;
    return item.price * item.quantity;
};

export const calculateCartSubtotal = (items) => {
    if (!Array.isArray(items)) return 0;
    return items.reduce((total, item) => total + calculateItemSubtotal(item), 0);
};
