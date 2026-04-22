import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { UserAvatar } from './UserAvatar';
import { NotificationBell } from './NotificationBell';
import { searchUsers } from '../services/api';
import '../App.css';

/**
 * @param {'home'|'library'|'profile'} active
 */
export function AppHeader({ active = 'home', userName }) {
  const navigate = useNavigate();
  const { logout, isAdmin } = useAuth();
  const { profilePic } = useProfilePic();

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const searchRef = useRef(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
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
    navigate(`/user/${username}`);
  };

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
          {showResults && (searchResults.length > 0 || searchLoading || searchQuery.trim().length >= 2) && (
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
          )}
        </div>
        <NotificationBell />
        <button type="button" className="header-user-btn" onClick={() => navigate(`/user/${userName}`)}>
          <UserAvatar src={profilePic} name={userName} size="small" />
          <span>@{userName}</span>
        </button>
        <button type="button" className="logout-button" onClick={handleLogout}>
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}
