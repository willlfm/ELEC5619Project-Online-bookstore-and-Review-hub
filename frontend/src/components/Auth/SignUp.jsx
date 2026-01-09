import React, { useState } from "react";
import "./SignUp.css";
import { Link, useNavigate } from "react-router-dom";

function SignUp() {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    phone: "",
    name: "",
    password: "",
    confirmPassword: "",
    securityQuestion: "",
    securityAnswer: "",
  });

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({ username: "", email: "" });

  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (name === "username" || name === "email") {
          setFieldErrors((prev) => ({ ...prev, [name]: "" }));
        }
  };

  const handleSubmit = async (e) => {
      e.preventDefault();
      setError(null);

      if (formData.password !== formData.confirmPassword) {
        setError("Two passwords are not the same.");
        return;
      }

      setSubmitting(true);
      try {
        const res = await fetch("/api/auth/signup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            username: formData.username,
            email: formData.email,
            name: formData.name,
            phone: formData.phone,
            password: formData.password,
            confirmPassword: formData.confirmPassword,
            securityQuestion: formData.securityQuestion,
            securityAnswer: formData.securityAnswer,
          }),
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          // 409: username of email exists
          if (res.status === 409) {
            const msg = data?.message || data?.error || "Conflict";
            const field = data?.field;

            if (field === "username" || /username/i.test(msg)) {
              setFieldErrors(prev => ({ ...prev, username: msg }));
            } else if (field === "email" || /email/i.test(msg)) {
              setFieldErrors(prev => ({ ...prev, email: msg }));
            } else {
              setError(msg || "Username or email already exists");
            }

            return;
          }

          throw new Error(data.message || `Register failed (${res.status})`);
        }

        // Success: 201
        alert("Sign up success! Please sign in.");
        navigate("/signin");
      } catch (err) {
        setError(err.message || "Network error");
      } finally {
        setSubmitting(false);
      }
    };

  return (
    <div className="signup-modal">
      <div className="signup-box">
        <h2 className="signup-title">Sign up</h2>

        <p className="signin-text">
          Already have an account? <Link to="/signin">Sign in</Link>
        </p>

        <form className="signup-form" onSubmit={handleSubmit}>
          <label>Username</label>
          <input
            type="text"
            name="username"
            placeholder="Enter your username"
            value={formData.username}
            onChange={handleChange}
            required
            minLength={3}
            maxLength={50}
          />
          {fieldErrors.username && (
            <p className="error-message">{fieldErrors.username}</p>
          )}

          <label>Email</label>
          <input
            type="email"
            name="email"
            placeholder="Enter your email"
            value={formData.email}
            onChange={handleChange}
            required
            maxLength={100}
          />
          {fieldErrors.email && (
            <p className="error-message">{fieldErrors.email}</p>
          )}

          <label>Phone number</label>
          <input
            type="tel"
            name="phone"
            placeholder="Enter your phone number"
            value={formData.phone}
            onChange={handleChange}
            required
            maxLength={20}
          />

          <label>Name</label>
          <input
            type="text"
            name="name"
            placeholder="Enter your full name"
            value={formData.name}
            onChange={handleChange}
            required
            maxLength={50}
          />

          <label>Password</label>
          <input
            type="password"
            name="password"
            placeholder="Enter your password"
            value={formData.password}
            onChange={handleChange}
            required
            minLength={8}
            maxLength={20}
          />

          <label>Confirm password</label>
          <input
            type="password"
            name="confirmPassword"
            placeholder="Re-enter your password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            minLength={8}
            maxLength={20}
          />

          <label>Security question</label>
          <input
            type="text"
            name="securityQuestion"
            placeholder="Enter a security question"
            value={formData.securityQuestion}
            onChange={handleChange}
            required
            maxLength={255}
          />

          <label>Security answer</label>
          <input
            type="text"
            name="securityAnswer"
            placeholder="Enter your security answer"
            value={formData.securityAnswer}
            onChange={handleChange}
            required
            maxLength={255}
          />

          <button type="submit" className="signup-btn">
            Sign up
          </button>
        </form>

        {error && <p className="error-message">{error}</p>}

        <p className="terms">
          By clicking the "Sign up" button, you are creating an account, and you agree to our Terms of Use.
        </p>
      </div>
    </div>
  );
}

export default SignUp;
