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

export default api;
