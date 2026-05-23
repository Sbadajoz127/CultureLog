import { useState, useEffect, useRef } from 'react';
import { X, Pencil, Trash2, Check, Plus } from 'lucide-react';
import { createTag, updateTag, deleteTag } from '../services/api';
import { useConfirm } from '../context/ConfirmContext';
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

export function TagManager({ open, onClose, tags, onTagsChange }) {
  const confirm = useConfirm();
  const overlayRef = useRef(null);
  const panelRef = useRef(null);
  const inputRef = useRef(null);

  const [editingId, setEditingId] = useState(null);
  const [busy, setBusy] = useState(false);
  const [newName, setNewName] = useState('');
  const [newColor, setNewColor] = useState('POR_DEFECTO');

  useEffect(() => {
    if (!open) return;
    setEditingId(null);
    setNewName('');
    setNewColor('POR_DEFECTO');
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const prevBody = document.body.style.overflow;
    const prevHtml = document.documentElement.style.overflow;
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
    const handleKey = (e) => {
      if (e.key === 'Escape') {
        if (editingId) cancelEdit();
        else onClose();
      }
    };
    document.addEventListener('keydown', handleKey);
    panelRef.current?.focus();
    return () => {
      document.body.style.overflow = prevBody;
      document.documentElement.style.overflow = prevHtml;
      document.removeEventListener('keydown', handleKey);
    };
  }, [open, onClose, editingId]);

  if (!open) return null;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onClose();
  };

  const startEdit = (tag) => {
    setEditingId(tag.id);
    setNewName(tag.name);
    setNewColor(tag.color);
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setNewName('');
    setNewColor('POR_DEFECTO');
  };

  const saveEdit = async () => {
    if (!newName.trim() || busy) return;
    setBusy(true);
    try {
      const { data } = await updateTag(editingId, { name: newName.trim(), color: newColor });
      onTagsChange(tags.map((t) => (t.id === editingId ? data : t)));
      cancelEdit();
      toast.success('Etiqueta actualizada.');
    } catch (err) {
      toast.error(err.response?.data?.message || 'No se pudo actualizar la etiqueta.');
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async (tag) => {
    const ok = await confirm({
      title: 'Eliminar etiqueta',
      message: `¿Eliminar «${tag.name}»? Se quitará de todos los ítems.`,
      confirmText: 'Eliminar',
      variant: 'danger',
    });
    if (!ok) return;
    setBusy(true);
    try {
      await deleteTag(tag.id);
      onTagsChange(tags.filter((t) => t.id !== tag.id));
      if (editingId === tag.id) cancelEdit();
      toast.success(`Etiqueta «${tag.name}» eliminada.`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'No se pudo eliminar la etiqueta.');
    } finally {
      setBusy(false);
    }
  };

  const handleCreate = async () => {
    if (!newName.trim() || busy) return;
    setBusy(true);
    try {
      const { data } = await createTag({ name: newName.trim(), color: newColor });
      onTagsChange([...tags, data]);
      setNewName('');
      setNewColor('POR_DEFECTO');
      toast.success(`Etiqueta «${data.name}» creada.`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'No se pudo crear la etiqueta.');
    } finally {
      setBusy(false);
    }
  };

  const handleSubmit = () => {
    if (editingId) saveEdit();
    else handleCreate();
  };

  const currentColorHex = TAG_COLORS.find((c) => c.value === newColor)?.hex || '#E0E0E0';

  return (
    <div className="mdm-overlay" ref={overlayRef} onClick={handleOverlayClick}>
      <div
        className="mdm-panel create-custom-modal"
        ref={panelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="tag-manage-title"
      >
        <header className="mdm-header">
          <h2 id="tag-manage-title" className="mdm-title">Gestionar etiquetas</h2>
          <button type="button" className="mdm-close" onClick={onClose} aria-label="Cerrar">
            <X size={20} />
          </button>
        </header>

        <div style={{ padding: '0 20px 20px' }}>
          {tags.length === 0 ? (
            <p className="tag-manage-empty">No tienes etiquetas. Crea la primera.</p>
          ) : (
            <ul className="tag-manage-list">
              {tags.map((tag) => (
                <li
                  key={tag.id}
                  className={`tag-manage-item${editingId === tag.id ? ' tag-manage-item--editing' : ''}`}
                >
                  <span
                    className="tag-manage-item-dot"
                    style={{ backgroundColor: tag.colorHex }}
                  />
                  <span className="tag-manage-item-name">{tag.name}</span>
                  <div className="tag-manage-item-actions">
                    <button
                      type="button"
                      className="tag-manage-icon-btn"
                      onClick={() => editingId === tag.id ? cancelEdit() : startEdit(tag)}
                      aria-label={editingId === tag.id ? 'Cancelar edición' : 'Editar'}
                    >
                      {editingId === tag.id ? <X size={14} /> : <Pencil size={14} />}
                    </button>
                    <button
                      type="button"
                      className="tag-manage-icon-btn tag-manage-icon-btn--danger"
                      onClick={() => handleDelete(tag)}
                      disabled={busy}
                      aria-label="Eliminar"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}

          <div className={`tag-manage-create-row${editingId ? ' tag-manage-create-row--editing' : ''}`}>
            <span
              className="tag-color-dot"
              style={{
                backgroundColor: currentColorHex,
                width: 20,
                height: 20,
                cursor: 'default',
              }}
            />
            <input
              ref={inputRef}
              type="text"
              placeholder={editingId ? 'Editando etiqueta…' : 'Nueva etiqueta…'}
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              maxLength={50}
              onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit(); }}
            />
            <div className="tag-color-selector-row" style={{ flexShrink: 0 }}>
              {TAG_COLORS.slice(0, 6).map((c) => (
                <span
                  key={c.value}
                  className={`tag-color-dot${newColor === c.value ? ' selected' : ''}`}
                  style={{ backgroundColor: c.hex, width: 18, height: 18 }}
                  onClick={() => setNewColor(c.value)}
                  role="button"
                  aria-label={c.value}
                />
              ))}
            </div>
            <button
              type="button"
              className="tag-manage-icon-btn"
              onClick={handleSubmit}
              disabled={busy || !newName.trim()}
              aria-label={editingId ? 'Guardar cambios' : 'Crear etiqueta'}
            >
              {editingId ? <Check size={16} /> : <Plus size={16} />}
            </button>
            {editingId && (
              <button
                type="button"
                className="tag-manage-icon-btn"
                onClick={cancelEdit}
                aria-label="Cancelar edición"
              >
                <X size={16} />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
