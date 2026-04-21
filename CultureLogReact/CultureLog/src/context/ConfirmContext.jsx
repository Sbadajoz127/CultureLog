import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';

const ConfirmContext = createContext(null);

function ConfirmModal({ state, onResolve }) {
  const overlayRef = useRef(null);
  const confirmBtnRef = useRef(null);

  useEffect(() => {
    if (!state) return;

    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const handleKey = (e) => {
      if (e.key === 'Escape') onResolve(false);
    };
    document.addEventListener('keydown', handleKey);
    confirmBtnRef.current?.focus();

    return () => {
      document.body.style.overflow = prev;
      document.removeEventListener('keydown', handleKey);
    };
  }, [state, onResolve]);

  if (!state) return null;

  const {
    title = 'Confirmar',
    message,
    confirmText = 'Confirmar',
    cancelText = 'Cancelar',
    variant = 'danger',
    isAlert = false,
  } = state;

  const handleOverlayClick = (e) => {
    if (e.target === overlayRef.current) onResolve(false);
  };

  return (
    <div
      className="confirm-modal-overlay"
      ref={overlayRef}
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-label={title}
    >
      <div className="confirm-modal-panel">
        <h3 className="confirm-modal-title">{title}</h3>
        <p className="confirm-modal-message">{message}</p>
        <div className="confirm-modal-actions">
          {!isAlert && (
            <button
              type="button"
              className="confirm-modal-btn confirm-modal-btn-cancel"
              onClick={() => onResolve(false)}
            >
              {cancelText}
            </button>
          )}
          <button
            ref={confirmBtnRef}
            type="button"
            className={`confirm-modal-btn confirm-modal-btn-${variant}`}
            onClick={() => onResolve(true)}
          >
            {isAlert ? 'Aceptar' : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

export function ConfirmProvider({ children }) {
  const [state, setState] = useState(null);
  const resolveRef = useRef(null);

  const confirm = useCallback((options) => {
    return new Promise((resolve) => {
      resolveRef.current = resolve;
      setState(options);
    });
  }, []);

  const handleResolve = useCallback((value) => {
    resolveRef.current?.(value);
    resolveRef.current = null;
    setState(null);
  }, []);

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      <ConfirmModal state={state} onResolve={handleResolve} />
    </ConfirmContext.Provider>
  );
}

export function useConfirm() {
  const ctx = useContext(ConfirmContext);
  if (!ctx) throw new Error('useConfirm must be used within a ConfirmProvider');
  return ctx;
}
