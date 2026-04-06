import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell, Heart, MessageCircle, UserPlus, BookOpen,
  Check, X, CheckCheck, Inbox, Users,
} from 'lucide-react';
import { useAuth } from './context/AuthContext';
import { useNotifications } from './context/NotificationContext';
import { getNotifications } from './services/api';
import { AppHeader } from './components/AppHeader';
import { UserAvatar } from './components/UserAvatar';
import './App.css';

const ICON_MAP = {
  LIKE_POST: Heart,
  COMENTARIO_POST: MessageCircle,
  NUEVO_SEGUIDOR: UserPlus,
  SOLICITUD_SEGUIMIENTO: UserPlus,
  SOLICITUD_ACEPTADA: UserPlus,
  NUEVO_POST: BookOpen,
};

const TYPE_LABELS = {
  LIKE_POST: 'Me gusta',
  COMENTARIO_POST: 'Comentario',
  NUEVO_SEGUIDOR: 'Nuevo seguidor',
  SOLICITUD_SEGUIMIENTO: 'Solicitud de seguimiento',
  SOLICITUD_ACEPTADA: 'Solicitud aceptada',
  NUEVO_POST: 'Nueva publicación',
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

function dateLabel(dateStr) {
  const date = new Date(dateStr);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const dateDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const diff = Math.floor((today - dateDay) / 86400000);
  if (diff === 0) return 'Hoy';
  if (diff === 1) return 'Ayer';
  if (diff < 7) return `Hace ${diff} días`;
  return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });
}

function groupByDate(items) {
  const groups = [];
  let currentLabel = null;
  for (const item of items) {
    const label = dateLabel(item.createdAt);
    if (label !== currentLabel) {
      currentLabel = label;
      groups.push({ label, items: [] });
    }
    groups[groups.length - 1].items.push(item);
  }
  return groups;
}

function SkeletonNotifCard() {
  return (
    <div className="nc-skeleton-card">
      <div className="skeleton skeleton-circle nc-skeleton-icon" />
      <div className="nc-skeleton-body">
        <div className="skeleton nc-skeleton-text-long" />
        <div className="nc-skeleton-meta-row">
          <div className="skeleton nc-skeleton-tag" />
          <div className="skeleton nc-skeleton-time" />
        </div>
      </div>
    </div>
  );
}

function SkeletonRequestCard() {
  return (
    <div className="nc-skeleton-request">
      <div className="nc-skeleton-request-user">
        <div className="skeleton skeleton-circle nc-skeleton-avatar" />
        <div className="nc-skeleton-request-lines">
          <div className="skeleton nc-skeleton-username" />
          <div className="skeleton nc-skeleton-req-time" />
        </div>
      </div>
      <div className="nc-skeleton-request-btns">
        <div className="skeleton nc-skeleton-action-btn" />
        <div className="skeleton nc-skeleton-action-btn" />
      </div>
    </div>
  );
}

function SkeletonActivityList() {
  return (
    <div className="nc-activity-list">
      <div className="nc-date-group">
        <div className="nc-date-separator">
          <div className="skeleton nc-skeleton-date-label" />
        </div>
        <SkeletonNotifCard />
        <SkeletonNotifCard />
        <SkeletonNotifCard />
      </div>
      <div className="nc-date-group">
        <div className="nc-date-separator">
          <div className="skeleton nc-skeleton-date-label" />
        </div>
        <SkeletonNotifCard />
        <SkeletonNotifCard />
      </div>
    </div>
  );
}

function SkeletonRequestsList() {
  return (
    <div className="nc-requests-list">
      <SkeletonRequestCard />
      <SkeletonRequestCard />
      <SkeletonRequestCard />
    </div>
  );
}

const PAGE_SIZE = 15;

