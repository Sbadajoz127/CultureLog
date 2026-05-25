import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { PostCard } from '../components/PostCard';
import { MEDIA_TYPE_LABELS, MEDIA_STATUS_LABELS } from '../constants/media';
import { Lock } from 'lucide-react';
import { toast } from 'sonner';
import {
  getUserProfile,
  followUser,
  unfollowUser,
  getFollowers,
  getFollowing,
  getSavedPosts,
  getLikedPosts,
  getMediaItems,
  addToLibraryFromSearch,
} from '../services/api';
import { MediaItemDetailModal } from '../components/MediaItemDetailModal';
import './PublicProfile.css';


const LIBRARY_TABS = [
  { id: 'VISTO', label: 'Visto' },
  { id: 'EN_PROGRESO', label: 'En progreso' },
  { id: 'POR_VER', label: 'Por ver' },
];

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
  const { user, isAdmin } = useAuth();
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
  const [selectedItem, setSelectedItem] = useState(null);
  const [myLibraryItems, setMyLibraryItems] = useState([]);
  const [connectionsModal, setConnectionsModal] = useState(null);
  const [connectionsLoading, setConnectionsLoading] = useState(false);
  const [connectionsError, setConnectionsError] = useState('');
  const [connectionsUsers, setConnectionsUsers] = useState([]);

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

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await getMediaItems({});
        if (!cancelled) setMyLibraryItems(Array.isArray(data) ? data : []);
      } catch {
        if (!cancelled) setMyLibraryItems([]);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const isItemInMyLibrary = useCallback((item) => {
    if (!item?.externalId || !item?.externalSource) return false;
    return myLibraryItems.some(
      (mi) => mi.externalId === item.externalId
        && mi.externalSource === item.externalSource
        && mi.type === item.type
    );
  }, [myLibraryItems]);

  const handleAddToLibrary = async (item) => {
    const payload = {
      externalId: item.externalId ?? null,
      source: item.externalSource ?? null,
      title: item.title,
      type: item.type,
      genre: item.genre ?? null,
      creator: item.creator ?? null,
      description: item.description ?? null,
      releaseDate: item.releaseDate ?? null,
      imageUrl: item.itemImageUrl ?? null,
      rating: item.rating ?? null,
      album: item.album ?? null,
    };
    try {
      const { data } = await addToLibraryFromSearch(payload);
      setMyLibraryItems((prev) => [...prev, data]);
    } catch {
      toast.error('No se pudo añadir a tu biblioteca.');
      throw new Error();
    }
  };

  const handleCreatePostFromItem = (item) => {
    navigate('/posts/create', {
      state: {
        linkedItem: {
          id: item.id,
          title: item.title,
          type: item.type,
          creator: item.creator,
          releaseDate: item.releaseDate,
          imageUrl: item.itemImageUrl,
          genre: item.genre,
          rating: item.rating,
        },
      },
    });
  };

  const handleAddAndCreatePost = async (item) => {
    try {
      const payload = {
        externalId: item.externalId ?? null,
        source: item.externalSource ?? null,
        title: item.title,
        type: item.type,
        genre: item.genre ?? null,
        creator: item.creator ?? null,
        description: item.description ?? null,
        releaseDate: item.releaseDate ?? null,
        imageUrl: item.itemImageUrl ?? null,
        rating: item.rating ?? null,
        album: item.album ?? null,
      };
      const { data: savedItem } = await addToLibraryFromSearch(payload);
      setMyLibraryItems((prev) => [...prev, savedItem]);
      toast.success(`«${savedItem.title}» añadido a tu biblioteca.`);
      navigate('/posts/create', {
        state: {
          linkedItem: {
            id: savedItem.id,
            title: savedItem.title,
            type: savedItem.type,
            creator: savedItem.creator,
            releaseDate: savedItem.releaseDate,
            imageUrl: savedItem.itemImageUrl,
            genre: savedItem.genre,
            rating: savedItem.rating,
          },
        },
      });
    } catch {
      toast.error('No se pudo añadir el ítem a la biblioteca.');
    }
  };

  const handleFollow = async () => {
    if (followLoading || !profile) return;
    setFollowLoading(true);
    try {
      if (profile.followStatus === 'ACCEPTED') {
        await unfollowUser(profile.id);
        setProfile((p) => ({ ...p, followStatus: 'NONE', followerCount: Math.max(0, p.followerCount - 1) }));
        toast.success('Has dejado de seguir a este usuario.');
      } else if (profile.followStatus === 'PENDING') {
        await unfollowUser(profile.id);
        setProfile((p) => ({ ...p, followStatus: 'NONE' }));
        toast.success('Solicitud de seguimiento cancelada.');
      } else if (profile.followStatus === 'NONE') {
        const { data } = await followUser(profile.id);
        const resultStatus = data?.status || 'ACCEPTED';
        if (resultStatus === 'PENDING') {
          setProfile((p) => ({ ...p, followStatus: 'PENDING' }));
          toast.success('Solicitud de seguimiento enviada.');
        } else {
          setProfile((p) => ({ ...p, followStatus: 'ACCEPTED', followerCount: p.followerCount + 1 }));
          toast.success('Ahora sigues a este usuario.');
          loadProfile(undefined, { silent: true });
        }
      }
    } catch {
      toast.error('No se pudo completar la acción.');
    } finally {
      setFollowLoading(false);
    }
  };

  const handlePostUpdate = (postId, updates) => {
    setProfile((prev) => ({
      ...prev,
      posts: prev.posts.map((p) => (p.id === postId ? { ...p, ...updates } : p)),
    }));
  };

  const handlePostDelete = (postId) => {
    setProfile((prev) => ({
      ...prev,
      posts: prev.posts.filter((p) => p.id !== postId),
      postCount: Math.max(0, prev.postCount - 1),
    }));
  };

  const handleSavedPostUpdate = (postId, updates) => {
    setSavedPosts((prev) => {
      const updated = prev.map((p) => (p.id === postId ? { ...p, ...updates } : p));
      if (updates.savedByCurrentUser === false) {
        return updated.filter((p) => p.id !== postId);
      }
      return updated;
    });
    setLikedPosts((prev) => prev.map((p) => (p.id === postId ? { ...p, ...updates } : p)));
  };

  const handleSavedPostDelete = (postId) => {
    setSavedPosts((prev) => prev.filter((p) => p.id !== postId));
    setLikedPosts((prev) => prev.filter((p) => p.id !== postId));
  };

  const handleLikedPostUpdate = (postId, updates) => {
    setLikedPosts((prev) => {
      const updated = prev.map((p) => (p.id === postId ? { ...p, ...updates } : p));
      if (updates.likedByCurrentUser === false) {
        return updated.filter((p) => p.id !== postId);
      }
      return updated;
    });
    setSavedPosts((prev) => prev.map((p) => (p.id === postId ? { ...p, ...updates } : p)));
  };

  const handleLikedPostDelete = (postId) => {
    setLikedPosts((prev) => prev.filter((p) => p.id !== postId));
    setSavedPosts((prev) => prev.filter((p) => p.id !== postId));
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

  useEffect(() => {
    if (!connectionsModal) return;
    const prevBody = document.body.style.overflow;
    const prevHtml = document.documentElement.style.overflow;
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
    const handleKey = (e) => { if (e.key === 'Escape') setConnectionsModal(null); };
    document.addEventListener('keydown', handleKey);
    return () => {
      document.body.style.overflow = prevBody;
      document.documentElement.style.overflow = prevHtml;
      document.removeEventListener('keydown', handleKey);
    };
  }, [connectionsModal]);

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

  const hasAccess =
    profile &&
    (profile.ownProfile ||
      isAdmin ||
      profile.profilePrivacy === 'PUBLICO' ||
      profile.followStatus === 'ACCEPTED');

  const filteredLibrary =
    profile?.libraryItems?.filter((item) => item.status === libraryStatus) ?? [];

  const showLibraryTab =
    !profile || profile.ownProfile || profile.showLibrary;

  const followBtnLabel = () => {
    if (!profile) return '';
    switch (profile.followStatus) {
      case 'ACCEPTED':
        return 'Siguiendo';
      case 'PENDING':
        return 'Cancelar solicitud';
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
                      disabled={followLoading || profile.followStatus === 'REJECTED' || profile.followStatus === 'BLOCKED'}
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
                    className={`pub-profile-stat${hasAccess ? ' pub-profile-stat-clickable' : ''}`}
                    role={hasAccess ? 'button' : undefined}
                    tabIndex={hasAccess ? 0 : undefined}
                    onClick={hasAccess ? () => openConnectionsModal('followers') : undefined}
                    onKeyDown={hasAccess ? (e) => { if (e.key === 'Enter') openConnectionsModal('followers'); } : undefined}
                  >
                    <span className="pub-profile-stat-count">{profile.followerCount}</span>
                    <span className="pub-profile-stat-label">seguidores</span>
                  </div>
                  <div
                    className={`pub-profile-stat${hasAccess ? ' pub-profile-stat-clickable' : ''}`}
                    role={hasAccess ? 'button' : undefined}
                    tabIndex={hasAccess ? 0 : undefined}
                    onClick={hasAccess ? () => openConnectionsModal('following') : undefined}
                    onKeyDown={hasAccess ? (e) => { if (e.key === 'Enter') openConnectionsModal('following'); } : undefined}
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
                  {showLibraryTab && (
                    <button
                      type="button"
                      className={`pub-profile-tab ${activeTab === 'library' ? 'active' : ''}`}
                      onClick={() => setActiveTab('library')}
                    >
                      Biblioteca
                    </button>
                  )}
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
                        Me gusta
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
                          <PostCard
                            key={post.id}
                            post={post}
                            onPostUpdate={handlePostUpdate}
                            onPostDelete={handlePostDelete}
                            authorAvatar={profile.profilePictureUrl}
                            showAuthorLink={false}
                          />
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'library' && showLibraryTab && (
                  <div className="pub-profile-content">
                    <nav className="pub-profile-library-tabs">
                      {LIBRARY_TABS.map((tab) => (
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
                      <div className="pub-profile-posts-list">
                        <SkeletonProfilePost />
                        <SkeletonProfilePost />
                        <SkeletonProfilePost />
                      </div>
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
                      <div className="pub-profile-posts-list">
                        {savedPosts.map((post) => (
                          <PostCard
                            key={post.id}
                            post={post}
                            onPostUpdate={handleSavedPostUpdate}
                            onPostDelete={handleSavedPostDelete}
                          />
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'liked' && profile.ownProfile && (
                  <div className="pub-profile-content">
                    {loadingLiked ? (
                      <div className="pub-profile-posts-list">
                        <SkeletonProfilePost />
                        <SkeletonProfilePost />
                        <SkeletonProfilePost />
                      </div>
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
                      <div className="pub-profile-posts-list">
                        {likedPosts.map((post) => (
                          <PostCard
                            key={post.id}
                            post={post}
                            onPostUpdate={handleLikedPostUpdate}
                            onPostDelete={handleLikedPostDelete}
                          />
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div className="pub-profile-private">
                <div className="pub-profile-private-icon"><Lock size={48} /></div>
                <h3 className="pub-profile-private-title">
                  {profile.profilePrivacy === 'SOLO_AMIGOS'
                    ? 'Esta cuenta es solo para amigos'
                    : 'Esta cuenta es privada'}
                </h3>
                <p className="pub-profile-private-text">
                  {profile.profilePrivacy === 'SOLO_AMIGOS'
                    ? 'Este usuario debe seguirte de vuelta para que puedas ver sus publicaciones y biblioteca.'
                    : 'Sigue a este usuario para ver sus publicaciones y biblioteca.'}
                </p>
              </div>
            )}
          </div>
        )}
      </main>

        <MediaItemDetailModal
          item={selectedItem}
          onClose={() => setSelectedItem(null)}
          isOwn={profile?.ownProfile ?? false}
          alreadyInLibrary={selectedItem ? isItemInMyLibrary(selectedItem) : false}
          onAddToLibrary={handleAddToLibrary}
          onCreatePost={profile?.ownProfile ? () => handleCreatePostFromItem(selectedItem) : undefined}
          onAddAndCreatePost={!profile?.ownProfile ? handleAddAndCreatePost : undefined}
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

    </>
  );
}

export default PublicProfile;
