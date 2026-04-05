import { UserAvatar } from './UserAvatar';
import '../App.css';

/**
 * @param {'home'|'library'|'profile'} active
 */
export function AppHeader({
  active = 'home',
  userName,
  profilePic,
  onGoHome,
  onGoLibrary,
  onGoProfile,
  onLogout,
}) {
  return (
    <header className="top-header">
      <div className="header-left">
        <h2 className="brand-logo brand-logo-clickable" onClick={onGoHome}>
          Culture<span>Log</span>
        </h2>
      </div>

      <nav className="header-center app-main-nav" aria-label="Principal">
        <button
          type="button"
          className={`nav-link ${active === 'home' ? 'active' : ''}`}
          onClick={onGoHome}
        >
          Inicio
        </button>
        <button
          type="button"
          className={`nav-link ${active === 'library' ? 'active' : ''}`}
          onClick={onGoLibrary}
        >
          Mi biblioteca
        </button>
        <button
          type="button"
          className={`nav-link ${active === 'profile' ? 'active' : ''}`}
          onClick={onGoProfile}
        >
          Perfil
        </button>
      </nav>

      <div className="header-right header-user-cluster">
        <button type="button" className="header-user-btn" onClick={onGoProfile}>
          <UserAvatar src={profilePic} name={userName} size="small" />
          <span>@{userName}</span>
        </button>
        <button type="button" className="logout-button" onClick={onLogout}>
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}
