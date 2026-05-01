/** Orden alineado con el enum MediaStatus del backend */
export const MEDIA_STATUS_ORDER = ['POR_VER', 'EN_PROGRESO', 'VISTO', 'ABANDONADO'];

export const MEDIA_STATUS_LABELS = {
  POR_VER: 'Por ver',
  EN_PROGRESO: 'En progreso',
  VISTO: 'Visto',
  ABANDONADO: 'Abandonado',
};

export const MEDIA_STATUS_TABS = MEDIA_STATUS_ORDER.map((value) => ({
  value,
  label: MEDIA_STATUS_LABELS[value],
}));

export const MEDIA_STATUS_TABS_WITH_ALL = [
  { value: '', label: 'Todos' },
  ...MEDIA_STATUS_TABS,
];

/** Tipos del backend (MediaType) para filtros de búsqueda */
export const MEDIA_TYPES = [
  { value: '', label: 'Todos los tipos' },
  { value: 'PELICULA', label: 'Película' },
  { value: 'SERIE', label: 'Serie' },
  { value: 'LIBRO', label: 'Libro' },
  { value: 'VIDEOJUEGO', label: 'Videojuego' },
  { value: 'ANIME', label: 'Anime' },
  { value: 'MANGA', label: 'Manga' },
  { value: 'MUSICA', label: 'Música' },
];

export const MEDIA_TYPE_LABELS = Object.fromEntries(
  MEDIA_TYPES.filter((t) => t.value).map((t) => [t.value, t.label])
);

/** Pestañas simplificadas del muro → conjuntos de MediaType (string del API) */
export const FEED_TABS = [
  { id: 'GENERAL', label: 'General' },
  { id: 'AUDIOVISUAL', label: 'Audiovisual' },
  { id: 'LECTURA', label: 'Lectura' },
  { id: 'JUEGOS_MUSICA', label: 'Juegos y música' },
];

export const FEED_CATEGORY_TYPES = {
  GENERAL: null,
  AUDIOVISUAL: ['PELICULA', 'SERIE', 'ANIME'],
  LECTURA: ['LIBRO', 'MANGA'],
  JUEGOS_MUSICA: ['VIDEOJUEGO', 'MUSICA'],
};

export function postMatchesFeedTab(post, tabId) {
  if (tabId === 'GENERAL') return true;
  const types = FEED_CATEGORY_TYPES[tabId];
  if (!types || !post.linkedItemType) return false;
  return types.includes(post.linkedItemType);
}