export default function Notifications() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const {
    pendingRequests,
    loadingRequests,
    fetchPendingRequests,
    markRead,
    markAllRead,
    acceptRequest,
    rejectRequest,
  } = useNotifications();

  const [tab, setTab] = useState('activity');
  const [allNotifs, setAllNotifs] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [initialReady, setInitialReady] = useState(false);

  const loadPage = useCallback(async (pageNum) => {
    setLoading(true);
    try {
      const { data } = await getNotifications({ page: pageNum, size: PAGE_SIZE });
      const content = data.content || [];
      if (pageNum === 0) {
        setAllNotifs(content);
      } else {
        setAllNotifs((prev) => [...prev, ...content]);
      }
      setHasMore(!data.last);
    } catch {
      /* silent */
    } finally {
      setLoading(false);
      setInitialReady(true);
    }
  }, []);

  useEffect(() => {
    loadPage(0);
  }, [loadPage]);

  useEffect(() => {
    if (tab === 'requests') fetchPendingRequests();
  }, [tab, fetchPendingRequests]);

  function handleLoadMore() {
    const next = page + 1;
    setPage(next);
    loadPage(next);
  }

  function handleNotificationClick(notif) {
    if (!notif.read) {
      markRead(notif.id);
      setAllNotifs((prev) =>
        prev.map((n) => (n.id === notif.id ? { ...n, read: true } : n))
      );
    }
    if (notif.type === 'NUEVO_SEGUIDOR' || notif.type === 'SOLICITUD_SEGUIMIENTO' || notif.type === 'SOLICITUD_ACEPTADA') {
      navigate(`/user/${notif.actorName}`);
    } else if (notif.referenceId) {
      navigate('/home');
    }
  }

  function handleMarkAllRead() {
    markAllRead();
    setAllNotifs((prev) => prev.map((n) => ({ ...n, read: true })));
  }

  const unreadLocal = allNotifs.filter((n) => !n.read).length;
  const grouped = groupByDate(allNotifs);

  return (
    <>
      <AppHeader userName={user?.username} />
      <div className="nc-layout">
        {/* --- Sidebar --- */}
        <aside className="nc-sidebar">
          <nav className="nc-sidebar-nav">
            <button
              type="button"
              className={`nc-sidebar-item ${tab === 'activity' ? 'active' : ''}`}
              onClick={() => setTab('activity')}
            >
              <Inbox size={18} />
              <span>Actividad</span>
              {unreadLocal > 0 && <span className="nc-sidebar-badge">{unreadLocal}</span>}
            </button>
            <button
              type="button"
              className={`nc-sidebar-item ${tab === 'requests' ? 'active' : ''}`}
              onClick={() => setTab('requests')}
            >
              <Users size={18} />
              <span>Solicitudes</span>
              {pendingRequests.length > 0 && (
                <span className="nc-sidebar-badge">{pendingRequests.length}</span>
              )}
            </button>
          </nav>
        </aside>

        {/* --- Main content --- */}
        <main className="nc-main">
          {/* Header bar */}
          <div className="nc-topbar">
            <h1 className="nc-title">
              {tab === 'activity' ? 'Actividad' : 'Solicitudes de seguimiento'}
            </h1>
            {tab === 'activity' && unreadLocal > 0 && (
              <button type="button" className="nc-mark-all-btn" onClick={handleMarkAllRead}>
                <CheckCheck size={15} />
                Marcar todas como leídas
              </button>
            )}
          </div>

          {/* Activity tab */}
          {tab === 'activity' && !initialReady && <SkeletonActivityList />}
          {tab === 'activity' && initialReady && (
            <div className="nc-activity-list feed-loaded">
              {allNotifs.length === 0 && (
                <div className="nc-empty-state">
                  <Bell size={40} className="nc-empty-icon" />
                  <h3>Todo al día</h3>
                  <p>No tienes notificaciones todavía</p>
                </div>
              )}
              {grouped.map((group) => (
                <div key={group.label} className="nc-date-group">
                  <div className="nc-date-separator">
                    <span>{group.label}</span>
                  </div>
                  {group.items.map((notif) => {
                    const Icon = ICON_MAP[notif.type] || Bell;
                    return (
                      <button
                        key={notif.id}
                        type="button"
                        className={`nc-notif-card ${!notif.read ? 'nc-notif-unread' : ''}`}
                        onClick={() => handleNotificationClick(notif)}
                      >
                        <div className={`nc-notif-icon nc-notif-icon--${notif.type.toLowerCase()}`}>
                          <Icon size={18} />
                        </div>
                        <div className="nc-notif-body">
                          <p className="nc-notif-text">
                            <strong>{notif.actorName}</strong> {notif.message}
                          </p>
                          <div className="nc-notif-meta">
                            <span className="nc-notif-type-tag">{TYPE_LABELS[notif.type]}</span>
                            <span className="nc-notif-time">{timeAgo(notif.createdAt)}</span>
                          </div>
                        </div>
                        {!notif.read && <span className="nc-notif-dot" />}
                      </button>
                    );
                  })}
                </div>
              ))}
              {hasMore && allNotifs.length > 0 && (
                <button
                  type="button"
                  className="nc-load-more"
                  onClick={handleLoadMore}
                  disabled={loading}
                >
                  {loading ? 'Cargando...' : 'Cargar más notificaciones'}
                </button>
              )}
            </div>
          )}

          {/* Requests tab */}
          {tab === 'requests' && loadingRequests && pendingRequests.length === 0 && (
            <SkeletonRequestsList />
          )}
          {tab === 'requests' && !(loadingRequests && pendingRequests.length === 0) && (
            <div className="nc-requests-list feed-loaded">
              {!loadingRequests && pendingRequests.length === 0 && (
                <div className="nc-empty-state">
                  <Users size={40} className="nc-empty-icon" />
                  <h3>Sin solicitudes</h3>
                  <p>No tienes solicitudes de seguimiento pendientes</p>
                </div>
              )}
              {pendingRequests.map((req) => (
                <div key={req.followerId} className="nc-request-card">
                  <div
                    className="nc-request-user"
                    onClick={() => navigate(`/user/${req.followerUsername}`)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${req.followerUsername}`); }}
                  >
                    <UserAvatar src={req.followerProfilePicture} name={req.followerUsername} size="small" />
                    <div className="nc-request-user-info">
                      <span className="nc-request-username">@{req.followerUsername}</span>
                      <span className="nc-request-time">{timeAgo(req.createdAt)}</span>
                    </div>
                  </div>
                  <div className="nc-request-actions">
                    <button
                      type="button"
                      className="nc-request-btn nc-request-btn--accept"
                      onClick={() => acceptRequest(req.followerId)}
                    >
                      <Check size={15} />
                      Aceptar
                    </button>
                    <button
                      type="button"
                      className="nc-request-btn nc-request-btn--reject"
                      onClick={() => rejectRequest(req.followerId)}
                    >
                      <X size={15} />
                      Rechazar
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>
    </>
  );
}
