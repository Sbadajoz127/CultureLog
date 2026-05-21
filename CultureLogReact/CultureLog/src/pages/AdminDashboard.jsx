import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useConfirm } from '../context/ConfirmContext';
import { AppHeader } from '../components/AppHeader';
import {
  getAdminUsers,
  getAdminStats,
  getAdminPosts,
  getAdminPostComments,
  adminDeleteUser,
  adminDeletePost,
  adminDeleteComment,
  getAdminUsersAutocomplete,
  getAdminLinkedItemsAutocomplete,
} from '../services/api';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import { toast } from 'sonner';
import '../App.css';

const PIE_COLORS = ['#448AFF', '#FF6384', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#36A2EB'];

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-ES', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function truncate(text, max = 80) {
  if (!text) return '—';
  return text.length > max ? text.slice(0, max) + '…' : text;
}

/* ──────────────────── Skeletons ──────────────────── */

function SkeletonTableRows({ colWidths, rows = 8 }) {
  return Array.from({ length: rows }).map((_, r) => (
    <tr key={r}>
      {colWidths.map((w, c) => (
        <td key={c}>
          <div className="skeleton skeleton-table-cell" style={{ width: w }} />
        </td>
      ))}
    </tr>
  ));
}

const USER_TABLE_COL_WIDTHS = [28, 110, 160, 56, 80, 32, 32, 42, 120];
const POST_TABLE_COL_WIDTHS = [28, 90, 200, 110, 110, 32, 42, 120];

function SkeletonStatsPanel() {
  return (
    <div className="admin-stats">
      <div className="admin-stats-cards">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="admin-stat-card">
            <div className="skeleton skeleton-stat-number" />
            <div className="skeleton skeleton-stat-label" />
          </div>
        ))}
      </div>
      <div className="admin-charts-row">
        <div className="admin-chart-container">
          <div className="skeleton skeleton-chart-title" />
          <div className="skeleton skeleton-chart-area" style={{ height: 300 }} />
        </div>
        <div className="admin-chart-container">
          <div className="skeleton skeleton-chart-title" />
          <div className="skeleton skeleton-chart-area" style={{ height: 300 }} />
        </div>
      </div>
      <div className="admin-charts-row">
        <div className="admin-chart-container">
          <div className="skeleton skeleton-chart-title" />
          <div className="skeleton skeleton-chart-area" style={{ height: 250 }} />
        </div>
        <div className="admin-chart-container">
          <div className="skeleton skeleton-chart-title" />
          <div className="skeleton skeleton-chart-area" style={{ height: 250 }} />
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Stats Panel ──────────────────── */

