import { useCallback, useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { PostCard } from '../components/PostCard';
import { FEED_TABS, postMatchesFeedTab } from '../constants/media';
import { toast } from 'sonner';
import {
  getFeed,
  getSuggestedUsers,
  followUser,
} from '../services/api';
import { useKeyedAsyncLock } from '../hooks/useAsyncAction';
import './Home.css';

const FEED_PAGE_SIZE = 10;

function SkeletonTabs() {
  return (
    <div className="skeleton-tabs">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={i} className="skeleton skeleton-tab" />
      ))}
    </div>
  );
}

function SkeletonPost() {
  return (
    <div className="skeleton-card">
      <div className="skeleton-post-header">
        <div className="skeleton-post-author">
          <div className="skeleton skeleton-circle skeleton-avatar" />
          <div className="skeleton-post-author-lines">
            <div className="skeleton skeleton-author-name" />
            <div className="skeleton skeleton-author-tag" />
          </div>
        </div>
        <div className="skeleton skeleton-date" />
      </div>
      <div className="skeleton skeleton-linked-work" />
      <div className="skeleton skeleton-content-l1" />
      <div className="skeleton skeleton-content-l2" />
      <div className="skeleton skeleton-content-l3" />
      <div className="skeleton-post-footer">
        <div className="skeleton skeleton-like-btn" />
      </div>
    </div>
  );
}

function SkeletonSidebar() {
  return (
    <div className="skeleton-sidebar-card">
      <div className="skeleton skeleton-sidebar-title" />
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="skeleton-suggestion">
          <div className="skeleton-suggestion-user">
            <div className="skeleton skeleton-circle skeleton-suggestion-avatar" />
            <div className="skeleton skeleton-suggestion-name" />
          </div>
          <div className="skeleton skeleton-suggestion-btn" />
        </div>
      ))}
    </div>
  );
}

