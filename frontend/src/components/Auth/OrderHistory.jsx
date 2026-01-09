import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import "./OrderHistory.css";

const pick = (obj, keys, fallback = undefined) => {
  for (const k of keys) {
    if (obj && Object.prototype.hasOwnProperty.call(obj, k) && obj[k] !== undefined && obj[k] !== null) {
      return obj[k];
    }
  }
  return fallback;
};

const currency = (n) =>
  new Intl.NumberFormat("en-AU", { style: "currency", currency: "AUD" }).format(Number(n || 0));

const formatDateTime = (s) => {
  if (!s) return "-";
  const d = typeof s === "string" ? new Date(s) : s;
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleString();
};

const normalizeOrder = (raw) => {
  const id = pick(raw, ["orderId", "order_id", "id"]);
  const s = pick(raw, ["shippingInfo", "shipping_info", "shipping", "delivery", "addressInfo"]) || {};
  return {
    id,
    orderDate: pick(raw, ["orderDate", "order_date"]),
    totalAmount: Number(pick(raw, ["totalAmount", "total_amount"], 0)),
    shippingName:
      pick(raw, ["shippingName", "shipping_name"]) ||
      pick(s, ["name", "shippingName", "receiverName", "recipientName"]),
    shippingAddress:
      pick(raw, ["shippingAddress", "shipping_address", "address"]) ||
      pick(s, ["address", "address1", "streetAddress", "line1"]),
    shippingCity:
      pick(raw, ["shippingCity", "shipping_city", "city"]) ||
      pick(s, ["city", "town"]),
    shippingPostcode:
      pick(raw, ["shippingPostcode", "shipping_postcode", "postalCode", "postCode", "zip", "zipCode"]) ||
      pick(s, ["postalCode", "postCode", "zip", "zipCode"]),
    shippingCountry:
      pick(raw, ["shippingCountry", "shipping_country", "country"]) ||
      pick(s, ["country", "countryCode", "country_name"]),
    status: pick(raw, ["status"], "pending"),
    items: pick(raw, ["items", "orderItems"], null),
  };
};

const normalizeItem = (raw) => {
  const title =
    pick(raw, ["bookTitle", "book_title"]) ||
    (raw.book && (raw.book.title || raw.book.bookTitle)) ||
    (raw.bookInfo && raw.bookInfo.title) ||
    null;

  return {
    bookTitle: title,
    format: pick(raw, ["format"]),
    quantity: Number(pick(raw, ["quantity"], 0)),
    price: Number(pick(raw, ["price"], 0)),
    bookId: pick(raw, ["bookId", "book_id"]) || (raw.book && (raw.book.id || raw.book.bookId)) || null,
    bookFormatId: pick(raw, ["bookFormatId", "book_format_id"]),
    _raw: raw,
  };
};

const getJson = async (url, init = {}) => {
  const res = await fetch(url, { credentials: "include", ...init });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
};

async function postJson(url, body, init = {}) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    ...init,
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(txt || `Request failed: ${res.status}`);
  }
  const ct = res.headers.get("Content-Type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
}

