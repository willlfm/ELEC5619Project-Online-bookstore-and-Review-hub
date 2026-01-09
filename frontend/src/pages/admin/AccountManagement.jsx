import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchAdminUsers,
  createAdminUser,
  updateAdminUser,
  deleteAdminUser,
} from "../../services/admin";
import "./AccountManagement.css";

const ADMIN_USERNAME = "test_admin1";

const EMPTY_FORM = {
  username: "",
  email: "",
  password: "",
  name: "",
  phone: "",
  role: "normal",
};

export default function AccountManagement() {
  const [users, setUsers] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 1 });
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    loadUsers({ page: pageInfo.page, size: pageInfo.size, keyword });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageInfo.page, keyword]);

  const loadUsers = async ({ page, size, keyword: query }) => {
    const data = await fetchAdminUsers({ page, size, keyword: query });
    const filteredContent = (data.content ?? []).filter((user) => user.username !== ADMIN_USERNAME);
    setUsers(filteredContent);
    setPageInfo({ page: data.number ?? 0, size: data.size ?? size, totalPages: data.totalPages ?? 1 });
  };

  // Keep client-side validation close to the submit handler so the UX is consistent with backend rules.
  const validateForm = () => {
    const errors = {};
    if (!form.username.trim()) errors.username = "Username is required";
    if (!form.email.trim()) errors.email = "Email is required";
    if (!form.name.trim()) errors.name = "Display name is required";
    if (!form.phone.trim()) errors.phone = "Phone is required";
    if (!editingId && !form.password.trim()) errors.password = "Password is required when creating";
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (evt) => {
    evt.preventDefault();
    if (!validateForm()) {
      setMessage("Please fix the highlighted fields before saving.");
      return;
    }

    try {
      if (editingId) {
        const payload = { ...form };
        if (!payload.password) delete payload.password;
        const updated = await updateAdminUser(editingId, payload);
        setUsers((prev) => prev.map((user) => (user.userId === editingId ? updated : user)));
        setMessage("Account updated successfully");
      } else {
        const created = await createAdminUser(form);
        if (created.username !== ADMIN_USERNAME) {
          setUsers((prev) => [created, ...prev]);
        }
        setMessage("Account created successfully");
      }
      setForm({ ...EMPTY_FORM });
      setEditingId(null);
      setFormErrors({});
    } catch (err) {
      setMessage("Unable to save account. Please check the input data.");
    }
  };

  const handleEdit = (user) => {
    if (user.username === ADMIN_USERNAME) return;
    setEditingId(user.userId);
    setForm({
      username: user.username,
      email: user.email,
      password: "",
      name: user.name,
      phone: user.phone,
      role: user.role,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = async (userId) => {
    const target = users.find((user) => user.userId === userId);
    if (!target || target.username === ADMIN_USERNAME) return;
    if (!window.confirm("Delete this account?")) return;
    await deleteAdminUser(userId);
    setUsers((prev) => prev.filter((user) => user.userId !== userId));
    setMessage("Account removed");
  };

  const handleChange = (evt) => {
    const { name, value } = evt.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="account-management" data-testid="account-management">
      <header>
        <div>
          <h1>Account Management</h1>
          <p>Maintain secure access by onboarding librarians, editors, or customer support members.</p>
        </div>
        <div className="header-actions">
          <div className="search-field">
            <input
              type="search"
              placeholder="Search by username, email, or name"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </div>
          <button
            type="button"
            className="admin-back-button"
            onClick={() => navigate("/admin")}
          >
            Back to Administration Board
          </button>
        </div>
      </header>

      {message && <div className="flash-message">{message}</div>}

      <section className="account-form">
        <h2>{editingId ? "Edit account" : "Create a new account"}</h2>
        <form onSubmit={handleSubmit}>
          <div className="grid">
            <label>
              Username
              <input
                type="text"
                name="username"
                required
                disabled={!!editingId}
                value={form.username}
                onChange={handleChange}
                aria-invalid={formErrors.username ? "true" : "false"}
              />
              {formErrors.username && <span className="field-error">{formErrors.username}</span>}
            </label>
            <label>
              Email
              <input
                type="email"
                name="email"
                required
                value={form.email}
                onChange={handleChange}
                aria-invalid={formErrors.email ? "true" : "false"}
              />
              {formErrors.email && <span className="field-error">{formErrors.email}</span>}
            </label>
            <label>
              Display name
              <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                aria-invalid={formErrors.name ? "true" : "false"}
              />
              {formErrors.name && <span className="field-error">{formErrors.name}</span>}
            </label>
            <label>
              Phone
              <input
                type="text"
                name="phone"
                value={form.phone}
                onChange={handleChange}
                aria-invalid={formErrors.phone ? "true" : "false"}
              />
              {formErrors.phone && <span className="field-error">{formErrors.phone}</span>}
            </label>
            <label>
              Role
              <select name="role" value={form.role} onChange={handleChange}>
                <option value="admin">Admin</option>
                <option value="normal">User</option>
              </select>
            </label>
            <label>
              Password
              <input
                type="password"
                name="password"
                placeholder={editingId ? "Leave blank to keep existing" : "Set an initial password"}
                value={form.password}
                onChange={handleChange}
                aria-invalid={formErrors.password ? "true" : "false"}
              />
              {formErrors.password && <span className="field-error">{formErrors.password}</span>}
            </label>
          </div>
          <div className="form-actions">
            <button className="primary" type="submit">
              {editingId ? "Save changes" : "Create account"}
            </button>
            {editingId && (
              <button
                type="button"
                className="link"
                onClick={() => {
                  setEditingId(null);
                  setForm({ ...EMPTY_FORM });
                  setFormErrors({});
                }}
              >
                Cancel editing
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="account-table" aria-label="Existing accounts">
        <table>
          <thead>
            <tr>
              <th>User</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  No accounts found.
                </td>
              </tr>
            )}
            {users.map((user) => (
              <tr key={user.userId}>
                <td>
                  <strong>{user.username}</strong>
                  <span className="muted">{user.name}</span>
                </td>
                <td>{user.email}</td>
                <td>{user.phone || "—"}</td>
                <td>
                  <span className={user.role === "admin" ? "badge badge-admin" : "badge"}>{user.role}</span>
                </td>
                <td className="actions">
                  <button type="button" onClick={() => handleEdit(user)}>
                    Edit
                  </button>
                  <button type="button" className="danger" onClick={() => handleDelete(user.userId)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="pagination">
          <button
            type="button"
            onClick={() => setPageInfo((prev) => ({ ...prev, page: Math.max(prev.page - 1, 0) }))}
            disabled={pageInfo.page === 0}
          >
            Previous
          </button>
          <span>
            Page {pageInfo.page + 1} of {pageInfo.totalPages}
          </span>
          <button
            type="button"
            onClick={() =>
              setPageInfo((prev) => ({ ...prev, page: Math.min(prev.page + 1, prev.totalPages - 1) }))
            }
            disabled={pageInfo.page + 1 >= pageInfo.totalPages}
          >
            Next
          </button>
        </div>
      </section>
    </div>
  );
}
