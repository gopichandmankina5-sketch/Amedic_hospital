import axios from 'axios';

export const API_BASE = import.meta.env.VITE_API_BASE || 'https://s81-abhiram-amedick.onrender.com';

export const api = axios.create({
  baseURL: API_BASE,
  withCredentials: false,
});

export const setAuthToken = (token) => {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common['Authorization'];
  }
};

export const assetUrl = (path) => `${API_BASE}${path.startsWith('/') ? '' : '/'}${path}`;
