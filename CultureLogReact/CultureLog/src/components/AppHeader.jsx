import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Menu, X, Home, BookOpen, User, Shield, LogOut, FileText, Film, Tv, Book, Music, Gamepad2, ChevronRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { UserAvatar } from './UserAvatar';
import { NotificationBell } from './NotificationBell';
import { searchUsers, searchPosts } from '../services/api';
import { useMediaQuery } from '../hooks/useMediaQuery';
import '../App.css';

const MEDIA_TYPE_ICONS = {
  PELICULA: Film,
  SERIE: Tv,
  LIBRO: Book,
  MUSICA: Music,
  VIDEOJUEGO: Gamepad2,
};

/**
 * @param {'home'|'library'|'profile'} active
 */
export function AppHeader({ active = 'home', userName }) {
  const navigate = useNavigate();
  const { logout, isAdmin } = useAuth();
  const { profilePic } = useProfilePic();
  const isMobile = useMediaQuery('(max-width: 768px)');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('users');
  const [searchResults, setSearchResults] = useState([]);
  const [postResults, setPostResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const searchRef = useRef(null);
  const mobileSearchRef = useRef(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    if (!isMobile) setDrawerOpen(false);
  }, [isMobile]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setShowResults(false);
      }
      if (mobileSearchRef.current && !mobileSearchRef.current.contains(e.target)) {
        setShowResults(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (searchQuery.trim().length < 2) {
      setSearchResults([]);
      setPostResults([]);
      return;
    }
    const timer = setTimeout(async () => {
      setSearchLoading(true);
      try {
        if (searchType === 'users') {
          const { data } = await searchUsers({ query: searchQuery.trim(), size: 5 });
          setSearchResults(Array.isArray(data?.content) ? data.content : []);
        } else {
          const { data } = await searchPosts({ query: searchQuery.trim(), size: 5 });
          setPostResults(Array.isArray(data?.content) ? data.content : []);
        }
      } catch {
        setSearchResults([]);
        setPostResults([]);
      } finally {
        setSearchLoading(false);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery, searchType]);

  const handleSelectUser = (username) => {
    setSearchQuery('');
    setSearchResults([]);
    setPostResults([]);
    setShowResults(false);
    setDrawerOpen(false);
    navigate(`/user/${username}`);
  };

  const handleSelectPost = (postId) => {
    setSearchQuery('');
    setSearchResults([]);
    setPostResults([]);
    setShowResults(false);
    setDrawerOpen(false);
    navigate(`/posts/${postId}`);
  };

  const handleSearchSubmit = (e) => {
    if (e) e.preventDefault();
    const q = searchQuery.trim();
    if (q.length < 2) return;
    setShowResults(false);
    setDrawerOpen(false);
    if (searchType === 'posts') {
      navigate(`/search?q=${encodeURIComponent(q)}`);
    } else {
      navigate(`/search/users?q=${encodeURIComponent(q)}`);
    }
    setSearchQuery('');
    setPostResults([]);
    setSearchResults([]);
  };

  const handleNavClick = (path) => {
    setDrawerOpen(false);
    navigate(path);
  };

  const handleSearchTypeChange = (type) => {
    setSearchType(type);
    setSearchResults([]);
    setPostResults([]);
  };

  const hasUserResults = searchResults.length > 0;
  const hasPostResults = postResults.length > 0;
  const showDropdown = showResults && (hasUserResults || hasPostResults || searchLoading || searchQuery.trim().length >= 2);

  const searchDropdown = showDropdown && (
    <div className="header-search-dropdown">
      <div className="header-search-tabs">
        <button
          type="button"
          className={`header-search-tab ${searchType === 'users' ? 'active' : ''}`}
          onClick={() => handleSearchTypeChange('users')}
        >
          <User size={14} /> Usuarios
        </button>
        <button
          type="button"
          className={`header-search-tab ${searchType === 'posts' ? 'active' : ''}`}
          onClick={() => handleSearchTypeChange('posts')}
        >
          <FileText size={14} /> Publicaciones
        </button>
      </div>

      {searchLoading && <p className="header-search-hint">Buscando...</p>}

      {searchType === 'users' && (
        <>
          {!searchLoading && searchResults.length === 0 && searchQuery.trim().length >= 2 && (
            <div className="header-search-no-results">
              <div className="header-search-no-results-icon">
                <User size={20} />
              </div>
              <p className="header-search-no-results-text">No se encontraron usuarios</p>
            </div>
          )}
          {searchResults.map((u) => (
            <div
              key={u.id}
              className="header-search-result"
              role="button"
              tabIndex={0}
              onClick={() => handleSelectUser(u.username)}
              onKeyDown={(e) => { if (e.key === 'Enter') handleSelectUser(u.username); }}
            >
              <UserAvatar src={u.profilePictureUrl} name={u.username} size="small" />
              <span className="header-search-result-name">@{u.username}</span>
            </div>
          ))}
          {searchResults.length > 0 && searchQuery.trim().length >= 2 && (
            <button
              type="button"
              className="header-search-view-all"
              onClick={handleSearchSubmit}
            >
              Ver todos los resultados <ChevronRight size={16} />
            </button>
          )}
        </>
      )}

      {searchType === 'posts' && (
        <>
          {!searchLoading && postResults.length === 0 && searchQuery.trim().length >= 2 && (
            <div className="header-search-no-results">
              <div className="header-search-no-results-icon">
                <Search size={20} />
              </div>
              <p className="header-search-no-results-text">No se encontraron publicaciones</p>
            </div>
          )}
          {postResults.map((p) => {
            const IconComponent = MEDIA_TYPE_ICONS[p.linkedItemType] || FileText;
            return (
              <div
                key={p.id}
                className="header-search-result header-search-result-post"
                role="button"
                tabIndex={0}
                onClick={() => handleSelectPost(p.id)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleSelectPost(p.id); }}
              >
                <div className="header-search-post-icon">
                  <IconComponent size={18} />
                </div>
                <div className="header-search-post-info">
                  <span className="header-search-post-title">
                    {p.linkedItemTitle || 'Sin título vinculado'}
                  </span>
                  <span className="header-search-post-author">por @{p.authorName}</span>
                </div>
                {p.linkedItemType && (
                  <span className="header-search-post-type">
                    {p.linkedItemType}
                  </span>
                )}
              </div>
            );
          })}
          {postResults.length > 0 && searchQuery.trim().length >= 2 && (
            <button
              type="button"
              className="header-search-view-all"
              onClick={handleSearchSubmit}
            >
              Ver todos los resultados <ChevronRight size={16} />
            </button>
          )}
        </>
      )}
    </div>
  );

  return (
    <header className="top-header">
      <div className="header-left">
        <h2 className="brand-logo brand-logo-clickable" onClick={() => navigate('/home')}>
          Culture<span>Log</span>
        </h2>
      </div>

      <nav className="header-center app-main-nav" aria-label="Principal">
        <button
          type="button"
          className={`nav-link ${active === 'home' ? 'active' : ''}`}
          onClick={() => navigate('/home')}
        >
          Inicio
        </button>
        <button
          type="button"
          className={`nav-link ${active === 'library' ? 'active' : ''}`}
          onClick={() => navigate('/library')}
        >
          Mi biblioteca
        </button>
        <button
          type="button"
          className={`nav-link ${active === 'profile' ? 'active' : ''}`}
          onClick={() => navigate(`/user/${userName}`)}
        >
          Perfil
        </button>
        {isAdmin && (
          <button
            type="button"
            className={`nav-link ${active === 'admin' ? 'active' : ''}`}
            onClick={() => navigate('/admin')}
          >
            Admin
          </button>
        )}
      </nav>

      <div className="header-right header-user-cluster">
        <div className="header-search-wrap" ref={searchRef}>
          <form className="header-search-input-wrap" onSubmit={handleSearchSubmit}>
            <Search size={16} className="header-search-icon" />
            <input
              type="text"
              className="header-search-input"
              placeholder={searchType === 'users' ? 'Buscar usuarios...' : 'Buscar películas, series...'}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => setShowResults(true)}
            />
          </form>
          {searchDropdown}
        </div>
        <NotificationBell />
        <button type="button" className="header-user-btn" onClick={() => navigate(`/user/${userName}`)}>
          <UserAvatar src={profilePic} name={userName} size="small" />
          <span>@{userName}</span>
        </button>
        <button type="button" className="logout-button" onClick={handleLogout}>
          Cerrar sesión
        </button>
        {isMobile && (
          <button
            type="button"
            className="mobile-menu-btn"
            onClick={() => setDrawerOpen(prev => !prev)}
            aria-label={drawerOpen ? 'Cerrar menú' : 'Abrir menú'}
          >
            {drawerOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        )}
      </div>

      {isMobile && (
        <>
          <div className={`mobile-drawer ${drawerOpen ? 'open' : ''}`}>
            <button
              type="button"
              className={`mobile-nav-link ${active === 'home' ? 'active' : ''}`}
              onClick={() => handleNavClick('/home')}
            >
              <Home size={18} /> Inicio
            </button>
            <button
              type="button"
              className={`mobile-nav-link ${active === 'library' ? 'active' : ''}`}
              onClick={() => handleNavClick('/library')}
            >
              <BookOpen size={18} /> Mi biblioteca
            </button>
            <button
              type="button"
              className={`mobile-nav-link ${active === 'profile' ? 'active' : ''}`}
              onClick={() => handleNavClick(`/user/${userName}`)}
            >
              <User size={18} /> Perfil
            </button>
            {isAdmin && (
              <button
                type="button"
                className={`mobile-nav-link ${active === 'admin' ? 'active' : ''}`}
                onClick={() => handleNavClick('/admin')}
              >
                <Shield size={18} /> Admin
              </button>
            )}

            <hr className="mobile-drawer-divider" />

            <div className="mobile-search-section" ref={mobileSearchRef}>
              <div className="header-search-wrap">
                <form className="header-search-input-wrap" onSubmit={handleSearchSubmit}>
                  <Search size={16} className="header-search-icon" />
                  <input
                    type="text"
                    className="header-search-input"
                    placeholder={searchType === 'users' ? 'Buscar usuarios...' : 'Buscar películas, series...'}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onFocus={() => setShowResults(true)}
                  />
                </form>
                {searchDropdown}
              </div>
            </div>

            <hr className="mobile-drawer-divider" />

            <button type="button" className="mobile-logout-btn" onClick={handleLogout}>
              <LogOut size={16} /> Cerrar sesión
            </button>
          </div>
          <div
            className={`mobile-drawer-backdrop ${drawerOpen ? 'open' : ''}`}
            onClick={() => setDrawerOpen(false)}
          />
        </>
      )}
    </header>
  );
}
