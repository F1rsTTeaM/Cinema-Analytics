import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import AuthPage from './components/AuthPage/AuthPage';
import ProfilePage from './components/ProfilePage/ProfilePage';
import Navbar from './components/Navbar/Navbar';
import UserDashboard from './pages/UserDashboard/UserDashboard';
import AdminDashboard from './pages/AdminDashboard/AdminDashboard';
import MoviesPage from './pages/MoviesPage/MoviesPage';
import ProductsPage from './pages/ProductsPage/ProductsPage';
import SalesPage from './pages/SalesPage/SalesPage';
import SessionsPage from './pages/SessionsPage/SessionsPage';
import HallsPage from './pages/HallsPage/HallsPage';
import ReportsPage from './pages/ReportsPage/ReportsPage';

function ProtectedRoute({ children, allowedRoles }) {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  
  if (!token) {
    return <Navigate to="/" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to="/user-dashboard" replace />;
  }

  return children;
}

function App() {
  const {
    user,
    message,
    loading,
    isLogin,
    errors,
    switchToLogin,
    switchToRegister,
    setMessage,
    login,
    register,
    logout,
    checkProtected
  } = useAuth();

  const handleLogin = async (username, password) => {
    await login(username, password);
  };

  const handleRegister = async (username, email, password, role) => {
    await register(username, email, password, role);
  };

  const handleSwitch = () => {
    if (isLogin) {
      switchToRegister();
    } else {
      switchToLogin();
    }
  };

  return (
    <BrowserRouter>
      {user && <Navbar user={user} onLogout={logout} />}
      
      <Routes>
        <Route path="/" element={
          user ? (
            <Navigate to="/user-dashboard" replace />
          ) : (
            <AuthPage
              isLogin={isLogin}
              onSwitch={handleSwitch}
              onLogin={handleLogin}
              onRegister={handleRegister}
              loading={loading}
              message={message}
              errors={errors}
            />
          )
        } />

        <Route path="/user-dashboard" element={
          <ProtectedRoute>
            <UserDashboard user={user} />
          </ProtectedRoute>
        } />

        <Route path="/admin-dashboard" element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <AdminDashboard user={user} />
          </ProtectedRoute>
        } />

        <Route path="/movies" element={
          <ProtectedRoute>
            <MoviesPage />
          </ProtectedRoute>
        } />

        <Route path="/products" element={
          <ProtectedRoute>
            <ProductsPage />
          </ProtectedRoute>
        } />

        <Route path="/sales" element={
          <ProtectedRoute>
            <SalesPage />
          </ProtectedRoute>
        } />

        <Route path="/sessions" element={
          <ProtectedRoute>
            <SessionsPage />
          </ProtectedRoute>
        } />

        <Route path="/halls" element={
          <ProtectedRoute>
            <HallsPage />
          </ProtectedRoute>
        } />

        <Route path="/reports" element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <ReportsPage />
          </ProtectedRoute>
        } />

        <Route path="/profile" element={
          <ProtectedRoute>
            <ProfilePage
              user={user}
              onLogout={logout}
              onCheckProtected={checkProtected}
              message={message}
            />
          </ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;