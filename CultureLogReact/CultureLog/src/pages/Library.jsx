import { useCallback, useEffect, useState, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useConfirm } from '../context/ConfirmContext';
import { AppHeader } from '../components/AppHeader';
import {
  MEDIA_STATUS_TABS,
  MEDIA_STATUS_TABS_WITH_ALL,
  MEDIA_TYPES,
  MEDIA_TYPE_LABELS,
  MEDIA_STATUS_LABELS,
} from '../constants/media';
import { mediaItemToRequest, searchResultToPayload, buildCustomItemPayload } from '../utils/mediaItem';
import {
  getMediaItems,
  searchMedia,
  addToLibraryFromSearch,
  updateMediaItem,
  deleteMediaItem,
  createCustomMediaItem,
  getUserTags,
} from '../services/api';
import { MediaItemDetailModal } from '../components/MediaItemDetailModal';
import { CreateCustomItemModal } from '../components/CreateCustomItemModal';
import { CustomSelect } from '../components/CustomSelect';
import { TagChip } from '../components/TagChip';
import { TagManager } from '../components/TagManager';
import { SearchX, PlusCircle, Trash2, Tag as TagIcon, X, Check } from 'lucide-react';
import { toast } from 'sonner';
import { formatReleaseDate } from '../utils/dateFormat';
import { useMediaQuery } from '../hooks/useMediaQuery';
import './Library.css';

function SkeletonLibraryItem() {
  return (
    <article className="library-item-card">
      <div className="library-item-main">
        <div className="skeleton library-item-cover library-item-cover-placeholder" />
        <div className="library-item-body">
          <div className="skeleton skeleton-line" style={{ width: '55%', height: 16 }} />
          <div className="skeleton skeleton-line" style={{ width: '80%', height: 13 }} />
          <div style={{ display: 'flex', gap: 6, marginTop: 4 }}>
            <div className="skeleton" style={{ width: 50, height: 18, borderRadius: 12 }} />
            <div className="skeleton" style={{ width: 40, height: 18, borderRadius: 12 }} />
          </div>
        </div>
      </div>
      <div className="library-item-actions">
        <div className="library-item-status-group">
          <div className="skeleton" style={{ width: 45, height: 12, marginBottom: 6 }} />
          <div className="library-item-status-pills">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ width: 70, height: 30, borderRadius: 16 }} />
            ))}
          </div>
        </div>
        <div className="skeleton" style={{ width: 72, height: 34, borderRadius: 6 }} />
      </div>
    </article>
  );
}

function SkeletonGridItem() {
  return (
    <div className="library-grid-card">
      <div className="skeleton" style={{ width: '100%', height: 200 }} />
      <div style={{ padding: '10px 12px' }}>
        <div className="skeleton skeleton-line" style={{ width: '70%', height: 14 }} />
        <div className="skeleton skeleton-line" style={{ width: '50%', height: 12, marginTop: 6 }} />
      </div>
    </div>
  );
}

function SkeletonSearchSection() {
  return (
    <section className="create-post-card library-search-section">
      <div className="skeleton" style={{ width: '30%', height: 18, marginBottom: 10 }} />
      <div className="skeleton" style={{ width: '70%', height: 13, marginBottom: 16 }} />
      <div className="library-search-form">
        <div className="skeleton skeleton-input library-search-input" style={{ height: 44 }} />
        <div className="skeleton" style={{ width: 130, height: 44, borderRadius: 8 }} />
        <div className="skeleton" style={{ width: 90, height: 44, borderRadius: 8 }} />
      </div>
    </section>
  );
}

