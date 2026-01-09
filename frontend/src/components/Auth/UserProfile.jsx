import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./UserProfile.css";
import { Link } from "react-router-dom";

function toText(v) {
  if (v === null || v === undefined) return "";
  return String(v);
}
function formatDate(v) {
  if (!v) return "";
  const d = new Date(v);
  if (isNaN(d)) return toText(v);
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}
function getInitial(name, username) {
  const src = (name || username || "").trim();
  return src ? src.charAt(0).toUpperCase() : "?";
}

export default function UserProfile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch("/api/auth/userprofile", { credentials: "include" });
        if (res.status === 401) {
          navigate("/signin", { replace: true });
          return;
        }
        const data = res.ok ? await res.json() : null;
        if (!cancelled) setUser(data);
      } catch {
        if (!cancelled) setUser(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [navigate]);

  if (loading) return <div className="up-loading">Loading...</div>;
  if (!user) return null;

  const initial = getInitial(user.name, user.username);

  return (
    <div className="up-wrapper">
      <Link to="/homepage" className="link">← Back</Link>
      <h1 className="up-title">Your personal profile</h1>

      <div className="up-card">
        <div className="up-left">
          <div aria-label="avatar" className="up-avatar">{initial}</div>
          <div className="up-actions">
              <Link to="/edituserprofile" className="up-edit-btn">Edit profile</Link>
              <Link to="/orderhistory" className="up-orderhistory-btn">Order History</Link>
              <Link to="/refundhistory" className="up-refundhistory-btn">Refund History</Link>
              <Link to="/myebook" className="up-myebook-btn">My Ebook</Link>
          </div>
        </div>

        <div className="up-right">
          <div className="up-row">
            <span className="up-label">Username:</span>
            <span className="up-value">{toText(user.username)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Name:</span>
            <span className="up-value">{toText(user.name)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Gender:</span>
            <span className="up-value">{toText(user.gender)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Email:</span>
            <span className="up-value">{toText(user.email)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Phone number:</span>
            <span className="up-value">{toText(user.phone)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Address:</span>
            <span className="up-value">{toText(user.address)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">City:</span>
            <span className="up-value">{toText(user.city)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">State:</span>
            <span className="up-value">{toText(user.state)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Postal code:</span>
            <span className="up-value">{toText(user.postal_code)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Country:</span>
            <span className="up-value">{toText(user.country)}</span>
          </div>
          <div className="up-row">
            <span className="up-label">Date of birth:</span>
            <span className="up-value">{formatDate(user.date_of_birth)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
