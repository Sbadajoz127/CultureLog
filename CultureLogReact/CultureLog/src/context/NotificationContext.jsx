import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';
import { useAuth } from './AuthContext';
import {
  getNotifications,
  getUnreadNotifications,
  getUnreadCount,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
  getPendingFollowRequests,
  getPendingFollowRequestsCount,
  acceptFollowRequest as apiAcceptFollow,
  rejectFollowRequest as apiRejectFollow,
} from '../services/api';

const NotificationContext = createContext(null);

const POLL_INTERVAL = 30_000;

export function useNotifications() {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error('useNotifications must be used within NotificationProvider');
  return ctx;
}

export function NotificationProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [pendingRequestsCount, setPendingRequestsCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [unreadNotifications, setUnreadNotifications] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [loadingNotifs, setLoadingNotifs] = useState(false);
  const [loadingUnread, setLoadingUnread] = useState(false);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const intervalRef = useRef(null);

  const fetchUnreadCount = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      const { data } = await getUnreadCount();
      setUnreadCount(data);
    } catch {
      /* silent */
    }
  }, [isAuthenticated]);

  const fetchPendingRequestsCount = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      const { data } = await getPendingFollowRequestsCount();
      setPendingRequestsCount(data);
    } catch {
      /* silent */
    }
  }, [isAuthenticated]);

  const fetchNotifications = useCallback(async (page = 0, size = 5) => {
    if (!isAuthenticated) return;
    setLoadingNotifs(true);
    try {
      const { data } = await getNotifications({ page, size });
      setNotifications(data.content || []);
    } catch {
      /* silent */
    } finally {
      setLoadingNotifs(false);
    }
  }, [isAuthenticated]);

  const fetchUnreadNotifications = useCallback(async (size = 6) => {
    if (!isAuthenticated) return;
    setLoadingUnread(true);
    try {
      const { data } = await getUnreadNotifications({ page: 0, size });
      setUnreadNotifications(data.content || []);
    } catch {
      /* silent */
    } finally {
      setLoadingUnread(false);
    }
  }, [isAuthenticated]);

  const fetchPendingRequests = useCallback(async () => {
    if (!isAuthenticated) return;
    setLoadingRequests(true);
    try {
      const { data } = await getPendingFollowRequests();
      setPendingRequests(data || []);
      setPendingRequestsCount((data || []).length);
    } catch {
      /* silent */
    } finally {
      setLoadingRequests(false);
    }
  }, [isAuthenticated]);

  const markRead = useCallback(async (notificationId) => {
    try {
      await apiMarkAsRead(notificationId);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, read: true } : n))
      );
      setUnreadNotifications((prev) => prev.filter((n) => n.id !== notificationId));
      setUnreadCount((c) => Math.max(0, c - 1));
    } catch {
      /* silent */
    }
  }, []);

  const markAllRead = useCallback(async () => {
    try {
      await apiMarkAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadNotifications([]);
      setUnreadCount(0);
    } catch {
      /* silent */
    }
  }, []);

  const acceptRequest = useCallback(async (followerId) => {
    try {
      await apiAcceptFollow(followerId);
      setPendingRequests((prev) => prev.filter((r) => r.followerId !== followerId));
      setPendingRequestsCount((c) => Math.max(0, c - 1));
      toast.success('Solicitud de seguimiento aceptada.');
    } catch {
      toast.error('No se pudo aceptar la solicitud.');
    }
  }, []);

  const rejectRequest = useCallback(async (followerId) => {
    try {
      await apiRejectFollow(followerId);
      setPendingRequests((prev) => prev.filter((r) => r.followerId !== followerId));
      setPendingRequestsCount((c) => Math.max(0, c - 1));
      toast.success('Solicitud de seguimiento rechazada.');
    } catch {
      toast.error('No se pudo rechazar la solicitud.');
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      setPendingRequestsCount(0);
      setNotifications([]);
      setUnreadNotifications([]);
      setPendingRequests([]);
      return;
    }

    fetchUnreadCount();
    fetchPendingRequestsCount();
    intervalRef.current = setInterval(() => {
      fetchUnreadCount();
      fetchPendingRequestsCount();
    }, POLL_INTERVAL);
    return () => clearInterval(intervalRef.current);
  }, [isAuthenticated, fetchUnreadCount, fetchPendingRequestsCount]);

  const value = {
    unreadCount,
    pendingRequestsCount,
    notifications,
    unreadNotifications,
    pendingRequests,
    loadingNotifs,
    loadingUnread,
    loadingRequests,
    fetchNotifications,
    fetchUnreadNotifications,
    fetchPendingRequests,
    fetchPendingRequestsCount,
    fetchUnreadCount,
    markRead,
    markAllRead,
    acceptRequest,
    rejectRequest,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}
