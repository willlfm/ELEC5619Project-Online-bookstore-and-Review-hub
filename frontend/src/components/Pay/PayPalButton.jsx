import React, { useEffect, useRef, useState } from "react";

const PayPalButton = ({ amount, onCreateLocalOrder, onApprovePayment }) => {
    const paypalRef = useRef();
    const [sdkReady, setSdkReady] = useState(false);

    useEffect(() => {
        if (window.paypal) {
            setSdkReady(true);
            return;
        }
        const script = document.createElement("script");
        script.src =
            "https://www.paypal.com/sdk/js?client-id=ATqXEU_-wixDaZq64240QLOfyBWBzyYCa8RMEyF-KpBji7tTQsfjnvvPOXA1b3VItY6T3dUQg_RuEf5z&currency=AUD";
        script.async = true;
        script.onload = () => setSdkReady(true);
        document.body.appendChild(script);
    }, []);

    useEffect(() => {
        if (!sdkReady) return;
        if (!onCreateLocalOrder) {
            console.error("PayPalButton error: onCreateLocalOrder is required");
            return;
        }
        if (!onApprovePayment) {
            console.error("PayPalButton error: onApprovePayment is required");
            return;
        }

        const buttons = window.paypal.Buttons({
            style: { layout: "vertical", color: "gold" },

            createOrder: async (data, actions) => {
                // 调用回调创建本地订单并获取 localOrderId
                const localOrderId = await onCreateLocalOrder();
                return actions.order.create({
                    purchase_units: [
                        {
                            description: "Online Bookstore Order",
                            amount: { value: amount },
                            custom_id: localOrderId.toString(), // 传给 PayPal
                        },
                    ],
                });
            },

            onApprove: async (data, actions) => {
                try {
                    const paypalOrder = await actions.order.capture();
                    await onApprovePayment(paypalOrder);
                } catch (err) {
                    console.error("PayPal onApprove error:", err);
                    alert("Payment processing error. Please try again.");
                }
            },

            onError: (err) => {
                console.error("PayPal error:", err);
                alert("PayPal error: " + err.message);
            },
        });

        buttons.render(paypalRef.current);

        return () => {
            if (paypalRef.current) paypalRef.current.innerHTML = "";
        };
    }, [sdkReady, amount, onCreateLocalOrder, onApprovePayment]);

    return <div ref={paypalRef}></div>;
};

export default PayPalButton;
