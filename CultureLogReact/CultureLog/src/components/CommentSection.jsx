import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserAvatar } from './UserAvatar';
import { addPostComment } from '../services/api';

const MAX_COMMENT_LENGTH = 1000;

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

export function CommentSection({ postId, comments, setComments, onCommentCountChange, loading, error, showTitle = false }) {
  const navigate = useNavigate();

  const [commentText, setCommentText] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [replyOpenForRootId, setReplyOpenForRootId] = useState(null);
  const [replyDrafts, setReplyDrafts] = useState({});
  const [submittingReplyId, setSubmittingReplyId] = useState(null);
  const [submitError, setSubmitError] = useState('');
  const [expandedReplies, setExpandedReplies] = useState({});

  const commentTree = useMemo(() => {
    const list = Array.isArray(comments) ? comments : [];
    const byId = new Map(list.map((c) => [c.id, { ...c, children: [] }]));
    const roots = [];

    const findRoot = (id) => {
      const node = byId.get(id);
      if (!node || !node.parentCommentId) return id;
      return findRoot(node.parentCommentId);
    };

    for (const c of byId.values()) {
      if (!c.parentCommentId || !byId.has(c.parentCommentId)) {
        roots.push(c);
      } else {
        const rootId = findRoot(c.parentCommentId);
        const rootNode = byId.get(rootId);
        if (rootNode && rootNode !== c) {
          rootNode.children.push(c);
        } else {
          roots.push(c);
        }
      }
    }

    const sortByCreatedAt = (a, b) => new Date(a.createdAt) - new Date(b.createdAt);
    roots.sort(sortByCreatedAt);
    roots.forEach((r) => r.children.sort(sortByCreatedAt));
    return roots;
  }, [comments]);

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    const text = commentText.trim();
    if (!text || !postId) return;
    setSubmittingComment(true);
    setSubmitError('');
    try {
      const { data: created } = await addPostComment(postId, text);
      setComments((prev) => [...prev, created]);
      onCommentCountChange?.(1);
      setCommentText('');
    } catch (err) {
      setSubmitError(err.response?.data?.message || err.response?.data?.error || 'No se pudo enviar el comentario.');
    } finally {
      setSubmittingComment(false);
    }
  };

  const openReplyFor = (rootId, targetAuthorName) => {
    if (replyOpenForRootId === rootId) {
      setReplyOpenForRootId(null);
      return;
    }
    setReplyOpenForRootId(rootId);
    if (targetAuthorName) {
      setReplyDrafts((prev) => {
        const current = (prev[rootId] || '').trim();
        if (!current) return { ...prev, [rootId]: `@${targetAuthorName} ` };
        return prev;
      });
    }
  };

  const submitReply = async (rootId) => {
    const text = (replyDrafts[rootId] || '').trim();
    if (!text || !postId) return;
    setSubmittingReplyId(rootId);
    setSubmitError('');
    try {
      const { data: created } = await addPostComment(postId, text, rootId);
      setComments((prev) => [...prev, created]);
      onCommentCountChange?.(1);
      setReplyDrafts((prev) => ({ ...prev, [rootId]: '' }));
      setReplyOpenForRootId(null);
      setExpandedReplies((prev) => ({ ...prev, [rootId]: true }));
    } catch (err) {
      setSubmitError(err.response?.data?.message || err.response?.data?.error || 'No se pudo enviar la respuesta.');
    } finally {
      setSubmittingReplyId(null);
    }
  };

  const toggleReplies = (commentId) => {
    setExpandedReplies((prev) => ({ ...prev, [commentId]: !prev[commentId] }));
  };

  const renderReply = (reply, rootId) => (
    <div key={reply.id} className="post-detail-comment-node is-reply">
      <article className="post-detail-comment-item">
        <div className="post-detail-comment-head">
          <UserAvatar src={reply.authorProfilePictureUrl} name={reply.authorName} size="small" />
          <div className="post-detail-comment-header">
            <span
              className="post-author post-author-link"
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/user/${reply.authorName}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${reply.authorName}`); }}
            >
              {reply.authorName}
            </span>
            <span className="text-dim">{formatDate(reply.createdAt)}</span>
          </div>
        </div>
        <p>{reply.text}</p>
        <div className="post-detail-comment-actions-row">
          <button
            type="button"
            className="post-action-btn"
            onClick={() => openReplyFor(rootId, reply.authorName)}
          >
            Responder
          </button>
        </div>
      </article>
    </div>
  );

  const renderRootComment = (c) => (
    <div key={c.id} className="post-detail-comment-node">
      <article className="post-detail-comment-item">
        <div className="post-detail-comment-head">
          <UserAvatar src={c.authorProfilePictureUrl} name={c.authorName} size="small" />
          <div className="post-detail-comment-header">
            <span
              className="post-author post-author-link"
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/user/${c.authorName}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${c.authorName}`); }}
            >
              {c.authorName}
            </span>
            <span className="text-dim">{formatDate(c.createdAt)}</span>
          </div>
        </div>
        <p>{c.text}</p>

        <div className="post-detail-comment-actions-row">
          <button
            type="button"
            className="post-action-btn"
            onClick={() => openReplyFor(c.id, null)}
          >
            Responder
          </button>
        </div>

        {replyOpenForRootId === c.id && (
          <div className="post-detail-reply-form">
            <textarea
              value={replyDrafts[c.id] || ''}
              onChange={(e) => setReplyDrafts((prev) => ({ ...prev, [c.id]: e.target.value }))}
              placeholder={`Responder a @${c.authorName}...`}
              maxLength={MAX_COMMENT_LENGTH}
            />
            <div className="post-detail-comment-actions">
              <span className="text-dim">{(replyDrafts[c.id] || '').length}/{MAX_COMMENT_LENGTH}</span>
              <button
                type="button"
                className="login-button"
                disabled={submittingReplyId === c.id || !(replyDrafts[c.id] || '').trim()}
                onClick={() => submitReply(c.id)}
              >
                {submittingReplyId === c.id ? 'Enviando...' : 'Responder'}
              </button>
            </div>
          </div>
        )}
      </article>

      {c.children.length > 0 && (
        <>
          {!expandedReplies[c.id] ? (
            <button
              type="button"
              className="post-toggle-replies-btn"
              onClick={() => toggleReplies(c.id)}
            >
              Ver {c.children.length} {c.children.length === 1 ? 'respuesta' : 'respuestas'}
            </button>
          ) : (
            <>
              <button
                type="button"
                className="post-toggle-replies-btn"
                onClick={() => toggleReplies(c.id)}
              >
                Ocultar respuestas
              </button>
              <div className="post-detail-comment-children">
                {c.children.map((child) => renderReply(child, c.id))}
              </div>
            </>
          )}
        </>
      )}
    </div>
  );

  return (
    <section className="post-detail-comments">
      {showTitle && <h3 className="post-detail-comments-title">Comentarios</h3>}

      {(error || submitError) && (
        <p className="auth-error">{error || submitError}</p>
      )}

      <form onSubmit={handleCommentSubmit} className="post-detail-comment-form">
        <textarea
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
          placeholder="Escribe un comentario..."
          maxLength={MAX_COMMENT_LENGTH}
        />
        <div className="post-detail-comment-actions">
          <span className="text-dim">{commentText.length}/{MAX_COMMENT_LENGTH}</span>
          <button type="submit" className="login-button" disabled={submittingComment || !commentText.trim()}>
            {submittingComment ? 'Enviando...' : 'Comentar'}
          </button>
        </div>
      </form>

      {loading ? (
        <p className="text-muted">Cargando comentarios...</p>
      ) : (
        <div className="post-detail-comment-list">
          {comments.length === 0 ? (
            <p className="text-muted">Sé el primero en comentar esta publicación.</p>
          ) : (
            commentTree.map((c) => renderRootComment(c))
          )}
        </div>
      )}
    </section>
  );
}
