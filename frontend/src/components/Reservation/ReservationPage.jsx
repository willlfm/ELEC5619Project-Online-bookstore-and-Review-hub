import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getUserReservations, cancelReservation, createReservation, getAvailableDates, getAvailableTimeSlots } from '../../services/reservation';
import { getBookById } from '../../services/books';
import './ReservationPage.css';

/**
 * Reservation management page
 */
const ReservationPage = () => {
  const navigate = useNavigate();
  const { bookId } = useParams();
  const [activeTab, setActiveTab] = useState(bookId ? 'new-reservation' : 'my-reservations'); // 'my-reservations' or 'new-reservation'
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // New reservation form
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTimeSlot, setSelectedTimeSlot] = useState(null);
  const [availableDates, setAvailableDates] = useState([]);
  const [availableTimeSlots, setAvailableTimeSlots] = useState([]);
  const [selectedBookId, setSelectedBookId] = useState(bookId ? parseInt(bookId) : null);
  const [selectedBook, setSelectedBook] = useState(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successReservation, setSuccessReservation] = useState(null);

  useEffect(() => {
    if (bookId && !selectedBook) {
      loadBookDetails();
    }
  }, [bookId]);

  useEffect(() => {
    if (activeTab === 'my-reservations') {
      loadReservations();
    } else if (activeTab === 'new-reservation') {
      loadAvailableDates();
    }
  }, [activeTab]);

  const loadBookDetails = async () => {
    try {
      const book = await getBookById(bookId);
      setSelectedBook(book);
    } catch (err) {
      console.error('Failed to load book details:', err);
      setError('Failed to load book details');
    }
  };

  useEffect(() => {
    if (selectedDate && selectedBookId) {
      loadAvailableTimeSlots(selectedDate, selectedBookId);
    }
  }, [selectedDate, selectedBookId]);

  const loadReservations = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getUserReservations();
      setReservations(data);
    } catch (err) {
      setError('Failed to load reservations: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableDates = async () => {
    try {
      const dates = await getAvailableDates();
      setAvailableDates(dates);
    } catch (err) {
      console.error('Failed to load available dates:', err);
    }
  };

  const loadAvailableTimeSlots = async (date, bookId) => {
    try {
      const slots = await getAvailableTimeSlots(date, bookId);
      setAvailableTimeSlots(slots);
    } catch (err) {
      console.error('Failed to load available time slots:', err);
      setAvailableTimeSlots([]);
    }
  };

  const handleCancelReservation = async (reservationId) => {
    if (!window.confirm('Are you sure you want to cancel this reservation? This action cannot be undone.')) {
      return;
    }

    setError('');
    try {
      await cancelReservation(reservationId);
      loadReservations();
    } catch (err) {
      const errorMessage = err.response?.data || err.message;
      setError('Failed to cancel reservation: ' + errorMessage);
    }
  };

  const canCancelReservation = (reservation) => {
    // Only RESERVED status can be cancelled
    if (reservation.status !== 'reserved') {
      return false;
    }

    // Can only cancel before reservation end time
    const reservationDate = new Date(reservation.reservationDate);
    const timeSlotEnd = reservation.timeSlot.split('-')[1]; // e.g., "10:00"
    const [hours, minutes] = timeSlotEnd.split(':');
    reservationDate.setHours(parseInt(hours), parseInt(minutes), 0, 0);

    return new Date() < reservationDate;
  };

  const getStatusDisplay = (status) => {
    const statusMap = {
      'reserved': 'Reserved',
      'picked': 'Picked',
      'reservation_cancelled': 'Cancelled',
      'warning': 'Warning',
      'returned': 'Returned'
    };
    return statusMap[status] || status;
  };

  const handleCreateReservation = async () => {
    if (!selectedDate || !selectedTimeSlot || !selectedBookId) {
      setError('Please select date, time slot, and book');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const reservation = await createReservation({
        bookId: selectedBookId,
        reservationDate: selectedDate,
        timeSlot: selectedTimeSlot
      });
      
      setSuccessReservation(reservation);
      setShowSuccessModal(true);
      
      // Reset form
      setSelectedDate(null);
      setSelectedTimeSlot(null);
      setSelectedBookId(null);
    } catch (err) {
      setError('Failed to create reservation: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'short', 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const isDateAvailable = (date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);
    const now = new Date();
    
    if (checkDate.toDateString() === today.toDateString()) {
      return now.getHours() < 18;
    }
    
    return true;
  };

  const closeSuccessModal = () => {
    setShowSuccessModal(false);
    setActiveTab('my-reservations');
    loadReservations();
  };

  return (
    <div className="reservation-page">
      <div className="reservation-header">
        <h1>Book Reservations</h1>
        <button className="back-button" onClick={() => navigate('/')}>
          ← Back to Home
        </button>
      </div>

      <div className="reservation-tabs">
        <button
          className={`tab-button ${activeTab === 'my-reservations' ? 'active' : ''}`}
          onClick={() => setActiveTab('my-reservations')}
        >
          My Reservations
        </button>
        <button
          className={`tab-button ${activeTab === 'new-reservation' ? 'active' : ''}`}
          onClick={() => setActiveTab('new-reservation')}
        >
          New Reservation
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {activeTab === 'my-reservations' && (
        <div className="my-reservations">
          {loading ? (
            <div className="loading">Loading...</div>
          ) : reservations.length === 0 ? (
            <div className="empty-state">
              <p>No active reservations</p>
              <button onClick={() => setActiveTab('new-reservation')} className="primary-button">
                Make a Reservation
              </button>
            </div>
          ) : (
            <div className="reservations-grid">
              {reservations.map((reservation) => (
                <div key={reservation.reservationId} className="reservation-card">
                  <img
                    src={reservation.bookCoverImageUrl ? `/${reservation.bookCoverImageUrl}` : '/placeholder-book.png'}
                    alt={reservation.bookTitle}
                    className="reservation-book-cover"
                  />
                  <div className="reservation-details">
                    <h3>{reservation.bookTitle}</h3>
                    <p className="reservation-date">
                      <strong>Date:</strong> {formatDate(reservation.reservationDate)}
                    </p>
                    <p className="reservation-time">
                      <strong>Time Slot:</strong> {reservation.timeSlot}
                    </p>
                    <p className="reservation-expiry">
                      <strong>Return by:</strong> {formatDateTime(reservation.expiryDate)}
                    </p>
                    <p className="reservation-status">
                      <span className={`status-badge status-${reservation.status}`}>
                        {getStatusDisplay(reservation.status)}
                      </span>
                    </p>
                  </div>
                  {canCancelReservation(reservation) && (
                    <button
                      className="cancel-button"
                      onClick={() => handleCancelReservation(reservation.reservationId)}
                    >
                      Cancel Reservation
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'new-reservation' && (
        <div className="new-reservation">
          {selectedBook && (
            <div className="selected-book-info">
              <h3>Selected Book:</h3>
              <div className="book-preview">
                <img src={`/${selectedBook.coverImageUrl}`} alt={selectedBook.title} className="book-preview-cover" />
                <div>
                  <h4>{selectedBook.title}</h4>
                  <p>by {selectedBook.author}</p>
                </div>
              </div>
            </div>
          )}
          
          <div className="reservation-info">
            <h3>Reservation Rules:</h3>
            <ul>
              <li>Maximum 3 active reservations at a time</li>
              <li>Cannot reserve the same book twice</li>
              <li>E-books cannot be reserved</li>
              <li>Books must be returned by 6:00 PM on the reservation date</li>
              <li>Reservation dates: within next 7 days</li>
              <li>Time slots: 8:00 AM - 6:00 PM (2-hour intervals)</li>
              <li>Cannot reserve after 6:00 PM on the same day</li>
            </ul>
          </div>

          <div className="reservation-form">
            <div className="form-section">
              <h3>Select Date</h3>
              <div className="date-grid">
                {availableDates.map((date) => {
                  const available = isDateAvailable(date);
                  return (
                    <button
                      key={date}
                      className={`date-button ${selectedDate === date ? 'selected' : ''} ${!available ? 'disabled' : ''}`}
                      onClick={() => available && setSelectedDate(date)}
                      disabled={!available}
                    >
                      {formatDate(date)}
                    </button>
                  );
                })}
              </div>
            </div>

            {selectedDate && (
              <div className="form-section">
                <h3>Select Time Slot</h3>
                <div className="timeslot-grid">
                  {availableTimeSlots.length === 0 ? (
                    <p className="no-slots">No available time slots for this date</p>
                  ) : (
                    availableTimeSlots.map((slot) => (
                      <button
                        key={slot}
                        className={`timeslot-button ${selectedTimeSlot === slot ? 'selected' : ''}`}
                        onClick={() => setSelectedTimeSlot(slot)}
                      >
                        {slot}
                      </button>
                    ))
                  )}
                </div>
              </div>
            )}

            <div className="form-actions">
              <button
                className="submit-button"
                onClick={handleCreateReservation}
                disabled={!selectedDate || !selectedTimeSlot || loading}
              >
                {loading ? 'Creating...' : 'Confirm Reservation'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showSuccessModal && successReservation && (
        <div className="modal-overlay" onClick={closeSuccessModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>✓ Reservation Successful!</h2>
            </div>
            <div className="modal-body">
              <p className="success-message">
                Your book has been reserved successfully.
              </p>
              <div className="reminder-box">
                <p><strong>Important Reminder:</strong></p>
                <p>
                  Please return the reserved book by{' '}
                  <strong>{formatDateTime(successReservation.expiryDate)}</strong>
                </p>
                <p className="highlight-text">
                  (Before 6:00 PM on {formatDate(successReservation.reservationDate)})
                </p>
              </div>
            </div>
            <div className="modal-footer">
              <button className="modal-close-button" onClick={closeSuccessModal}>
                Got it!
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReservationPage;

