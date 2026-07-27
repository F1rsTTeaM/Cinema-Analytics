import React, { useState } from 'react';
import styles from './RegisterForm.module.css';

function RegisterForm({ onSubmit, onSwitch, loading, errors }) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(username, email, password, role);
  };

  return (
    <>
      <h2 className={styles.subtitle}>Регистрация</h2>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>Имя пользователя</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Введите логин (мин. 3 символа)"
            required
            className={`${styles.input} ${errors.username ? styles.inputError : ''}`}
          />
          {errors.username && (
            <div className={styles.fieldError}>{errors.username}</div>
          )}
        </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Введите почту"
            required
            className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
          />
          {errors.email && (
            <div className={styles.fieldError}>{errors.email}</div>
          )}
        </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>Пароль</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Введите пароль (мин. 6 символов)"
            required
            className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
          />
          {errors.password && (
            <div className={styles.fieldError}>{errors.password}</div>
          )}
        </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>Роль</label>
          <div className={styles.radioGroup}>
            <label className={`${styles.radioLabel} ${role === 'USER' ? styles.radioActive : ''}`}>
              <input
                type="radio"
                value="USER"
                checked={role === 'USER'}
                onChange={(e) => setRole(e.target.value)}
                className={styles.radioInput}
              />
              Пользователь
            </label>
            <label className={`${styles.radioLabel} ${role === 'ADMIN' ? styles.radioActive : ''}`}>
              <input
                type="radio"
                value="ADMIN"
                checked={role === 'ADMIN'}
                onChange={(e) => setRole(e.target.value)}
                className={styles.radioInput}
              />
              Администратор
            </label>
          </div>
          <div className={styles.roleHint}>
            {role === 'USER' 
              ? 'Вам будет доступна только страница пользователя' 
              : 'Вам будут доступны обе страницы'}
          </div>
        </div>

        <button type="submit" className={styles.button} disabled={loading}>
          {loading ? 'Загрузка...' : 'Зарегистрироваться'}
        </button>
      </form>

      <button onClick={onSwitch} className={styles.switchButton}>
        Уже есть аккаунт? Войти
      </button>
    </>
  );
}

export default RegisterForm;