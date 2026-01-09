import { useEffect, useState } from "react";

const DEFAULT_ADMIN_USERNAME = "test_admin1";

/**
 * Centralised helper that checks the current session against the required administrator account.
 * It keeps UI code simple by exposing a tiny state machine {checking, allowed}.
 */
export function useAdminAccess(expectedUsername = DEFAULT_ADMIN_USERNAME) {
  const [state, setState] = useState({ checking: true, allowed: false });

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const resp = await fetch("/api/auth/me", { credentials: "include" });
        if (cancelled) return;

        if (!resp.ok) {
          setState({ checking: false, allowed: false });
          return;
        }

        const data = await resp.json();
        setState({
          checking: false,
          allowed: data?.username === expectedUsername,
        });
      } catch {
        if (!cancelled) setState({ checking: false, allowed: false });
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [expectedUsername]);

  return state;
}
