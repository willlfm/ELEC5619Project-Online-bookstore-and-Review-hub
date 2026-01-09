import React, { useState, useEffect } from "react";
import "./SignIn.css";
import { Link, useNavigate } from "react-router-dom";
import {
  signIn as signInApi,
  getCurrentUser,
  getNextPath,
  isForceStayOnSignin,
  HttpError,
} from "../../services/auth";
import { useCart } from "../../contexts/CartContext.jsx";


function SignIn() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [checking, setChecking] = useState(true);

  const navigate = useNavigate();
  const { setUserAndRefreshCart } = useCart();

  useEffect(() => {
    let cancelled = false;
    const controller = new AbortController();
    (async () => {
      try {
        if (isForceStayOnSignin()) {
          setChecking(false);
          return;
        }
        const me = await getCurrentUser({ signal: controller.signal });
        if (!cancelled && me) {
          const next = getNextPath();
          navigate(next, { replace: true });
          return;
        }
      } catch (_) {
        // other error
      } finally {
        if (!cancelled) setChecking(false);
      }
    })();
    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [navigate]);


  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await signInApi({ username, password });

      const me = await getCurrentUser();
      if (me) {
        await setUserAndRefreshCart(me);
      }

      const next = getNextPath();
      navigate(next, { replace: true });
    } catch (err) {
      if (err instanceof HttpError) {
        const msg =
          (err.data && (err.data.error || err.data.message)) ||
          (err.status === 401
            ? "Invalid username or password"
            : `Sign-in failed (HTTP ${err.status})`);
        setError(msg);
      } else {
        setError("Network error, please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="signin-modal">
      <div className="signin-box">
        <h2 className="signin-title">Sign in</h2>

        <p className="signup-text">
          Don't have an account? <Link to="/signup">Sign up</Link>
        </p>

        {error && <p className="error-message">{error}</p>}

        <form className="signin-form" onSubmit={handleSubmit}>
          <label>Username</label>
          <input
            type="text"
            placeholder="Enter your username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            minLength={3}
            maxLength={50}
          />

          <label>Password</label>
          <input
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            maxLength={20}
          />

          <Link to="/forgot" className="forgot-password">
            Forgot your password?
          </Link>

          <button type="submit" className="signin-btn">
            Sign in
          </button>
        </form>

        <p className="terms">
          By clicking the "Sign in" button, you agree to our Terms of Use.
        </p>
      </div>
    </div>
  );
}

export default SignIn;
