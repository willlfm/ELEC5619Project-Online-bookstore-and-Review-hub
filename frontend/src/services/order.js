export const createOrder = async ({ items, totalAmount, shippingInfo, paypalOrderId }) => {
    const response = await fetch("/api/orders/confirm", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ items, totalAmount, shippingInfo, paypalOrderId }),
        credentials: "include", // make the browser send HttpOnly JWT Cookie
    });

    if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || "Order creation failed!");
    }

    return response.json();
};
