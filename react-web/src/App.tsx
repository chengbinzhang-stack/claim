import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { CssBaseline } from '@mui/material';
import LoginPage from './features/auth/LoginPage';
import RegisterPage from './features/auth/RegisterPage';
import ClaimsListPage from './features/claims/ClaimsListPage';
import ClaimFormPage from './features/claims/ClaimFormPage';
import ClaimDetailPage from './features/claims/ClaimDetailPage';
import AdjusterClaimsPage from './features/claims/AdjusterClaimsPage';
import DashboardPage from './features/dashboard/DashboardPage';

const getUserRole = (): string | null => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;
  try { return JSON.parse(userStr).roleName || null; } catch { return null; }
};

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" />;
};

const App: React.FC = () => {
  const role = getUserRole();

  return (
    <BrowserRouter>
      <CssBaseline />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/claims" element={<ProtectedRoute><ClaimsListPage /></ProtectedRoute>} />
        <Route path="/claims/new" element={<ProtectedRoute><ClaimFormPage /></ProtectedRoute>} />
        <Route path="/claims/:id" element={<ProtectedRoute><ClaimDetailPage /></ProtectedRoute>} />
        {(role === 'ADJUSTER' || role === 'ADMIN') && (
          <Route path="/adjuster/claims" element={<ProtectedRoute><AdjusterClaimsPage /></ProtectedRoute>} />
        )}
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
