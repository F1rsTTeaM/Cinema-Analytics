import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Navbar.module.css';

function Navbar({ user, onLogout }) {
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo}>Cinema Analytics</div>
      
      <div className={styles.navLinks}>
        <button 
          onClick={() => navigate('/user-dashboard')} 
          className={styles.navButton}
        >
          Пользователь
        </button>

        <button 
          onClick={() => navigate('/movies')} 
          className={styles.navButton}
        >
          Фильмы
        </button>

        {isAdmin && (
          <button 
            onClick={() => navigate('/admin-dashboard')} 
            className={`${styles.navButton} ${styles.adminButton}`}
          >
            Админ
          </button>
        )}

        <span className={styles.userInfo}>
          {user.username} ({user.role === 'ROLE_ADMIN' ? 'Админ' : 'Пользователь'})
        </span>

        <button onClick={onLogout} className={styles.logoutButton}>
          Выйти
        </button>
      </div>
    </nav>
  );
}

export default Navbar;