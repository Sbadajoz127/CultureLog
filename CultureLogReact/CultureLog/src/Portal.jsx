// src/Portal.jsx
import { useState } from 'react';
import './App.css';

// --- MINI COMPONENTE: Representa un solo elemento de la lista ---
function PortalItem({ item, type, onDelete, onAddTag }) {
  const [tagInput, setTagInput] = useState('');

  const handleTagSubmit = (e) => {
    e.preventDefault();
    if (tagInput.trim()) {
      onAddTag(item.id, type, tagInput);
      setTagInput(''); // Limpia el input tras añadir el tag
    }
  };

  return (
    <div className="portal-item">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', marginBottom: '8px' }}>
        <span style={{ fontWeight: '600' }}>{item.title}</span>
        <button onClick={() => onDelete(item.id, type)} className="delete-btn">❌</button>
      </div>
      
      {/* Zona de etiquetas existentes */}
      <div className="tags-container">
        {item.tags && item.tags.map((tag, index) => (
          <span key={index} className="custom-tag">#{tag}</span>
        ))}
      </div>

      {/* Formulario pequeñito para añadir una nueva etiqueta a ESTE elemento */}
      <form onSubmit={handleTagSubmit} style={{ display: 'flex', gap: '5px', marginTop: '10px' }}>
        <input 
          type="text" 
          placeholder="Añadir tag..." 
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
          className="tag-input"
        />
        <button type="submit" className="tag-submit-btn">+</button>
      </form>
    </div>
  );
}

// --- COMPONENTE PRINCIPAL: La pantalla del Portal ---
function Portal({ userName, onBack, onLogout }) {
  const [newItemTitle, setNewItemTitle] = useState('');
  const [selectedType, setSelectedType] = useState('Película');

  // Ahora los elementos iniciales tienen un array "tags" vacío o con algún ejemplo
  const [movies, setMovies] = useState([{ id: 1, title: 'Interstellar', tags: ['Sci-Fi', 'Favorita'] }]);
  const [books, setBooks] = useState([{ id: 2, title: '1984 - George Orwell', tags: ['Distopía'] }]);
  const [music, setMusic] = useState([{ id: 3, title: 'Abbey Road - The Beatles', tags: [] }]);

  const handleAddItem = (e) => {
    e.preventDefault();
    if (!newItemTitle.trim()) return;

    // Al crear un nuevo elemento, nos aseguramos de que nazca con su array de tags vacío
    const newItem = { id: Date.now(), title: newItemTitle, tags: [] };

    if (selectedType === 'Película') setMovies([...movies, newItem]);
    if (selectedType === 'Libro') setBooks([...books, newItem]);
    if (selectedType === 'Música') setMusic([...music, newItem]);

    setNewItemTitle(''); 
  };

  const handleDelete = (id, type) => {
    if (type === 'Película') setMovies(movies.filter(m => m.id !== id));
    if (type === 'Libro') setBooks(books.filter(b => b.id !== id));
    if (type === 'Música') setMusic(music.filter(m => m.id !== id));
  };

  // NUEVA FUNCIÓN: Añadir un tag a un elemento específico
  const handleAddTag = (id, type, newTag) => {
    // Esta función busca el elemento por su ID y le inyecta el nuevo tag a su array
    const updateList = (list) => list.map(item => 
      item.id === id ? { ...item, tags: [...item.tags, newTag] } : item
    );

    if (type === 'Película') setMovies(updateList(movies));
    if (type === 'Libro') setBooks(updateList(books));
    if (type === 'Música') setMusic(updateList(music));
  };

  return (
    <div className="home-container">
      <header className="top-header">
        <div className="header-left">
          <h2 className="brand-logo" style={{ fontSize: '1.5rem', margin: 0, cursor: 'pointer' }} onClick={onBack}>
            Culture<span>Log</span>
          </h2>
        </div>
        <div className="header-right">
          <span style={{ color: '#a0a0a0', fontSize: '0.9rem', marginRight: '15px' }}>Portal de {userName}</span>
          <button className="logout-button" onClick={onBack}>Volver al Muro</button>
        </div>
      </header>

      <main style={{ maxWidth: '1000px', margin: '40px auto', padding: '0 20px' }}>
        <h2 style={{ marginBottom: '20px', fontSize: '1.8rem' }}>Mi Colección Pendiente</h2>

        <div className="create-post-card" style={{ marginBottom: '30px' }}>
          <form onSubmit={handleAddItem} style={{ display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
            <input 
              type="text" 
              placeholder="Ej: El Padrino, Cien años de soledad..." 
              value={newItemTitle}
              onChange={(e) => setNewItemTitle(e.target.value)}
              className="portal-input"
              required
            />
            <select value={selectedType} onChange={(e) => setSelectedType(e.target.value)} className="portal-select">
              <option value="Película">🎬 Película</option>
              <option value="Libro">📚 Libro</option>
              <option value="Música">🎵 Música</option>
            </select>
            <button type="submit" className="login-button" style={{ padding: '10px 20px' }}>
              Añadir a pendientes
            </button>
          </form>
        </div>

        <div className="portal-grid">
          
          <div className="portal-column">
            <h3 style={{ borderBottom: '2px solid #333', paddingBottom: '10px', marginBottom: '15px' }}>🎬 Películas por ver</h3>
            {movies.length === 0 && <p style={{ color: '#888', fontSize: '0.9rem' }}>No hay películas pendientes.</p>}
            {/* Aquí usamos nuestro mini-componente en lugar del div de antes */}
            {movies.map(movie => (
              <PortalItem key={movie.id} item={movie} type="Película" onDelete={handleDelete} onAddTag={handleAddTag} />
            ))}
          </div>

          <div className="portal-column">
            <h3 style={{ borderBottom: '2px solid #333', paddingBottom: '10px', marginBottom: '15px' }}>📚 Libros por leer</h3>
            {books.length === 0 && <p style={{ color: '#888', fontSize: '0.9rem' }}>No hay libros pendientes.</p>}
            {books.map(book => (
              <PortalItem key={book.id} item={book} type="Libro" onDelete={handleDelete} onAddTag={handleAddTag} />
            ))}
          </div>

          <div className="portal-column">
            <h3 style={{ borderBottom: '2px solid #333', paddingBottom: '10px', marginBottom: '15px' }}>🎵 Música por escuchar</h3>
            {music.length === 0 && <p style={{ color: '#888', fontSize: '0.9rem' }}>No hay música pendiente.</p>}
            {music.map(track => (
              <PortalItem key={track.id} item={track} type="Música" onDelete={handleDelete} onAddTag={handleAddTag} />
            ))}
          </div>

        </div>
      </main>
    </div>
  );
}

export default Portal;