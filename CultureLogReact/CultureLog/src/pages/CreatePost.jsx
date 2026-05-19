import { useCallback, useEffect, useState, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { MEDIA_TYPE_LABELS, MEDIA_TYPES } from '../constants/media';
import { CustomSelect } from '../components/CustomSelect';
import { searchResultToPayload } from '../utils/mediaItem';
import {
  createPost,
  searchMedia,
  addToLibraryFromSearch,
  getMediaItems,
} from '../services/api';
import { Search, X, BookOpen, ArrowLeft, Image } from 'lucide-react';
import { toast } from 'sonner';
import { formatReleaseDate } from '../utils/dateFormat';
import { useMediaQuery } from '../hooks/useMediaQuery';
import '../App.css';

const MAX_POST_LENGTH = 2000;

function SkeletonCreatePost() {
  return (
    <div className="cp-page">
      <div className="cp-main">
        <div className="cp-skeleton-title-row">
          <div className="skeleton cp-skeleton-title" />
        </div>
        <div className="cp-form">
          <div className="cp-content-section">
            <div className="cp-skeleton-author-row">
              <div className="skeleton skeleton-circle cp-skeleton-avatar" />
              <div className="skeleton cp-skeleton-author-name" />
            </div>
            <div className="skeleton cp-skeleton-textarea" />
            <div className="cp-skeleton-footer">
              <div className="skeleton cp-skeleton-counter" />
            </div>
          </div>
          <div className="cp-search-section">
            <div className="skeleton cp-skeleton-section-title" />
            <div className="cp-skeleton-tabs-row">
              <div className="skeleton cp-skeleton-tab" />
              <div className="skeleton cp-skeleton-tab" />
            </div>
            <div className="skeleton cp-skeleton-search-input" />
            <div className="cp-skeleton-items">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="cp-skeleton-item-row">
                  <div className="skeleton cp-skeleton-item-thumb" />
                  <div className="cp-skeleton-item-info">
                    <div className="skeleton cp-skeleton-item-title" />
                    <div className="skeleton cp-skeleton-item-creator" />
                  </div>
                  <div className="skeleton cp-skeleton-item-badge" />
                </div>
              ))}
            </div>
          </div>
          <div className="cp-skeleton-actions">
            <div className="skeleton cp-skeleton-btn-cancel" />
            <div className="skeleton cp-skeleton-btn-submit" />
          </div>
        </div>
      </div>
    </div>
  );
}

