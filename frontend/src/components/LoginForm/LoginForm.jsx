import React, { useState } from "react";
import styles from './LoginForm.module.css';

function LoginForm({ onSubmit, onSwitch, loading, errors }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(username, password);
  };

  return (
    <>
      <h2 className={styles.subtitle}>Вход</h2>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>Имя пользователя</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Введите логин"
            required
            className={`${styles.input} ${errors.username ? styles.inputError : ''}`}
          />
          {errors.username && (
            <div className={styles.fieldError}>{errors.username}</div>
          )}
        </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>Пароль</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Введите пароль"
            required
            className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
          />
          {errors.password && (
            <div className={styles.fieldError}>{errors.password}</div>
          )}
        </div>

        <button type="submit" className={styles.button} disabled={loading}>
          {loading ? 'Загрузка...' : 'Войти'}
        </button>
      </form>

      <button onClick={onSwitch} className={styles.switchButton}>
        Нет аккаунта? Зарегистрироваться
      </button>
    </>
  );
}

export default LoginForm;