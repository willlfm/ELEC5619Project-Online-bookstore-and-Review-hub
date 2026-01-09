import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAdminAccess } from "../../hooks/useAdminAccess";

/**
 * Simple route-guard that keeps admin-only pages hidden unless the configured admin is signed in.
 */
export default function AdminRoute({ children }) {
  const { checking, allowed } = useAdminAccess();
  const location = useLocation();

  if (checking) {
    return <div className="admin-board">Checking administrator access…</div>;
  }

  if (!allowed) {
    return (
      <Navigate
        to={`/signin?next=${encodeURIComponent(location.pathname)}`}
        replace
      />
    );
  }

  return <>{children}</>;
}