function CreatePost() {
  const { user } = useAuth();
  const { profilePic } = useProfilePic();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 768px)');

  const [content, setContent] = useState('');
  const [linkedItem, setLinkedItem] = useState(() => location.state?.linkedItem ?? null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [initialReady, setInitialReady] = useState(false);

  const [activeSearchTab, setActiveSearchTab] = useState('library');

  const [libraryItems, setLibraryItems] = useState([]);
  const [libraryLoading, setLibraryLoading] = useState(true);
  const [libraryFilter, setLibraryFilter] = useState('');

  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [addingItemId, setAddingItemId] = useState(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLibraryLoading(true);
      try {
        const { data } = await getMediaItems({});
        if (!cancelled) setLibraryItems(Array.isArray(data) ? data : []);
      } catch {
        if (!cancelled) setLibraryItems([]);
      } finally {
        if (!cancelled) {
          setLibraryLoading(false);
          setInitialReady(true);
        }
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const filteredLibrary = useMemo(() => {
    const q = libraryFilter.trim().toLowerCase();
    if (!q) return libraryItems;
    return libraryItems.filter((it) =>
      it.title.toLowerCase().includes(q) ||
      (it.creator && it.creator.toLowerCase().includes(q))
    );
  }, [libraryItems, libraryFilter]);

  const runExternalSearch = useCallback(async () => {
    const q = searchTerm.trim();
    if (q.length < 2) { setSearchResults([]); return; }
    setSearchLoading(true);
    try {
      const { data } = await searchMedia({
        query: q,
        type: searchType || undefined,
        page: 0,
      });
      setSearchResults(Array.isArray(data) ? data : []);
    } catch {
      setSearchResults([]);
      toast.error('Error al buscar obras.');
    } finally {
      setSearchLoading(false);
    }
  }, [searchTerm, searchType]);

  useEffect(() => {
    const t = setTimeout(() => {
      if (searchTerm.trim().length >= 2) runExternalSearch();
      else setSearchResults([]);
    }, 400);
    return () => clearTimeout(t);
  }, [searchTerm, runExternalSearch]);

  const attachLibraryItem = (item) => {
    setLinkedItem({
      id: item.id,
      title: item.title,
      type: item.type,
      creator: item.creator,
      releaseDate: item.releaseDate,
      imageUrl: item.itemImageUrl,
      genre: item.genre,
      rating: item.rating,
    });
    setLibraryFilter('');
    setError('');
  };

  const attachExternalResult = async (result) => {
    setError('');
    const trackId = `${result.source}-${result.externalId}`;
    setAddingItemId(trackId);
    try {
      const { data: item } = await addToLibraryFromSearch(searchResultToPayload(result));
      setLinkedItem({
        id: item.id,
        title: item.title,
        type: item.type,
        creator: item.creator,
        releaseDate: item.releaseDate,
        imageUrl: item.itemImageUrl,
        genre: item.genre,
        rating: item.rating,
      });
      setSearchTerm('');
      setSearchResults([]);
      setLibraryItems((prev) => {
        if (prev.some((it) => it.id === item.id)) return prev;
        return [item, ...prev];
      });
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'No se pudo vincular la obra.';
      setError(msg);
      toast.error(msg);
    } finally {
      setAddingItemId(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      await createPost({
        content: content.trim(),
        linkedMediaItemId: linkedItem?.id ?? null,
      });
      toast.success('Publicación creada.');
      navigate('/home');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'No se pudo publicar.';
      setError(msg);
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (!initialReady) {
    return (
      <div className="home-container">
        <AppHeader active="home" userName={user.username} />
        <SkeletonCreatePost />
      </div>
    );
  }

  return (
    <div className="home-container">
      <AppHeader active="home" userName={user.username} />

      <div className="cp-page">
        <div className="cp-main">
          <div className="cp-header-row">
            <h2 className="cp-title">Nueva publicación</h2>
            <button
              type="button"
              className="cp-back-btn"
              onClick={() => navigate(-1)}
              aria-label="Volver"
            >
              <ArrowLeft size={20} /> Volver
            </button>
          </div>

          <form onSubmit={handleSubmit} className="cp-form">
            {error && <p className="auth-error">{error}</p>}

            {/* Linked item preview */}
            {linkedItem && (
              <div className="cp-linked-preview">
                <div className="cp-linked-image-wrap">
                  {linkedItem.imageUrl ? (
                    <img
                      src={linkedItem.imageUrl}
                      alt={linkedItem.title}
                      className="cp-linked-image"
                    />
                  ) : (
                    <div className="cp-linked-image cp-linked-placeholder">
                      <Image size={32} />
                    </div>
                  )}
                </div>
                <div className="cp-linked-info">
                  <h3 className="cp-linked-title">{linkedItem.title}</h3>
                  <span className="post-category-tag">
                    {MEDIA_TYPE_LABELS[linkedItem.type] || linkedItem.type}
                  </span>
                  {linkedItem.creator && (
                    <p className="cp-linked-meta">{linkedItem.creator}</p>
                  )}
                  {linkedItem.releaseDate && (
                    <p className="cp-linked-meta">{formatReleaseDate(linkedItem.releaseDate, isMobile)}</p>
                  )}
                  {linkedItem.genre && (
                    <p className="cp-linked-meta">{linkedItem.genre}</p>
                  )}
                  {linkedItem.rating != null && (
                    <p className="cp-linked-meta">Puntuación: {linkedItem.rating}/10</p>
                  )}
                </div>
                <button
                  type="button"
                  className="cp-linked-remove"
                  onClick={() => setLinkedItem(null)}
                  aria-label="Quitar obra vinculada"
                >
                  <X size={18} />
                </button>
              </div>
            )}

            {/* Content textarea */}
            <div className="cp-content-section">
              <div className="cp-author-row">
                <UserAvatar src={profilePic} name={user.username} size="small" />
                <span className="cp-author-name">@{user.username}</span>
              </div>
              <textarea
                placeholder="¿Qué te ha parecido? Escribe tu reseña o comentario…"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                maxLength={MAX_POST_LENGTH}
                className="cp-textarea"
              />
              <div className="cp-textarea-footer">
                <span className="text-dim cp-char-count">
                  {content.length}/{MAX_POST_LENGTH}
                </span>
              </div>
            </div>

            {/* Item search section */}
            {!linkedItem && (
              <div className="cp-search-section">
                <h3 className="cp-section-title">Vincular una obra</h3>
                <nav className="cp-search-tabs">
                  <button
                    type="button"
                    className={`cp-search-tab ${activeSearchTab === 'library' ? 'active' : ''}`}
                    onClick={() => setActiveSearchTab('library')}
                  >
                    <BookOpen size={16} />
                    Mi biblioteca
                  </button>
                  <button
                    type="button"
                    className={`cp-search-tab ${activeSearchTab === 'external' ? 'active' : ''}`}
                    onClick={() => setActiveSearchTab('external')}
                  >
                    <Search size={16} />
                    Buscar obra
                  </button>
                </nav>

                {activeSearchTab === 'library' && (
                  <div className="cp-library-panel">
                    <div className="cp-search-input-wrap">
                      <Search size={16} className="cp-search-icon" />
                      <input
                        type="text"
                        placeholder="Filtrar tu biblioteca…"
                        value={libraryFilter}
                        onChange={(e) => setLibraryFilter(e.target.value)}
                        className="cp-search-input"
                      />
                      {libraryFilter && (
                        <button
                          type="button"
                          className="cp-search-clear"
                          onClick={() => setLibraryFilter('')}
                          aria-label="Limpiar filtro"
                        >
                          <X size={14} />
                        </button>
                      )}
                    </div>
                    {libraryLoading ? (
                      <p className="text-muted cp-loading-hint">Cargando biblioteca…</p>
                    ) : filteredLibrary.length === 0 ? (
                      <p className="text-muted cp-empty-hint">
                        {libraryFilter
                          ? 'Sin resultados en tu biblioteca.'
                          : 'Tu biblioteca está vacía.'}
                      </p>
                    ) : (
                      <div className="cp-items-list">
                        {filteredLibrary.map((it) => (
                          <button
                            key={it.id}
                            type="button"
                            className="cp-item-row"
                            onClick={() => attachLibraryItem(it)}
                          >
                            <div className="cp-item-thumb-wrap">
                              {it.itemImageUrl ? (
                                <img
                                  src={it.itemImageUrl}
                                  alt={it.title}
                                  className="cp-item-thumb"
                                />
                              ) : (
                                <div className="cp-item-thumb cp-item-thumb-empty">
                                  <Image size={18} />
                                </div>
                              )}
                            </div>
                            <div className="cp-item-info">
                              <span className="cp-item-title">{it.title}</span>
                              {it.creator && (
                                <span className="cp-item-creator">{it.creator}</span>
                              )}
                            </div>
                            <span className="post-category-tag">
                              {MEDIA_TYPE_LABELS[it.type] || it.type}
                            </span>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeSearchTab === 'external' && (
                  <div className="cp-external-panel">
                    <div className="cp-external-search-row">
                      <div className="cp-search-input-wrap">
                        <Search size={16} className="cp-search-icon" />
                        <input
                          type="text"
                          placeholder="Buscar en catálogos externos…"
                          value={searchTerm}
                          onChange={(e) => setSearchTerm(e.target.value)}
                          className="cp-search-input"
                        />
                        {searchTerm && (
                          <button
                            type="button"
                            className="cp-search-clear"
                            onClick={() => { setSearchTerm(''); setSearchResults([]); }}
                            aria-label="Limpiar búsqueda"
                          >
                            <X size={14} />
                          </button>
                        )}
                      </div>
                      <CustomSelect
                        className="cp-search-type-select"
                        options={MEDIA_TYPES}
                        value={searchType}
                        onChange={setSearchType}
                        ariaLabel="Filtrar por tipo de obra"
                      />
                    </div>
                    {searchLoading && (
                      <p className="text-muted cp-loading-hint">Buscando…</p>
                    )}
                    {searchResults.length > 0 && (
                      <div className="cp-items-list">
                        {searchResults.map((result, idx) => {
                          const trackId = `${result.source}-${result.externalId}`;
                          const isAdding = addingItemId === trackId;
                          return (
                            <button
                              key={`${trackId}-${idx}`}
                              type="button"
                              className="cp-item-row"
                              onClick={() => !isAdding && attachExternalResult(result)}
                              disabled={isAdding}
                            >
                              <div className="cp-item-thumb-wrap">
                                {result.imageUrl ? (
                                  <img
                                    src={result.imageUrl}
                                    alt={result.title}
                                    className="cp-item-thumb"
                                  />
                                ) : (
                                  <div className="cp-item-thumb cp-item-thumb-empty">
                                    <Image size={18} />
                                  </div>
                                )}
                              </div>
                              <div className="cp-item-info">
                                <span className="cp-item-title">{result.title}</span>
                                <span className="cp-item-creator">
                                  {[result.creator, result.releaseDate ? formatReleaseDate(result.releaseDate, isMobile) : null].filter(Boolean).join(' · ')}
                                </span>
                              </div>
                              <span className="post-category-tag">
                                {isAdding
                                  ? 'Añadiendo…'
                                  : MEDIA_TYPE_LABELS[result.type] || result.type}
                              </span>
                            </button>
                          );
                        })}
                      </div>
                    )}
                    {!searchLoading && searchTerm.trim().length >= 2 && searchResults.length === 0 && (
                      <p className="text-muted cp-empty-hint">
                        No se encontraron resultados para &quot;{searchTerm.trim()}&quot;.
                      </p>
                    )}
                  </div>
                )}
              </div>
            )}

            {/* Actions */}
            <div className="cp-actions">
              <button
                type="button"
                className="logout-button"
                onClick={() => navigate('/home')}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="login-button cp-submit-btn"
                disabled={submitting || !content.trim()}
              >
                {submitting ? 'Publicando…' : 'Publicar'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreatePost;
