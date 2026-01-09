import React, { useState } from "react";
import "./ForgotPassword.css";
import { Link, useNavigate } from "react-router-dom";

function ForgotPassword() {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    securityAnswer: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [securityQuestion, setSecurityQuestion] = useState("");
  const [errors, setErrors] = useState({ step1: "", step2: "", step3: "" });
  const [flowId, setFlowId] = useState("");
  const [loading, setLoading] = useState(false);

  const gotoStep = (n) => {
    setErrors((e) => ({ ...e, [`step${n}`]: "" }));
    setStep(n);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Step 1: username and email
  const handleVerifyUser = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors((e) => ({ ...e, step1: "" }));
    try {
      const res = await fetch("/api/auth/forgot/verify-user", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                username: formData.username.trim(),
                email: formData.email.trim(),
              }),
            });
      const data = await res.json();
      if (!res.ok) {
        setErrors((e) => ({ ...e, step1: "Incorrect username or email" }));
        return;
      }
      setFlowId(data.flowId);
      setSecurityQuestion(data.securityQuestion || "Security question");
      gotoStep(2);
    } catch {
      setErrors((e) => ({ ...e, step1: "Network error. Please try again." }));
    } finally { setLoading(false); }
  };

  // step 2: Security answer
  const handleVerifyAnswer = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors((e) => ({ ...e, step2: "" }));
    try {
      const res = await fetch("/api/auth/forgot/verify-answer", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                flowId,
                securityAnswer: formData.securityAnswer.trim(),
              }),
            });
      if (!res.ok) {
        setErrors((e) => ({ ...e, step2: "Incorrect Security answer" }));
        return;
      }
      gotoStep(3);
    } catch {
      setErrors((e) => ({ ...e, step2: "Network error. Please try again." }));
    } finally { setLoading(false); }
  };

  // Step 3: password and confirmPassword
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setErrors((e) => ({ ...e, step3: "" }));
    if (formData.newPassword !== formData.confirmPassword) {
      setErrors((e) => ({ ...e, step3: "Passwords do not match" }));
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("/api/auth/forgot/reset-password", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                flowId,
                newPassword: formData.newPassword,
                confirmPassword: formData.confirmPassword,
              }),
            });
      const data = await res.json().catch(() => ({}));
      if (!res.ok) {
        setErrors((e) => ({ ...e, step3: data?.message || "Failed to reset password" }));
        return;
      }
      alert("Password reset successful! You can now log in.");
      window.location.href = "/signin";
    } catch {
      setErrors((e) => ({ ...e, step3: "Network error. Please try again." }));
    } finally { setLoading(false); }
  };


  return (
    <div className="forgot-modal">
      <div className="forgot-box">
        <h2 className="forgot-title">Forgot Password</h2>

        {step === 1 && (
          <form onSubmit={handleVerifyUser} className="forgot-form">
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

            {errors.step1 && <p className="error-message">{errors.step1}</p>}

            <Link to="/signin" className="back-btn">Back to Sign in</Link>
            <button disabled={loading} type="submit" className="forgot-btn">
                {loading ? "Verifying..." : "Verify"}
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleVerifyAnswer} className="forgot-form">
            <p className="security-question">Security Question: {securityQuestion}</p>

            <label>Answer</label>
            <input
              type="text"
              name="securityAnswer"
              placeholder="Enter your answer"
              value={formData.securityAnswer}
              onChange={handleChange}
              required
              maxLength={255}
            />

            {errors.step2 && <p className="error-message">{errors.step2}</p>}

            <Link to="/signin" className="back-btn">Back to Sign in</Link>
            <button disabled={loading} type="submit" className="forgot-btn">
                {loading ? "Checking..." : "Submit Answer"}
            </button>
          </form>
        )}

        {step === 3 && (
          <form onSubmit={handleResetPassword} className="forgot-form">
            <label>New Password</label>
            <input
              type="password"
              name="newPassword"
              placeholder="Enter new password"
              value={formData.newPassword}
              onChange={handleChange}
              required
              minLength={8}
              maxLength={20}
            />

            <label>Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              placeholder="Re-enter new password"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              minLength={8}
              maxLength={20}
            />

            {errors.step3 && <p className="error-message">{errors.step3}</p>}

            <Link to="/signin" className="back-btn">Back to Sign in</Link>
            <button type="submit" className="forgot-btn">Reset Password</button>
          </form>
        )}
      </div>
    </div>
  );
}

export default ForgotPassword;
