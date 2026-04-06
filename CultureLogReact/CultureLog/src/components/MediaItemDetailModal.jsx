import { useEffect, useRef } from 'react';
import { X, Star, Calendar, Clock, Tag, BookOpen, User, Layers } from 'lucide-react';
import { MEDIA_TYPE_LABELS, MEDIA_STATUS_LABELS } from '../constants/media';

const SOURCE_LABELS = {
  TMDB: 'TMDB',
  GOOGLE_BOOKS: 'Google Books',
  JIKAN: 'Jikan (MAL)',
  RAWG: 'RAWG',
};

function formatDate(iso) {
  if (!iso) return null;
  try {
    return new Date(iso + 'T00:00:00').toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return iso;
  }
}

function RatingDisplay({ rating }) {
  if (rating == null) return null;
  const filled = Math.round(rating / 2);
  return (
    <div className="mdm-rating">
      <div className="mdm-rating-stars">
        {Array.from({ length: 5 }, (_, i) => (
          <Star
            key={i}
            size={18}
            className={i < filled ? 'mdm-star-filled' : 'mdm-star-empty'}
            fill={i < filled ? 'currentColor' : 'none'}
          />
        ))}
      </div>
      <span className="mdm-rating-number">{rating}/10</span>
    </div>
  );
}

export function MediaItemDetailModal({ item, onClose }) {
  const overlayRef = useRef(null);
  const panelRef = useRef(null);

  useEffect(() => {
    if (!item) return;

    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);

    panelRef.current?.focus();

    return () => {
      document.body.style.overflow = prev;
      document.removeEventListener('keydown', handleKey);
    };
  }, [item, onClose]);

  if (!item) return null;

  const tagList = item.tagNames ? Array.from(item.tagNames) : [];
  const releaseDateFmt = formatDate(item.releaseDate);
  const dateAddedFmt = formatDate(item.dateAdded);
  const sourceLabel = SOURCE_LABELS[item.externalSource] || item.externalSource;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onClose();
  };

  return (
    <div
      className="mdm-overlay"
      ref={overlayRef}
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-label={`Detalle de ${item.title}`}
    >
      <div className="mdm-panel" ref={panelRef} tabIndex={-1}>
        <button
          type="button"
          className="mdm-close"
          onClick={onClose}
          aria-label="Cerrar"
        >
          <X size={20} />
        </button>

        <div className="mdm-hero">
          {item.itemImageUrl ? (
            <img src={item.itemImageUrl} alt={item.title} className="mdm-cover" />
          ) : (
            <div className="mdm-cover mdm-cover-placeholder">
              <Layers size={40} />
              <span>{MEDIA_TYPE_LABELS[item.type] || item.type}</span>
            </div>
          )}

          <div className="mdm-hero-info">
            <h2 className="mdm-title">{item.title}</h2>

            <div className="mdm-meta-row">
              <span className="mdm-badge mdm-badge-type">
                {MEDIA_TYPE_LABELS[item.type] || item.type}
              </span>
              <span className="mdm-badge mdm-badge-status">
                {MEDIA_STATUS_LABELS[item.status] || item.status}
              </span>
            </div>

            {item.creator && (
              <p className="mdm-meta-line">
                <User size={14} />
                <span>{item.creator}</span>
              </p>
            )}
            {item.genre && (
              <p className="mdm-meta-line">
                <Tag size={14} />
                <span>{item.genre}</span>
              </p>
            )}
            {releaseDateFmt && (
              <p className="mdm-meta-line">
                <Calendar size={14} />
                <span>{releaseDateFmt}</span>
              </p>
            )}

            <RatingDisplay rating={item.rating} />
          </div>
        </div>

        {tagList.length > 0 && (
          <div className="mdm-section mdm-tags">
            {tagList.map((t) => (
              <span key={t} className="mdm-tag-chip">#{t}</span>
            ))}
          </div>
        )}

        {item.description && (
          <div className="mdm-section">
            <h3 className="mdm-section-title">
              <BookOpen size={16} /> Sinopsis
            </h3>
            <p className="mdm-section-text">{item.description}</p>
          </div>
        )}

        {item.comment && (
          <div className="mdm-section">
            <h3 className="mdm-section-title">
              <Layers size={16} /> Notas personales
            </h3>
            <p className="mdm-section-text mdm-comment">{item.comment}</p>
          </div>
        )}

        <div className="mdm-footer-meta">
          {dateAddedFmt && (
            <span className="mdm-footer-item">
              <Clock size={13} /> Añadido el {dateAddedFmt}
            </span>
          )}
          {sourceLabel && (
            <span className="mdm-footer-item">
              Fuente: {sourceLabel}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
