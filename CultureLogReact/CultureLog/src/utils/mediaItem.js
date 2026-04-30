/**
 * Convierte un resultado de búsqueda externa al payload esperado por POST /api/search/add-to-library.
 */
export function searchResultToPayload(result) {
  return {
    externalId: result.externalId ?? null,
    source: result.source ?? null,
    title: result.title,
    type: result.type,
    genre: result.genre ?? null,
    creator: result.creator ?? null,
    description: result.description ?? null,
    releaseDate: result.releaseDate ?? null,
    imageUrl: result.imageUrl ?? null,
    rating: result.rating ?? null,
    album: result.album ?? null,
  };
}

/**
 * Construye el cuerpo MediaItemRequest a partir de un ítem devuelto por GET /api/items.
 */
export function mediaItemToRequest(item, overrides = {}) {
  return {
    title: overrides.title ?? item.title,
    itemImageUrl: overrides.itemImageUrl !== undefined ? overrides.itemImageUrl : item.itemImageUrl ?? null,
    type: overrides.type ?? item.type,
    status: overrides.status ?? item.status,
    genre: overrides.genre !== undefined ? overrides.genre : item.genre ?? null,
    creator: overrides.creator !== undefined ? overrides.creator : item.creator ?? null,
    rating: overrides.rating !== undefined ? overrides.rating : item.rating ?? null,
    comment: overrides.comment !== undefined ? overrides.comment : item.comment ?? null,
    releaseDate: overrides.releaseDate !== undefined ? overrides.releaseDate : item.releaseDate ?? null,
    description: overrides.description !== undefined ? overrides.description : item.description ?? null,
    externalId: overrides.externalId !== undefined ? overrides.externalId : item.externalId ?? null,
    externalSource: overrides.externalSource !== undefined ? overrides.externalSource : item.externalSource ?? null,
    album: overrides.album !== undefined ? overrides.album : item.album ?? null,
  };
}
