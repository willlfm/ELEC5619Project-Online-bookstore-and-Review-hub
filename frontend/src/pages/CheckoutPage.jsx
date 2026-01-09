import React, { useState } from "react";
import { useCart } from "../contexts/CartContext";
import PayPalButton from "../components/Pay/PayPalButton";
import { formatPrice } from "../services/cart";
import { createOrder } from "../services/order.js";
import "./CheckoutPage.css";

const CheckoutPage = () => {
    const { user, items, totalPrice, clearCart } = useCart();
    const [shippingInfo, setShippingInfo] = useState({
        name: "",
        address: "",
        city: "",
        postcode: "",
        country: "",
    });
    const [orderCreated, setOrderCreated] = useState(false);

    const TAX_RATE = 0;
    const taxAmount = totalPrice * TAX_RATE;
    const totalWithTax = totalPrice + taxAmount;

    const isShippingInfoComplete = () =>
        Object.values(shippingInfo).every((value) => value.trim() !== "");

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setShippingInfo({ ...shippingInfo, [name]: value });
    };

    const createLocalOrder = async () => {
        // 创建本地订单
        const response = await fetch("/api/orders/confirm", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                items: items.map((i) => ({
                    bookFormatId: i.bookFormatId,
                    quantity: i.quantity,
                    price: i.price,
                })),
                totalAmount: totalWithTax,
                shippingInfo,
            }),
        });

        if (!response.ok) {
            const errData = await response.json();
            console.error("Failed to create local order:", errData);
            throw new Error("Failed to create local order");
        }

        const data = await response.json();
        return data.order.orderId; // 返回本地订单ID
    };

    const handleApprovePayment = async (paypalOrder) => {
        // localOrderId 可能是字符串，需要转成整数
        const localOrderId = parseInt(paypalOrder.purchase_units[0].custom_id, 10);

        const response = await fetch("/api/payments/save-payment", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                localOrderId, // 确保是整数
                order: paypalOrder,
            }),
        });

        if (!response.ok) {
            const errData = await response.json();
            console.error("Failed to save payment:", errData);
            alert("Failed to record payment on server.");
            return;
        }

        clearCart();
        setOrderCreated(true);
    };

    if (orderCreated) {
         return (
                <div className="checkout-container">
                        ✅ Payment successful! Your order has been placed.
                    <button
                        className="return-home-btn"
                        onClick={() => (window.location.href = "/")}
                    >
                        Return to Homepage
                    </button>
                </div>
            );
    }

    if (!user) return <div>Please sign in to checkout.</div>;
    if (orderCreated) return <div className="checkout-container">✅ Payment successful! Your order has been placed.</div>;

    return (
        <div className="checkout-container">
            <h2>Checkout</h2>
            <div className="checkout-sections">
                <div className="checkout-left">
                    <h3>Shipping Information</h3>
                    {["name", "address", "city", "postcode", "country"].map((field) => (
                        <input
                            key={field}
                            type="text"
                            name={field}
                            placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
                            value={shippingInfo[field]}
                            onChange={handleInputChange}
                            className="checkout-input"
                        />
                    ))}
                </div>

                <div className="checkout-right">
                    <h3>Order Summary</h3>
                    <div className="order-items">
                        {items.map((item) => (
                            <div key={item.bookFormatId} className="summary-row">
                                <span>{item.title} × {item.quantity}</span>
                                <span>{formatPrice(item.price * item.quantity)}</span>
                            </div>
                        ))}
                    </div>
                    <div className="summary-row total">
                        <span>Total:</span>
                        <span>{formatPrice(totalWithTax)}</span>
                    </div>

                    <div className="paypal-button-container">
                        {isShippingInfoComplete() ? (
                            <PayPalButton
                                amount={totalWithTax.toFixed(2)}
                                onCreateLocalOrder={createLocalOrder}
                                onApprovePayment={handleApprovePayment}
                            />
                        ) : (
                            <button
                                disabled
                                style={{
                                    width: "100%",
                                    padding: "12px 0",
                                    backgroundColor: "#ccc",
                                    color: "#666",
                                    border: "none",
                                    borderRadius: "6px",
                                    cursor: "not-allowed",
                                    fontSize: "16px",
                                }}
                            >
                                Complete shipping info to pay
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CheckoutPage;
