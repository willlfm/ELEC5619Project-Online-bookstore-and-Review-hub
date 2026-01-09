import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ReservationManagement.css';

const STATUS_OPTIONS = [
  'reserved',
  'picked',
  'reservation_cancelled',
  'warning',
  'returned'
];

/**
 * Admin reservation management page
 */
const ReservationManagement = () => {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadReservations();
  }, [statusFilter]);

  const loadReservations = async () => {
    setLoading(true);
    setMessage('');
    try {
      const url = statusFilter 
        ? `/api/admin/reservations?status=${statusFilter}`
        : '/api/admin/reservations';
      const response = await fetch(url, {
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error('Failed to load reservations');
      }
      
      const data = await response.json();
      setReservations(data);
    } catch (err) {
      setMessage('Failed to load reservations: ' + err.message);
      console.error('Error loading reservations:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (reservationId, newStatus) => {
    // Optimistic update
    const oldReservations = [...reservations];
    setReservations(prev => prev.map(r => 
      r.reservationId === reservationId ? { ...r, status: newStatus } : r
    ));

    try {
      const response = await fetch(`/api/admin/reservations/${reservationId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ status: newStatus })
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Status update failed:', response.status, errorText);
        throw new Error(errorText || 'Failed to update status');
      }

      const updated = await response.json();
      setReservations(prev => prev.map(r => r.reservationId === reservationId ? updated : r));
      setMessage(`✓ Reservation #${reservationId} updated to ${getStatusDisplay(newStatus)}. User notification sent.`);
      
      // Clear message after 3 seconds
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      // Revert on error
      setReservations(oldReservations);
      console.error('Error updating status:', err);
      setMessage('✗ Failed to update status: ' + (err.message || 'Unknown error'));
      setTimeout(() => setMessage(''), 5000);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '—';
    return new Date(dateTimeString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusDisplay = (status) => {
    const displayMap = {
      'reserved': 'Reserved',
      'picked': 'Picked',
      'reservation_cancelled': 'Reservation Cancelled',
      'warning': 'Warning',
      'returned': 'Returned'
    };
    return displayMap[status] || status;
  };

  const getAvailableStatusOptions = (currentStatus) => {
    // Define state transition logic
    switch (currentStatus) {
      case 'reserved':
        return ['reserved', 'picked', 'reservation_cancelled'];
      case 'picked':
        return ['picked', 'returned', 'warning'];
      case 'warning':
        return ['warning', 'returned'];
      case 'reservation_cancelled':
      case 'returned':
        // Terminal states - cannot be changed
        return [currentStatus];
      default:
        return [currentStatus];
    }
  };

  return (
    <div className="reservation-management" data-testid="reservation-management">
      <header>
        <div>
          <h1>Reservation Management</h1>
          <p>Monitor book reservations, update status, and notify users of changes.</p>
        </div>
        <div className="filters">
          <label>
            Status filter
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">All statuses</option>
              {STATUS_OPTIONS.map((status) => (
                <option key={status} value={status}>
                  {getStatusDisplay(status)}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button
          type="button"
          className="admin-back-button"
          onClick={() => navigate("/admin")}
        >
          Back to Administration Board
        </button>
      </header>

      {message && <div className="flash-message">{message}</div>}

      <section className="reservation-table">
        <table>
          <thead>
            <tr>
              <th>Reservation</th>
              <th>User</th>
              <th>Book</th>
              <th>Schedule</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={5} className="empty">Loading...</td>
              </tr>
            )}
            {!loading && reservations.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">No reservations found.</td>
              </tr>
            )}
            {!loading && reservations.map((reservation) => (
              <tr key={reservation.reservationId}>
                <td>
                  <strong>#{reservation.reservationId}</strong>
                  <span className="muted">Created: {formatDateTime(reservation.createdAt)}</span>
                </td>
                <td>
                  <strong>User #{reservation.userId}</strong>
                </td>
                <td className="book-info">
                  {reservation.bookCoverImageUrl && (
                    <img 
                      src={`/${reservation.bookCoverImageUrl}`} 
                      alt={reservation.bookTitle}
                      className="book-thumbnail"
                    />
                  )}
                  <div>
                    <strong>{reservation.bookTitle || 'N/A'}</strong>
                    <span className="muted">Book ID: {reservation.bookId}</span>
                  </div>
                </td>
                <td className="schedule">
                  <span><strong>Date:</strong> {formatDate(reservation.reservationDate)}</span>
                  <span><strong>Time:</strong> {reservation.timeSlot}</span>
                  <span className="expiry"><strong>Due:</strong> {formatDateTime(reservation.expiryDate)}</span>
                </td>
                <td>
                  <select
                    value={reservation.status}
                    onChange={(e) => handleStatusChange(reservation.reservationId, e.target.value)}
                    className={`status-select status-${reservation.status}`}
                    disabled={['reservation_cancelled', 'returned'].includes(reservation.status)}
                  >
                    {getAvailableStatusOptions(reservation.status).map((status) => (
                      <option key={status} value={status}>
                        {getStatusDisplay(status)}
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default ReservationManagement;
