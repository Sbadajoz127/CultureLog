import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { UserAvatar } from './UserAvatar';
import { NotificationBell } from './NotificationBell';
import '../App.css';

/**
 * @param {'home'|'library'|'profile'} active
 */
export function AppHeader({ active = 'home', userName }) {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { profilePic } = useProfilePic();

  const handleLogout = () => {
    logout();
    navigate('/login');
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
      </nav>

      <div className="header-right header-user-cluster">
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
