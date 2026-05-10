import { useEffect, useRef, useState } from 'react';
import { X, ImagePlus, Trash2 } from 'lucide-react';
import { MEDIA_TYPES, MEDIA_STATUS_TABS } from '../constants/media';
import { uploadImage } from '../services/api';

const TYPE_OPTIONS = MEDIA_TYPES.filter((t) => t.value);
const STATUS_OPTIONS = MEDIA_STATUS_TABS;
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

export function CreateCustomItemModal({ open, onClose, onSubmit, submitting = false }) {
  const overlayRef = useRef(null);
  const panelRef = useRef(null);
  const fileInputRef = useRef(null);

  const [title, setTitle] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('POR_VER');
  const [creator, setCreator] = useState('');
  const [genre, setGenre] = useState('');
  const [releaseDate, setReleaseDate] = useState('');
  const [description, setDescription] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!open) return;
    setTitle('');
    setType('');
    setStatus('POR_VER');
    setCreator('');
    setGenre('');
    setReleaseDate('');
    setDescription('');
    setImageFile(null);
    setImagePreview(null);
    setUploadingImage(false);
    setError('');
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const prevBody = document.body.style.overflow;
    const prevHtml = document.documentElement.style.overflow;
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);
    panelRef.current?.focus();
    return () => {
      document.body.style.overflow = prevBody;
      document.documentElement.style.overflow = prevHtml;
      document.removeEventListener('keydown', handleKey);
    };
  }, [open, onClose]);

  useEffect(() => {
    if (!imageFile) {
      setImagePreview(null);
      return;
    }
    const url = URL.createObjectURL(imageFile);
    setImagePreview(url);
    return () => URL.revokeObjectURL(url);
  }, [imageFile]);

  if (!open) return null;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onClose();
  };

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!ALLOWED_TYPES.includes(file.type)) {
      setError('Tipo de archivo no permitido. Solo se aceptan: JPEG, PNG, GIF, WEBP.');
      return;
    }
    if (file.size > MAX_FILE_SIZE) {
      setError('La imagen no puede superar los 5 MB.');
      return;
    }
    setError('');
    setImageFile(file);
  };

  const handleRemoveImage = () => {
    setImageFile(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('El título es obligatorio.');
      return;
    }
    if (!type) {
      setError('El tipo de medio es obligatorio.');
      return;
    }
    setError('');

    let imageUrl = null;
    if (imageFile) {
      setUploadingImage(true);
      try {
        const { data } = await uploadImage(imageFile);
        imageUrl = data;
      } catch {
        setError('No se pudo subir la imagen. Inténtalo de nuevo.');
        setUploadingImage(false);
        return;
      }
      setUploadingImage(false);
    }

    onSubmit({
      title: title.trim(),
      type,
      status,
      creator: creator.trim(),
      genre: genre.trim(),
      releaseDate: releaseDate || null,
      description: description.trim(),
      imageUrl,
    });
  };

  const isBusy = submitting || uploadingImage;

  return (
    <div className="mdm-overlay" ref={overlayRef} onClick={handleOverlayClick}>
      <div
        className="mdm-panel create-custom-modal"
        ref={panelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="ccm-title"
      >
        <header className="mdm-header">
          <h2 id="ccm-title" className="mdm-title">Añadir ítem personalizado</h2>
          <button type="button" className="mdm-close" onClick={onClose} aria-label="Cerrar">
            <X size={20} />
          </button>
        </header>

        <form className="create-custom-form" onSubmit={handleSubmit}>
          <div className="ccm-field">
            <label htmlFor="ccm-item-title" className="ccm-label">Título *</label>
            <input
              id="ccm-item-title"
              type="text"
              className="portal-input"
              placeholder="Nombre de la obra"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              autoFocus
              maxLength={300}
            />
          </div>

          <div className="ccm-row">
            <div className="ccm-field ccm-field-half">
              <label htmlFor="ccm-type" className="ccm-label">Tipo *</label>
              <select
                id="ccm-type"
                className="portal-input ccm-select"
                value={type}
                onChange={(e) => setType(e.target.value)}
              >
                <option value="">Seleccionar tipo</option>
                {TYPE_OPTIONS.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            <div className="ccm-field ccm-field-half">
              <label htmlFor="ccm-status" className="ccm-label">Estado</label>
              <select
                id="ccm-status"
                className="portal-input ccm-select"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                {STATUS_OPTIONS.map((s) => (
                  <option key={s.value} value={s.value}>{s.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="ccm-row">
            <div className="ccm-field ccm-field-half">
              <label htmlFor="ccm-creator" className="ccm-label">Creador / Autor</label>
              <input
                id="ccm-creator"
                type="text"
                className="portal-input"
                placeholder="Autor, director, estudio…"
                value={creator}
                onChange={(e) => setCreator(e.target.value)}
                maxLength={200}
              />
            </div>

            <div className="ccm-field ccm-field-half">
              <label htmlFor="ccm-genre" className="ccm-label">Género</label>
              <input
                id="ccm-genre"
                type="text"
                className="portal-input"
                placeholder="Ciencia ficción, Terror…"
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                maxLength={100}
              />
            </div>
          </div>

          <div className="ccm-field">
            <label htmlFor="ccm-release" className="ccm-label">Fecha de lanzamiento</label>
            <input
              id="ccm-release"
              type="date"
              className="portal-input"
              value={releaseDate}
              onChange={(e) => setReleaseDate(e.target.value)}
            />
          </div>

          <div className="ccm-field">
            <label className="ccm-label">Imagen (portada, póster)</label>
            <div className="ccm-image-upload">
              {imagePreview ? (
                <div className="ccm-image-preview-wrapper">
                  <img src={imagePreview} alt="Vista previa" className="ccm-image-preview" />
                  <button
                    type="button"
                    className="ccm-image-remove"
                    onClick={handleRemoveImage}
                    aria-label="Quitar imagen"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ) : (
                <button
                  type="button"
                  className="ccm-image-select-btn"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <ImagePlus size={22} />
                  <span>Seleccionar imagen</span>
                </button>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                className="ccm-file-input"
                onChange={handleFileSelect}
              />
            </div>
          </div>

          <div className="ccm-field">
            <label htmlFor="ccm-desc" className="ccm-label">Descripción</label>
            <textarea
              id="ccm-desc"
              className="portal-input ccm-textarea"
              placeholder="Sinopsis o notas sobre la obra…"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              maxLength={2000}
            />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <div className="ccm-actions">
            <button type="button" className="ccm-cancel-btn" onClick={onClose} disabled={isBusy}>
              Cancelar
            </button>
            <button type="submit" className="login-button ccm-submit-btn" disabled={isBusy}>
              {uploadingImage ? 'Subiendo imagen…' : submitting ? 'Añadiendo…' : 'Añadir a biblioteca'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