const OrderHistory = () => {
  const [loading, setLoading] = useState(true);
  const [me, setMe] = useState(null);
  const [orders, setOrders] = useState([]);
  const [expanded, setExpanded] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch("/api/auth/me", { credentials: "include" });
        if (!cancelled) setMe(res.ok ? await res.json() : null);
      } catch {
        if (!cancelled) setMe(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => (cancelled = true);
  }, []);

  const onRefund = async (order) => {
    const amount = Number(order.totalAmount || 0);
    const oid = order.id ?? order.orderId;
    if (!window.confirm(`Refund order #${oid}? Amount: ${currency(amount)}`)) return;
    if (!window.confirm("Are you sure you want to submit the refund request?")) return;
    const reason = window.prompt("Reason (optional):", "") || "";

    try {
      await postJson("/api/refund/comfirm", { orderId: oid, amount, reason });
      alert("Refund application submitted. Your order is now in \"refunding\" status.");
      setOrders(prev => prev.map(o => o.id === oid ? { ...o, status: "refunding" } : o));
    } catch (e) {
      alert(e.message || "Refund failed");
    }
  };

  useEffect(() => {
    if (!me) return;
    let cancelled = false;

    (async () => {
      setError("");
      try {
        let data;
        try {
          data = await getJson("/api/orders/me?includeItems=true");
        } catch {
          throw new Error("Endpoint /api/orders/me not found");
        }

        const list = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : data?.orders || [];
        const normalized = list.map(normalizeOrder).sort((a, b) => {
          const da = new Date(a.orderDate || 0).getTime();
          const db = new Date(b.orderDate || 0).getTime();
          return db - da;
        });
        if (!cancelled) setOrders(normalized);
      } catch (e) {
        if (!cancelled) setError("Can not get /api/orders/me");
      }
    })();

    return () => (cancelled = true);
  }, [me]);

  const loadItemsIfNeeded = async (order) => {
    if (order.items && Array.isArray(order.items)) return;
    try {
      const itemsData = await getJson(`/api/orders/${order.id}/items`);
      const itemsArray = Array.isArray(itemsData) ? itemsData : itemsData?.items || [];
      const normalized = itemsArray.map(normalizeItem);
      setOrders((prev) => prev.map((o) => (o.id === order.id ? { ...o, items: normalized } : o)));
    } catch {
      setOrders((prev) => prev.map((o) => (o.id === order.id ? { ...o, items: [] } : o)));
    }
  };

  const onToggle = async (order) => {
    const now = { ...expanded, [order.id]: !expanded[order.id] };
    setExpanded(now);
    if (now[order.id]) await loadItemsIfNeeded(order);
  };

  const isLoggedOut = useMemo(() => !loading && !me, [loading, me]);

  return (
    <div className="oh-container">
      <div className="oh-header">
        <h1 className="oh-title">My Order History</h1>
        <Link className="oh-link" to="/userprofile">← Back to User profile</Link>
      </div>

      {loading && <div className="oh-card">Loading...</div>}

      {isLoggedOut && (
        <div className="oh-card">
          <p>You need to sign in first.</p>
          <a className="oh-button" href="/signin">Sign in</a>
        </div>
      )}

      {!loading && me && error && <div className="oh-error">{error}</div>}

      {!loading && me && !error && (
        <>
          {orders.length === 0 ? (
            <div className="oh-card">There is no order.</div>
          ) : (
            <div className="oh-table-wrap">
              <table className="oh-table" aria-label="Order history">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Order Date</th>
                    <th>Total Amount</th>
                    <th>Shipping Name</th>
                    <th>Shipping Address</th>
                    <th>Shipping City</th>
                    <th>Shipping Postcode</th>
                    <th>Shipping Country</th>
                    <th>Status</th>
                    <th>Items</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((o) => {
                    const open = !!expanded[o.id];
                    return (
                      <React.Fragment key={o.id}>
                        <tr className="oh-row">
                          <td className="mono">{o.id}</td>
                          <td>{formatDateTime(o.orderDate)}</td>
                          <td className="mono">{currency(o.totalAmount)}</td>
                          <td>{o.shippingName || "-"}</td>
                          <td className="oh-addr">{o.shippingAddress || "-"}</td>
                          <td>{o.shippingCity || "-"}</td>
                          <td>{o.shippingPostcode || "-"}</td>
                          <td>{o.shippingCountry || "-"}</td>
                          <td>
                            <span className={`oh-status ${String(o.status || "").toLowerCase()}`}>
                              {o.status}
                            </span>
                          </td>
                          <td>
                            <button
                              className="oh-link as-button"
                              onClick={() => onToggle(o)}
                              aria-expanded={open}
                              aria-controls={`items-${o.id}`}
                            >
                              {open ? "Hide" : "View"} Items
                            </button>
                          </td>
                          <td>
                            <button className="oh-btn-refund" onClick={() => onRefund(o)} disabled={["cancelled","refunding","completed"].includes(String(o.status).toLowerCase())}>
                              Refund
                            </button>
                          </td>

                        </tr>

                        {open && (
                          <tr id={`items-${o.id}`}>
                            <td colSpan={11} className="oh-items-cell">
                              {!o.items && <div className="oh-subtle">Loading Order items...</div>}
                              {o.items && o.items.length === 0 && (
                                <div className="oh-subtle">No details for this order</div>
                              )}
                              {o.items && o.items.length > 0 && (
                                <ul className="oh-items">
                                  {o.items.map((raw, idx) => {
                                    const it = normalizeItem(raw);
                                    return (
                                      <li key={idx} className="oh-item">
                                        {it.bookId ? (
                                          <Link className="oh-link oh-item-title" to={`/book/${it.bookId}`}>
                                            {it.bookTitle || "（Can not load book name）"}
                                          </Link>
                                        ) : (
                                          <span className="oh-item-title">{it.bookTitle || "（Can not load book name）"}</span>
                                        )}
                                        {it.format ? <><span className="oh-dot" /><span>Format：{it.format}</span></> : null}
                                        <span className="oh-dot" />
                                        <span>Number：{it.quantity}</span>
                                        <span className="oh-dot" />
                                        <span>Price：{currency(it.price)}</span>
                                      </li>
                                    );
                                  })}
                                </ul>
                              )}
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default OrderHistory;
