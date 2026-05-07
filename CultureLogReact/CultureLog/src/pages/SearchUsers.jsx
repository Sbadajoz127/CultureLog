import { useCallback, useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Search, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { searchUsers } from '../services/api';
import '../App.css';

const SEARCH_PAGE_SIZE = 10;

function SearchUsers() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [results, setResults] = useState([]);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [hasSearched, setHasSearched] = useState(false);

  const inputRef = useRef(null);

  const doSearch = useCallback(async (query, pageNum, append) => {
    if (!query.trim()) {
      setResults([]);
      setHasSearched(false);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const { data } = await searchUsers({ query: query.trim(), page: pageNum, size: SEARCH_PAGE_SIZE });
      const content = data?.content ?? [];
      setIsLast(data?.last ?? true);
      setResults((prev) => (append ? [...prev, ...content] : content));
      setHasSearched(true);
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.error || 'No se pudieron cargar los resultados.');
      if (!append) setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (initialQuery) {
      doSearch(initialQuery, 0, false);
    }
  }, [initialQuery, doSearch]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setSearchParams({ q: searchQuery.trim() });
    setPage(0);
    doSearch(searchQuery.trim(), 0, false);
  };

  const loadMore = () => {
    const next = page + 1;
    setPage(next);
    doSearch(searchQuery.trim(), next, true);
  };

  const openProfile = (username) => {
    navigate(`/user/${username}`);
  };

  return (
    <div className="home-container">
      <AppHeader active="" userName={user.username} />

      <div className="home-content">
        <main className="feed feed-loaded">
          <div className="search-posts-header">
            <h2 className="search-posts-title">Buscar usuarios</h2>
            <p className="text-muted">
              Encuentra perfiles por nombre de usuario.
            </p>
          </div>

          <form className="search-posts-form" onSubmit={handleSubmit}>
            <div className="search-posts-input-wrap">
              <Search size={18} className="search-posts-icon" />
              <input
                ref={inputRef}
                type="text"
                className="search-posts-input"
                placeholder="Nombre de usuario..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoFocus
              />
            </div>
            <button type="submit" className="search-posts-btn" disabled={loading || !searchQuery.trim()}>
              {loading ? 'Buscando...' : 'Buscar'}
            </button>
          </form>

          {error && <p className="auth-error">{error}</p>}

          {loading && !hasSearched && (
            <div className="text-muted">Cargando…</div>
          )}

          {hasSearched && (
            <div className="search-users-list">
              {results.length > 0 ? (
                results.map((u) => (
                  <div
                    key={u.id}
                    className="search-users-row"
                    role="button"
                    tabIndex={0}
                    onClick={() => openProfile(u.username)}
                    onKeyDown={(e) => { if (e.key === 'Enter') openProfile(u.username); }}
                  >
                    <UserAvatar src={u.profilePictureUrl} name={u.username} size="small" />
                    <span className="search-users-name">@{u.username}</span>
                  </div>
                ))
              ) : (
                <div className="bg-card feed-placeholder">
                  <p className="text-faint">
                    No se encontraron usuarios para &quot;{initialQuery || searchQuery}&quot;.
                  </p>
                  <p className="text-muted feed-placeholder-sub">
                    Prueba con otro término de búsqueda.
                  </p>
                </div>
              )}
            </div>
          )}

          {!hasSearched && !loading && (
            <div className="bg-card feed-placeholder">
              <div className="header-search-no-results-icon search-users-placeholder-icon">
                <User size={20} />
              </div>
              <p className="text-faint">Escribe un nombre de usuario para buscar.</p>
            </div>
          )}

          {!isLast && !loading && results.length > 0 && (
            <button type="button" className="logout-button feed-load-more" onClick={loadMore}>
              Cargar más
            </button>
          )}
          {loading && results.length > 0 && (
            <p className="text-muted feed-loading-more">Cargando…</p>
          )}
        </main>
      </div>
    </div>
  );
}

export default SearchUsers;
