import { useCallback, useRef, useState } from 'react';

/**
 * Envuelve una acción asíncrona impidiendo ejecuciones concurrentes o reentrantes.
 *
 * El cerrojo se basa en un `useRef` que se actualiza de forma SÍNCRONA, por lo que
 * dos clics disparados en el mismo ciclo (doble clic rápido) no pueden colarse:
 * el segundo se descarta antes de lanzar una segunda petición. Esto evita los
 * envíos duplicados que el `useState` por sí solo no frena (su actualización es
 * asíncrona y ambos clics verían el estado anterior).
 *
 * @param {(...args: any[]) => Promise<any>} action acción asíncrona a ejecutar
 * @returns {[(...args: any[]) => Promise<any>, boolean]} tupla `[run, pending]`
 *   donde `run` es el manejador protegido y `pending` sirve para deshabilitar el botón.
 */
export function useAsyncAction(action) {
  const [pending, setPending] = useState(false);
  const lockRef = useRef(false);
  const actionRef = useRef(action);
  actionRef.current = action;

  const run = useCallback(async (...args) => {
    if (lockRef.current) return undefined;
    lockRef.current = true;
    setPending(true);
    try {
      return await actionRef.current(...args);
    } finally {
      lockRef.current = false;
      setPending(false);
    }
  }, []);

  return [run, pending];
}

/**
 * Variante con cerrojo por clave, para listas donde cada elemento dispara su
 * propia acción (p. ej. seguir a distintos usuarios o responder a comentarios).
 *
 * Devuelve un envoltorio que recibe la clave del elemento y la acción a ejecutar;
 * ignora la llamada si ya hay una en curso para esa misma clave.
 *
 * @returns {(key: any, action: () => Promise<any>) => Promise<any>}
 */
export function useKeyedAsyncLock() {
  const inFlightRef = useRef(new Set());

  return useCallback(async (key, action) => {
    if (inFlightRef.current.has(key)) return undefined;
    inFlightRef.current.add(key);
    try {
      return await action();
    } finally {
      inFlightRef.current.delete(key);
    }
  }, []);
}
