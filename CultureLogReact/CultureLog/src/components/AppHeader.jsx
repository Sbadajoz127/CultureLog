import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Menu, X, Home, BookOpen, User, Shield, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { UserAvatar } from './UserAvatar';
import { NotificationBell } from './NotificationBell';
import { searchUsers } from '../services/api';
import { useMediaQuery } from '../hooks/useMediaQuery';
import '../App.css';

/**
 * @param {'home'|'library'|'profile'} active
 */
export function AppHeader({ active = 'home', userName }) {
  const navigate = useNavigate();
  const { logout, isAdmin } = useAuth();
  const { profilePic } = useProfilePic();
  const isMobile = useMediaQuery('(max-width: 768px)');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
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
      return;
    }
    const timer = setTimeout(async () => {
      setSearchLoading(true);
      try {
        const { data } = await searchUsers(searchQuery.trim());
        setSearchResults(Array.isArray(data) ? data : []);
      } catch {
        setSearchResults([]);
      } finally {
        setSearchLoading(false);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleSelectUser = (username) => {
    setSearchQuery('');
    setSearchResults([]);
    setShowResults(false);
    setDrawerOpen(false);
    navigate(`/user/${username}`);
  };

  const handleNavClick = (path) => {
    setDrawerOpen(false);
    navigate(path);
  };

  const searchDropdown = (showResults && (searchResults.length > 0 || searchLoading || searchQuery.trim().length >= 2)) && (
    <div className="header-search-dropdown">
      {searchLoading && <p className="header-search-hint">Buscando...</p>}
      {!searchLoading && searchResults.length === 0 && searchQuery.trim().length >= 2 && (
        <p className="header-search-hint">No se encontraron usuarios</p>
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
          <div className="header-search-input-wrap">
            <Search size={16} className="header-search-icon" />
            <input
              type="text"
              className="header-search-input"
              placeholder="Buscar usuarios..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => setShowResults(true)}
            />
          </div>
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
                <div className="header-search-input-wrap">
                  <Search size={16} className="header-search-icon" />
                  <input
                    type="text"
                    className="header-search-input"
                    placeholder="Buscar usuarios..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onFocus={() => setShowResults(true)}
                  />
                </div>
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
