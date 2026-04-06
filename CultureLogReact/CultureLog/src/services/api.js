import axios from 'axios';

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

export function getUserSettings(userId) {
  return api.get(`/users/${userId}/settings`);
}

export function updateUserSettings(userId, settings) {
  return api.put(`/users/${userId}/settings`, settings);
}

export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/images/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
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

export function getFeed({ page = 0, size = 10 } = {}) {
  return api.get('/posts/feed', { params: { page, size } });
}

export function createPost(body) {
  return api.post('/posts', body);
}

export function togglePostLike(postId) {
  return api.post(`/posts/${postId}/like`);
}

export function getSuggestedUsers() {
  return api.get('/users/suggestions');
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

export function acceptFollowRequest(followerId) {
  return api.post(`/follows/accept?followerId=${followerId}`);
}

export function rejectFollowRequest(followerId) {
  return api.post(`/follows/reject?followerId=${followerId}`);
}

export default api;