function Home() {
  const { user } = useAuth();
  const { profilePic } = useProfilePic();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('GENERAL');
  const [posts, setPosts] = useState([]);
  const [feedPage, setFeedPage] = useState(0);
  const [feedLast, setFeedLast] = useState(true);
  const [feedLoading, setFeedLoading] = useState(true);
  const [feedError, setFeedError] = useState('');

  const [suggestions, setSuggestions] = useState([]);
  const [followingIds, setFollowingIds] = useState(new Set());
  const [pendingIds, setPendingIds] = useState(new Set());

  const [initialReady, setInitialReady] = useState(false);
  const feedDone = useRef(false);
  const suggestionsDone = useRef(false);

  const markReady = useCallback(() => {
    if (feedDone.current && suggestionsDone.current) setInitialReady(true);
  }, []);

  const loadFeed = useCallback(async (page, append) => {
    setFeedLoading(true);
    setFeedError('');
    try {
      const { data } = await getFeed({ page, size: FEED_PAGE_SIZE });
      const content = data?.content ?? [];
      setFeedLast(data?.last ?? true);
      setPosts((prev) => (append ? [...prev, ...content] : content));
    } catch (e) {
      setFeedError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar el muro.'
      );
      if (!append) setPosts([]);
    } finally {
      setFeedLoading(false);
      if (!feedDone.current) {
        feedDone.current = true;
        markReady();
      }
    }
  }, [markReady]);

  useEffect(() => {
    setFeedPage(0);
    loadFeed(0, false);
  }, [loadFeed]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await getSuggestedUsers();
        if (!cancelled) setSuggestions(Array.isArray(data) ? data : []);
      } catch {
        /* ignore */
      } finally {
        if (!cancelled && !suggestionsDone.current) {
          suggestionsDone.current = true;
          markReady();
        }
      }
    })();
    return () => { cancelled = true; };
  }, [markReady]);

  const handlePostUpdate = (postId, updates) => {
    setPosts((prev) => prev.map((p) => (p.id === postId ? { ...p, ...updates } : p)));
  };

  const handlePostDelete = (postId) => {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  };

  const runFollowLocked = useKeyedAsyncLock();

  const handleFollow = (targetId) => {
    if (followingIds.has(targetId) || pendingIds.has(targetId)) return;
    return runFollowLocked(targetId, async () => {
      try {
        const { data } = await followUser(targetId);
        if (data?.status === 'PENDING') {
          setPendingIds((prev) => new Set(prev).add(targetId));
          toast.success('Solicitud de seguimiento enviada.');
        } else {
          setFollowingIds((prev) => new Set(prev).add(targetId));
          toast.success('Ahora sigues a este usuario.');
          loadFeed(0, false);
          setFeedPage(0);
        }
      } catch {
        toast.error('No se pudo seguir al usuario.');
      }
    });
  };

  const loadMore = () => {
    const next = feedPage + 1;
    setFeedPage(next);
    loadFeed(next, true);
  };

  const filteredPosts = posts.filter((p) => postMatchesFeedTab(p, activeTab));

  const renderSuggestionsInline = () => {
    if (suggestions.length === 0) return null;
    return (
      <div className="suggestions-card suggestions-inline">
        <h4 className="suggestions-title">Sugerencias para ti</h4>
        <div className="suggestions-list suggestions-list-inline">
          {suggestions.slice(0, 3).map((u) => {
            const isFollowed = followingIds.has(u.id);
            const isPending = pendingIds.has(u.id);
            return (
              <div key={u.id} className={`suggestion-item suggestion-item-inline ${isFollowed || isPending ? 'followed' : ''}`}>
                <div
                  className="suggestion-user suggestion-user-link"
                  role="button"
                  tabIndex={0}
                  onClick={() => navigate(`/user/${u.username}`)}
                  onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${u.username}`); }}
                >
                  <UserAvatar src={u.profilePictureUrl} name={u.username} size="small" />
                  <span className="suggestion-username">@{u.username}</span>
                </div>
                <button
                  type="button"
                  className={`suggestion-follow-btn ${isFollowed ? 'following' : ''} ${isPending ? 'pending' : ''}`}
                  onClick={() => handleFollow(u.id)}
                  disabled={isFollowed || isPending}
                >
                  {isFollowed ? 'Siguiendo' : isPending ? 'Pendiente' : 'Seguir'}
                </button>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  if (!initialReady) {
    return (
      <div className="home-container">
        <AppHeader active="home" userName={user.username} />
        <div className="home-content">
          <main className="feed">
            <SkeletonTabs />
            <div className="posts-list">
              <SkeletonPost />
              <SkeletonPost />
              <SkeletonPost />
            </div>
          </main>
          <aside className="home-sidebar">
            <SkeletonSidebar />
          </aside>
        </div>
      </div>
    );
  }

  return (
    <div className="home-container">
      <AppHeader active="home" userName={user.username} />

      <div className="home-content">
        <main className="feed feed-loaded">
          <div className="create-post-cta">
            <UserAvatar src={profilePic} name={user.username} size="small" />
            <button
              type="button"
              className="create-post-cta-btn"
              onClick={() => navigate('/posts/create')}
            >
              ¿Qué te ha parecido? Crea una publicación…
            </button>
          </div>

          <nav className="feed-category-nav" aria-label="Filtrar muro por tipo de obra vinculada">
            {FEED_TABS.map((tab) => (
              <button
                key={tab.id}
                type="button"
                className={`category-btn ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          {feedError && <p className="auth-error">{feedError}</p>}

          <div className="posts-list">
            {filteredPosts.length > 0 ? (
              filteredPosts.map((post, index) => (
                <div key={post.id}>
                  <PostCard
                    post={post}
                    onPostUpdate={handlePostUpdate}
                    onPostDelete={handlePostDelete}
                    showAuthorLink
                  />
                  {index === 2 && renderSuggestionsInline()}
                </div>
              ))
            ) : (
              <div className="bg-card feed-placeholder">
                <p className="text-faint">No hay publicaciones en esta vista.</p>
                <p className="text-muted feed-placeholder-sub">Prueba otra pestaña o publica algo nuevo.</p>
              </div>
            )}
          </div>

          {!feedLast && !feedLoading && filteredPosts.length > 0 && (
            <button type="button" className="logout-button feed-load-more" onClick={loadMore}>
              Cargar más
            </button>
          )}
          {feedLoading && posts.length > 0 && (
            <p className="text-muted feed-loading-more">Cargando…</p>
          )}
        </main>

        <aside className="home-sidebar feed-loaded">
          {suggestions.length > 0 && (
            <div className="suggestions-card">
              <h4 className="suggestions-title">Sugerencias para ti</h4>
              <div className="suggestions-list">
                {suggestions.slice(0, 3).map((u) => {
                  const isFollowed = followingIds.has(u.id);
                  const isPending = pendingIds.has(u.id);
                  return (
                    <div key={u.id} className={`suggestion-item ${isFollowed || isPending ? 'followed' : ''}`}>
                      <div
                        className="suggestion-user suggestion-user-link"
                        role="button"
                        tabIndex={0}
                        onClick={() => navigate(`/user/${u.username}`)}
                        onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${u.username}`); }}
                      >
                        <UserAvatar src={u.profilePictureUrl} name={u.username} size="small" />
                        <span className="suggestion-username">@{u.username}</span>
                      </div>
                      <button
                        type="button"
                        className={`suggestion-follow-btn ${isFollowed ? 'following' : ''} ${isPending ? 'pending' : ''}`}
                        onClick={() => handleFollow(u.id)}
                        disabled={isFollowed || isPending}
                      >
                        {isFollowed ? 'Siguiendo' : isPending ? 'Pendiente' : 'Seguir'}
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}

export default Home;