function LibraryItemCard({ item, onStatusChange, onDelete, onItemClick, busyId }) {
  const isMobile = useMediaQuery('(max-width: 768px)');
  const busy = busyId === item.id;
  const tagList = item.tags || [];

  return (
    <article className="library-item-card">
      <div className="library-item-main">
        <button
          type="button"
          className="library-item-clickable"
          onClick={() => onItemClick(item)}
        >
          {item.itemImageUrl ? (
            <img src={item.itemImageUrl} alt="" className="library-item-cover" />
          ) : (
            <div className="library-item-cover library-item-cover-placeholder" aria-hidden />
          )}
          <div className="library-item-body">
            <h4 className="library-item-title">
              {item.title}
              {item.custom && <span className="custom-item-badge">Personalizado</span>}
            </h4>
            <p className="library-item-meta text-muted">
              {MEDIA_TYPE_LABELS[item.type] || item.type}
              {item.creator ? ` · ${item.creator}` : ''}
              {item.releaseDate ? ` · ${formatReleaseDate(item.releaseDate, isMobile)}` : ''}
            </p>
            {tagList.length > 0 && (
              <div className="tags-container">
                {tagList.map((tag) => (
                  <TagChip key={tag.id} tag={tag} />
                ))}
              </div>
            )}
          </div>
        </button>
      </div>
      <div className="library-item-actions">
        <div className="library-item-status-group">
          <span className="library-item-status-label text-muted">Estado</span>
          <div className="library-item-status-pills">
            {MEDIA_STATUS_TABS.map(({ value, label }) => (
              <button
                key={value}
                type="button"
                className={`library-status-pill ${item.status === value ? 'active' : ''}`}
                disabled={busy}
                onClick={() => onStatusChange(item, value)}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
        <button
          type="button"
          className="logout-button library-item-delete"
          disabled={busy}
          onClick={() => onDelete(item)}
        >
          <Trash2 size={16} className="library-item-delete-icon" />
          <span className="library-item-delete-text">Eliminar</span>
        </button>
      </div>
    </article>
  );
}

function LibraryGridCard({ item, onItemClick }) {
  return (
    <div
      className="library-grid-card"
      role="button"
      tabIndex={0}
      onClick={() => onItemClick(item)}
      onKeyDown={(e) => { if (e.key === 'Enter') onItemClick(item); }}
    >
      {item.itemImageUrl ? (
        <img
          src={item.itemImageUrl}
          alt={item.title}
          className="library-grid-card-img"
        />
      ) : (
        <div className="library-grid-card-img-placeholder">
          {MEDIA_TYPE_LABELS[item.type] || item.type}
        </div>
      )}
      <div className="library-grid-card-body">
        <h4 className="library-grid-card-title">{item.title}</h4>
        <span className="library-grid-card-meta">
          {MEDIA_TYPE_LABELS[item.type] || item.type}
          {item.rating != null && <> · {item.rating}/10</>}
        </span>
        {item.custom && <span className="custom-item-badge">Personalizado</span>}
      </div>
    </div>
  );
}

const ListIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
    <line x1="3" y1="4.5" x2="15" y2="4.5" />
    <line x1="3" y1="9" x2="15" y2="9" />
    <line x1="3" y1="13.5" x2="15" y2="13.5" />
  </svg>
);

const GridIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2.5" y="2.5" width="5" height="5" rx="1" />
    <rect x="10.5" y="2.5" width="5" height="5" rx="1" />
    <rect x="2.5" y="10.5" width="5" height="5" rx="1" />
    <rect x="10.5" y="10.5" width="5" height="5" rx="1" />
  </svg>
);

function Library() {
  const { user } = useAuth();
  const confirm = useConfirm();
  const navigate = useNavigate();

  const [activeStatus, setActiveStatus] = useState('');
  const [items, setItems] = useState([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState('');
  const [initialReady, setInitialReady] = useState(false);
  const initialDone = useRef(false);

  const [nameFilter, setNameFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [tagFilter, setTagFilter] = useState(null);
  const [viewMode, setViewMode] = useState('list');
  const [allTags, setAllTags] = useState([]);
  const [tagFilterOpen, setTagFilterOpen] = useState(false);
  const tagFilterRef = useRef(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState('');

  const [hasSearched, setHasSearched] = useState(false);
  const [lastSearchQuery, setLastSearchQuery] = useState('');
  const [addingKey, setAddingKey] = useState(null);
  const [addedKeys, setAddedKeys] = useState(new Set());
  const [busyItemId, setBusyItemId] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);
  const [customModalOpen, setCustomModalOpen] = useState(false);
  const [customSubmitting, setCustomSubmitting] = useState(false);
  const [tagManagerOpen, setTagManagerOpen] = useState(false);

  const filteredItems = useMemo(() => {
    let result = items;
    if (typeFilter) {
      result = result.filter((item) => item.type === typeFilter);
    }
    if (nameFilter.trim()) {
      const q = nameFilter.toLowerCase();
      result = result.filter((item) => item.title.toLowerCase().includes(q));
    }
    if (tagFilter) {
      result = result.filter((item) =>
        item.tags && item.tags.some((t) => t.id === tagFilter)
      );
    }
    return result;
  }, [items, nameFilter, typeFilter, tagFilter]);

  const loadItems = useCallback(async (status) => {
    setListLoading(true);
    setListError('');
    try {
      const params = status ? { status } : {};
      const { data } = await getMediaItems(params);
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setListError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar la biblioteca.'
      );
      setItems([]);
    } finally {
      setListLoading(false);
      if (!initialDone.current) {
        initialDone.current = true;
        setInitialReady(true);
      }
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setListLoading(true);
      setListError('');
      try {
        const params = activeStatus ? { status: activeStatus } : {};
        const { data } = await getMediaItems(params);
        if (cancelled) return;
        setItems(Array.isArray(data) ? data : []);
      } catch (e) {
        if (cancelled) return;
        setListError(
          e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar la biblioteca.'
        );
        setItems([]);
      } finally {
        if (!cancelled) {
          setListLoading(false);
          if (!initialDone.current) {
            initialDone.current = true;
            setInitialReady(true);
          }
        }
      }
    })();
    return () => { cancelled = true; };
  }, [activeStatus]);

  useEffect(() => {
    getUserTags().then(({ data }) => setAllTags(data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (!tagFilterOpen) return;
    const handle = (e) => {
      if (tagFilterRef.current && !tagFilterRef.current.contains(e.target)) {
        setTagFilterOpen(false);
      }
    };
    document.addEventListener('mousedown', handle);
    return () => document.removeEventListener('mousedown', handle);
  }, [tagFilterOpen]);

  useEffect(() => {
    setNameFilter('');
    setTypeFilter('');
    setTagFilter(null);
  }, [activeStatus]);

  const handleStatusChange = async (item, newStatus) => {
    if (newStatus === item.status) return;
    setBusyItemId(item.id);
    try {
      const body = mediaItemToRequest(item, { status: newStatus });
      await updateMediaItem(item.id, body);
      await loadItems(activeStatus);
      toast.success(`Estado de «${item.title}» actualizado a ${MEDIA_STATUS_LABELS[newStatus]}.`);
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data?.error || 'No se pudo actualizar el estado.';
      setListError(msg);
      toast.error(msg);
    } finally {
      setBusyItemId(null);
    }
  };

  const handleDelete = async (item) => {
    const ok = await confirm({
      title: 'Eliminar de biblioteca',
      message: `¿Eliminar «${item.title}» de tu biblioteca?`,
      confirmText: 'Eliminar',
      variant: 'danger',
    });
    if (!ok) return;
    setBusyItemId(item.id);
    try {
      await deleteMediaItem(item.id);
      await loadItems(activeStatus);
      toast.success(`«${item.title}» eliminado de tu biblioteca.`);
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data?.error || 'No se pudo eliminar el ítem.';
      setListError(msg);
      toast.error(msg);
    } finally {
      setBusyItemId(null);
    }
  };

  const runSearch = async (e) => {
    e.preventDefault();
    const q = searchQuery.trim();
    if (q.length < 2) {
      setSearchError('Escribe al menos 2 caracteres.');
      setSearchResults([]);
      setHasSearched(false);
      return;
    }
    setSearchLoading(true);
    setSearchError('');
    setLastSearchQuery(q);
    setAddedKeys(new Set());
    try {
      const { data } = await searchMedia({
        query: q,
        type: searchType || undefined,
        page: 0,
      });
      setSearchResults(Array.isArray(data) ? data : []);
    } catch (err) {
      setSearchError(
        err.response?.data?.message || err.response?.data?.error || 'Error en la búsqueda.'
      );
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
      setHasSearched(true);
    }
  };

  const addSearchResultWithStatus = async (result, targetStatus) => {
    setAddingKey(`${result.source}-${result.externalId}-${result.title}`);
    setSearchError('');
    try {
      const payload = searchResultToPayload(result);
      const res = await addToLibraryFromSearch(payload);
      const item = res.data;
      if (targetStatus !== 'POR_VER') {
        const body = mediaItemToRequest(item, { status: targetStatus });
        await updateMediaItem(item.id, body);
      }
      if (res.status === 200) {
        toast(`«${result.title}» ya estaba en tu biblioteca.` +
          (targetStatus !== 'POR_VER'
            ? ` Estado actualizado a ${MEDIA_STATUS_LABELS[targetStatus]}.`
            : ''));
      } else {
        toast.success(`«${result.title}» añadido a ${MEDIA_STATUS_LABELS[targetStatus]}.`);
      }
      setAddedKeys(prev => new Set(prev).add(`${result.source}-${result.externalId}-${result.title}`));
      await loadItems(activeStatus);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'No se pudo añadir a la biblioteca.';
      setSearchError(msg);
      toast.error(msg);
    } finally {
      setAddingKey(null);
    }
  };

  const clearSearchResults = () => {
    setSearchResults([]);
    setSearchQuery('');
    setHasSearched(false);
    setLastSearchQuery('');
    setAddedKeys(new Set());
  };

  const handleCustomItemSubmit = async (formData) => {
    setCustomSubmitting(true);
    try {
      const payload = buildCustomItemPayload(formData);
      await createCustomMediaItem(payload);
      toast.success(`«${formData.title}» añadido a tu biblioteca.`);
      setCustomModalOpen(false);
      await loadItems(activeStatus);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'No se pudo crear el ítem.';
      toast.error(msg);
    } finally {
      setCustomSubmitting(false);
    }
  };

  if (!initialReady) {
    return (
      <div className="home-container">
        <AppHeader active="library" userName={user.username} />
        <main className="library-main">
          <div className="skeleton" style={{ width: '40%', height: 28, marginBottom: 24, borderRadius: 8 }} />
          <SkeletonSearchSection />
          <section className="library-tabs-section">
            <div className="library-status-tabs">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="skeleton" style={{ width: 100, height: 42, borderRadius: 8 }} />
              ))}
            </div>
            <div className="library-toolbar">
              <div className="skeleton" style={{ flex: 1, height: 38, borderRadius: 8 }} />
              <div className="skeleton" style={{ width: 76, height: 38, borderRadius: 8 }} />
            </div>
            <div className="library-items-list">
              <SkeletonLibraryItem />
              <SkeletonLibraryItem />
              <SkeletonLibraryItem />
              <SkeletonLibraryItem />
            </div>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="home-container">
      <AppHeader active="library" userName={user.username} />

      <main className="library-main feed-loaded">
        <h1 className="library-page-title">Mi biblioteca</h1>

        <section className="create-post-card library-search-section" aria-labelledby="library-search-heading">
          <h2 id="library-search-heading" className="library-section-title">
            Buscar obras
          </h2>
          <p className="text-muted library-section-hint">
            Búsqueda en catálogos externos. Añade a la pestaña que quieras (por defecto el backend crea en «Por ver»).
          </p>
          <form className="library-search-form" onSubmit={runSearch}>
            <input
              type="search"
              className="portal-input library-search-input"
              placeholder="Título, autor…"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              aria-label="Consulta de búsqueda"
            />
            <CustomSelect
              className="library-search-type-select"
              options={MEDIA_TYPES}
              value={searchType}
              onChange={setSearchType}
              ariaLabel="Filtrar por tipo"
            />
            <button type="submit" className="login-button library-search-submit" disabled={searchLoading}>
              {searchLoading ? 'Buscando…' : 'Buscar'}
            </button>
          </form>
          {searchError && <p className="auth-error library-inline-msg">{searchError}</p>}

          {searchLoading && (
            <div className="library-search-loading">
              <div className="library-search-spinner" />
              <p className="text-muted">Buscando en catálogos externos…</p>
            </div>
          )}

          {searchResults.length > 0 && (
            <>
            <div className="library-search-results-header">
              <span className="library-search-results-count">
                {searchResults.length} resultado{searchResults.length !== 1 ? 's' : ''}
              </span>
              <button
                type="button"
                className="library-search-close-btn"
                onClick={clearSearchResults}
              >
                <X size={16} />
                Cerrar resultados
              </button>
            </div>
            <ul className="library-search-results">
              {searchResults.map((r, idx) => {
                const k = `${r.source}-${r.externalId}-${idx}`;
                const rowKey = `${r.source}-${r.externalId}-${r.title}`;
                const rowAdding = addingKey === rowKey;
                const rowAdded = addedKeys.has(rowKey);
                const inLibrary = r.libraryStatus || rowAdded;
                const statusLabel = r.libraryStatus
                  ? MEDIA_STATUS_LABELS[r.libraryStatus] || r.libraryStatus
                  : 'Añadido';
                return (
                  <li key={k} className={`library-search-result-row${inLibrary ? ' library-search-result-added' : ''}`}>
                    <div className="library-search-result-info">
                      {r.imageUrl ? (
                        <img src={r.imageUrl} alt="" className="library-search-thumb" />
                      ) : (
                        <div className="library-search-thumb library-search-thumb-placeholder" />
                      )}
                      <div>
                        <strong>{r.title}</strong>
                        <div className="text-muted library-search-sub">
                          {MEDIA_TYPE_LABELS[r.type] || r.type}
                          {r.creator ? ` · ${r.creator}` : ''}
                          {r.album ? ` · ${r.album}` : ''}
                          {r.source ? ` · ${r.source}` : ''}
                        </div>
                      </div>
                    </div>
                    {inLibrary ? (
                      <span className="library-added-label">
                        <Check size={16} />
                        {statusLabel}
                      </span>
                    ) : rowAdding ? (
                      <p className="text-muted library-adding-label">Añadiendo…</p>
                    ) : (
                      <div className="library-add-status-btns">
                        {MEDIA_STATUS_TABS.map(({ value }) => (
                          <button
                            key={value}
                            type="button"
                            className="logout-button library-add-status-btn"
                            disabled={addingKey !== null}
                            onClick={() => addSearchResultWithStatus(r, value)}
                          >
                            {MEDIA_STATUS_LABELS[value]}
                          </button>
                        ))}
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
            </>
          )}

          {!searchLoading && hasSearched && searchResults.length === 0 && !searchError && (
            <div className="library-search-empty">
              <div className="library-search-empty-icon">
                <SearchX size={28} />
              </div>
              <p className="library-search-empty-title">
                Sin resultados para &laquo;{lastSearchQuery}&raquo;
              </p>
              <p className="library-search-empty-hint">
                Prueba con otro término o cambia el tipo de medio.
              </p>
            </div>
          )}

          <div className="library-custom-item-cta">
            <button
              type="button"
              className="library-custom-item-btn"
              onClick={() => setCustomModalOpen(true)}
            >
              <PlusCircle size={18} />
              <span>¿No encuentras lo que buscas? Añade un ítem manualmente</span>
            </button>
          </div>
        </section>

        <section className="library-tabs-section" aria-label="Estados de la biblioteca">
          <div className="library-status-tabs" role="tablist">
            {MEDIA_STATUS_TABS_WITH_ALL.map(({ value, label }) => (
              <button
                key={value || '__all__'}
                type="button"
                role="tab"
                aria-selected={activeStatus === value}
                className={`library-status-tab ${activeStatus === value ? 'active' : ''}`}
                onClick={() => setActiveStatus(value)}
              >
                {label}
              </button>
            ))}
          </div>

          <div className="library-toolbar">
            <input
              type="text"
              className="portal-input library-name-filter"
              placeholder="Filtrar por nombre…"
              value={nameFilter}
              onChange={(e) => setNameFilter(e.target.value)}
              aria-label="Filtrar items por nombre"
            />
            <CustomSelect
              className="library-toolbar-type-select"
              options={MEDIA_TYPES}
              value={typeFilter}
              onChange={setTypeFilter}
              ariaLabel="Filtrar por tipo de medio"
            />
            {allTags.length > 0 && (
              <div className="library-tag-filter" ref={tagFilterRef}>
                <button
                  type="button"
                  className={`library-tag-filter-trigger${tagFilter ? ' active' : ''}`}
                  onClick={() => setTagFilterOpen((o) => !o)}
                >
                  <span className="cselect-value">{tagFilter ? allTags.find((t) => t.id === tagFilter)?.name || 'Etiqueta' : 'Etiqueta'}</span>
                  <svg className="cselect-chevron" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="6 9 12 15 18 9" />
                  </svg>
                </button>
                {tagFilterOpen && (
                  <ul className="library-tag-filter-dropdown">
                    <li
                      className={`library-tag-filter-option${!tagFilter ? ' active' : ''}`}
                      onClick={() => { setTagFilter(null); setTagFilterOpen(false); }}
                    >
                      Todas
                    </li>
                    {allTags.map((tag) => (
                      <li
                        key={tag.id}
                        className={`library-tag-filter-option${tagFilter === tag.id ? ' active' : ''}`}
                        onClick={() => { setTagFilter(tag.id); setTagFilterOpen(false); }}
                      >
                        <span className="library-tag-filter-dot" style={{ backgroundColor: tag.colorHex }} />
                        {tag.name}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
            <button
              type="button"
              className="library-view-btn"
              onClick={() => setTagManagerOpen(true)}
              aria-label="Gestionar etiquetas"
              title="Gestionar etiquetas"
            >
              <TagIcon size={18} />
            </button>
            <div className="library-view-toggle">
              <button
                type="button"
                className={`library-view-btn ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => setViewMode('list')}
                aria-label="Vista de lista"
                title="Vista de lista"
              >
                <ListIcon />
              </button>
              <button
                type="button"
                className={`library-view-btn ${viewMode === 'grid' ? 'active' : ''}`}
                onClick={() => setViewMode('grid')}
                aria-label="Vista de cuadrícula"
                title="Vista de cuadrícula"
              >
                <GridIcon />
              </button>
            </div>
          </div>

          {listError && <p className="auth-error library-inline-msg">{listError}</p>}

          {listLoading ? (
            viewMode === 'list' ? (
              <div className="library-items-list">
                <SkeletonLibraryItem />
                <SkeletonLibraryItem />
                <SkeletonLibraryItem />
              </div>
            ) : (
              <div className="library-grid">
                <SkeletonGridItem />
                <SkeletonGridItem />
                <SkeletonGridItem />
                <SkeletonGridItem />
                <SkeletonGridItem />
                <SkeletonGridItem />
              </div>
            )
          ) : filteredItems.length === 0 ? (
            <p className="text-muted library-empty">
              {nameFilter.trim() || typeFilter || tagFilter
                ? 'No se encontraron ítems con esos filtros.'
                : 'No hay ítems en esta lista.'}
            </p>
          ) : viewMode === 'list' ? (
            <div className="library-items-list feed-loaded">
              {filteredItems.map((item) => (
                <LibraryItemCard
                  key={item.id}
                  item={item}
                  onStatusChange={handleStatusChange}
                  onDelete={handleDelete}
                  onItemClick={setSelectedItem}
                  busyId={busyItemId}
                />
              ))}
            </div>
          ) : (
            <div className="library-grid feed-loaded">
              {filteredItems.map((item) => (
                <LibraryGridCard
                  key={item.id}
                  item={item}
                  onItemClick={setSelectedItem}
                />
              ))}
            </div>
          )}
        </section>
      </main>

      <MediaItemDetailModal
        item={selectedItem}
        onClose={() => setSelectedItem(null)}
        isOwn
        alreadyInLibrary
        allTags={allTags}
        onTagsChange={(newTags) => {
          if (!selectedItem) return;
          const updated = { ...selectedItem, tags: newTags };
          setSelectedItem(updated);
          setItems((prev) => prev.map((it) => it.id === updated.id ? { ...it, tags: newTags } : it));
        }}
        onAllTagsChange={setAllTags}
        onCreatePost={selectedItem ? () => {
          navigate('/posts/create', {
            state: {
              linkedItem: {
                id: selectedItem.id,
                title: selectedItem.title,
                type: selectedItem.type,
                creator: selectedItem.creator,
                releaseDate: selectedItem.releaseDate,
                imageUrl: selectedItem.itemImageUrl,
                genre: selectedItem.genre,
                rating: selectedItem.rating,
              },
            },
          });
        } : undefined}
      />

      <CreateCustomItemModal
        open={customModalOpen}
        onClose={() => setCustomModalOpen(false)}
        onSubmit={handleCustomItemSubmit}
        submitting={customSubmitting}
      />

      <TagManager
        open={tagManagerOpen}
        onClose={() => setTagManagerOpen(false)}
        tags={allTags}
        onTagsChange={(updated) => {
          setAllTags(updated);
          setItems((prev) => prev.map((it) => ({
            ...it,
            tags: it.tags
              ? it.tags.map((t) => updated.find((u) => u.id === t.id) || t).filter((t) => updated.some((u) => u.id === t.id))
              : [],
          })));
        }}
      />
    </div>
  );
}

export default Library;
