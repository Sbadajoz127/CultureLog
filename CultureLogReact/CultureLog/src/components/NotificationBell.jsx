import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Heart, MessageCircle, UserPlus, BookOpen, Check, X } from 'lucide-react';
import { useNotifications } from '../context/NotificationContext';
import { UserAvatar } from './UserAvatar';
import '../App.css';

const ICON_MAP = {
  LIKE_POST: Heart,
  COMENTARIO_POST: MessageCircle,
  NUEVO_SEGUIDOR: UserPlus,
  NUEVO_POST: BookOpen,
};

function timeAgo(dateStr) {
  const now = new Date();
  const date = new Date(dateStr);
  const seconds = Math.floor((now - date) / 1000);
  if (seconds < 60) return 'ahora';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `hace ${minutes}m`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `hace ${hours}h`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `hace ${days}d`;
  return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
}

const MAX_DROPDOWN_ITEMS = 6;

export function NotificationBell() {
  const navigate = useNavigate();
  const {
    unreadCount,
    unreadNotifications,
    pendingRequests,
    loadingUnread,
    loadingRequests,
    fetchUnreadNotifications,
    fetchPendingRequests,
    markRead,
    acceptRequest,
    rejectRequest,
  } = useNotifications();

  const [open, setOpen] = useState(false);
  const [tab, setTab] = useState('activity');
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    if (tab === 'activity') fetchUnreadNotifications(MAX_DROPDOWN_ITEMS);
    else fetchPendingRequests();
  }, [open, tab, fetchUnreadNotifications, fetchPendingRequests]);

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    if (open) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  function handleNotificationClick(notif) {
    if (!notif.read) markRead(notif.id);
    setOpen(false);
    if (notif.type === 'NUEVO_SEGUIDOR') {
      navigate(`/user/${notif.actorName}`);
    } else if (notif.referenceId) {
      navigate('/home');
    }
  }

  function handleGoToCenter() {
    setOpen(false);
    navigate('/notifications');
  }

  return (
    <div className="notification-bell-wrapper" ref={dropdownRef}>
      <button
        type="button"
        className="notification-bell-btn"
        onClick={() => setOpen((v) => !v)}
        aria-label="Notificaciones"
      >
        <Bell size={22} />
        {unreadCount > 0 && (
          <span className="notification-badge">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="notification-dropdown">
          <div className="notification-dropdown-header">
            <h3>Notificaciones</h3>
          </div>

          <div className="notification-tabs">
            <button
              type="button"
              className={`notification-tab ${tab === 'activity' ? 'active' : ''}`}
              onClick={() => setTab('activity')}
            >
              Actividad
            </button>
            <button
              type="button"
              className={`notification-tab ${tab === 'requests' ? 'active' : ''}`}
              onClick={() => setTab('requests')}
            >
              Solicitudes
              {pendingRequests.length > 0 && (
                <span className="notification-tab-badge">{pendingRequests.length}</span>
              )}
            </button>
          </div>

          <div className="notification-dropdown-body">
            {tab === 'activity' && (
              <>
                {loadingUnread && unreadNotifications.length === 0 && (
                  <div className="notification-empty">Cargando...</div>
                )}
                {!loadingUnread && unreadNotifications.length === 0 && (
                  <div className="notification-empty">No tienes notificaciones sin leer</div>
                )}
                {unreadNotifications.map((notif) => {
                  const Icon = ICON_MAP[notif.type] || Bell;
                  return (
                    <button
                      key={notif.id}
                      type="button"
                      className="notification-item unread"
                      onClick={() => handleNotificationClick(notif)}
                    >
                      <div className="notification-item-icon">
                        <Icon size={18} />
                      </div>
                      <div className="notification-item-content">
                        <p>
                          <strong>{notif.actorName}</strong> {notif.message}
                        </p>
                        <span className="notification-item-time">{timeAgo(notif.createdAt)}</span>
                      </div>
                      <span className="notification-unread-dot" />
                    </button>
                  );
                })}
                {unreadCount > MAX_DROPDOWN_ITEMS && (
                  <div className="notification-dropdown-overflow">
                    +{unreadCount - MAX_DROPDOWN_ITEMS} más sin leer
                  </div>
                )}
              </>
            )}

            {tab === 'requests' && (
              <>
                {loadingRequests && pendingRequests.length === 0 && (
                  <div className="notification-empty">Cargando...</div>
                )}
                {!loadingRequests && pendingRequests.length === 0 && (
                  <div className="notification-empty">No tienes solicitudes pendientes</div>
                )}
                {pendingRequests.map((req) => (
                  <div key={req.followerId} className="notification-request-item">
                    <div
                      className="notification-request-user"
                      onClick={() => { setOpen(false); navigate(`/user/${req.followerUsername}`); }}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => { if (e.key === 'Enter') { setOpen(false); navigate(`/user/${req.followerUsername}`); } }}
                    >
                      <UserAvatar src={req.followerProfilePicture} name={req.followerUsername} size="small" />
                      <span className="notification-request-name">@{req.followerUsername}</span>
                    </div>
                    <div className="notification-request-actions">
                      <button
                        type="button"
                        className="notification-request-accept"
                        onClick={() => acceptRequest(req.followerId)}
                        aria-label="Aceptar"
                      >
                        <Check size={16} />
                      </button>
                      <button
                        type="button"
                        className="notification-request-reject"
                        onClick={() => rejectRequest(req.followerId)}
                        aria-label="Rechazar"
                      >
                        <X size={16} />
                      </button>
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>

          <div className="notification-dropdown-footer">
            <button type="button" className="notification-center-link" onClick={handleGoToCenter}>
              Ir al Centro de Notificaciones
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
