import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './AdminDashboard.module.css';

function AdminDashboard({ user }) {
  const navigate = useNavigate();

  if (!user || user.role !== 'ROLE_ADMIN') {
    navigate('/user-dashboard');
    return null;
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Админ-панель</h1>
        <p className={styles.access}>Только для администраторов</p>
        
        <div className={styles.info}>
          <p><strong>Имя:</strong> {user.username}</p>
          <p><strong>Роль:</strong> Администратор</p>
        </div>

        <div className={styles.content}>
          <h3>Панель управления</h3>
          <ul>
            <li>Управление пользователями</li>
            <li>Статистика и аналитика</li>
            <li>Настройки системы</li>
            <li>Логирование действий</li>
          </ul>
        </div>

        <div className={styles.secret}>
          <h4>Секретная информация</h4>
          <p>Эта информация доступна только администраторам!</p>
          <code>API_KEY: sk-12345-admin-secret</code>
        </div>

        <button onClick={() => navigate('/user-dashboard')} className={styles.backButton}>
          Перейти на страницу пользователя
        </button>
      </div>
    </div>
  );
}

export default AdminDashboard;