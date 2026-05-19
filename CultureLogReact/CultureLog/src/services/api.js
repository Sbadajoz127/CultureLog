import axios from 'axios';
import { toast } from 'sonner';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || '';
    if (error.response?.status === 401 && !url.includes('/auth/')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      toast.warning('Tu sesión ha expirado. Inicia sesión de nuevo.');
      window.dispatchEvent(new Event('auth:expired'));
    }
    return Promise.reject(error);
  }
);

export function loginUser(username, password) {
  return api.post('/auth/login', { username, password });
}

export function registerUser(username, password, email) {
  return api.post('/auth/register', { username, password, email });
}

export function requestPasswordReset(email) {
  return api.post(`/auth/request-reset?email=${encodeURIComponent(email)}`);
}

export function resetPassword(token, newPassword) {
  return api.post('/auth/reset-password', { token, newPassword });
}

export function verifyEmail(token) {
  return api.post('/auth/verify-email', { token });
}

export function resendVerificationEmail(email) {
  return api.post(`/auth/resend-verification?email=${encodeURIComponent(email)}`);
}

export function getUserSettings() {
  return api.get('/users/settings');
}

export function updateUserSettings(settings) {
  return api.put('/users/settings', settings);
}

export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/images/upload', formData, {
    headers: { 'Content-Type': undefined },
  });
}

export function updateProfilePicture(imageUrl) {
  return api.put(`/users/profile-picture?imageUrl=${encodeURIComponent(imageUrl)}`);
}

export function removeProfilePicture() {
  return api.delete('/users/profile-picture');
}

export function searchMedia({ query, type, page = 0 }) {
  const params = new URLSearchParams({ query, page: String(page) });
  if (type) params.set('type', type);
  return api.get(`/search?${params.toString()}`);
}

export function addToLibraryFromSearch(body) {
  return api.post('/search/add-to-library', body);
}

export function getMediaItems({ type, status } = {}) {
  const params = new URLSearchParams();
  if (type) params.set('type', type);
  if (status) params.set('status', status);
  const q = params.toString();
  return api.get(q ? `/items?${q}` : '/items');
}

export function updateMediaItem(id, body) {
  return api.put(`/items/${id}`, body);
}

export function deleteMediaItem(id) {
  return api.delete(`/items/${id}`);
}

export function createCustomMediaItem(body) {
  return api.post('/items', body);
}

export function getFeed({ page = 0, size = 10 } = {}) {
  return api.get('/posts/feed', { params: { page, size } });
}

export function createPost(body) {
  return api.post('/posts', body);
}

export function togglePostLike(postId) {
  return api.post(`/posts/${postId}/like`);
}

export function togglePostSave(postId) {
  return api.post(`/posts/${postId}/save`);
}

export function getSavedPosts({ page = 0, size = 10 } = {}) {
  return api.get('/posts/saved', { params: { page, size } });
}

export function getLikedPosts({ page = 0, size = 10 } = {}) {
  return api.get('/posts/liked', { params: { page, size } });
}

export function searchPosts({ query, page = 0, size = 10 } = {}) {
  return api.get('/posts/search', { params: { q: query, page, size } });
}

export function getPostById(postId) {
  return api.get(`/posts/${postId}`);
}

export function getPostComments(postId) {
  return api.get(`/posts/${postId}/comments`);
}

export function addPostComment(postId, text, parentCommentId) {
  return api.post(`/posts/${postId}/comments`, { text, parentCommentId: parentCommentId ?? null });
}

export function deletePostComment(commentId) {
  return api.delete(`/posts/comments/${commentId}`);
}

export function deletePost(postId) {
  return api.delete(`/posts/${postId}`);
}

export function getSuggestedUsers() {
  return api.get('/users/suggestions');
}

export function searchUsers({ query, page = 0, size = 10 } = {}) {
  return api.get('/users/search', { params: { q: query, page, size } });
}

export function followUser(targetId) {
  return api.post(`/follows?targetId=${targetId}`);
}

export function unfollowUser(targetId) {
  return api.delete(`/follows?targetId=${targetId}`);
}

export function getUserProfile(username) {
  return api.get(`/users/profile/${encodeURIComponent(username)}`);
}

export function getNotifications({ page = 0, size = 10 } = {}) {
  return api.get('/notifications', { params: { page, size } });
}

export function getUnreadNotifications({ page = 0, size = 5 } = {}) {
  return api.get('/notifications/unread', { params: { page, size } });
}

export function getUnreadCount() {
  return api.get('/notifications/unread-count');
}

export function markAsRead(notificationId) {
  return api.post(`/notifications/${notificationId}/read`);
}

export function markAllAsRead() {
  return api.post('/notifications/read-all');
}

export function getPendingFollowRequests() {
  return api.get('/follows/pending');
}

export function getPendingFollowRequestsCount() {
  return api.get('/follows/pending-count');
}

export function acceptFollowRequest(followerId) {
  return api.post(`/follows/accept?followerId=${followerId}`);
}

export function rejectFollowRequest(followerId) {
  return api.post(`/follows/reject?followerId=${followerId}`);
}

export function getFollowers(userId) {
  return api.get(`/follows/followers/${userId}`);
}

export function getFollowing(userId) {
  return api.get(`/follows/following/${userId}`);
}

// ── Admin endpoints ──

export function getAdminUsers(page = 0, size = 20, filters = {}) {
  const params = { page, size };
  if (filters.userId) params.userId = filters.userId;
  if (filters.sortBy) params.sortBy = filters.sortBy;
  if (filters.sortDir) params.sortDir = filters.sortDir;
  return api.get('/admin/users', { params });
}

export function getAdminUsersAutocomplete(q = '') {
  return api.get('/admin/users/autocomplete', { params: { q } });
}

export function getAdminUserDetail(userId) {
  return api.get(`/admin/users/${userId}`);
}

export function adminDeleteUser(userId) {
  return api.delete(`/admin/users/${userId}`);
}

export function adminDeletePost(postId) {
  return api.delete(`/admin/posts/${postId}`);
}

export function adminDeleteComment(commentId) {
  return api.delete(`/admin/comments/${commentId}`);
}

export function adminDeleteItem(itemId) {
  return api.delete(`/admin/items/${itemId}`);
}

export function getAdminPosts(page = 0, size = 20, filters = {}) {
  const params = { page, size };
  if (filters.authorId) params.authorId = filters.authorId;
  if (filters.linkedItemId) params.linkedItemId = filters.linkedItemId;
  if (filters.sortBy) params.sortBy = filters.sortBy;
  if (filters.sortDir) params.sortDir = filters.sortDir;
  return api.get('/admin/posts', { params });
}

export function getAdminLinkedItemsAutocomplete(q = '') {
  return api.get('/admin/items/linked-autocomplete', { params: { q } });
}

export function getAdminPostComments(postId) {
  return api.get(`/admin/posts/${postId}/comments`);
}

export function getAdminStats() {
  return api.get('/admin/stats');
}

// ── Account management endpoints ──

export function requestAccountDeletion() {
  return api.post('/users/request-deletion');
}

export function confirmAccountDeletion(code) {
  return api.delete(`/users/confirm-deletion?code=${encodeURIComponent(code)}`);
}

export function updateBanner(imageUrl) {
  return api.put(`/users/banner?imageUrl=${encodeURIComponent(imageUrl)}`);
}

export function removeBanner() {
  return api.delete('/users/banner');
}

export default api;
