import React, { useState } from "react";
import { submitFeedback } from "../../services/feedback";

export default function FeedbackForm({ user }) {
  const [text, setText] = useState("");
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!user || busy) return;
    if (!text.trim()) {
      setNote("Please enter your feedback");
      return;
    }
    setBusy(true);
    try {
      await submitFeedback(text.trim());
      setText("");
      setNote("Thank you, your feedback has been sent.");
    } catch (err) {
      setNote("Failed to send, please try again later");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="search-card" style={{ marginTop: 12 }}>
      <div style={{ width: "100%" }}>
        <h3 style={{ marginTop: 0, color: "#d0e8ff" }}>Feedback & Issues</h3>
        {note && <div className="flash-message">{note}</div>}
        <form onSubmit={onSubmit}>
          <textarea
            placeholder="What would you like to say? Issues or suggestions are welcome…"
            value={text}
            onChange={(e) => setText(e.target.value)}
            rows={3}
            style={{ width: "100%", padding: 12, borderRadius: 10 }}
            disabled={!user || busy}
          />
          <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
            <button type="submit" className="button" disabled={!user || busy || !text.trim()}>
              Submit
            </button>
            {!user && <span className="muted">Please sign in to send feedback</span>}
          </div>
        </form>
      </div>
    </div>
  );
}