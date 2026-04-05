import { useCallback, useEffect, useState } from 'react';
import { AppHeader } from './components/AppHeader';
import {
  MEDIA_STATUS_TABS,
  MEDIA_TYPES,
  MEDIA_TYPE_LABELS,
  MEDIA_STATUS_LABELS,
} from './constants/media';
import { mediaItemToRequest, searchResultToPayload } from './utils/mediaItem';
import {
  getMediaItems,
  searchMedia,
  addToLibraryFromSearch,
  updateMediaItem,
  deleteMediaItem,
} from './services/api';
import './App.css';

function LibraryItemCard({ item, onStatusChange, onDelete, busyId }) {
  const busy = busyId === item.id;
  const tagList = item.tagNames ? Array.from(item.tagNames) : [];

  return (
    <article className="library-item-card">
      <div className="library-item-main">
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

function Library({ userName, profilePic, onGoHome, onGoLibrary, onGoProfile, onLogout }) {
  const [activeStatus, setActiveStatus] = useState('POR_VER');
  const [items, setItems] = useState([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState('');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState('');
  const [searchMsg, setSearchMsg] = useState('');
  const [addingKey, setAddingKey] = useState(null);
  const [busyItemId, setBusyItemId] = useState(null);

  const loadItems = useCallback(async (status) => {
    setListLoading(true);
    setListError('');
    try {
      const { data } = await getMediaItems({ status });
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setListError(
        e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar la biblioteca.'
      );
      setItems([]);
    } finally {
      setListLoading(false);
    }
  }, []);

  useEffect(() => {
    loadItems(activeStatus);
  }, [activeStatus, loadItems]);

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
    if (!window.confirm(`¿Eliminar «${item.title}» de tu biblioteca?`)) return;
    setBusyItemId(item.id);
    try {
      await deleteMediaItem(item.id);
      await loadItems(activeStatus);
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
    setSearchMsg('');
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
    setSearchMsg('');
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
        setSearchMsg(
          `«${result.title}» ya estaba en tu biblioteca.` +
            (targetStatus !== 'POR_VER'
              ? ` Estado actualizado a ${MEDIA_STATUS_LABELS[targetStatus]}.`
              : '')
        );
      } else {
        setSearchMsg(`«${result.title}» añadido a ${MEDIA_STATUS_LABELS[targetStatus]}.`);
      }
      await loadItems(activeStatus);
    } catch (err) {
      setSearchError(
        err.response?.data?.message || err.response?.data?.error || 'No se pudo añadir a la biblioteca.'
      );
    } finally {
      setAddingKey(null);
    }
  };

  return (
    <div className="home-container">
      <AppHeader
        active="library"
        userName={userName}
        profilePic={profilePic}
        onGoHome={onGoHome}
        onGoLibrary={onGoLibrary}
        onGoProfile={onGoProfile}
        onLogout={onLogout}
      />

      <main className="library-main">
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
            <select
              className="portal-select"
              value={searchType}
              onChange={(e) => setSearchType(e.target.value)}
              aria-label="Filtrar por tipo"
            >
              {MEDIA_TYPES.map((t) => (
                <option key={t.value || 'all'} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
            <button type="submit" className="login-button library-search-submit" disabled={searchLoading}>
              {searchLoading ? 'Buscando…' : 'Buscar'}
            </button>
          </form>
          {searchError && <p className="auth-error library-inline-msg">{searchError}</p>}
          {searchMsg && <p className="library-success-msg">{searchMsg}</p>}

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
            {MEDIA_STATUS_TABS.map(({ value, label }) => (
              <button
                key={value}
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

          {listError && <p className="auth-error library-inline-msg">{listError}</p>}

          {listLoading ? (
            <p className="text-muted library-empty">Cargando…</p>
          ) : items.length === 0 ? (
            <p className="text-muted library-empty">No hay ítems en esta lista.</p>
          ) : (
            <div className="library-items-list">
              {items.map((item) => (
                <LibraryItemCard
                  key={item.id}
                  item={item}
                  onStatusChange={handleStatusChange}
                  onDelete={handleDelete}
                  busyId={busyItemId}
                />
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default Library;
