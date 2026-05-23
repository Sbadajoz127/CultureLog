import { useState, useRef, useEffect, useCallback } from 'react';
import { createPortal } from 'react-dom';
import { Plus, Check } from 'lucide-react';
import { createTag, addTagToItem, removeTagFromItem } from '../services/api';
import { toast } from 'sonner';

const TAG_COLORS = [
  { value: 'ROJO', hex: '#FF5252' },
  { value: 'AZUL', hex: '#448AFF' },
  { value: 'VERDE', hex: '#69F0AE' },
  { value: 'AMARILLO', hex: '#FFD740' },
  { value: 'NARANJA', hex: '#FFAB40' },
  { value: 'MORADO', hex: '#E040FB' },
  { value: 'ROSA', hex: '#FF4081' },
  { value: 'CYAN', hex: '#18FFFF' },
  { value: 'GRIS', hex: '#9E9E9E' },
  { value: 'NEGRO', hex: '#212121' },
  { value: 'POR_DEFECTO', hex: '#E0E0E0' },
];

const DROPDOWN_WIDTH = 280;

export function TagPicker({ itemId, itemTags = [], allTags, onTagsChange, onAllTagsChange }) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');
  const [newColor, setNewColor] = useState('POR_DEFECTO');
  const [busy, setBusy] = useState(false);
  const [pos, setPos] = useState({ top: 0, left: 0 });

  const triggerRef = useRef(null);
  const dropdownRef = useRef(null);
  const searchRef = useRef(null);

  const updatePosition = useCallback(() => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const spaceRight = window.innerWidth - rect.left;

    let top = rect.bottom + 6;
    let left = rect.left;

    // If not enough room on the right, align to right edge of trigger
    if (spaceRight < DROPDOWN_WIDTH) {
      left = rect.right - DROPDOWN_WIDTH;
    }
    // Clamp left to viewport
    left = Math.max(8, Math.min(left, window.innerWidth - DROPDOWN_WIDTH - 8));

    // If not enough room below (~340px max dropdown height), open upward
    if (spaceBelow < 200 && rect.top > 200) {
      top = rect.top - 6;
    }

    setPos({ top, left, openUp: spaceBelow < 200 && rect.top > 200 });
  }, []);

  useEffect(() => {
    if (!open) return;
    updatePosition();
    // Reposition on scroll of any ancestor (the modal panel scrolls)
    const handleReposition = () => updatePosition();
    window.addEventListener('scroll', handleReposition, true);
    window.addEventListener('resize', handleReposition);
    return () => {
      window.removeEventListener('scroll', handleReposition, true);
      window.removeEventListener('resize', handleReposition);
    };
  }, [open, updatePosition]);

  useEffect(() => {
    if (!open) return;
    const handle = (e) => {
      if (
        triggerRef.current && !triggerRef.current.contains(e.target) &&
        dropdownRef.current && !dropdownRef.current.contains(e.target)
      ) {
        setOpen(false);
        setCreating(false);
        setSearch('');
      }
    };
    document.addEventListener('mousedown', handle);
    return () => document.removeEventListener('mousedown', handle);
  }, [open]);

  useEffect(() => {
    if (open && searchRef.current) searchRef.current.focus();
  }, [open, creating]);

  const assignedIds = new Set(itemTags.map((t) => t.id));

  const filtered = allTags.filter((t) =>
    t.name.toLowerCase().includes(search.toLowerCase())
  );

  const exactMatch = allTags.some(
    (t) => t.name.toLowerCase() === search.trim().toLowerCase()
  );

  const handleToggleTag = async (tag) => {
    if (busy) return;
    setBusy(true);
    try {
      if (assignedIds.has(tag.id)) {
        const { data } = await removeTagFromItem(itemId, tag.id);
        onTagsChange(data.tags);
      } else {
        const { data } = await addTagToItem(itemId, tag.id);
        onTagsChange(data.tags);
      }
    } catch {
      toast.error('No se pudo actualizar la etiqueta.');
    } finally {
      setBusy(false);
    }
  };

  const handleCreate = async (quickCreate = false) => {
    const name = (creating ? newName : search).trim();
    if (!name || busy) return;
    const color = quickCreate
      ? TAG_COLORS.filter((c) => c.value !== 'POR_DEFECTO')[Math.floor(Math.random() * (TAG_COLORS.length - 1))].value
      : newColor;
    setBusy(true);
    try {
      const { data: tag } = await createTag({ name, color });
      onAllTagsChange([...allTags, tag]);

      const { data: updatedItem } = await addTagToItem(itemId, tag.id);
      onTagsChange(updatedItem.tags);

      setCreating(false);
      setNewName('');
      setNewColor('POR_DEFECTO');
      setSearch('');
      toast.success(`Etiqueta «${tag.name}» creada.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'No se pudo crear la etiqueta.';
      toast.error(msg);
    } finally {
      setBusy(false);
    }
  };

  const startCreating = () => {
    setNewName(search.trim());
    setNewColor('POR_DEFECTO');
    setCreating(true);
  };

  const dropdownStyle = {
    position: 'fixed',
    zIndex: 1100,
    width: DROPDOWN_WIDTH,
    left: pos.left,
    ...(pos.openUp
      ? { bottom: window.innerHeight - pos.top }
      : { top: pos.top }),
  };

  const dropdown = open && createPortal(
    <div
      className="tag-picker-dropdown"
      ref={dropdownRef}
      style={dropdownStyle}
      onClick={(e) => e.stopPropagation()}
    >
      {!creating ? (
        <>
          <div className="tag-picker-search">
            <input
              ref={searchRef}
              type="text"
              placeholder="Buscar o crear etiqueta…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && search.trim() && !exactMatch) {
                  handleCreate(true);
                }
              }}
            />
            {search.trim() && !exactMatch && (
              <span className="tag-picker-search-hint">Enter para crear rápido</span>
            )}
          </div>

          {filtered.length > 0 ? (
            <ul className="tag-picker-list">
              {filtered.map((tag) => (
                <li
                  key={tag.id}
                  className="tag-picker-item"
                  onClick={() => handleToggleTag(tag)}
                >
                  <span
                    className="tag-picker-item-dot"
                    style={{ backgroundColor: tag.colorHex }}
                  />
                  <span>{tag.name}</span>
                  {assignedIds.has(tag.id) && (
                    <Check size={16} className="tag-picker-item-check" />
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <div className="tag-picker-empty">Sin etiquetas</div>
          )}

          {search.trim() && !exactMatch && (
            <div className="tag-picker-create-section">
              <button
                type="button"
                className="tag-picker-create-btn"
                onClick={startCreating}
              >
                <Plus size={14} />
                Crear «{search.trim()}» con color…
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="tag-color-selector">
          <div className="tag-color-new-input">
            <input
              type="text"
              placeholder="Nombre de la etiqueta"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              maxLength={50}
              autoFocus
            />
          </div>
          <p className="tag-color-selector-label">Color</p>
          <div className="tag-color-selector-row">
            {TAG_COLORS.map((c) => (
              <span
                key={c.value}
                className={`tag-color-dot${newColor === c.value ? ' selected' : ''}`}
                style={{ backgroundColor: c.hex }}
                onClick={() => setNewColor(c.value)}
                role="button"
                aria-label={c.value}
              />
            ))}
          </div>
          <div className="tag-color-create-actions">
            <button
              type="button"
              className="tag-color-cancel-btn"
              onClick={() => setCreating(false)}
            >
              Cancelar
            </button>
            <button
              type="button"
              className="tag-color-save-btn"
              disabled={!newName.trim() || busy}
              onClick={handleCreate}
            >
              {busy ? 'Creando…' : 'Crear'}
            </button>
          </div>
        </div>
      )}
    </div>,
    document.body
  );

  return (
    <div className="tag-picker-wrapper">
      <button
        type="button"
        className="tag-picker-trigger"
        ref={triggerRef}
        onClick={() => { setOpen((o) => !o); setCreating(false); setSearch(''); }}
      >
        <Plus size={14} />
        <span>Etiqueta</span>
      </button>
      {dropdown}
    </div>
  );
}
