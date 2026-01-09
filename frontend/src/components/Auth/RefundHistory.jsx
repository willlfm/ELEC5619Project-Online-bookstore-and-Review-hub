import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import "./RefundHistory.css";

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

const getJson = async (url, init = {}) => {
  const res = await fetch(url, { credentials: "include", ...init });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
};

const postJson = async (url, body, init = {}) => {
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
};

const normalizeRefund = (raw) => {
  return {
    id: pick(raw, ["refundId", "refund_id", "id"]),
    orderId: pick(raw, ["orderId", "order_id"]),
    refundDate: pick(raw, ["refundDate", "refund_date", "created_at"]),
    amount: Number(pick(raw, ["amount"], 0)),
    reason: pick(raw, ["reason"], "") || "-",
    status: pick(raw, ["status"], "pending"),
    processedAt: pick(raw, ["processedAt", "processed_at"]),
  };
};

const RefundHistory = () => {
  const [loading, setLoading] = useState(true);
  const [me, setMe] = useState(null);
  const [refunds, setRefunds] = useState([]);
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

  useEffect(() => {
    if (!me) return;
    let cancelled = false;
    (async () => {
      setError("");
      try {
        let data;
        try {
          data = await getJson("/api/refund/me");
        } catch {
          try {
            data = await getJson("/api/refund/list");
          } catch {
            data = await getJson("/api/refund");
          }
        }
        const list = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : data?.refunds || [];
        const normalized = list.map(normalizeRefund).sort((a, b) => {
          const da = new Date(a.refundDate || 0).getTime();
          const db = new Date(b.refundDate || 0).getTime();
          return db - da;
        });
        if (!cancelled) setRefunds(normalized);
      } catch (e) {
        if (!cancelled) setError("Can not get refund list.");
      }
    })();
    return () => (cancelled = true);
  }, [me]);

  const onCancel = async (r) => {
    if (String(r.status).toLowerCase() !== "pending") return;
    if (!window.confirm(`Cancel refund #${r.id}?`)) return;

    try {
      await postJson("/api/refund/cancel", { refundId: r.id });
      setRefunds(prev => prev.map(x => x.id === r.id ? { ...x, status: "cancelled" } : x));
    } catch (e) {
      alert(e.message || "Cancel refund failed");
    }
  };

  const isLoggedOut = useMemo(() => !loading && !me, [loading, me]);

  return (
    <div className="rh-container">
      <div className="rh-header">
        <h1 className="rh-title">My Refund History</h1>
        <Link className="rh-link" to="/userprofile">← Back to User profile</Link>
      </div>

      {loading && <div className="rh-card">Loading...</div>}

      {isLoggedOut && (
        <div className="rh-card">
          <p>You need to sign in first.</p>
          <a className="rh-button" href="/signin">Sign in</a>
        </div>
      )}

      {!loading && me && error && <div className="rh-error">{error}</div>}

      {!loading && me && !error && (
        <>
          {refunds.length === 0 ? (
            <div className="rh-card">There is no refund.</div>
          ) : (
            <div className="rh-table-wrap">
              <table className="rh-table" aria-label="Refund history">
                <thead>
                  <tr>
                    <th>Refund #</th>
                    <th>Order #</th>
                    <th>Refund Date</th>
                    <th>Amount</th>
                    <th>Reason</th>
                    <th>Status</th>
                    <th>Cancel</th>
                  </tr>
                </thead>
                <tbody>
                  {refunds.map((r) => {
                    const pending = String(r.status || "").toLowerCase() === "pending";
                    return (
                      <tr key={r.id} className="rh-row">
                        <td className="mono">{r.id}</td>
                        <td className="mono">{r.orderId}</td>
                        <td>{formatDateTime(r.refundDate)}</td>
                        <td className="mono">{currency(r.amount)}</td>
                        <td className="rh-ellipsis" title={r.reason}>{r.reason}</td>
                        <td>
                          <span className={`rh-status ${String(r.status || "").toLowerCase()}`}>{r.status}</span>
                        </td>
                        <td>
                          <button
                            className="rh-btn-cancel"
                            onClick={() => onCancel(r)}
                            disabled={!pending}
                            aria-disabled={!pending}
                            title={pending ? "Cancel this refund" : "Only pending can be cancelled"}
                          >
                            Cancel
                          </button>
                        </td>
                      </tr>
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

export default RefundHistory;
