import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserDashboard.module.css';

function UserDashboard({ user }) {
  const navigate = useNavigate();

  if (!user) {
    navigate('/');
    return null;
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Страница пользователя</h1>
        <p className={styles.access}>Доступна всем авторизованным пользователям</p>
        
        <div className={styles.info}>
          <p><strong>Имя:</strong> {user.username}</p>
          <p><strong>Роль:</strong> {user.role === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь'}</p>
        </div>

        <div className={styles.content}>
          <h3>Ваши данные</h3>
          <ul>
            <li>Вы успешно авторизованы</li>
            <li>Эта страница доступна всем пользователям</li>
            <li>Ваши данные защищены</li>
          </ul>
        </div>

        <button onClick={() => navigate('/profile')} className={styles.profileButton}>
          Перейти в профиль
        </button>
      </div>
    </div>
  );
}

export default UserDashboard;