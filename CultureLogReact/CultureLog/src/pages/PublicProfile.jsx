import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { MEDIA_TYPE_LABELS, MEDIA_STATUS_LABELS } from '../constants/media';
import { Heart, Lock, MessageCircle, Expand } from 'lucide-react';
import {
  getUserProfile,
  followUser,
  unfollowUser,
  togglePostLike,
  getPostComments,
  addPostComment,
} from '../services/api';
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
  const [likingPostId, setLikingPostId] = useState(null);
  const [openCommentsPostId, setOpenCommentsPostId] = useState(null);
  const [postCommentsMap, setPostCommentsMap] = useState({});
  const [commentDrafts, setCommentDrafts] = useState({});
  const [loadingCommentsPostId, setLoadingCommentsPostId] = useState(null);
  const [submittingCommentPostId, setSubmittingCommentPostId] = useState(null);
  const [commentsErrorByPost, setCommentsErrorByPost] = useState({});

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
    if (likingPostId === post.id) return;
    setLikingPostId(post.id);
    try {
      const { data } = await togglePostLike(post.id);
      setProfile((p) => ({
        ...p,
        posts: p.posts.map((pt) =>
          pt.id === post.id
            ? { ...pt, likedByCurrentUser: data.liked, likeCount: data.likeCount }
            : pt
        ),
      }));
    } catch {
      /* ignore */
    } finally {
      setLikingPostId(null);
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
    <div className="home-container">
      <AppHeader active="profile" userName={user.username} />

      <main className="feed profile-feed">
        {loading && <SkeletonProfilePage />}

        {error && <p className="auth-error">{error}</p>}

        {profile && !loading && (
          <div className="feed-loaded">
            <div className="pub-profile-header">
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
                  <div className="pub-profile-stat">
                    <span className="pub-profile-stat-count">{profile.followerCount}</span>
                    <span className="pub-profile-stat-label">seguidores</span>
                  </div>
                  <div className="pub-profile-stat">
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
                              <button
                                type="button"
                                className={`post-like-btn ${post.likedByCurrentUser ? 'liked' : ''}`}
                                onClick={() => handleLike(post)}
                                disabled={likingPostId === post.id}
                              >
                                <Heart size={16} fill={post.likedByCurrentUser ? 'currentColor' : 'none'} /> {post.likeCount} Me gusta
                              </button>
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
                          <div key={item.id} className="pub-profile-library-card">
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
    </div>
  );
}

export default PublicProfile;
