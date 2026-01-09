import React, { createContext, useContext, useReducer, useEffect, useState } from "react";
import { getCurrentUser } from "../services/auth";
import {
    getCart,
    addToCart as addToCartApi,
    updateCartItem as updateCartItemApi,
    removeFromCart as removeFromCartApi,
    clearCart as clearCartApi
} from "../services/cart";

const initialCartState = {
    items: [],
    totalItems: 0,
    totalPrice: 0
};

const CART_ACTIONS = {
    LOAD_CART: "LOAD_CART",
    ADD_ITEM: "ADD_ITEM",
    UPDATE_ITEM: "UPDATE_ITEM",
    REMOVE_ITEM: "REMOVE_ITEM",
    CLEAR_CART: "CLEAR_CART"
};

const cartReducer = (state, action) => {
    switch (action.type) {
        case CART_ACTIONS.LOAD_CART: {
            const payload = action.payload || {};
            return {
                items: Array.isArray(payload.items) ? payload.items : [],
                totalItems: payload.totalItems || 0,
                totalPrice: payload.totalPrice || 0
            };
        }
        case CART_ACTIONS.ADD_ITEM: {
            const { item } = action.payload;
            const existingIndex = state.items.findIndex(
                i => i.bookId === item.bookId && i.format === item.format
            );
            let newItems;
            if (existingIndex >= 0) {
                newItems = state.items.map((i, idx) =>
                    idx === existingIndex ? { ...i, quantity: i.quantity + item.quantity } : i
                );
            } else {
                newItems = [...state.items, item];
            }
            const totalItems = newItems.reduce((sum, i) => sum + i.quantity, 0);
            const totalPrice = newItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
            return { ...state, items: newItems, totalItems, totalPrice };
        }
        case CART_ACTIONS.UPDATE_ITEM: {
            const { bookId, quantity, format } = action.payload;
            const newItems = state.items.map(i =>
                i.bookId === bookId && i.format === format ? { ...i, quantity } : i
            );
            const totalItems = newItems.reduce((sum, i) => sum + i.quantity, 0);
            const totalPrice = newItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
            return { ...state, items: newItems, totalItems, totalPrice };
        }
        case CART_ACTIONS.REMOVE_ITEM: {
            const { bookId, format } = action.payload;
            const newItems = state.items.filter(i => !(i.bookId === bookId && i.format === format));
            const totalItems = newItems.reduce((sum, i) => sum + i.quantity, 0);
            const totalPrice = newItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
            return { ...state, items: newItems, totalItems, totalPrice };
        }
        case CART_ACTIONS.CLEAR_CART:
            return initialCartState;
        default:
            return state;
    }
};

const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [state, dispatch] = useReducer(cartReducer, initialCartState);

    const refreshCart = async (currentUser) => {
        if (!currentUser || !currentUser.id) return;
        try {
            const cartData = await getCart(currentUser.id);
            const items = Array.isArray(cartData.data) ? cartData.data : [];
            const formattedData = {
                items,
                totalItems: items.reduce((sum, i) => sum + i.quantity, 0),
                totalPrice: items.reduce((sum, i) => sum + i.price * i.quantity, 0)
            };
            dispatch({ type: CART_ACTIONS.LOAD_CART, payload: formattedData });
        } catch (err) {
            console.error("Error refreshing cart:", err);
        }
    };

    useEffect(() => {
        let cancelled = false;
        (async () => {
            try {
                const currentUser = await getCurrentUser();
                if (cancelled) return;
                setUser(currentUser);

                if (currentUser) {
                    await refreshCart(currentUser);
                }
            } catch (err) {
                console.error("Error initializing cart:", err);
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();
        return () => { cancelled = true; };
    }, []);

    const addToCart = async (book, quantity = 1) => {
        if (!user || !user.id) return alert("Please log in to add items to cart");
        try {
            // lowercase
            const formatEnum = book.format ? book.format.toLowerCase() : null;
            await addToCartApi(user.id, book.bookId, formatEnum, quantity);
            await refreshCart(user);
        } catch (err) {
            console.error("Error adding to cart:", err);
        }
    };

    const updateQuantity = async (bookId, quantity, format) => {
        if (!user || !user.id) return;
        try {
            const formatEnum = format ? format.toLowerCase() : null;
            await updateCartItemApi(user.id, bookId, quantity, formatEnum);
            await refreshCart(user);
        } catch (err) {
            console.error("Error updating cart item:", err);
        }
    };

    const removeFromCart = async (bookId, format) => {
        if (!user || !user.id) return;
        try {
            const formatEnum = format ? format.toLowerCase() : null;
            await removeFromCartApi(user.id, bookId, formatEnum);
            await refreshCart(user);
        } catch (err) {
            console.error("Error removing from cart:", err);
        }
    };

    const clearCart = async () => {
        if (!user || !user.id) return;
        try {
            await clearCartApi(user.id);
            await refreshCart(user);
        } catch (err) {
            console.error("Error clearing cart:", err);
        }
    };

    const setUserAndRefreshCart = async (currentUser) => {
        if (!currentUser || !currentUser.id) return;
        setUser(currentUser);
        await refreshCart(currentUser);
    };

    const value = {
        user,
        loading,
        items: state.items,
        totalItems: state.totalItems,
        totalPrice: state.totalPrice,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
        refreshCart,
        setUserAndRefreshCart
    };

    return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) throw new Error("useCart must be used within CartProvider");
    return context;
};

export default CartContext;
