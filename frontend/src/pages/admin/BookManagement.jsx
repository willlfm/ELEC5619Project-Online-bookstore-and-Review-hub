import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getAllBooks,
  addBook,
  updateBook,
  deleteBook,
} from "../../services/books";
import "./BookManagement.css";

const EMPTY_BOOK = {
  bookId: null,
  title: "",
  author: "",
  isbn: "",
  category: "",
  coverImageUrl: "",
  description: "",
  paperbackPrice: "",
  paperbackStock: "",
  ebookPrice: "",
  ebookStock: "",
  ebookFile: null,
  ebookFileName: "",
};

export default function BookManagement() {
  const [books, setBooks] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 8, totalPages: 1 });
  const [form, setForm] = useState({ ...EMPTY_BOOK });
  const [selectedFormat, setSelectedFormat] = useState("paperback");
  const [message, setMessage] = useState("");
  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadBooks(pageInfo.page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageInfo.page]);

  const loadBooks = async (page) => {
    const data = await getAllBooks(page, pageInfo.size);
    setBooks(data?.content ?? []);
    setPageInfo({ page: data?.number ?? 0, size: data?.size ?? pageInfo.size, totalPages: data?.totalPages ?? 1 });
  };

  const buildPayload = () => {
    const formats = [];
    if (form.paperbackPrice) {
      formats.push({
        format: "PAPERBACK",
        price: Number(form.paperbackPrice),
        stockQuantity: Number(form.paperbackStock || 0),
      });
    }
    if (form.ebookPrice) {
      formats.push({
        format: "EBOOK",
        price: Number(form.ebookPrice),
        stockQuantity: Number(form.ebookStock || 0),
        sourceUrl: form.ebookFile ? form.ebookFile.name : form.ebookFileName || null,
      });
    }
    return {
      title: form.title,
      author: form.author,
      isbn: form.isbn,
      category: form.category,
      coverImageUrl: form.coverImageUrl,
      description: form.description,
      formats,
    };
  };

  const handleSubmit = async (evt) => {
    evt.preventDefault();
    const payload = buildPayload();
    try {
      if (form.bookId) {
        const updated = await updateBook(form.bookId, payload);
        setBooks((prev) => prev.map((book) => (book.bookId === form.bookId ? updated : book)));
        setMessage("Book updated successfully");
      } else {
        const created = await addBook(payload);
        setBooks((prev) => [created, ...prev]);
        setMessage("Book added successfully");
      }
      setForm({ ...EMPTY_BOOK });
      setSelectedFormat("paperback");
    } catch (err) {
      setMessage("Unable to save book. Please verify the details.");
    }
  };

  const handleEdit = (book) => {
    const paperback = book.formats?.find((item) => item.format === "PAPERBACK") ?? {};
    const ebook = book.formats?.find((item) => item.format === "EBOOK") ?? {};
    setForm({
      bookId: book.bookId,
      title: book.title || "",
      author: book.author || "",
      isbn: book.isbn || "",
      category: book.category || "",
      coverImageUrl: book.coverImageUrl || "",
      description: book.description || "",
      paperbackPrice: paperback.price ?? "",
      paperbackStock: paperback.stockQuantity ?? "",
      ebookPrice: ebook.price ?? "",
      ebookStock: ebook.stockQuantity ?? "",
      ebookFile: null,
      ebookFileName: ebook.sourceUrl ?? "",
    });
    setSelectedFormat(ebook.price != null || ebook.sourceUrl ? "ebook" : "paperback");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = async (bookId) => {
    if (!window.confirm("Delete this book?")) return;
    await deleteBook(bookId);
    setBooks((prev) => prev.filter((book) => book.bookId !== bookId));
    setMessage("Book removed");
  };

  const handleChange = (evt) => {
    const { name, value } = evt.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleFormatSwitch = (evt) => {
    setSelectedFormat(evt.target.value);
  };

  const handlePriceChange = (value) => {
    setForm((prev) =>
      selectedFormat === "paperback"
        ? { ...prev, paperbackPrice: value }
        : { ...prev, ebookPrice: value }
    );
  };

  const handleStockChange = (value) => {
    setForm((prev) =>
      selectedFormat === "paperback"
        ? { ...prev, paperbackStock: value }
        : { ...prev, ebookStock: value }
    );
  };

  const handleEbookFileChange = (evt) => {
    const file = evt.target.files?.[0] ?? null;
    setForm((prev) => ({
      ...prev,
      ebookFile: file,
      ebookFileName: file ? file.name : prev.ebookFileName,
    }));
  };

  const triggerFileDialog = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="book-management" data-testid="book-management">
      <header>
        <div>
          <h1>Book Management</h1>
          <p>Curate the store catalogue, adjust pricing, and ensure inventory stays accurate.</p>
        </div>
        <div className="header-actions">
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

      <section className="book-form">
        <h2>{form.bookId ? "Edit book" : "Add a new title"}</h2>
        <form onSubmit={handleSubmit}>
          <div className="grid">
            <label>
              Title
              <input name="title" value={form.title} onChange={handleChange} required />
            </label>
            <label>
              Author
              <input name="author" value={form.author} onChange={handleChange} required />
            </label>
            <label>
              ISBN
              <input name="isbn" value={form.isbn} onChange={handleChange} />
            </label>
            <label>
              Category
              <input name="category" value={form.category} onChange={handleChange} />
            </label>
            <label>
              Cover image URL
              <input name="coverImageUrl" value={form.coverImageUrl} onChange={handleChange} />
            </label>
            <label className="textarea">
              Description
              <textarea name="description" rows={3} value={form.description} onChange={handleChange} />
            </label>
          </div>

          <div className="format-section">
            <div className="format-toggle">
              <label>
                <input
                  type="radio"
                  name="formatType"
                  value="paperback"
                  checked={selectedFormat === "paperback"}
                  onChange={handleFormatSwitch}
                />
                Paperback
              </label>
              <label>
                <input
                  type="radio"
                  name="formatType"
                  value="ebook"
                  checked={selectedFormat === "ebook"}
                  onChange={handleFormatSwitch}
                />
                E-book
              </label>
            </div>

            <div className="formats-grid single">
              <label>
                Price (AUD)
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={selectedFormat === "paperback" ? form.paperbackPrice : form.ebookPrice}
                  onChange={(e) => handlePriceChange(e.target.value)}
                />
              </label>
              <label>
                Stock
                <input
                  type="number"
                  min="0"
                  value={selectedFormat === "paperback" ? form.paperbackStock : form.ebookStock}
                  onChange={(e) => handleStockChange(e.target.value)}
                />
              </label>
              {selectedFormat === "ebook" && (
                <div className="file-upload-row">
                  <button
                    type="button"
                    className="upload-button"
                    onClick={triggerFileDialog}
                  >
                    Upload Ebook File
                  </button>
                  <span className="file-name">
                    {form.ebookFileName || "No file selected"}
                  </span>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".pdf,.epub,.mobi,.azw"
                    onChange={handleEbookFileChange}
                    className="hidden-file-input"
                  />
                </div>
              )}
            </div>
          </div>

          <div className="form-actions">
            <button className="primary" type="submit">
              {form.bookId ? "Save changes" : "Add book"}
            </button>
            {form.bookId && (
              <button
                type="button"
                className="link"
                onClick={() => {
                  setForm({ ...EMPTY_BOOK });
                  setSelectedFormat("paperback");
                }}
              >
                Cancel editing
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="book-table">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Author</th>
              <th>Category</th>
              <th>Formats</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {books.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  No books available.
                </td>
              </tr>
            )}
            {books.map((book) => (
              <tr key={book.bookId}>
                <td>
                  <strong>{book.title}</strong>
                  <span className="muted">{book.isbn || "No ISBN"}</span>
                </td>
                <td>{book.author}</td>
                <td>{book.category || "—"}</td>
                <td className="formats-cell">
                  {book.formats?.map((format) => (
                    <div key={format.bookFormatId} className="format-pill">
                      <span className="badge">
                        {format.format}
                        <em>${Number(format.price).toFixed(2)}</em>
                      </span>
                      {format.format === "EBOOK" && format.sourceUrl && (
                        <span className="format-file-tag">{format.sourceUrl}</span>
                      )}
                    </div>
                  )) || "—"}
                </td>
                <td className="actions">
                  <button type="button" onClick={() => handleEdit(book)}>
                    Edit
                  </button>
                  <button type="button" className="danger" onClick={() => handleDelete(book.bookId)}>
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
