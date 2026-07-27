import React from 'react';
import LoginForm from '../LoginForm/LoginForm';
import RegisterForm from '../RegisterForm/RegisterForm';
import styles from './AuthPage.module.css';

function AuthPage({ 
  isLogin,
  onSwitch,
  onLogin,
  onRegister,
  loading,
  message,
  errors
}) {
  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Cinema Analytics</h1>

        {isLogin ? (
          <LoginForm
            onSubmit={onLogin}
            onSwitch={onSwitch}
            loading={loading}
            errors={errors}
          />
        ) : (
          <RegisterForm
            onSubmit={onRegister}
            onSwitch={onSwitch}
            loading={loading}
            errors={errors}
          />
        )}

        {message && (
          <div className={`${styles.message} ${message.includes('✔️') ? styles.success : styles.error}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}

export default AuthPage;