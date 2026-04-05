import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { AppHeader } from './components/AppHeader';
import { UserAvatar } from './components/UserAvatar';
import { MEDIA_TYPE_LABELS, MEDIA_STATUS_LABELS } from './constants/media';
import {
  getUserProfile,
  followUser,
  unfollowUser,
  togglePostLike,
} from './services/api';
import './App.css';

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
  const { userId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('posts');
  const [libraryStatus, setLibraryStatus] = useState('VISTO');
  const [followLoading, setFollowLoading] = useState(false);

  const loadProfile = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getUserProfile(userId);
      setProfile(data);
    } catch (e) {
      setError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar el perfil.'
      );
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const handleFollow = async () => {
    if (followLoading || !profile) return;
    setFollowLoading(true);
    try {
      if (profile.followStatus === 'ACCEPTED' || profile.followStatus === 'PENDING') {
        await unfollowUser(userId);
        setProfile((p) => ({ ...p, followStatus: 'NONE', followerCount: Math.max(0, p.followerCount - (p.followStatus === 'ACCEPTED' ? 1 : 0)) }));
      } else {
        await followUser(userId);
        const isPrivate = profile.profilePrivacy === 'PRIVADO' || profile.profilePrivacy === 'SOLO_AMIGOS';
        if (isPrivate) {
          setProfile((p) => ({ ...p, followStatus: 'PENDING' }));
        } else {
          setProfile((p) => ({ ...p, followStatus: 'ACCEPTED', followerCount: p.followerCount + 1 }));
          loadProfile();
        }
      }
    } catch {
      /* ignore */
    } finally {
      setFollowLoading(false);
    }
  };

  const handleLike = async (post) => {
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
                      className={followBtnClass()}
                      onClick={handleFollow}
                      disabled={followLoading}
                    >
                      {followLoading ? '...' : followBtnLabel()}
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
                              >
                                {post.likedByCurrentUser ? '❤️' : '🤍'} {post.likeCount} Me gusta
                              </button>
                            </div>
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
                <div className="pub-profile-private-icon">🔒</div>
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
