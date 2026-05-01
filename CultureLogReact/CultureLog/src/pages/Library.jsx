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
import { mediaItemToRequest, searchResultToPayload } from '../utils/mediaItem';
import {
  getMediaItems,
  searchMedia,
  addToLibraryFromSearch,
  updateMediaItem,
  deleteMediaItem,
} from '../services/api';
import { MediaItemDetailModal } from '../components/MediaItemDetailModal';
import { CustomSelect } from '../components/CustomSelect';
import { toast } from 'sonner';
import '../App.css';

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
  const busy = busyId === item.id;
  const tagList = item.tagNames ? Array.from(item.tagNames) : [];

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
            <h4 className="library-item-title">{item.title}</h4>
            <p className="library-item-meta text-muted">
              {MEDIA_TYPE_LABELS[item.type] || item.type}
              {item.creator ? ` · ${item.creator}` : ''}
              {item.releaseDate ? ` · ${item.releaseDate}` : ''}
            </p>
            {tagList.length > 0 && (
              <div className="tags-container">
                {tagList.map((tag) => (
                  <span key={tag} className="custom-tag">
                    #{tag}
                  </span>
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
          Eliminar
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
  const [viewMode, setViewMode] = useState('list');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState('');

  const [addingKey, setAddingKey] = useState(null);
  const [busyItemId, setBusyItemId] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);

  const filteredItems = useMemo(() => {
    let result = items;
    if (typeFilter) {
      result = result.filter((item) => item.type === typeFilter);
    }
    if (nameFilter.trim()) {
      const q = nameFilter.toLowerCase();
      result = result.filter((item) => item.title.toLowerCase().includes(q));
    }
    return result;
  }, [items, nameFilter, typeFilter]);

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
    loadItems(activeStatus);
  }, [activeStatus, loadItems]);

  useEffect(() => {
    setNameFilter('');
    setTypeFilter('');
  }, [activeStatus]);

  const handleStatusChange = async (item, newStatus) => {
    if (newStatus === item.status) return;
    setBusyItemId(item.id);
    try {
      const body = mediaItemToRequest(item, { status: newStatus });
      await updateMediaItem(item.id, body);
      await loadItems(activeStatus);
    } catch (e) {
      setListError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo actualizar el estado.'
      );
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
      setListError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo eliminar el ítem.'
      );
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
      return;
    }
    setSearchLoading(true);
    setSearchError('');
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
      setSearchResults([]);
      setSearchQuery('');
      await loadItems(activeStatus);
    } catch (err) {
      setSearchError(
        err.response?.data?.message || err.response?.data?.error || 'No se pudo añadir a la biblioteca.'
      );
    } finally {
      setAddingKey(null);
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

          {searchResults.length > 0 && (
            <ul className="library-search-results">
              {searchResults.map((r, idx) => {
                const k = `${r.source}-${r.externalId}-${idx}`;
                const rowKey = `${r.source}-${r.externalId}-${r.title}`;
                const rowAdding = addingKey === rowKey;
                return (
                  <li key={k} className="library-search-result-row">
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
                    {rowAdding ? (
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
          )}
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
              {nameFilter.trim() || typeFilter
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
    </div>
  );
}

export default Library;
