import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { ConfirmModal } from '../components/ConfirmModal';
import { MEDIA_TYPE_LABELS, MEDIA_STATUS_LABELS } from '../constants/media';
import { Heart, Lock, MessageCircle, Expand, Trash2, Bookmark } from 'lucide-react';
import {
  getUserProfile,
  followUser,
  unfollowUser,
  togglePostLike,
  togglePostSave,
  getPostComments,
  addPostComment,
  deletePostComment,
  deletePost,
  getFollowers,
  getFollowing,
  getSavedPosts,
  getLikedPosts,
} from '../services/api';
import { MediaItemDetailModal } from '../components/MediaItemDetailModal';
import '../App.css';

const LIBRARY_TABS = [
  { id: 'VISTO', label: 'Visto' },
  { id: 'EN_PROGRESO', label: 'Viendo' },
  { id: 'POR_VER', label: 'Por ver' },
];

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  } catch {
    return iso;
  }
}

function SkeletonProfileHeader() {
  return (
    <div className="pub-profile-header">
      <div className="pub-profile-avatar-section">
        <div className="skeleton skeleton-circle" style={{ width: 96, height: 96 }} />
      </div>
      <div className="pub-profile-info">
        <div className="pub-profile-top-row">
          <div className="skeleton" style={{ width: 160, height: 22, borderRadius: 6 }} />
          <div className="skeleton" style={{ width: 110, height: 36, borderRadius: 8 }} />
        </div>
        <div className="pub-profile-stats">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="pub-profile-stat">
              <div className="skeleton" style={{ width: 28, height: 18, borderRadius: 4 }} />
              <div className="skeleton" style={{ width: 70, height: 14, borderRadius: 4 }} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function SkeletonProfileTabs() {
  return (
    <nav className="pub-profile-tabs">
      <div className="skeleton" style={{ width: 110, height: 32, borderRadius: 6 }} />
      <div className="skeleton" style={{ width: 90, height: 32, borderRadius: 6 }} />
    </nav>
  );
}

function SkeletonProfilePost() {
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
      <div className="skeleton skeleton-content-l1" />
      <div className="skeleton skeleton-content-l2" />
      <div className="skeleton skeleton-content-l3" />
      <div className="skeleton-post-footer">
        <div className="skeleton skeleton-like-btn" />
      </div>
    </div>
  );
}

function SkeletonProfilePage() {
  return (
    <>
      <SkeletonProfileHeader />
      <SkeletonProfileTabs />
      <div className="pub-profile-content">
        <div className="pub-profile-posts-list">
          <SkeletonProfilePost />
          <SkeletonProfilePost />
        </div>
      </div>
    </>
  );
}

function PublicProfile() {
  const { username } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('posts');
  const [libraryStatus, setLibraryStatus] = useState('VISTO');
  const [followLoading, setFollowLoading] = useState(false);
  const [savedPosts, setSavedPosts] = useState([]);
  const [likedPosts, setLikedPosts] = useState([]);
  const [loadingSaved, setLoadingSaved] = useState(false);
  const [loadingLiked, setLoadingLiked] = useState(false);
  const [openCommentsPostId, setOpenCommentsPostId] = useState(null);
  const [postCommentsMap, setPostCommentsMap] = useState({});
  const [commentDrafts, setCommentDrafts] = useState({});
  const [loadingCommentsPostId, setLoadingCommentsPostId] = useState(null);
  const [submittingCommentPostId, setSubmittingCommentPostId] = useState(null);
  const [commentsErrorByPost, setCommentsErrorByPost] = useState({});
  const [selectedItem, setSelectedItem] = useState(null);
  const [connectionsModal, setConnectionsModal] = useState(null);
  const [connectionsLoading, setConnectionsLoading] = useState(false);
  const [connectionsError, setConnectionsError] = useState('');
  const [connectionsUsers, setConnectionsUsers] = useState([]);
  const [confirmModal, setConfirmModal] = useState({ open: false, type: null, id: null, postId: null });

  const loadProfile = useCallback(async (signal, { silent = false } = {}) => {
    if (!silent) {
      setLoading(true);
      setError('');
    }
    try {
      const { data } = await getUserProfile(username);
      if (!signal?.aborted) setProfile(data);
    } catch (e) {
      if (!signal?.aborted && !silent) {
        setError(
          e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar el perfil.'
        );
      }
    } finally {
      if (!signal?.aborted && !silent) setLoading(false);
    }
  }, [username]);

  useEffect(() => {
    const controller = new AbortController();
    loadProfile(controller.signal);
    return () => controller.abort();
  }, [loadProfile]);

  const handleFollow = async () => {
    if (followLoading || !profile) return;
    setFollowLoading(true);
    try {
      if (profile.followStatus === 'ACCEPTED') {
        await unfollowUser(profile.id);
        setProfile((p) => ({ ...p, followStatus: 'NONE', followerCount: Math.max(0, p.followerCount - 1) }));
      } else if (profile.followStatus === 'NONE') {
        await followUser(profile.id);
        if (profile.profilePrivacy !== 'PUBLICO') {
          setProfile((p) => ({ ...p, followStatus: 'PENDING' }));
        } else {
          setProfile((p) => ({ ...p, followStatus: 'ACCEPTED', followerCount: p.followerCount + 1 }));
          loadProfile(undefined, { silent: true });
        }
      }
    } catch {
      /* ignore */
    } finally {
      setFollowLoading(false);
    }
  };

  const handleLike = async (post) => {
    const wasLiked = post.likedByCurrentUser;
    const prevCount = post.likeCount;

    setProfile((p) => ({
      ...p,
      posts: p.posts.map((pt) =>
        pt.id === post.id
          ? { ...pt, likedByCurrentUser: !wasLiked, likeCount: wasLiked ? Math.max(0, prevCount - 1) : prevCount + 1 }
          : pt
      ),
    }));

    if (likedPosts.length > 0) {
      if (wasLiked) {
        setLikedPosts((prev) => prev.filter((p) => p.id !== post.id));
      } else {
        setLikedPosts((prev) => [{ ...post, likedByCurrentUser: true, likeCount: prevCount + 1 }, ...prev]);
      }
    }

    try {
      await togglePostLike(post.id);
    } catch {
      setProfile((p) => ({
        ...p,
        posts: p.posts.map((pt) =>
          pt.id === post.id
            ? { ...pt, likedByCurrentUser: wasLiked, likeCount: prevCount }
            : pt
        ),
      }));
    }
  };

  const handleSave = async (post) => {
    const wasSaved = post.savedByCurrentUser;

    setProfile((p) => ({
      ...p,
      posts: p.posts.map((pt) =>
        pt.id === post.id
          ? { ...pt, savedByCurrentUser: !wasSaved }
          : pt
      ),
    }));

    if (savedPosts.length > 0) {
      if (wasSaved) {
        setSavedPosts((prev) => prev.filter((p) => p.id !== post.id));
      } else {
        setSavedPosts((prev) => [{ ...post, savedByCurrentUser: true }, ...prev]);
      }
    }

    try {
      await togglePostSave(post.id);
    } catch {
      setProfile((p) => ({
        ...p,
        posts: p.posts.map((pt) =>
          pt.id === post.id
            ? { ...pt, savedByCurrentUser: wasSaved }
            : pt
        ),
      }));
    }
  };

  const loadSavedPosts = async () => {
    if (loadingSaved || savedPosts.length > 0) return;
    setLoadingSaved(true);
    try {
      const { data } = await getSavedPosts({ page: 0, size: 50 });
      setSavedPosts(data?.content || []);
    } catch {
      /* ignore */
    } finally {
      setLoadingSaved(false);
    }
  };

  const loadLikedPosts = async () => {
    if (loadingLiked || likedPosts.length > 0) return;
    setLoadingLiked(true);
    try {
      const { data } = await getLikedPosts({ page: 0, size: 50 });
      setLikedPosts(data?.content || []);
    } catch {
      /* ignore */
    } finally {
      setLoadingLiked(false);
    }
  };

  const toggleInlineComments = async (postId) => {
    if (openCommentsPostId === postId) {
      setOpenCommentsPostId(null);
      return;
    }
    setOpenCommentsPostId(postId);
    if (postCommentsMap[postId]) return;
    setLoadingCommentsPostId(postId);
    setCommentsErrorByPost((prev) => ({ ...prev, [postId]: '' }));
    try {
      const { data } = await getPostComments(postId);
      setPostCommentsMap((prev) => ({ ...prev, [postId]: Array.isArray(data) ? data : [] }));
    } catch (e) {
      setCommentsErrorByPost((prev) => ({
        ...prev,
        [postId]: e.response?.data?.message || e.response?.data?.error || 'No se pudieron cargar los comentarios.',
      }));
    } finally {
      setLoadingCommentsPostId(null);
    }
  };

  const handleInlineCommentSubmit = async (postId) => {
    const text = (commentDrafts[postId] || '').trim();
    if (!text) return;
    setSubmittingCommentPostId(postId);
    setCommentsErrorByPost((prev) => ({ ...prev, [postId]: '' }));
    try {
      const { data: created } = await addPostComment(postId, text);
      setPostCommentsMap((prev) => ({
        ...prev,
        [postId]: [...(prev[postId] || []), created],
      }));
      setProfile((prev) => ({
        ...prev,
        posts: prev.posts.map((p) => (
          p.id === postId ? { ...p, commentCount: (p.commentCount || 0) + 1 } : p
        )),
      }));
      setCommentDrafts((prev) => ({ ...prev, [postId]: '' }));
    } catch (e) {
      setCommentsErrorByPost((prev) => ({
        ...prev,
        [postId]: e.response?.data?.message || e.response?.data?.error || 'No se pudo comentar.',
      }));
    } finally {
      setSubmittingCommentPostId(null);
    }
  };

  const openConnectionsModal = async (type) => {
    if (!profile?.id) return;
    setConnectionsModal(type);
    setConnectionsUsers([]);
    setConnectionsError('');
    setConnectionsLoading(true);
    try {
      const { data } = type === 'followers'
        ? await getFollowers(profile.id)
        : await getFollowing(profile.id);
      setConnectionsUsers(Array.isArray(data) ? data : []);
    } catch (e) {
      setConnectionsError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar la lista.'
      );
    } finally {
      setConnectionsLoading(false);
    }
  };

  const openDeletePostModal = (postId) => {
    setConfirmModal({ open: true, type: 'post', id: postId, postId: null });
  };

  const openDeleteCommentModal = (postId, commentId) => {
    setConfirmModal({ open: true, type: 'comment', id: commentId, postId });
  };

  const closeConfirmModal = () => {
    setConfirmModal({ open: false, type: null, id: null, postId: null });
  };

  const handleConfirmDelete = async () => {
    const { type, id, postId } = confirmModal;
    closeConfirmModal();

    if (type === 'post') {
      try {
        await deletePost(id);
        setProfile((prev) => ({
          ...prev,
          posts: prev.posts.filter((p) => p.id !== id),
          postCount: Math.max(0, prev.postCount - 1),
        }));
      } catch {
        /* ignore */
      }
    } else if (type === 'comment') {
      try {
        await deletePostComment(id);
        setPostCommentsMap((prev) => ({
          ...prev,
          [postId]: (prev[postId] || []).filter((c) => c.id !== id),
        }));
        setProfile((prev) => ({
          ...prev,
          posts: prev.posts.map((p) => (
            p.id === postId ? { ...p, commentCount: Math.max(0, (p.commentCount || 0) - 1) } : p
          )),
        }));
      } catch (e) {
        setCommentsErrorByPost((prev) => ({
          ...prev,
          [postId]: e.response?.data?.message || e.response?.data?.error || 'No se pudo eliminar el comentario.',
        }));
      }
    }
  };

  const hasAccess =
    profile &&
    (profile.ownProfile ||
      profile.profilePrivacy === 'PUBLICO' ||
      profile.followStatus === 'ACCEPTED');

  const filteredLibrary =
    profile?.libraryItems?.filter((item) => item.status === libraryStatus) ?? [];

  const visibleLibraryTabs =
    profile && !profile.ownProfile && !profile.showFutureList
      ? LIBRARY_TABS.filter((t) => t.id !== 'POR_VER')
      : LIBRARY_TABS;

  const followBtnLabel = () => {
    if (!profile) return '';
    switch (profile.followStatus) {
      case 'ACCEPTED':
        return 'Siguiendo';
      case 'PENDING':
        return 'Solicitud pendiente';
      case 'REJECTED':
        return 'Solicitud rechazada';
      case 'BLOCKED':
        return 'Bloqueado';
      default:
        return 'Seguir';
    }
  };

  const followBtnClass = () => {
    if (!profile) return '';
    switch (profile.followStatus) {
      case 'ACCEPTED':
        return 'pub-profile-follow-btn following';
      case 'PENDING':
        return 'pub-profile-follow-btn pending';
      case 'REJECTED':
        return 'pub-profile-follow-btn rejected';
      case 'BLOCKED':
        return 'pub-profile-follow-btn blocked';
      default:
        return 'pub-profile-follow-btn';
    }
  };

  return (
    <>
      <div className="home-container">
      <AppHeader active="profile" userName={user.username} />

      <main className="feed profile-feed">
        {loading && <SkeletonProfilePage />}

        {error && <p className="auth-error">{error}</p>}

        {profile && !loading && (
          <div className="feed-loaded">
            {profile.bannerUrl && (
              <div className="pub-profile-banner">
                <img src={profile.bannerUrl} alt="Banner" className="pub-profile-banner-img" />
              </div>
            )}
            <div className={`pub-profile-header ${profile.bannerUrl ? 'has-banner' : ''}`}>
              <div className="pub-profile-avatar-section">
                <UserAvatar
                  src={profile.profilePictureUrl}
                  name={profile.username}
                  size="large"
                  className="pub-profile-avatar"
                />
              </div>

              <div className="pub-profile-info">
                <div className="pub-profile-top-row">
                  <h2 className="pub-profile-username">{profile.username}</h2>
                  {profile.ownProfile ? (
                    <button
                      type="button"
                      className="pub-profile-edit-btn"
                      onClick={() => navigate('/profile')}
                    >
                      Editar perfil
                    </button>
                  ) : (
                    <button
                      type="button"
                      className={`${followBtnClass()}${followLoading ? ' loading' : ''}`}
                      onClick={handleFollow}
                      disabled={followLoading || profile.followStatus === 'PENDING' || profile.followStatus === 'REJECTED' || profile.followStatus === 'BLOCKED'}
                    >
                      {followBtnLabel()}
                    </button>
                  )}
                </div>

                <div className="pub-profile-stats">
                  <div className="pub-profile-stat">
                    <span className="pub-profile-stat-count">{profile.postCount}</span>
                    <span className="pub-profile-stat-label">publicaciones</span>
                  </div>
                  <div
                    className="pub-profile-stat pub-profile-stat-clickable"
                    role="button"
                    tabIndex={0}
                    onClick={() => openConnectionsModal('followers')}
                    onKeyDown={(e) => { if (e.key === 'Enter') openConnectionsModal('followers'); }}
                  >
                    <span className="pub-profile-stat-count">{profile.followerCount}</span>
                    <span className="pub-profile-stat-label">seguidores</span>
                  </div>
                  <div
                    className="pub-profile-stat pub-profile-stat-clickable"
                    role="button"
                    tabIndex={0}
                    onClick={() => openConnectionsModal('following')}
                    onKeyDown={(e) => { if (e.key === 'Enter') openConnectionsModal('following'); }}
                  >
                    <span className="pub-profile-stat-count">{profile.followingCount}</span>
                    <span className="pub-profile-stat-label">seguidos</span>
                  </div>
                </div>
              </div>
            </div>

            {hasAccess ? (
              <>
                <nav className="pub-profile-tabs" aria-label="Secciones del perfil">
                  <button
                    type="button"
                    className={`pub-profile-tab ${activeTab === 'posts' ? 'active' : ''}`}
                    onClick={() => setActiveTab('posts')}
                  >
                    Publicaciones
                  </button>
                  <button
                    type="button"
                    className={`pub-profile-tab ${activeTab === 'library' ? 'active' : ''}`}
                    onClick={() => setActiveTab('library')}
                  >
                    Biblioteca
                  </button>
                  {profile.ownProfile && (
                    <>
                      <button
                        type="button"
                        className={`pub-profile-tab ${activeTab === 'saved' ? 'active' : ''}`}
                        onClick={() => {
                          setActiveTab('saved');
                          loadSavedPosts();
                        }}
                      >
                        Guardados
                      </button>
                      <button
                        type="button"
                        className={`pub-profile-tab ${activeTab === 'liked' ? 'active' : ''}`}
                        onClick={() => {
                          setActiveTab('liked');
                          loadLikedPosts();
                        }}
                      >
                        Likes
                      </button>
                    </>
                  )}
                </nav>

                {activeTab === 'posts' && (
                  <div className="pub-profile-content">
                    {profile.posts.length === 0 ? (
                      <div className="pub-profile-empty">
                        {profile.ownProfile ? (
                          <>
                            <p className="pub-profile-empty-text">
                              Aún no tienes publicaciones.
                            </p>
                            <button
                              type="button"
                              className="login-button pub-profile-cta-btn"
                              onClick={() => navigate('/home')}
                            >
                              Crea tu primera publicación
                            </button>
                          </>
                        ) : (
                          <p className="pub-profile-empty-text">
                            Este usuario aún no tiene publicaciones.
                          </p>
                        )}
                      </div>
                    ) : (
                      <div className="pub-profile-posts-list">
                        {profile.posts.map((post) => (
                          <article key={post.id} className="post-card">
                            <div className="post-card-header">
                              <div className="post-card-author">
                                <UserAvatar
                                  src={profile.profilePictureUrl}
                                  name={post.authorName}
                                  size="small"
                                />
                                <div>
                                  <h4 className="post-author">{post.authorName}</h4>
                                  {post.linkedItemType && (
                                    <span className="post-category-tag">
                                      {MEDIA_TYPE_LABELS[post.linkedItemType] || post.linkedItemType}
                                    </span>
                                  )}
                                </div>
                              </div>
                              <span className="text-dim post-date">
                                {formatDate(post.createdAt)}
                              </span>
                              {profile.ownProfile && (
                                <button
                                  type="button"
                                  className="post-like-btn post-delete-btn"
                                  onClick={() => openDeletePostModal(post.id)}
                                >
                                  <Trash2 size={16} /> Eliminar
                                </button>
                              )}
                            </div>

                            {(post.linkedItemTitle || post.linkedItemId) && (
                              <div className="post-linked-work">
                                Reseña de: <strong>{post.linkedItemTitle}</strong>
                                {post.linkedItemRating != null && (
                                  <span className="text-muted">
                                    {' '}
                                    · {post.linkedItemRating}/10
                                  </span>
                                )}
                              </div>
                            )}

                            <p className="post-content">{post.content}</p>

                            <div className="post-footer">
                              <div className="post-footer-left">
                                <button
                                  type="button"
                                  className={`post-like-btn ${post.likedByCurrentUser ? 'liked' : ''}`}
                                  onClick={() => handleLike(post)}
                                >
                                  <Heart size={16} fill={post.likedByCurrentUser ? 'currentColor' : 'none'} /> {post.likeCount}
                                </button>
                                <button
                                  type="button"
                                  className={`post-like-btn ${post.savedByCurrentUser ? 'saved' : ''}`}
                                  onClick={() => handleSave(post)}
                                >
                                  <Bookmark size={16} fill={post.savedByCurrentUser ? 'currentColor' : 'none'} />
                                </button>
                              </div>
                              <div className="post-footer-actions">
                                <button
                                  type="button"
                                  className="post-like-btn"
                                  onClick={() => toggleInlineComments(post.id)}
                                >
                                  <MessageCircle size={16} /> {post.commentCount || 0} comentarios
                                </button>
                                <button
                                  type="button"
                                  className="post-like-btn"
                                  onClick={() => navigate(`/posts/${post.id}`)}
                                >
                                  <Expand size={16} /> Ver publicación
                                </button>
                              </div>
                            </div>
                            {openCommentsPostId === post.id && (
                              <div className="post-inline-comments">
                                {commentsErrorByPost[post.id] && (
                                  <p className="auth-error">{commentsErrorByPost[post.id]}</p>
                                )}
                                {loadingCommentsPostId === post.id ? (
                                  <p className="text-muted">Cargando comentarios...</p>
                                ) : (
                                  <div className="post-inline-comments-list">
                                    {(postCommentsMap[post.id] || []).map((c) => (
                                      <div key={c.id} className="post-inline-comment-item">
                                        <UserAvatar src={c.authorProfilePictureUrl} name={c.authorName} size="small" />
                                        <p>
                                          <strong
                                            role="button"
                                            tabIndex={0}
                                            className="post-author-link"
                                            onClick={() => navigate(`/user/${c.authorName}`)}
                                            onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${c.authorName}`); }}
                                          >
                                            {c.authorName}
                                          </strong>
                                          {' '}
                                          {c.text}
                                        </p>
                                        {(c.authorId === user.id || profile.id === user.id) && (
                                          <button
                                            type="button"
                                            className="post-like-btn post-comment-delete-btn"
                                            onClick={() => openDeleteCommentModal(post.id, c.id)}
                                          >
                                            <Trash2 size={14} />
                                          </button>
                                        )}
                                      </div>
                                    ))}
                                    {(postCommentsMap[post.id] || []).length === 0 && (
                                      <p className="text-muted">Todavía no hay comentarios.</p>
                                    )}
                                  </div>
                                )}
                                <div className="post-inline-comment-form">
                                  <textarea
                                    value={commentDrafts[post.id] || ''}
                                    onChange={(e) => setCommentDrafts((prev) => ({ ...prev, [post.id]: e.target.value }))}
                                    maxLength={1000}
                                    placeholder="Añade un comentario..."
                                  />
                                  <div className="post-inline-comment-form-footer">
                                    <span className="text-dim">{(commentDrafts[post.id] || '').length}/1000</span>
                                    <button
                                      type="button"
                                      className="login-button"
                                      onClick={() => handleInlineCommentSubmit(post.id)}
                                      disabled={submittingCommentPostId === post.id || !(commentDrafts[post.id] || '').trim()}
                                    >
                                      {submittingCommentPostId === post.id ? 'Enviando...' : 'Comentar'}
                                    </button>
                                  </div>
                                </div>
                              </div>
                            )}
                          </article>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'library' && (
                  <div className="pub-profile-content">
                    <nav className="pub-profile-library-tabs">
                      {visibleLibraryTabs.map((tab) => (
                        <button
                          key={tab.id}
                          type="button"
                          className={`category-btn ${libraryStatus === tab.id ? 'active' : ''}`}
                          onClick={() => setLibraryStatus(tab.id)}
                        >
                          {tab.label}
                        </button>
                      ))}
                    </nav>

                    {filteredLibrary.length === 0 ? (
                      <div className="pub-profile-empty">
                        {profile.ownProfile ? (
                          <>
                            <p className="pub-profile-empty-text">
                              No tienes obras con estado &quot;{MEDIA_STATUS_LABELS[libraryStatus]}&quot;.
                            </p>
                            <button
                              type="button"
                              className="login-button pub-profile-cta-btn"
                              onClick={() => navigate('/library')}
                            >
                              Añade tu primera obra
                            </button>
                          </>
                        ) : (
                          <p className="pub-profile-empty-text">
                            No hay obras con estado &quot;{MEDIA_STATUS_LABELS[libraryStatus]}&quot;.
                          </p>
                        )}
                      </div>
                    ) : (
                      <div className="pub-profile-library-grid">
                        {filteredLibrary.map((item) => (
                          <div
                            key={item.id}
                            className="pub-profile-library-card"
                            role="button"
                            tabIndex={0}
                            onClick={() => setSelectedItem(item)}
                            onKeyDown={(e) => { if (e.key === 'Enter') setSelectedItem(item); }}
                          >
                            {item.itemImageUrl ? (
                              <img
                                src={item.itemImageUrl}
                                alt={item.title}
                                className="pub-profile-library-img"
                              />
                            ) : (
                              <div className="pub-profile-library-img-placeholder">
                                {MEDIA_TYPE_LABELS[item.type] || item.type}
                              </div>
                            )}
                            <div className="pub-profile-library-card-body">
                              <h4 className="pub-profile-library-title">{item.title}</h4>
                              <span className="pub-profile-library-meta">
                                {MEDIA_TYPE_LABELS[item.type] || item.type}
                                {item.rating != null && <> · {item.rating}/10</>}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'saved' && profile.ownProfile && (
                  <div className="pub-profile-content">
                    {loadingSaved ? (
                      <p className="text-muted">Cargando posts guardados...</p>
                    ) : savedPosts.length === 0 ? (
                      <div className="pub-profile-empty">
                        <p className="pub-profile-empty-text">
                          Aún no has guardado ninguna publicación.
                        </p>
                        <button
                          type="button"
                          className="login-button pub-profile-cta-btn"
                          onClick={() => navigate('/home')}
                        >
                          Explorar publicaciones
                        </button>
                      </div>
                    ) : (
                      <div className="posts-list">
                        {savedPosts.map((post) => (
                          <article key={post.id} className="post-card">
                            <div className="post-card-header">
                              <div
                                className="post-card-author"
                                role="button"
                                tabIndex={0}
                                onClick={() => navigate(`/user/${post.authorName}`)}
                                onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
                              >
                                <UserAvatar src={post.authorProfilePictureUrl} name={post.authorName} size="small" />
                                <span className="post-author-link">@{post.authorName}</span>
                              </div>
                              <span className="text-dim post-date">{formatDate(post.createdAt)}</span>
                            </div>
                            <p className="post-content">{post.content}</p>
                            <div className="post-footer">
                              <div className="post-footer-left">
                                <button
                                  type="button"
                                  className={`post-like-btn ${post.likedByCurrentUser ? 'liked' : ''}`}
                                  onClick={() => handleLike(post)}
                                >
                                  <Heart size={16} fill={post.likedByCurrentUser ? 'currentColor' : 'none'} /> {post.likeCount}
                                </button>
                                <button
                                  type="button"
                                  className="post-like-btn saved"
                                  onClick={() => handleSave(post)}
                                >
                                  <Bookmark size={16} fill="currentColor" />
                                </button>
                              </div>
                              <button
                                type="button"
                                className="post-like-btn"
                                onClick={() => navigate(`/posts/${post.id}`)}
                              >
                                <Expand size={16} /> Ver publicación
                              </button>
                            </div>
                          </article>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'liked' && profile.ownProfile && (
                  <div className="pub-profile-content">
                    {loadingLiked ? (
                      <p className="text-muted">Cargando likes...</p>
                    ) : likedPosts.length === 0 ? (
                      <div className="pub-profile-empty">
                        <p className="pub-profile-empty-text">
                          Aún no has dado like a ninguna publicación.
                        </p>
                        <button
                          type="button"
                          className="login-button pub-profile-cta-btn"
                          onClick={() => navigate('/home')}
                        >
                          Explorar publicaciones
                        </button>
                      </div>
                    ) : (
                      <div className="posts-list">
                        {likedPosts.map((post) => (
                          <article key={post.id} className="post-card">
                            <div className="post-card-header">
                              <div
                                className="post-card-author"
                                role="button"
                                tabIndex={0}
                                onClick={() => navigate(`/user/${post.authorName}`)}
                                onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
                              >
                                <UserAvatar src={post.authorProfilePictureUrl} name={post.authorName} size="small" />
                                <span className="post-author-link">@{post.authorName}</span>
                              </div>
                              <span className="text-dim post-date">{formatDate(post.createdAt)}</span>
                            </div>
                            <p className="post-content">{post.content}</p>
                            <div className="post-footer">
                              <div className="post-footer-left">
                                <button
                                  type="button"
                                  className="post-like-btn liked"
                                  onClick={() => handleLike(post)}
                                >
                                  <Heart size={16} fill="currentColor" /> {post.likeCount}
                                </button>
                                <button
                                  type="button"
                                  className={`post-like-btn ${post.savedByCurrentUser ? 'saved' : ''}`}
                                  onClick={() => handleSave(post)}
                                >
                                  <Bookmark size={16} fill={post.savedByCurrentUser ? 'currentColor' : 'none'} />
                                </button>
                              </div>
                              <button
                                type="button"
                                className="post-like-btn"
                                onClick={() => navigate(`/posts/${post.id}`)}
                              >
                                <Expand size={16} /> Ver publicación
                              </button>
                            </div>
                          </article>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div className="pub-profile-private">
                <div className="pub-profile-private-icon"><Lock size={48} /></div>
                <h3 className="pub-profile-private-title">Esta cuenta es privada</h3>
                <p className="pub-profile-private-text">
                  Sigue a este usuario para ver sus publicaciones y biblioteca.
                </p>
              </div>
            )}
          </div>
        )}
      </main>

        <MediaItemDetailModal
          item={selectedItem}
          onClose={() => setSelectedItem(null)}
        />
      </div>

      {connectionsModal && (
        <div className="modal-overlay" onClick={() => setConnectionsModal(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{connectionsModal === 'followers' ? 'Seguidores' : 'Seguidos'}</h3>
              <button type="button" className="close-btn" onClick={() => setConnectionsModal(null)}>
                ×
              </button>
            </div>
            {connectionsError && <p className="auth-error">{connectionsError}</p>}
            {connectionsLoading ? (
              <p className="text-muted">Cargando...</p>
            ) : (
              <div className="post-inline-comments-list">
                {connectionsUsers.map((u) => (
                  <div
                    key={u.id}
                    className="suggestion-user suggestion-user-link"
                    role="button"
                    tabIndex={0}
                    onClick={() => {
                      setConnectionsModal(null);
                      navigate(`/user/${u.username}`);
                    }}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        setConnectionsModal(null);
                        navigate(`/user/${u.username}`);
                      }
                    }}
                  >
                    <UserAvatar src={u.profilePictureUrl} name={u.username} size="small" />
                    <span className="suggestion-username">@{u.username}</span>
                  </div>
                ))}
                {connectionsUsers.length === 0 && (
                  <p className="text-muted">
                    {connectionsModal === 'followers'
                      ? 'Este usuario no tiene seguidores todavía.'
                      : 'Este usuario todavía no sigue a nadie.'}
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      <ConfirmModal
        isOpen={confirmModal.open}
        title={confirmModal.type === 'post' ? 'Eliminar publicación' : 'Eliminar comentario'}
        message={
          confirmModal.type === 'post'
            ? '¿Estás seguro de que quieres eliminar esta publicación? Esta acción no se puede deshacer.'
            : '¿Estás seguro de que quieres eliminar este comentario?'
        }
        confirmText="Eliminar"
        cancelText="Cancelar"
        danger
        onConfirm={handleConfirmDelete}
        onCancel={closeConfirmModal}
      />
    </>
  );
}

export default PublicProfile;
