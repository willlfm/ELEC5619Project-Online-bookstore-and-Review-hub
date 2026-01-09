import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./EditUserProfile.css";

export default function EditUserProfile() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    name: "",
    gender: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    postal_code: "",
    country: "",
    date_of_birth: "", // yyyy-MM-dd
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch("/api/auth/userprofile", { credentials: "include" });
        if (res.status === 401 || res.status === 403) {
          navigate("/signin", { replace: true });
          return;
        }
        const data = res.ok ? await res.json() : null;
        if (!cancelled && data) {
          setForm({
            name: data.name ?? "",
            gender: data.gender ?? "",
            email: data.email ?? "",
            phone: data.phone ?? "",
            address: data.address ?? "",
            city: data.city ?? "",
            state: data.state ?? "",
            postal_code: data.postal_code ?? "",
            country: data.country ?? "",
            date_of_birth: data.date_of_birth ?? "",
          });
        }
      } catch (_) {
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [navigate]);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((s) => ({ ...s, [name]: value }));
  };

  const onBack = () => navigate(-1);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      const payload = Object.fromEntries(
        Object.entries(form).map(([k, v]) => [k, (v ?? "").toString().trim() === "" ? null : v])
      );

      const res = await fetch("/api/auth/edituserprofile", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload),
      });

      if (res.status === 401 || res.status === 403) {
        navigate("/signin", { replace: true });
        return;
      }
      if (!res.ok) {
        const msg = (await res.json().catch(() => ({})))?.message || "Failed to save profile.";
        setError(msg);
        return;
      }

      navigate(-1);
    } catch (err) {
      setError("Network error, please try again.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="edit-profile-page">Loading…</div>;

  return (
    <div className="edit-profile-page">
      <div className="edit-profile-header">
        <button type="button" className="back-btn" onClick={onBack}>← Back</button>
        <h2 className="edit-title">Edit profile</h2>
      </div>

      <form className="edit-profile-form" onSubmit={onSubmit}>
        <LabeledInput label="Name" name="name" value={form.name} onChange={onChange} />

        <div className="form-field">
          <label htmlFor="gender">Gender</label>
          <select id="gender" name="gender" value={form.gender} onChange={onChange}>
            <option value="">-- Select --</option>
            <option>Male</option>
            <option>Female</option>
            <option>Other</option>
          </select>
        </div>

        <LabeledInput label="Email" name="email" type="email" value={form.email} onChange={onChange} />
        <LabeledInput label="Phone number" name="phone" value={form.phone} onChange={onChange} />
        <LabeledInput label="Address" name="address" value={form.address} onChange={onChange} />
        <LabeledInput label="City" name="city" value={form.city} onChange={onChange} />
        <LabeledInput label="State" name="state" value={form.state} onChange={onChange} />
        <LabeledInput label="Postal code" name="postal_code" value={form.postal_code} onChange={onChange} />
        <LabeledInput label="Country" name="country" value={form.country} onChange={onChange} />

        <div className="form-field">
          <label htmlFor="date_of_birth">Date of birth</label>
          <input
            id="date_of_birth"
            name="date_of_birth"
            type="date"
            value={form.date_of_birth || ""}
            onChange={onChange}
          />
        </div>

        <div className="edit-actions">
          <button type="button" className="cancel-btn" onClick={onBack}>Cancel</button>
          <button type="submit" className="save-btn">Save</button>
        </div>
      </form>
    </div>
  );
}

function LabeledInput({ label, name, value, onChange, type = "text" }) {
  return (
    <div className="form-field">
      <label htmlFor={name}>{label}</label>
      <input id={name} name={name} type={type} value={value} onChange={onChange} />
    </div>
  );
}
