import React, { useEffect, useState } from "react";
import FeedbackForm from "../components/Feedback/FeedbackForm.jsx";
import { getCurrentUser } from "../services/auth";

export default function FeedbackPage() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    getCurrentUser().then(setUser).catch(() => setUser(null));
  }, []);

  return (
    <div style={{ maxWidth: 800, margin: "24px auto", padding: "0 16px" }}>
      <h1 style={{ marginBottom: 12 }}>Feedback & Issue Reporting</h1>
      <p className="muted" style={{ marginTop: 0 }}>
        Tell us about any problems or suggestions you have.
      </p>
      <FeedbackForm user={user} />
    </div>
  );
}