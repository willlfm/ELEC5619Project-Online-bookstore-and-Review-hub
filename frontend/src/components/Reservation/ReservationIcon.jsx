import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserReservations } from '../../services/reservation';
import './ReservationIcon.css';

/**
 * Reservation icon component with active count badge
 */
const ReservationIcon = () => {
  const navigate = useNavigate();
  const [reservationCount, setReservationCount] = useState(0);

  useEffect(() => {
    loadReservationCount();
  }, []);

  const loadReservationCount = async () => {
    try {
      const reservations = await getUserReservations();
      // Count all reservations except cancelled and returned ones
      const activeCount = reservations.filter(r => 
        r.status !== 'reservation_cancelled' && r.status !== 'returned'
      ).length;
      setReservationCount(activeCount);
    } catch (error) {
      console.error("Failed to load reservations:", error);
    }
  };

  const handleClick = () => {
    navigate('/reservations');
  };

  return (
    <div className="reservation-icon" onClick={handleClick}>
      <div className="reservation-icon-container">
        <svg
          className="reservation-icon-svg"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          {/* Calendar */}
          <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
          <line x1="3" y1="8" x2="21" y2="8" stroke="currentColor" strokeWidth="2"/>
          <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          
          {/* Book mark/ribbon */}
          <path d="M12 11 L12 19 L9 17 L12 15 L15 17 L12 19 Z" fill="currentColor"/>
        </svg>
        {reservationCount > 0 && (
          <span className="reservation-badge">{reservationCount}</span>
        )}
      </div>
    </div>
  );
};

export default ReservationIcon;

