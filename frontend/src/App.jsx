import React, { useEffect, useState } from "react";
import axios from "axios";

function App() {

  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');

    if (token && savedUsername) {
      setUser({ username: savedUsername, token });
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      const response = await axios.post('https://localhost:8443/api/auth/login', {
        username: username,
        password: password
      });

      const { token, username: userName } = response.data

      localStorage.setItem('token', token);
      localStorage.setItem('username', userName);

      setUser({ username: userName, token });

      setMessage(`✅ Добро пожаловать, ${userName}!`);
    } catch (error) {
      setMessage('❌ Неверный логин или пароль');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      await axios.post('https://localhost:8443/api/auth/register', {
        username,
        email,
        password
      });

      setMessage('✅ Регистрация успешна! Теперь войдите.');

      setTimeout(() => {
        setIsLogin(true);
        setMessage('');
      }, 2000);
    } catch (error) {
      setMessage('❌ Ошибка регистрации');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setUser(null);
    setMessage('👋 Вы вышли из системы');
  };

  if (user) {
    return (
      <div style={styles.container}>
        <div style={styles.card}>
          <h1 style={styles.title}>🎬 Cinema Analytics</h1>
          <h2 style={styles.subtitle}>Добро пожаловать, {user.username}!</h2>
          
          <button onClick={handleLogout} style={styles.button}>
            Выйти
          </button>

          {message && (
            <div style={{
              ...styles.message,
              ...(message.includes('✅') ? styles.success : styles.error)
            }}>
              {message}
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🎬 Cinema Analytics</h1>

        {isLogin ? (
          <>
            <h2 style={styles.subtitle}>🔐 Вход</h2>
            <form onSubmit={handleLogin}>
              <div style={styles.inputGroup}>
                <label style={styles.label}>Имя пользователя</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Введите username"
                  required
                  style={styles.input}
                />
              </div>

              <div style={styles.inputGroup}>
                <label style={styles.label}>Пароль</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Введите пароль"
                  required
                  style={styles.input}
                />
              </div>

              <button type="submit" style={styles.button}>
                Войти
              </button>
            </form>

            <button onClick={() => setIsLogin(false)} style={styles.switchButton}>
              Нет аккаунта? Зарегистрироваться
            </button>
          </>
        ) : (
          <>
            <h2 style={styles.subtitle}>📝 Регистрация</h2>
            <form onSubmit={handleRegister}>
              <div style={styles.inputGroup}>
                <label style={styles.label}>Имя пользователя</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Введите username"
                  required
                  style={styles.input}
                />
              </div>

              <div style={styles.inputGroup}>
                <label style={styles.label}>Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Введите email"
                  required
                  style={styles.input}
                />
              </div>

              <div style={styles.inputGroup}>
                <label style={styles.label}>Пароль</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Введите пароль"
                  required
                  style={styles.input}
                />
              </div>

              <button type="submit" style={styles.button}>
                Зарегистрироваться
              </button>
            </form>

            <button onClick={() => setIsLogin(true)} style={styles.switchButton}>
              Уже есть аккаунт? Войти
            </button>
          </>
        )}

        {message && (
          <div style={{
            ...styles.message,
            ...(message.includes('✅') ? styles.success : styles.error)
          }}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}

export default App;

const styles = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundColor: '#667eea',
    padding: '20px'
  },
  card: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '16px',
    maxWidth: '400px',
    width: '100%',
    boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
  },
  title: {
    textAlign: 'center',
    marginBottom: '10px',
    color: '#333'
  },
  subtitle: {
    textAlign: 'center',
    marginBottom: '30px',
    color: '#666'
  },
  inputGroup: {
    marginBottom: '16px'
  },
  label: {
    display: 'block',
    marginBottom: '6px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#333'
  },
  input: {
    width: '100%',
    padding: '12px',
    border: '2px solid #e0e0e0',
    borderRadius: '8px',
    fontSize: '16px',
    boxSizing: 'border-box',
    outline: 'none'
  },
  button: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#667eea',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer'
  },
  switchButton: {
    width: '100%',
    marginTop: '16px',
    padding: '10px',
    background: 'transparent',
    color: '#667eea',
    border: 'none',
    cursor: 'pointer',
    fontSize: '14px',
    textDecoration: 'underline'
  },
  message: {
    marginTop: '16px',
    padding: '12px',
    borderRadius: '8px',
    fontSize: '14px',
    textAlign: 'center'
  },
  error: {
    backgroundColor: '#ffebee',
    color: '#c62828'
  },
  success: {
    backgroundColor: '#e8f5e9',
    color: '#2e7d32'
  }
};