function StatsPanel({ stats }) {
  if (!stats) return null;

  const mediaTypeData = Object.entries(stats.itemsByMediaType || {}).map(([name, value]) => ({ name, value }));
  const statusData = Object.entries(stats.itemsByStatus || {}).map(([name, value]) => ({ name, value }));

  return (
    <div className="admin-stats">
      <div className="admin-stats-cards">
        <div className="admin-stat-card">
          <span className="admin-stat-number">{stats.totalUsers}</span>
          <span className="admin-stat-label">Usuarios</span>
        </div>
        <div className="admin-stat-card">
          <span className="admin-stat-number">{stats.totalPosts}</span>
          <span className="admin-stat-label">Posts</span>
        </div>
        <div className="admin-stat-card">
          <span className="admin-stat-number">{stats.totalItems}</span>
          <span className="admin-stat-label">Items</span>
        </div>
        <div className="admin-stat-card">
          <span className="admin-stat-number">{stats.totalComments}</span>
          <span className="admin-stat-label">Comentarios</span>
        </div>
      </div>

      <div className="admin-charts-row">
        {mediaTypeData.length > 0 && (
          <div className="admin-chart-container">
            <h3>Items por tipo de medio</h3>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={mediaTypeData} margin={{ top: 10, right: 20, left: 0, bottom: 40 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
                <XAxis dataKey="name" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} angle={-30} textAnchor="end" />
                <YAxis tick={{ fill: 'var(--text-muted)' }} allowDecimals={false} />
                <Tooltip contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }} />
                <Bar dataKey="value" fill="var(--accent-color)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {statusData.length > 0 && (
          <div className="admin-chart-container">
            <h3>Items por estado</h3>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label>
                  {statusData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }} />
                <Legend wrapperStyle={{ color: 'var(--text-secondary)' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="admin-charts-row">
        {stats.topUsersByItems?.length > 0 && (
          <div className="admin-chart-container">
            <h3>Top usuarios por items</h3>
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={stats.topUsersByItems} layout="vertical" margin={{ top: 10, right: 20, left: 60, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
                <XAxis type="number" tick={{ fill: 'var(--text-muted)' }} allowDecimals={false} />
                <YAxis type="category" dataKey="username" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} width={80} />
                <Tooltip contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }} />
                <Bar dataKey="count" fill="#4BC0C0" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {stats.topUsersByPosts?.length > 0 && (
          <div className="admin-chart-container">
            <h3>Top usuarios por posts</h3>
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={stats.topUsersByPosts} layout="vertical" margin={{ top: 10, right: 20, left: 60, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
                <XAxis type="number" tick={{ fill: 'var(--text-muted)' }} allowDecimals={false} />
                <YAxis type="category" dataKey="username" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} width={80} />
                <Tooltip contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }} />
                <Bar dataKey="count" fill="#FF6384" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
}

/* ──────────────────── AutocompleteSelect ──────────────────── */

function AutocompleteSelect({ placeholder, fetchOptions, value, onChange, labelKey = 'username', valueKey = 'id' }) {
  const [inputValue, setInputValue] = useState('');
  const [options, setOptions] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const containerRef = useRef(null);
  const debounceRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const loadOptions = useCallback(async (q) => {
    setLoading(true);
    try {
      const { data } = await fetchOptions(q);
      setOptions(data);
    } catch (err) {
      console.error('Error loading autocomplete options:', err);
      setOptions([]);
    } finally {
      setLoading(false);
    }
  }, [fetchOptions]);

  useEffect(() => {
    loadOptions('');
  }, [loadOptions]);

  const handleInputChange = (e) => {
    const val = e.target.value;
    setInputValue(val);
    setIsOpen(true);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      loadOptions(val);
    }, 300);
  };

  const handleSelect = (option) => {
    onChange(option ? { id: option[valueKey], label: option[labelKey] } : null);
    setInputValue('');
    setIsOpen(false);
  };

  const handleClear = () => {
    onChange(null);
    setInputValue('');
    loadOptions('');
  };

  const handleFocus = () => {
    setIsOpen(true);
  };

  return (
    <div className="admin-autocomplete" ref={containerRef}>
      <div className="admin-autocomplete-input-wrapper">
        {value ? (
          <div className="admin-autocomplete-selected">
            <span>{value.label}</span>
            <button type="button" onClick={handleClear} className="admin-autocomplete-clear">&times;</button>
          </div>
        ) : (
          <input
            type="text"
            className="admin-autocomplete-input"
            placeholder={placeholder}
            value={inputValue}
            onChange={handleInputChange}
            onFocus={handleFocus}
          />
        )}
      </div>
      {isOpen && !value && (
        <div className="admin-autocomplete-dropdown">
          {loading && <div className="admin-autocomplete-loading">Cargando...</div>}
          {!loading && (
            <>
              <div
                className="admin-autocomplete-option admin-autocomplete-option-all"
                onClick={() => handleSelect(null)}
              >
                Todos
              </div>
              {options.map((opt) => (
                <div
                  key={opt[valueKey]}
                  className="admin-autocomplete-option"
                  onClick={() => handleSelect(opt)}
                >
                  {opt[labelKey]}
                </div>
              ))}
              {options.length === 0 && (
                <div className="admin-autocomplete-empty">Sin resultados</div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}

/* ──────────────────── SortableHeader ──────────────────── */

function SortableHeader({ label, field, currentSort, onSort }) {
  const isActive = currentSort.sortBy === field;
  const direction = isActive ? currentSort.sortDir : null;

  const handleClick = () => {
    if (isActive) {
      onSort(field, direction === 'asc' ? 'desc' : 'asc');
    } else {
      onSort(field, 'asc');
    }
  };

  return (
    <th className="admin-sortable-header" onClick={handleClick}>
      <span>{label}</span>
      <span className="admin-sort-icon">
        {isActive && direction === 'asc' && ' ▲'}
        {isActive && direction === 'desc' && ' ▼'}
        {!isActive && ' ⇅'}
      </span>
    </th>
  );
}

/* ──────────────────── Users Panel ──────────────────── */

function UsersPanel({ users, page, totalPages, totalElements, pageSize, onPageSizeChange, onPageChange, onDelete, onViewProfile, selectedUser, onUserFilterChange, sortBy, sortDir, onSort, tableLoading }) {
  const handleSort = (field, dir) => {
    onSort(field, dir);
  };

  return (
    <div className="admin-users">
      <div className="admin-filters-bar">
        <AutocompleteSelect
          placeholder="Filtrar por usuario..."
          fetchOptions={getAdminUsersAutocomplete}
          value={selectedUser}
          onChange={onUserFilterChange}
          labelKey="username"
          valueKey="id"
        />
      </div>

      <div className="admin-table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <SortableHeader label="ID" field="id" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <SortableHeader label="Usuario" field="username" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <th>Email</th>
              <th>Rol</th>
              <SortableHeader label="Registro" field="createdAt" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <SortableHeader label="Posts" field="postCount" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <SortableHeader label="Items" field="itemCount" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <th>Seguidores</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {tableLoading
              ? <SkeletonTableRows colWidths={USER_TABLE_COL_WIDTHS} />
              : <>
                  {users.map((u) => (
                    <tr key={u.id} className="feed-loaded">
                      <td>{u.id}</td>
                      <td>{u.username}</td>
                      <td>{u.email}</td>
                      <td>
                        <span className={`admin-role-badge ${u.role === 'ADMIN' ? 'admin-role-admin' : 'admin-role-user'}`}>
                          {u.role}
                        </span>
                      </td>
                      <td>{formatDate(u.createdAt)}</td>
                      <td>{u.postCount}</td>
                      <td>{u.itemCount}</td>
                      <td>{u.followerCount}</td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-view" onClick={() => onViewProfile(u.username)}>
                          Ver
                        </button>
                        {u.role !== 'ADMIN' && (
                          <button className="admin-btn admin-btn-delete" onClick={() => onDelete(u.id, u.username)}>
                            Eliminar
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {users.length === 0 && (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>
                      {selectedUser ? 'No se encontraron usuarios' : 'No hay usuarios'}
                    </td></tr>
                  )}
                </>
            }
          </tbody>
        </table>
      </div>

      {!tableLoading && (
        <div className="admin-pagination">
          <div className="admin-pagination-size">
            <label>Mostrar</label>
            <select value={pageSize} onChange={(e) => onPageSizeChange(Number(e.target.value))}>
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
            </select>
            <span>de {totalElements}</span>
          </div>
          <div className="admin-pagination-nav">
            <button disabled={page <= 0} onClick={() => onPageChange(page - 1)} className="admin-btn">
              Anterior
            </button>
            <span className="admin-page-info">Página {page + 1} de {totalPages || 1}</span>
            <button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)} className="admin-btn">
              Siguiente
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ──────────────────── Comments Modal ──────────────────── */

function SkeletonCommentRows() {
  return Array.from({ length: 4 }).map((_, i) => (
    <div key={i} className="admin-comment-row">
      <div className="admin-comment-info">
        <div className="skeleton skeleton-comment-author" />
        <div className="skeleton skeleton-comment-date" />
      </div>
      <div className="skeleton skeleton-comment-text" />
    </div>
  ));
}

function CommentsModal({ postId, onClose, onDeleteComment }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const confirm = useConfirm();

  useEffect(() => {
    const prevBody = document.body.style.overflow;
    const prevHtml = document.documentElement.style.overflow;
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => {
      document.body.style.overflow = prevBody;
      document.documentElement.style.overflow = prevHtml;
      document.removeEventListener('keydown', handleKey);
    };
  }, [onClose]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await getAdminPostComments(postId);
        if (!cancelled) setComments(data);
      } catch (err) {
        console.error('Error cargando comentarios:', err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [postId]);

  const handleDelete = async (commentId) => {
    const ok = await confirm({
      title: 'Eliminar comentario',
      message: '¿Eliminar este comentario?',
      confirmText: 'Eliminar',
      variant: 'danger',
    });
    if (!ok) return;
    try {
      await onDeleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
      toast.success('Comentario eliminado.');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al eliminar comentario.');
    }
  };

  return (
    <div className="admin-modal-overlay" onClick={onClose}>
      <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
        <div className="admin-modal-header">
          <h3>Comentarios del post #{postId}</h3>
          <button className="admin-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="admin-modal-body">
          {loading && <SkeletonCommentRows />}
          {!loading && comments.length === 0 && (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>Sin comentarios</p>
          )}
          {!loading && comments.map((c) => (
            <div key={c.id} className={`admin-comment-row feed-loaded ${c.parentCommentId ? 'admin-comment-reply' : ''}`}>
              <div className="admin-comment-info">
                <strong>{c.authorUsername}</strong>
                <span className="admin-comment-date">{formatDateTime(c.createdAt)}</span>
                {c.parentCommentId && <span className="admin-comment-reply-badge">respuesta</span>}
              </div>
              <p className="admin-comment-text">{c.text}</p>
              <button className="admin-btn admin-btn-delete admin-btn-sm" onClick={() => handleDelete(c.id)}>
                Eliminar
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Posts Panel ──────────────────── */

function PostsPanel({ posts, page, totalPages, totalElements, pageSize, onPageSizeChange, onPageChange, onDeletePost, onViewPost, selectedAuthor, onAuthorFilterChange, selectedItem, onItemFilterChange, sortBy, sortDir, onSort, tableLoading }) {
  const [commentsPostId, setCommentsPostId] = useState(null);

  const handleDeleteComment = async (commentId) => {
    await adminDeleteComment(commentId);
  };

  const handleSort = (field, dir) => {
    onSort(field, dir);
  };

  return (
    <div className="admin-posts">
      <div className="admin-filters-bar">
        <AutocompleteSelect
          placeholder="Filtrar por autor..."
          fetchOptions={getAdminUsersAutocomplete}
          value={selectedAuthor}
          onChange={onAuthorFilterChange}
          labelKey="username"
          valueKey="id"
        />
        <AutocompleteSelect
          placeholder="Filtrar por item enlazado..."
          fetchOptions={getAdminLinkedItemsAutocomplete}
          value={selectedItem}
          onChange={onItemFilterChange}
          labelKey="title"
          valueKey="id"
        />
      </div>

      <div className="admin-table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <SortableHeader label="ID" field="id" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <th>Autor</th>
              <th>Contenido</th>
              <th>Item enlazado</th>
              <SortableHeader label="Fecha" field="createdAt" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <SortableHeader label="Likes" field="likeCount" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <SortableHeader label="Comentarios" field="commentCount" currentSort={{ sortBy, sortDir }} onSort={handleSort} />
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {tableLoading
              ? <SkeletonTableRows colWidths={POST_TABLE_COL_WIDTHS} />
              : <>
                  {posts.map((p) => (
                    <tr key={p.id} className="feed-loaded">
                      <td>{p.id}</td>
                      <td>{p.authorUsername}</td>
                      <td className="admin-cell-content" title={p.content}>{truncate(p.content, 50)}</td>
                      <td className="admin-cell-linked-item" title={p.linkedItemTitle}>{p.linkedItemTitle || '—'}</td>
                      <td>{formatDateTime(p.createdAt)}</td>
                      <td>{p.likeCount}</td>
                      <td>
                        <button
                          className="admin-btn-link"
                          onClick={() => setCommentsPostId(p.id)}
                          title="Ver comentarios"
                        >
                          {p.commentCount}
                        </button>
                      </td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-view" onClick={() => onViewPost(p.id)}>
                          Ver
                        </button>
                        <button className="admin-btn admin-btn-delete" onClick={() => onDeletePost(p.id, p.authorUsername)}>
                          Eliminar
                        </button>
                      </td>
                    </tr>
                  ))}
                  {posts.length === 0 && (
                    <tr><td colSpan={8} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>
                      No hay posts
                    </td></tr>
                  )}
                </>
            }
          </tbody>
        </table>
      </div>

      {!tableLoading && (
        <div className="admin-pagination">
          <div className="admin-pagination-size">
            <label>Mostrar</label>
            <select value={pageSize} onChange={(e) => onPageSizeChange(Number(e.target.value))}>
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
            </select>
            <span>de {totalElements}</span>
          </div>
          <div className="admin-pagination-nav">
            <button disabled={page <= 0} onClick={() => onPageChange(page - 1)} className="admin-btn">
              Anterior
            </button>
            <span className="admin-page-info">Página {page + 1} de {totalPages || 1}</span>
            <button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)} className="admin-btn">
              Siguiente
            </button>
          </div>
        </div>
      )}

      {commentsPostId && (
        <CommentsModal
          postId={commentsPostId}
          onClose={() => setCommentsPostId(null)}
          onDeleteComment={handleDeleteComment}
        />
      )}
    </div>
  );
}

/* ──────────────────── Main Dashboard ──────────────────── */

export default function AdminDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const confirm = useConfirm();

  const [tab, setTab] = useState('users');

  const [users, setUsers] = useState([]);
  const [userPage, setUserPage] = useState(0);
  const [userTotalPages, setUserTotalPages] = useState(0);
  const [userTotalElements, setUserTotalElements] = useState(0);
  const [userPageSize, setUserPageSize] = useState(5);
  const [usersLoading, setUsersLoading] = useState(true);
  const [userFilter, setUserFilter] = useState(null);
  const [userSortBy, setUserSortBy] = useState('id');
  const [userSortDir, setUserSortDir] = useState('asc');

  const [posts, setPosts] = useState([]);
  const [postPage, setPostPage] = useState(0);
  const [postTotalPages, setPostTotalPages] = useState(0);
  const [postTotalElements, setPostTotalElements] = useState(0);
  const [postPageSize, setPostPageSize] = useState(5);
  const [postsLoading, setPostsLoading] = useState(true);
  const [postAuthorFilter, setPostAuthorFilter] = useState(null);
  const [postItemFilter, setPostItemFilter] = useState(null);
  const [postSortBy, setPostSortBy] = useState('createdAt');
  const [postSortDir, setPostSortDir] = useState('desc');

  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

  const loadUsers = useCallback(async (p = 0, filters = {}) => {
    setUsersLoading(true);
    try {
      const effectiveUserId = 'userId' in filters ? filters.userId : userFilter?.id;
      const { data } = await getAdminUsers(p, userPageSize, {
        userId: effectiveUserId,
        sortBy: filters.sortBy ?? userSortBy,
        sortDir: filters.sortDir ?? userSortDir,
      });
      setUsers(data.content);
      setUserTotalPages(data.totalPages);
      setUserTotalElements(data.totalElements);
      setUserPage(data.number);
    } catch (err) {
      console.error('Error cargando usuarios:', err);
      toast.error('Error al cargar usuarios.');
    } finally {
      setUsersLoading(false);
    }
  }, [userPageSize, userFilter, userSortBy, userSortDir]);

  const loadPosts = useCallback(async (p = 0, filters = {}) => {
    setPostsLoading(true);
    try {
      const effectiveAuthorId = 'authorId' in filters ? filters.authorId : postAuthorFilter?.id;
      const effectiveLinkedItemId = 'linkedItemId' in filters ? filters.linkedItemId : postItemFilter?.id;
      const { data } = await getAdminPosts(p, postPageSize, {
        authorId: effectiveAuthorId,
        linkedItemId: effectiveLinkedItemId,
        sortBy: filters.sortBy ?? postSortBy,
        sortDir: filters.sortDir ?? postSortDir,
      });
      setPosts(data.content);
      setPostTotalPages(data.totalPages);
      setPostTotalElements(data.totalElements);
      setPostPage(data.number);
    } catch (err) {
      console.error('Error cargando posts:', err);
      toast.error('Error al cargar publicaciones.');
    } finally {
      setPostsLoading(false);
    }
  }, [postPageSize, postAuthorFilter, postItemFilter, postSortBy, postSortDir]);

  const loadStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const { data } = await getAdminStats();
      setStats(data);
    } catch (err) {
      console.error('Error cargando estadísticas:', err);
      toast.error('Error al cargar estadísticas.');
    } finally {
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (tab === 'users') loadUsers(0);
    else if (tab === 'posts') loadPosts(0);
    else if (tab === 'stats') loadStats();
  }, [tab]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleUserPageSizeChange = (newSize) => {
    setUserPageSize(newSize);
  };

  const handlePostPageSizeChange = (newSize) => {
    setPostPageSize(newSize);
  };

  useEffect(() => {
    if (tab === 'users') loadUsers(0);
  }, [userPageSize]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (tab === 'posts') loadPosts(0);
  }, [postPageSize]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleUserFilterChange = (selected) => {
    setUserFilter(selected);
    loadUsers(0, { userId: selected ? selected.id : null, sortBy: userSortBy, sortDir: userSortDir });
  };

  const handleUserSort = (field, dir) => {
    setUserSortBy(field);
    setUserSortDir(dir);
    loadUsers(0, { userId: userFilter ? userFilter.id : null, sortBy: field, sortDir: dir });
  };

  const handlePostAuthorFilterChange = (selected) => {
    setPostAuthorFilter(selected);
    loadPosts(0, { authorId: selected ? selected.id : null, linkedItemId: postItemFilter ? postItemFilter.id : null, sortBy: postSortBy, sortDir: postSortDir });
  };

  const handlePostItemFilterChange = (selected) => {
    setPostItemFilter(selected);
    loadPosts(0, { authorId: postAuthorFilter ? postAuthorFilter.id : null, linkedItemId: selected ? selected.id : null, sortBy: postSortBy, sortDir: postSortDir });
  };

  const handlePostSort = (field, dir) => {
    setPostSortBy(field);
    setPostSortDir(dir);
    loadPosts(0, { authorId: postAuthorFilter ? postAuthorFilter.id : null, linkedItemId: postItemFilter ? postItemFilter.id : null, sortBy: field, sortDir: dir });
  };

  const handleDeleteUser = async (userId, username) => {
    const ok = await confirm({
      title: 'Eliminar usuario',
      message: `¿Eliminar al usuario "${username}" y todos sus datos? Esta acción no se puede deshacer.`,
      confirmText: 'Eliminar',
      variant: 'danger',
    });
    if (!ok) return;
    try {
      await adminDeleteUser(userId);
      toast.success(`Usuario «${username}» eliminado.`);
      loadUsers(userPage);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al eliminar usuario.');
    }
  };

  const handleDeletePost = async (postId, authorUsername) => {
    const ok = await confirm({
      title: 'Eliminar post',
      message: `¿Eliminar el post #${postId} de "${authorUsername}"?`,
      confirmText: 'Eliminar',
      variant: 'danger',
    });
    if (!ok) return;
    try {
      await adminDeletePost(postId);
      toast.success('Publicación eliminada.');
      loadPosts(postPage);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al eliminar publicación.');
    }
  };

  return (
    <div className="home-container">
      <AppHeader active="admin" userName={user?.username} />

      <main className="admin-main">
        <div className="admin-header">
          <h1>Panel de Administración</h1>
        </div>

        <div className="admin-tabs">
          <button
            className={`admin-tab ${tab === 'users' ? 'active' : ''}`}
            onClick={() => setTab('users')}
          >
            Gestión de usuarios
          </button>
          <button
            className={`admin-tab ${tab === 'posts' ? 'active' : ''}`}
            onClick={() => setTab('posts')}
          >
            Gestión de posts
          </button>
          <button
            className={`admin-tab ${tab === 'stats' ? 'active' : ''}`}
            onClick={() => setTab('stats')}
          >
            Estadísticas
          </button>
        </div>

        {tab === 'users' && (
          <UsersPanel
            users={users}
            page={userPage}
            totalPages={userTotalPages}
            totalElements={userTotalElements}
            pageSize={userPageSize}
            onPageSizeChange={handleUserPageSizeChange}
            onPageChange={(p) => loadUsers(p)}
            onDelete={handleDeleteUser}
            onViewProfile={(username) => navigate(`/user/${username}`)}
            selectedUser={userFilter}
            onUserFilterChange={handleUserFilterChange}
            sortBy={userSortBy}
            sortDir={userSortDir}
            onSort={handleUserSort}
            tableLoading={usersLoading}
          />
        )}

        {tab === 'posts' && (
          <PostsPanel
            posts={posts}
            page={postPage}
            totalPages={postTotalPages}
            totalElements={postTotalElements}
            pageSize={postPageSize}
            onPageSizeChange={handlePostPageSizeChange}
            onPageChange={(p) => loadPosts(p)}
            onDeletePost={handleDeletePost}
            onViewPost={(postId) => navigate(`/posts/${postId}`)}
            selectedAuthor={postAuthorFilter}
            onAuthorFilterChange={handlePostAuthorFilterChange}
            selectedItem={postItemFilter}
            onItemFilterChange={handlePostItemFilterChange}
            sortBy={postSortBy}
            sortDir={postSortDir}
            onSort={handlePostSort}
            tableLoading={postsLoading}
          />
        )}

        {tab === 'stats' && (statsLoading
          ? <SkeletonStatsPanel />
          : <div className="feed-loaded"><StatsPanel stats={stats} /></div>
        )}
      </main>
    </div>
  );
}
