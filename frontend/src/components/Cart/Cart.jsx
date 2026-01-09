import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { formatPrice } from '../../services/cart';
import './Cart.css';

const Cart = () => {
    const {
        user,
        loading,
        items,
        totalItems,
        totalPrice,
        updateQuantity,
        removeFromCart,
        clearCart,
        refreshCart,
    } = useCart();

    const [isClearing, setIsClearing] = useState(false);
    const navigate = useNavigate();

    // load cart in first loading
    useEffect(() => {
        if (user) {
            refreshCart();
        }
    }, [user, refreshCart]);

    if (loading) return <div>Loading...</div>;
    if (!user) return <div>Please <a href="/signin">sign in</a> to view your cart.</div>;

    const handleQuantityChange = async (bookId, format, newQuantity) => {
        const quantity = parseInt(newQuantity);
        if (quantity > 0 && quantity <= 99) {
            await updateQuantity(bookId, quantity, format);
            await refreshCart(); // refresh after number changed
        }
    };

    const handleRemoveItem = async (bookId, format, title) => {
        if (window.confirm(`Are you sure you want to remove "${title}" from your cart?`)) {
            await removeFromCart(bookId, format);
            await refreshCart(); // refresh after deletion
        }
    };

    const handleClearCart = async () => {
        if (window.confirm('Are you sure you want to clear your cart? This action cannot be undone.')) {
            setIsClearing(true);
            try {
                await clearCart();
                await refreshCart(); // refresh after clearing
            } finally {
                setIsClearing(false);
            }
        }
    };

    if (items.length === 0) {
        return (
            <div className="cart-container">
                <div className="cart-header">
                    <h1>Shopping Cart</h1>
                </div>
                <div className="empty-cart">
                    <div className="empty-cart-icon">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M3 3H5L5.4 5M7 13H17L21 5H5.4M7 13L5.4 5M7 13L4.7 15.3C4.3 15.7 4.6 16.5 5.1 16.5H17M17 13V16.5M9 19.5C9.8 19.5 10.5 20.2 10.5 21S9.8 22.5 9 22.5 7.5 21.8 7.5 21 8.2 19.5 9 19.5ZM20 19.5C20.8 19.5 21.5 20.2 21.5 21S20.8 22.5 20 22.5 18.5 21.8 18.5 21 19.2 19.5 20 19.5Z"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    </div>
                    <h2>Your cart is empty</h2>
                    <p>No items have been added to your cart yet</p>
                    <Link to="/" className="continue-shopping-btn">Continue Shopping</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="cart-container">
            <div className="cart-header">
                <h1>Shopping Cart</h1>
                <div className="cart-summary">
                    <span className="item-count">{totalItems} items</span>
                    <button className="clear-cart-btn" onClick={handleClearCart} disabled={isClearing}>
                        {isClearing ? 'Clearing...' : 'Clear Cart'}
                    </button>
                </div>
            </div>

            <div className="cart-content">
                <div className="cart-items">
                    {items.map((item) => (
                        <div key={`${item.bookId}-${item.format}`} className="cart-item">
                            <div className="item-image">
                                <img
                                    src={item.coverImageUrl || '/placeholder-book.png'}
                                    alt={item.title}
                                    onError={(e) => { e.target.src = '/placeholder-book.png'; }}
                                />
                            </div>

                            <div className="item-details">
                                <Link to={`/book/${item.bookId}`} className="item-title">{item.title}</Link>
                                <p className="item-author">Author: {item.author}</p>
                                <p className="item-publisher">Publisher: {item.publisher}</p>
                                <p className="item-format" style={{ color: '#000' }}>{item.format}</p>
                                <p className="item-price">{formatPrice(item.price)}</p>
                            </div>

                            <div className="item-quantity">
                                <label htmlFor={`quantity-${item.bookId}-${item.format}`}>Quantity:</label>
                                <div className="quantity-controls">
                                    <button
                                        className="quantity-btn"
                                        onClick={() => handleQuantityChange(item.bookId, item.format, item.quantity - 1)}
                                        disabled={item.quantity <= 1}
                                    >-</button>
                                    <input
                                        id={`quantity-${item.bookId}-${item.format}`}
                                        type="number"
                                        min="1"
                                        max="99"
                                        value={item.quantity}
                                        onChange={(e) => handleQuantityChange(item.bookId, item.format, e.target.value)}
                                        className="quantity-input"
                                    />
                                    <button
                                        className="quantity-btn"
                                        onClick={() => handleQuantityChange(item.bookId, item.format, item.quantity + 1)}
                                        disabled={item.quantity >= 99}
                                    >+</button>
                                </div>
                            </div>

                            <div className="item-subtotal">
                                <p className="subtotal-price">{formatPrice(item.price * item.quantity)}</p>
                            </div>

                            <div className="item-actions">
                                <button
                                    className="remove-btn"
                                    onClick={() => handleRemoveItem(item.bookId, item.format, item.title)}
                                    aria-label={`Remove ${item.title}`}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path
                                            d="M3 6H5H21M8 6V4C8 3.4 8.4 3 9 3H15C15.6 3 16 3.4 16 4V6M19 6V20C19 20.6 18.6 21 18 21H6C5.4 21 5 20.6 5 20V6H19ZM10 11V17M14 11V17"
                                            stroke="currentColor"
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                        />
                                    </svg>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="cart-sidebar">
                    <div className="order-summary">
                        <h3>Order Summary</h3>
                        <div className="summary-row">
                            <span>Subtotal:</span>
                            <span>{formatPrice(totalPrice)}</span>
                        </div>
                        <div className="summary-row">
                            <span>Shipping:</span>
                            <span>Free</span>
                        </div>
                        <div className="summary-row total">
                            <span>Total:</span>
                            <span>{formatPrice(totalPrice)}</span>
                        </div>

                        <button className="checkout-btn" onClick={() => navigate("/checkout")}>
                            Checkout ({totalItems} items)
                        </button>

                        <Link to="/" className="continue-shopping-link">Continue Shopping</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Cart;
