import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, MessageCircle, Expand, Bookmark, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { useAuth } from '../context/AuthContext';
import { UserAvatar } from './UserAvatar';
import { CommentSection } from './CommentSection';
import { ConfirmModal } from './ConfirmModal';
import { MEDIA_TYPE_LABELS } from '../constants/media';
import { getPostComments, togglePostLike, togglePostSave, deletePost } from '../services/api';

const CONTENT_TRUNCATE_LENGTH = 300;

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

export function PostCard({ post, onPostUpdate, onPostDelete, authorAvatar, showAuthorLink = true }) {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [commentsOpen, setCommentsOpen] = useState(false);
  const [comments, setComments] = useState([]);
  const [commentsLoaded, setCommentsLoaded] = useState(false);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentsError, setCommentsError] = useState('');
  const [liking, setLiking] = useState(false);
  const [saving, setSaving] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const avatarUrl = authorAvatar ?? post.authorProfilePictureUrl;
  const isLiked = post.likedByCurrentUser;
  const isSaved = post.savedByCurrentUser;
  const isOwner = user?.id === post.authorId;
  const content = post.content || '';
  const isTruncated = content.length > CONTENT_TRUNCATE_LENGTH && !expanded;

  const handleLike = async () => {
    if (liking) return;
    setLiking(true);
    try {
      const { data } = await togglePostLike(post.id);
      onPostUpdate?.(post.id, { likedByCurrentUser: data.liked, likeCount: data.likeCount });
    } catch {
      toast.error('No se pudo registrar el like.');
    } finally {
      setLiking(false);
    }
  };

  const handleSave = async () => {
    if (saving) return;
    setSaving(true);
    try {
      const { data } = await togglePostSave(post.id);
      onPostUpdate?.(post.id, { savedByCurrentUser: data.saved });
    } catch {
      toast.error('No se pudo guardar la publicación.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deletePost(post.id);
      onPostDelete?.(post.id);
      toast.success('Publicación eliminada.');
    } catch {
      toast.error('No se pudo eliminar la publicación.');
    } finally {
      setDeleting(false);
      setConfirmDeleteOpen(false);
    }
  };

  const toggleComments = useCallback(async () => {
    if (commentsOpen) {
      setCommentsOpen(false);
      return;
    }
    setCommentsOpen(true);
    if (commentsLoaded) return;
    setCommentsLoading(true);
    setCommentsError('');
    try {
      const { data } = await getPostComments(post.id);
      setComments(Array.isArray(data) ? data : []);
      setCommentsLoaded(true);
    } catch (e) {
      setCommentsError(e.response?.data?.message || e.response?.data?.error || 'No se pudieron cargar los comentarios.');
    } finally {
      setCommentsLoading(false);
    }
  }, [commentsOpen, commentsLoaded, post.id]);

  const handleCommentCountChange = (delta) => {
    onPostUpdate?.(post.id, { commentCount: (post.commentCount || 0) + delta });
  };

  return (
    <article className="post-card">
      <div className="post-card-header">
        <div className="post-card-author">
          <UserAvatar src={avatarUrl} name={post.authorName} size="small" />
          <div>
            {showAuthorLink ? (
              <h4
                className="post-author post-author-link"
                role="button"
                tabIndex={0}
                onClick={() => navigate(`/user/${post.authorName}`)}
                onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
              >
                {post.authorName}
              </h4>
            ) : (
              <h4 className="post-author">{post.authorName}</h4>
            )}
            {post.linkedItemType && (
              <span className="post-category-tag">
                {MEDIA_TYPE_LABELS[post.linkedItemType] || post.linkedItemType}
              </span>
            )}
          </div>
        </div>
        <div className="post-card-header-right">
          <span className="text-dim post-date">{formatDate(post.createdAt)}</span>
          {isOwner && (
            <button
              type="button"
              className="post-like-btn post-delete-btn"
              onClick={() => setConfirmDeleteOpen(true)}
              disabled={deleting}
            >
              <Trash2 size={16} />
            </button>
          )}
        </div>
      </div>

      {(post.linkedItemTitle || post.linkedItemId) && (
        <div className="post-linked-work">
          Reseña de: <strong>{post.linkedItemTitle}</strong>
          {post.linkedItemRating != null && (
            <span className="text-muted"> · {post.linkedItemRating}/10</span>
          )}
        </div>
      )}

      <p className="post-content">
        {isTruncated ? (
          <>
            {content.slice(0, CONTENT_TRUNCATE_LENGTH)}...{' '}
            <span
              className="post-read-more"
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/posts/${post.id}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/posts/${post.id}`); }}
            >
              leer más
            </span>
          </>
        ) : (
          content
        )}
      </p>

      <div className="post-footer">
        <div className="post-footer-left">
          <button
            type="button"
            className={`post-like-btn ${isLiked ? 'liked' : ''}`}
            onClick={handleLike}
            disabled={liking}
          >
            <Heart size={16} fill={isLiked ? 'currentColor' : 'none'} /> {post.likeCount}
          </button>
          <button
            type="button"
            className={`post-like-btn ${isSaved ? 'saved' : ''}`}
            onClick={handleSave}
            disabled={saving}
          >
            <Bookmark size={16} fill={isSaved ? 'currentColor' : 'none'} />
          </button>
        </div>
        <div className="post-footer-actions">
          <button
            type="button"
            className={`post-action-btn ${commentsOpen ? 'active' : ''}`}
            onClick={toggleComments}
          >
            <MessageCircle size={16} /> {post.commentCount || 0}
          </button>
          <button
            type="button"
            className="post-action-btn"
            onClick={() => navigate(`/posts/${post.id}`)}
          >
            <Expand size={16} />
          </button>
        </div>
      </div>

      {commentsOpen && (
        <>
          <div className="post-comments-separator" />
          <CommentSection
            postId={post.id}
            comments={comments}
            setComments={setComments}
            onCommentCountChange={handleCommentCountChange}
            loading={commentsLoading}
            error={commentsError}
            showTitle
          />
        </>
      )}

      <ConfirmModal
        isOpen={confirmDeleteOpen}
        title="Eliminar publicación"
        message="¿Estás seguro de que quieres eliminar esta publicación? Esta acción no se puede deshacer."
        confirmText="Eliminar"
        cancelText="Cancelar"
        danger
        onConfirm={handleDelete}
        onCancel={() => setConfirmDeleteOpen(false)}
      />
    </article>
  );
}
