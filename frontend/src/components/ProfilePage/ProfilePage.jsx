import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './ProfilePage.module.css';

function ProfilePage({ user, onLogout, onCheckProtected, message }) {
  const navigate = useNavigate();

  if (!user) {
    navigate('/');
    return null;
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}> Мой профиль</h1>
        
        <div className={styles.info}>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Имя пользователя:</span>
            <span className={styles.infoValue}>{user.username}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Роль:</span>
            <span className={`${styles.infoValue} ${user.role === 'ROLE_ADMIN' ? styles.adminRole : styles.userRole}`}>
              {user.role === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь'}
            </span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Токен:</span>
            <span className={styles.tokenValue}>{user.token.substring(0, 30)}...</span>
          </div>
        </div>

        <div className={styles.buttonGroup}>
          <button onClick={onCheckProtected} className={styles.checkButton}>
            Проверить защищённый эндпоинт
          </button>
          <button onClick={() => navigate('/user-dashboard')} className={styles.dashboardButton}>
            Перейти на страницу пользователя
          </button>
          {user.role === 'ROLE_ADMIN' && (
            <button onClick={() => navigate('/admin-dashboard')} className={styles.adminButton}>
              Перейти в админ-панель
            </button>
          )}
          <button onClick={onLogout} className={styles.logoutButton}>
            Выйти
          </button>
        </div>

        {message && (
          <div className={`${styles.message} ${message.includes('✅') ? styles.success : styles.error}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}

export default ProfilePage;