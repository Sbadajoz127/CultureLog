import { useCallback, useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { AppHeader } from '../components/AppHeader';
import { PostCard } from '../components/PostCard';
import { FEED_TABS, postMatchesFeedTab } from '../constants/media';
import { searchPosts } from '../services/api';
import '../App.css';

const SEARCH_PAGE_SIZE = 10;

function SkeletonPost() {
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
      <div className="skeleton skeleton-linked-work" />
      <div className="skeleton skeleton-content-l1" />
      <div className="skeleton skeleton-content-l2" />
      <div className="skeleton skeleton-content-l3" />
      <div className="skeleton-post-footer">
        <div className="skeleton skeleton-like-btn" />
      </div>
    </div>
  );
}

function SearchPosts() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [activeTab, setActiveTab] = useState('GENERAL');
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [hasSearched, setHasSearched] = useState(false);

  const inputRef = useRef(null);

  const doSearch = useCallback(async (query, pageNum, append) => {
    if (!query.trim()) {
      setPosts([]);
      setHasSearched(false);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const { data } = await searchPosts({ query: query.trim(), page: pageNum, size: SEARCH_PAGE_SIZE });
      const content = data?.content ?? [];
      setIsLast(data?.last ?? true);
      setPosts((prev) => (append ? [...prev, ...content] : content));
      setHasSearched(true);
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.error || 'No se pudieron cargar los resultados.');
      if (!append) setPosts([]);
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

  const handlePostUpdate = (postId, updates) => {
    setPosts((prev) => prev.map((p) => (p.id === postId ? { ...p, ...updates } : p)));
  };

  const handlePostDelete = (postId) => {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  };

  const loadMore = () => {
    const next = page + 1;
    setPage(next);
    doSearch(searchQuery.trim(), next, true);
  };

  const filteredPosts = posts.filter((p) => postMatchesFeedTab(p, activeTab));

  return (
    <div className="home-container">
      <AppHeader active="" userName={user.username} />

      <div className="home-content">
        <main className="feed feed-loaded">
          <div className="search-posts-header">
            <h2 className="search-posts-title">Buscar publicaciones</h2>
            <p className="text-muted">
              Busca publicaciones por el nombre de la película, serie, libro u obra que reseñan.
            </p>
          </div>

          <form className="search-posts-form" onSubmit={handleSubmit}>
            <div className="search-posts-input-wrap">
              <Search size={18} className="search-posts-icon" />
              <input
                ref={inputRef}
                type="text"
                className="search-posts-input"
                placeholder="Ej: Interstellar, El Padrino, Harry Potter..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoFocus
              />
            </div>
            <button type="submit" className="search-posts-btn" disabled={loading || !searchQuery.trim()}>
              {loading ? 'Buscando...' : 'Buscar'}
            </button>
          </form>

          {hasSearched && posts.length > 0 && (
            <nav className="feed-category-nav" aria-label="Filtrar resultados por tipo de obra">
              {FEED_TABS.map((tab) => (
                <button
                  key={tab.id}
                  type="button"
                  className={`category-btn ${activeTab === tab.id ? 'active' : ''}`}
                  onClick={() => setActiveTab(tab.id)}
                >
                  {tab.label}
                </button>
              ))}
            </nav>
          )}

          {error && <p className="auth-error">{error}</p>}

          {loading && !hasSearched && (
            <div className="posts-list">
              <SkeletonPost />
              <SkeletonPost />
              <SkeletonPost />
            </div>
          )}

          {hasSearched && (
            <div className="posts-list">
              {filteredPosts.length > 0 ? (
                filteredPosts.map((post) => (
                  <PostCard
                    key={post.id}
                    post={post}
                    onPostUpdate={handlePostUpdate}
                    onPostDelete={handlePostDelete}
                    showAuthorLink
                  />
                ))
              ) : (
                <div className="bg-card feed-placeholder">
                  <p className="text-faint">
                    {posts.length === 0
                      ? `No se encontraron publicaciones para "${initialQuery || searchQuery}".`
                      : 'No hay publicaciones en esta categoría.'}
                  </p>
                  <p className="text-muted feed-placeholder-sub">
                    {posts.length === 0
                      ? 'Prueba con otro término de búsqueda.'
                      : 'Prueba otra pestaña para ver más resultados.'}
                  </p>
                </div>
              )}
            </div>
          )}

          {!hasSearched && !loading && (
            <div className="bg-card feed-placeholder">
              <p className="text-faint">Escribe el nombre de una obra para buscar publicaciones relacionadas.</p>
              <p className="text-muted feed-placeholder-sub">
                Por ejemplo: "Inception", "Breaking Bad", "1984"...
              </p>
            </div>
          )}

          {!isLast && !loading && filteredPosts.length > 0 && (
            <button type="button" className="logout-button feed-load-more" onClick={loadMore}>
              Cargar más
            </button>
          )}
          {loading && posts.length > 0 && (
            <p className="text-muted feed-loading-more">Cargando…</p>
          )}
        </main>
      </div>
    </div>
  );
}

export default SearchPosts;
