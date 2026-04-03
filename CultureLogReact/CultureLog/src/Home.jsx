import { useState } from 'react';
import './App.css'; 

function Home({ userName, profilePic, onLogout, onGoToProfile, onGoToPortal }) {
  const [activeCategory, setActiveCategory] = useState('General');
  const [newPostContent, setNewPostContent] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedWork, setSelectedWork] = useState(null);

  const fakeDatabase = [
    { id: 101, title: 'Interstellar', type: 'Películas', year: '2014', creator: 'Christopher Nolan' },
    { id: 102, title: 'El Padrino', type: 'Películas', year: '1972', creator: 'Francis Ford Coppola' },
    { id: 103, title: '1984', type: 'Libros', year: '1949', creator: 'George Orwell' },
    { id: 104, title: 'Cien Años de Soledad', type: 'Libros', year: '1967', creator: 'G. García Márquez' },
    { id: 105, title: 'Abbey Road', type: 'Música', year: '1969', creator: 'The Beatles' },
    { id: 106, title: 'Thriller', type: 'Música', year: '1982', creator: 'Michael Jackson' },
  ];

  const searchResults = searchTerm.length > 1 
    ? fakeDatabase.filter(work => 
        (activeCategory === 'General' || work.type === activeCategory) &&
        work.title.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : [];

  const [posts, setPosts] = useState([
    {
      id: 1,
      author: 'Equipo CultureLog',
      authorPic: 'https://www.pngarts.com/files/3/Monkey-Transparent-Background-PNG.png',
      content: '¡Bienvenido a tu nuevo espacio! Aquí puedes llevar el registro de todas tus aventuras culturales. Mira este ejemplo de reseña vinculada:',
      category: 'Películas',
      linkedWork: { title: 'Interstellar', year: '2014', type: 'Películas' }, 
      
      // 1. CAMBIO: Pasamos de un número a un array con los nombres de quienes han dado like
      likedBy: ['Carlos', 'Maria'], 
      date: 'Hace un momento'
    }
  ]);

  const handlePostSubmit = (e) => {
    e.preventDefault();
    if (!newPostContent.trim()) return;

    const newPost = {
      id: Date.now(),
      author: userName || 'Usuario', 
      authorPic: profilePic,         
      content: newPostContent,
      category: activeCategory === 'General' ? 'General' : activeCategory, 
      linkedWork: selectedWork, 
      
      // 2. CAMBIO: Al crear un post, nace con 0 likes (un array vacío)
      likedBy: [], 
      date: 'Ahora mismo'
    };

    setPosts([newPost, ...posts]);
    setNewPostContent('');
    setSelectedWork(null); 
    setSearchTerm('');     
  };

  // 3. CAMBIO: La nueva función súper inteligente de Likes
  const handleLike = (postId) => {
    const updatedPosts = posts.map((post) => {
      if (post.id === postId) {
        // ¿El usuario actual ya está en la lista de likes de este post?
        const hasLiked = post.likedBy.includes(userName);

        if (hasLiked) {
          // Si ya le dio like, lo quitamos de la lista (Unlike)
          const newLikedBy = post.likedBy.filter(name => name !== userName);
          return { ...post, likedBy: newLikedBy };
        } else {
          // Si no le ha dado like, lo añadimos a la lista (Like)
          return { ...post, likedBy: [...post.likedBy, userName] };
        }
      }
      return post;
    });
    setPosts(updatedPosts);
  };

  const filteredPosts = activeCategory === 'General' 
    ? posts 
    : posts.filter(post => post.category === activeCategory);

  return (
    <div className="home-container">
      
      <header className="top-header">
        <div className="header-left">
          <h2 className="brand-logo" style={{ fontSize: '1.5rem', margin: 0 }}>
            Culture<span>Log</span>
          </h2>
        </div>

        <nav className="header-center">
          {['General', 'Libros', 'Películas', 'Música'].map((category) => (
            <button 
              key={category}
              className={`category-btn ${activeCategory === category ? 'active' : ''}`}
              onClick={() => setActiveCategory(category)}
            >
              {category}
            </button>
          ))}
        </nav>

        <div className="header-right">
          <button 
              style={{ background: 'none', border: '1px solid #444', borderRadius: '6px', color: '#e0e0e0', padding: '6px 12px', fontSize: '0.9rem', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' }}
              onClick={onGoToPortal}
          >
            Mi Portal 
          </button>

            <button 
                style={{ background: 'none', border: 'none', color: '#a0a0a0', fontSize: '0.95rem', fontWeight: '600', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px' }}
                onClick={onGoToProfile}
            >
             @{userName} ⚙️
            </button>
          <button className="logout-button" onClick={onLogout}>
            Salir
          </button>
        </div>
      </header>

      <main className="feed">
        <div className="create-post-card">
          <form onSubmit={handlePostSubmit}>
            <div style={{ position: 'relative', marginBottom: '15px' }}>
              {!selectedWork ? (
                <>
                  <input 
                    type="text" 
                    placeholder={`🔍 Buscar obra para reseñar...`}
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="portal-input"
                    style={{ width: '100%', padding: '10px 15px', backgroundColor: '#252525', border: '1px solid #444', borderRadius: '8px', color: '#fff' }}
                  />
                  {searchResults.length > 0 && (
                    <div className="search-dropdown">
                      {searchResults.map(result => (
                        <div 
                          key={result.id} 
                          className="search-result-item"
                          onClick={() => { setSelectedWork(result); setSearchTerm(''); }}
                        >
                          <strong>{result.title}</strong> 
                          <span style={{color: '#888', fontSize: '0.85rem', marginLeft: '10px'}}>
                            ({result.year}) - {result.creator}
                          </span>
                          <span className="post-category-tag" style={{float: 'right'}}>{result.type}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <div className="selected-work-box">
                  <span>Vinculado a: <strong>{selectedWork.title}</strong> <span style={{color: '#888'}}>({selectedWork.year})</span></span>
                  <button type="button" onClick={() => setSelectedWork(null)} className="remove-work-btn">❌</button>
                </div>
              )}
            </div>

            <textarea 
              placeholder={`¿Qué te ha parecido?`}
              value={newPostContent}
              onChange={(e) => setNewPostContent(e.target.value)}
              maxLength={280}
            />
            <div className="post-actions">
              <span style={{ color: '#555', fontSize: '0.8rem', marginRight: '15px' }}>
                {newPostContent.length}/280
              </span>
              <button type="submit" className="login-button" style={{ padding: '8px 25px', fontSize: '0.9rem', width: 'auto' }}>
                Publicar en {activeCategory}
              </button>
            </div>
          </form>
        </div>

        <div className="posts-list">
          {filteredPosts.length > 0 ? (
            filteredPosts.map((post) => {
              
              // 4. CAMBIO: Comprobamos si TU nombre está en la lista de likes de ESTA publicación
              const isLikedByMe = post.likedBy.includes(userName);
              
              return (
                <div key={post.id} className="post-card">
                  
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', alignItems: 'flex-start' }}>  
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <img src={post.authorPic} alt={post.author} className="post-avatar-small" />
                      <div>
                        <h4 className="post-author" style={{ marginBottom: '2px' }}>{post.author}</h4>
                        <span className="post-category-tag">{post.category}</span>
                      </div>
                    </div>
                    <span style={{ color: '#555', fontSize: '0.8rem' }}>{post.date}</span>
                  </div>
                  
                  {post.linkedWork && (
                    <div className="post-linked-work">
                      Reseña de: <strong>{post.linkedWork.title}</strong> <span style={{color: '#a0a0a0'}}>({post.linkedWork.year})</span>
                    </div>
                  )}
                  
                  <p className="post-content">{post.content}</p>
                  
                  <div className="post-footer">
                    <button 
                      // Si te gusta, lo pintamos de rojo suave (#ff4b4b). Si no, gris (#888)
                      style={{ 
                        background: 'none', 
                        border: 'none', 
                        color: isLikedByMe ? '#ff4b4b' : '#888', 
                        cursor: 'pointer', 
                        padding: 0, 
                        fontSize: '0.95rem', 
                        fontWeight: '600',
                        transition: 'color 0.2s'
                      }}
                      onClick={() => handleLike(post.id)}
                    >
                      {/* 5. CAMBIO: Corazón dinámico y contamos cuánta gente hay en el array */}
                      {isLikedByMe ? '❤️' : '🤍'} {post.likedBy.length} Me gusta
                    </button>
                  </div>

                </div>
              );
            })
          ) : (
            <div style={{ textAlign: 'center', color: '#888', marginTop: '40px', padding: '40px', backgroundColor: '#1e1e1e', borderRadius: '12px' }}>
              <p style={{ fontSize: '1.2rem', marginBottom: '10px' }}>Aún no hay publicaciones de {activeCategory}.</p>
              <p style={{ fontSize: '0.9rem' }}>¡Sé el primero en compartir algo interesante!</p>
            </div>
          )}
        </div>
      </main>

    </div>
  );
}

export default Home;