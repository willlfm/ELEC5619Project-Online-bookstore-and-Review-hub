import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import "./MyEbook.css";

const BLOCKED_STATUSES = new Set(["pending", "cancelled", "refunding"]);

const parseContentDispositionFilename = (cd) => {
  if (!cd) return "";
  const star = cd.match(/filename\*\s*=\s*[^']*'[^']*'([^;]+)/i);
  if (star) { try { return decodeURIComponent(star[1]); } catch { return star[1]; } }
  const normal = cd.match(/filename\s*=\s*"(.*?)"|filename\s*=\s*([^;]+)/i);
  return (normal && (normal[1] || normal[2] || "")).trim().replace(/^"(.*)"$/, "$1");
};

const pick = (obj, keys, fallback = undefined) => {
  for (const k of keys) {
    if (obj && Object.prototype.hasOwnProperty.call(obj, k) && obj[k] !== undefined && obj[k] !== null) {
      return obj[k];
    }
  }
  return fallback;
};

const formatDateTime = (s) => {
  if (!s) return "-";
  const d = typeof s === "string" ? new Date(s) : s;
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleString();
};

const getJson = async (url, init = {}) => {
  const res = await fetch(url, { credentials: "include", ...init });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const ct = res.headers.get("Content-Type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
};

const ENDPOINTS = {
  me: "/api/auth/me",
  myEbooks: "/api/ebooks/me",
  download: (orderItemId) => `/api/ebooks/${orderItemId}/download`,
};

const normalizeMyEbookItem = (raw) => {
  const title =
    pick(raw, ["bookTitle", "book_title", "title"]) ||
    (raw.book && (raw.book.title || raw.book.bookTitle)) ||
    (raw.bookInfo && raw.bookInfo.title) ||
    null;

  const author =
    pick(raw, ["author"]) ||
    (raw.book && raw.book.author) ||
    (raw.bookInfo && raw.bookInfo.author) ||
    null;

  return {
    orderId: pick(raw, ["orderId", "order_id", "orderID"]),
    orderItemId: pick(raw, ["orderItemId", "order_item_id", "orderItemID"]),
    orderDate: pick(raw, ["orderDate", "order_date", "purchasedAt", "purchased_at"]),
    bookId: pick(raw, ["bookId", "book_id"]) || (raw.book && (raw.book.id || raw.book.bookId)) || null,
    bookTitle: title,
    author,
    format: pick(raw, ["format"], "ebook"),
    status: pick(raw, ["status", "orderStatus", "statusText"], ""),
    sourceUrl: pick(raw, ["sourceUrl", "source_url", "filename"], null),
    downloadUrl: pick(raw, ["downloadUrl", "download_url"], null),
    _raw: raw,
  };
};

const MyEbook = () => {
  const [loading, setLoading] = useState(true);
  const [me, setMe] = useState(null);
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(ENDPOINTS.me, { credentials: "include" });
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
        const data = await getJson(ENDPOINTS.myEbooks);
        const arr = Array.isArray(data) ? data : Array.isArray(data?.items) ? data.items : [];
        if (!Array.isArray(arr)) throw new Error("Unexpected response shape");

        const normalized = arr.map(normalizeMyEbookItem).sort((a, b) => {
          const da = new Date(a.orderDate || 0).getTime();
          const db = new Date(b.orderDate || 0).getTime();
          return db - da;
        });

        if (!cancelled) setItems(normalized);
      } catch (e) {
        if (!cancelled) setError("Failed to load your eBooks. Please try again later.");
      }
    })();
    return () => (cancelled = true);
  }, [me]);

  const isLoggedOut = useMemo(() => !loading && !me, [loading, me]);

  const onDownload = async (item) => {
    const url = item.downloadUrl || ENDPOINTS.download(item.orderItemId);
    try {
      const res = await fetch(url, { credentials: "include" });
      if (res.redirected) {
        window.open(res.url, "_blank", "noopener,noreferrer");
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const blob = await res.blob();
      const cd = res.headers.get("Content-Disposition") || "";
      const suggested = parseContentDispositionFilename(cd);
      if (!suggested) throw new Error("Server did not provide filename.");

      const a = document.createElement("a");
      a.href = window.URL.createObjectURL(blob);
      a.download = suggested;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(a.href);
    } catch (e) {
      alert(e.message || "Download failed");
    }
  };

  return (
    <div className="meb-container">
      <div className="meb-header">
        <h1 className="meb-title">My eBooks</h1>
        <Link className="meb-link" to="/userprofile">← Back to Profile</Link>
      </div>

      {loading && <div className="meb-card">Loading...</div>}

      {isLoggedOut && (
        <div className="meb-card">
          <p>You need to sign in.</p>
          <a className="meb-button" href="/signin">Sign in</a>
        </div>
      )}

      {!loading && me && error && <div className="meb-error">{error}</div>}

      {!loading && me && !error && (
        <>
          {items.length === 0 ? (
            <div className="meb-card">You have no purchased eBooks yet.</div>
          ) : (
            <div className="meb-table-wrap">
              <table className="meb-table" aria-label="My eBooks">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Author</th>
                    <th>Purchase Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {(items.filter(it => !BLOCKED_STATUSES.has(String(it.status || "").toLowerCase()))
                  ).map((it) => {
                    const status = String(it.status || "").toLowerCase();
                    const canDownload = !BLOCKED_STATUSES.has(status);
                    return (
                      <tr key={`${it.orderItemId}-${it.orderId}`} className="meb-row">
                        <td className="meb-ellipsis">
                          {it.bookId ? (
                            <Link className="meb-link" to={`/book/${it.bookId}`}>
                              {it.bookTitle || "(title unavailable)"}
                            </Link>
                          ) : (
                            <span>{it.bookTitle || "(title unavailable)"}</span>
                          )}
                        </td>
                        <td className="meb-ellipsis">{it.author || "-"}</td>
                        <td className="mono">{formatDateTime(it.orderDate)}</td>
                        <td>
                          <button
                            className="meb-btn-download"
                            onClick={() => onDownload(it)}
                            disabled={!canDownload}
                            aria-disabled={!canDownload}
                            title={canDownload ? "Download eBook" : "Order not completed yet"}
                          >
                            Download
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

export default MyEbook;
