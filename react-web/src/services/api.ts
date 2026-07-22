/* eslint-disable @typescript-eslint/no-explicit-any */
declare const process: { env: { [key: string]: any } };

import axios from 'axios';

const API_BASE_URL = (process.env.VITE_API_URL as string) || 'http://localhost:8082';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle response errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
};

export const claimService = {
  getMyClaims: (page = 0, size = 10) =>
    api.get(`/claims/my?page=${page}&size=${size}`),
  getClaimById: (id: number) =>
    api.get(`/claims/${id}`),
  submitClaim: (data: any) =>
    api.post('/claims', data),
  updateClaimStatus: (id: number, status: string, reviewNotes?: string) =>
    api.put(`/claims/${id}/status`, { status, reviewNotes }),
};

export const dashboardService = {
  getStats: () =>
    api.get('/dashboard/stats'),
};

export default api;
