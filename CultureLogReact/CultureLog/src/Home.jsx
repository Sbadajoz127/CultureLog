import { useCallback, useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { AppHeader } from './components/AppHeader';
import { UserAvatar } from './components/UserAvatar';
import { Heart } from 'lucide-react';
import { FEED_TABS, MEDIA_TYPE_LABELS, postMatchesFeedTab } from './constants/media';
import { searchResultToPayload } from './utils/mediaItem';
import {
  getFeed,
  createPost,
  togglePostLike,
  searchMedia,
  addToLibraryFromSearch,
  getMediaItems,
  getSuggestedUsers,
  followUser,
} from './services/api';
import './App.css';

const FEED_PAGE_SIZE = 10;
const MAX_POST_LENGTH = 2000;

function formatFeedDate(iso) {
  if (!iso) return '';
  try {
    const d = new Date(iso);
    return d.toLocaleString(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  } catch {
    return iso;
  }
}

function SkeletonComposer() {
  return (
    <div className="skeleton-composer">
      <div className="skeleton-composer-row">
        <div className="skeleton skeleton-input" />
        <div className="skeleton skeleton-btn" />
      </div>
      <div className="skeleton skeleton-textarea" />
      <div className="skeleton-composer-footer">
        <div className="skeleton skeleton-counter" />
        <div className="skeleton skeleton-submit" />
      </div>
    </div>
  );
}

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
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('GENERAL');
  const [posts, setPosts] = useState([]);
  const [feedPage, setFeedPage] = useState(0);
  const [feedLast, setFeedLast] = useState(true);
  const [feedLoading, setFeedLoading] = useState(true);
  const [feedError, setFeedError] = useState('');

  const [newPostContent, setNewPostContent] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [libraryItems, setLibraryItems] = useState([]);
  const [libraryOpen, setLibraryOpen] = useState(false);
  const [linkedItem, setLinkedItem] = useState(null);
  const [composerError, setComposerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const [suggestions, setSuggestions] = useState([]);
  const [followingIds, setFollowingIds] = useState(new Set());

  const [likingPostId, setLikingPostId] = useState(null);
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

  const loadLibraryPicker = useCallback(async () => {
    try {
      const { data } = await getMediaItems({});
      setLibraryItems(Array.isArray(data) ? data : []);
    } catch {
      setLibraryItems([]);
    }
  }, []);

  useEffect(() => {
    if (libraryOpen) loadLibraryPicker();
  }, [libraryOpen, loadLibraryPicker]);

  const runWorkSearch = useCallback(async () => {
    const q = searchTerm.trim();
    if (q.length < 2) {
      setSearchResults([]);
      return;
    }
    setSearchLoading(true);
    try {
      const { data } = await searchMedia({ query: q, page: 0 });
      setSearchResults(Array.isArray(data) ? data : []);
    } catch {
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
    }
  }, [searchTerm]);

  useEffect(() => {
    const t = setTimeout(() => {
      if (searchTerm.trim().length >= 2 && !linkedItem) runWorkSearch();
      else setSearchResults([]);
    }, 400);
    return () => clearTimeout(t);
  }, [searchTerm, linkedItem, runWorkSearch]);

  const attachSearchResultAsLinked = async (result) => {
    setComposerError('');
    try {
      const { data: item } = await addToLibraryFromSearch(searchResultToPayload(result));
      setLinkedItem({
        id: item.id,
        title: item.title,
        type: item.type,
        releaseDate: item.releaseDate,
      });
      setSearchTerm('');
      setSearchResults([]);
    } catch (err) {
      setComposerError(
        err.response?.data?.message || err.response?.data?.error || 'No se pudo vincular la obra.'
      );
    }
  };

  const handlePostSubmit = async (e) => {
    e.preventDefault();
    if (!newPostContent.trim()) return;
    setSubmitting(true);
    setComposerError('');
    try {
      await createPost({
        content: newPostContent.trim(),
        linkedMediaItemId: linkedItem?.id ?? null,
      });
      setNewPostContent('');
      setLinkedItem(null);
      setFeedPage(0);
      await loadFeed(0, false);
    } catch (err) {
      setComposerError(
        err.response?.data?.message || err.response?.data?.error || 'No se pudo publicar.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleLike = async (post) => {
    if (likingPostId === post.id) return;
    setLikingPostId(post.id);
    try {
      const { data } = await togglePostLike(post.id);
      setPosts((prev) =>
        prev.map((p) =>
          p.id === post.id
            ? {
                ...p,
                likedByCurrentUser: data.liked,
                likeCount: data.likeCount,
              }
            : p
        )
      );
    } catch {
      /* ignore */
    } finally {
      setLikingPostId(null);
    }
  };

  const handleFollow = async (targetId) => {
    if (followingIds.has(targetId)) return;
    try {
      await followUser(targetId);
      setFollowingIds((prev) => new Set(prev).add(targetId));
      loadFeed(0, false);
      setFeedPage(0);
    } catch {
      /* ignore */
    }
  };

  const loadMore = () => {
    const next = feedPage + 1;
    setFeedPage(next);
    loadFeed(next, true);
  };

  const filteredPosts = posts.filter((p) => postMatchesFeedTab(p, activeTab));

  if (!initialReady) {
    return (
      <div className="home-container">
        <AppHeader active="home" userName={user.username} />
        <div className="home-content">
          <main className="feed">
            <SkeletonComposer />
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
          <div className="create-post-card">
            <form onSubmit={handlePostSubmit}>
              {composerError && <p className="auth-error">{composerError}</p>}

              <div className="composer-work-wrap">
                {!linkedItem ? (
                  <>
                    <div className="composer-work-row">
                      <input
                        type="text"
                        placeholder="Buscar obra para vincular (API)…"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="portal-input composer-search-input"
                        aria-label="Buscar obra en catálogos externos"
                      />
                      <button
                        type="button"
                        className="logout-button composer-library-toggle"
                        onClick={() => setLibraryOpen((o) => !o)}
                      >
                        {libraryOpen ? 'Ocultar biblioteca' : 'Desde mi biblioteca'}
                      </button>
                    </div>
                    {libraryOpen && (
                      <div className="library-picker-panel">
                        <p className="text-muted library-picker-label">Tus ítems</p>
                        {libraryItems.length === 0 ? (
                          <p className="text-muted library-picker-empty">No tienes ítems en tu biblioteca.</p>
                        ) : (
                          <div className="library-picker-grid">
                            {libraryItems.map((it) => (
                              <button
                                key={it.id}
                                type="button"
                                className="library-picker-item"
                                onClick={() => {
                                  setLinkedItem({
                                    id: it.id,
                                    title: it.title,
                                    type: it.type,
                                    releaseDate: it.releaseDate,
                                  });
                                  setSearchTerm('');
                                  setSearchResults([]);
                                  setLibraryOpen(false);
                                }}
                              >
                                <span className="library-picker-item-title">{it.title}</span>
                                <span className="library-picker-item-badge">
                                  {MEDIA_TYPE_LABELS[it.type] || it.type}
                                </span>
                              </button>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                    {searchLoading && <p className="text-muted composer-hint">Buscando…</p>}
                    {searchResults.length > 0 && (
                      <div className="search-dropdown composer-search-dropdown">
                        {searchResults.map((result, idx) => (
                          <div
                            key={`${result.source}-${result.externalId}-${idx}`}
                            className="search-result-item"
                            role="button"
                            tabIndex={0}
                            onClick={() => attachSearchResultAsLinked(result)}
                            onKeyDown={(ev) => {
                              if (ev.key === 'Enter' || ev.key === ' ') {
                                ev.preventDefault();
                                attachSearchResultAsLinked(result);
                              }
                            }}
                          >
                            <strong>{result.title}</strong>
                            <span className="search-result-meta">
                              {result.releaseDate ? `${result.releaseDate} · ` : ''}
                              {result.creator || ''}
                            </span>
                            <span className="post-category-tag search-result-type">
                              {MEDIA_TYPE_LABELS[result.type] || result.type}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </>
                ) : (
                  <div className="selected-work-box">
                    <span>
                      Vinculado a: <strong>{linkedItem.title}</strong>
                      {linkedItem.releaseDate && (
                        <span className="text-muted"> ({linkedItem.releaseDate})</span>
                      )}
                    </span>
                    <button
                      type="button"
                      onClick={() => setLinkedItem(null)}
                      className="remove-work-btn"
                      aria-label="Quitar obra vinculada"
                    >
                      Quitar
                    </button>
                  </div>
                )}
              </div>

              <textarea
                placeholder="¿Qué te ha parecido?"
                value={newPostContent}
                onChange={(e) => setNewPostContent(e.target.value)}
                maxLength={MAX_POST_LENGTH}
              />
              <div className="post-actions">
                <span className="text-dim post-char-count">
                  {newPostContent.length}/{MAX_POST_LENGTH}
                </span>
                <button type="submit" className="login-button post-submit-btn" disabled={submitting}>
                  {submitting ? 'Publicando…' : 'Publicar'}
                </button>
              </div>
            </form>
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
              filteredPosts.map((post) => {
                const isLiked = post.likedByCurrentUser;
                return (
                  <article key={post.id} className="post-card">
                    <div className="post-card-header">
                      <div className="post-card-author">
                        <UserAvatar src={post.authorProfilePictureUrl} name={post.authorName} size="small" />
                        <div>
                          <h4
                            className="post-author post-author-link"
                            role="button"
                            tabIndex={0}
                            onClick={() => navigate(`/user/${post.authorName}`)}
                            onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
                          >
                            {post.authorName}
                          </h4>
                          {post.linkedItemType && (
                            <span className="post-category-tag">
                              {MEDIA_TYPE_LABELS[post.linkedItemType] || post.linkedItemType}
                            </span>
                          )}
                        </div>
                      </div>
                      <span className="text-dim post-date">{formatFeedDate(post.createdAt)}</span>
                    </div>

                    {(post.linkedItemTitle || post.linkedItemId) && (
                      <div className="post-linked-work">
                        Reseña de: <strong>{post.linkedItemTitle}</strong>
                        {post.linkedItemRating != null && (
                          <span className="text-muted"> · {post.linkedItemRating}/10</span>
                        )}
                      </div>
                    )}

                    <p className="post-content">{post.content}</p>

                    <div className="post-footer">
                      <button
                        type="button"
                        className={`post-like-btn ${isLiked ? 'liked' : ''}`}
                        onClick={() => handleLike(post)}
                        disabled={likingPostId === post.id}
                      >
                        <Heart size={16} fill={isLiked ? 'currentColor' : 'none'} /> {post.likeCount} Me gusta
                      </button>
                      {post.commentCount > 0 && (
                        <span className="text-muted post-comment-count">{post.commentCount} comentarios</span>
                      )}
                    </div>
                  </article>
                );
              })
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
                  return (
                    <div key={u.id} className={`suggestion-item ${isFollowed ? 'followed' : ''}`}>
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
                        className={`suggestion-follow-btn ${isFollowed ? 'following' : ''}`}
                        onClick={() => handleFollow(u.id)}
                        disabled={isFollowed}
                      >
                        {isFollowed ? 'Siguiendo' : 'Seguir'}
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
