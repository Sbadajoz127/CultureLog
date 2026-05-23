import { X } from 'lucide-react';

export function TagChip({ tag, onRemove, onClick }) {
  const hex = tag.colorHex || '#E0E0E0';

  const style = {
    '--tag-color': hex,
  };

  return (
    <span
      className={`tag-chip${onClick ? ' tag-chip--clickable' : ''}`}
      style={style}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={onClick ? (e) => { if (e.key === 'Enter') onClick(); } : undefined}
    >
      <span className="tag-chip-name">#{tag.name}</span>
      {onRemove && (
        <button
          type="button"
          className="tag-chip-remove"
          onClick={(e) => { e.stopPropagation(); onRemove(tag); }}
          aria-label={`Quitar etiqueta ${tag.name}`}
        >
          <X size={12} />
        </button>
      )}
    </span>
  );
}
