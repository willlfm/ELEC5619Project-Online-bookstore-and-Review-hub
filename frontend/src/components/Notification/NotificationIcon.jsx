import React, { useState, useEffect } from 'react';
import { getUnreadNotifications } from '../../services/notification';
import NotificationPanel from './NotificationPanel';
import './NotificationIcon.css';

const NotificationIcon = () => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [isPanelOpen, setIsPanelOpen] = useState(false);

  useEffect(() => {
    loadUnreadCount();
    
    // Poll for new notifications every 30 seconds
    const interval = setInterval(() => {
      loadUnreadCount();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  const loadUnreadCount = async () => {
    try {
      const notifications = await getUnreadNotifications();
      setUnreadCount(notifications.length);
    } catch (err) {
      // Silently fail - user might not be logged in
      console.debug('Could not load unread notifications:', err);
    }
  };

  const handleOpenPanel = () => {
    setIsPanelOpen(true);
  };

  const handleClosePanel = () => {
    setIsPanelOpen(false);
    // Reload count after closing panel
    loadUnreadCount();
  };

  return (
    <>
      <div className="notification-icon-wrapper">
        <button
          className="notification-icon-btn"
          onClick={handleOpenPanel}
          aria-label="Notifications"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="notification-bell-icon"
          >
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          {unreadCount > 0 && (
            <span className="notification-badge">{unreadCount}</span>
          )}
        </button>
      </div>

      <NotificationPanel isOpen={isPanelOpen} onClose={handleClosePanel} />
    </>
  );
};

export default NotificationIcon;

