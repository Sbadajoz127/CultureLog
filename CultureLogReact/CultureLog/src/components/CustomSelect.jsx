import { useState, useRef, useEffect } from 'react';

export function CustomSelect({ options, value, onChange, ariaLabel, className = '' }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  const selected = options.find((o) => o.value === value) || options[0];

  useEffect(() => {
    function handleClickOutside(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    function handleEsc(e) {
      if (e.key === 'Escape') setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEsc);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEsc);
    };
  }, []);

  function handleSelect(opt) {
    onChange(opt.value);
    setOpen(false);
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      setOpen((o) => !o);
    } else if (e.key === 'ArrowDown' && open) {
      e.preventDefault();
      const idx = options.findIndex((o) => o.value === value);
      const next = options[Math.min(idx + 1, options.length - 1)];
      if (next) onChange(next.value);
    } else if (e.key === 'ArrowUp' && open) {
      e.preventDefault();
      const idx = options.findIndex((o) => o.value === value);
      const prev = options[Math.max(idx - 1, 0)];
      if (prev) onChange(prev.value);
    }
  }

  return (
    <div className={`cselect ${className}`} ref={containerRef}>
      <button
        type="button"
        className={`cselect-trigger ${open ? 'open' : ''}`}
        onClick={() => setOpen((o) => !o)}
        onKeyDown={handleKeyDown}
        aria-label={ariaLabel}
        aria-expanded={open}
        aria-haspopup="listbox"
      >
        <span className="cselect-value">{selected.label}</span>
        <svg className="cselect-chevron" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </button>
      {open && (
        <ul className="cselect-dropdown" role="listbox">
          {options.map((opt) => (
            <li
              key={opt.value ?? '__empty__'}
              role="option"
              aria-selected={opt.value === value}
              className={`cselect-option ${opt.value === value ? 'selected' : ''}`}
              onClick={() => handleSelect(opt)}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
