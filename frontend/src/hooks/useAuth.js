import { useState, useEffect } from 'react';
import axios from 'axios';

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [isLogin, setIsLogin] = useState(true);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');
    const savedRole = localStorage.getItem('role');

    if (token && savedUsername) {
      setUser({
        username: savedUsername,
        token,
        role: savedRole || 'ROLE_USER'
      });
    }
  }, []);

  const login = async (username, password) => {
    setLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await axios.post('https://localhost:8443/api/auth/login', {username, password});

      const { token, username: userName, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('username', userName);
      localStorage.setItem('role', role);

      setUser({ username: userName, token, role});
      setMessage(`Добро пожаловать, ${userName}!`);

      return true;
    } catch (error) {
      console.error('Ошибка входа: ', error);
      console.error('Ответ сервера: ', error.response?.data);
      console.error('Статус ошибки:', error.response?.status);

      if (error.response?.status === 400) {
        if (error.response?.data?.username || error.response?.data?.password) {
          setErrors(error.response.data);
        } else {
          setMessage('❌ ' + (error.response?.data?.message || 'Ошибка входа'));
        }
      } else if (error.response?.status === 401) {
        setMessage('❌ Неверный логин или пароль');
      } else {
        setMessage('❌ Ошибка подключения к серверу');
      }

      return false;
    } finally {
      setLoading(false);
    }
  };

  const register = async (username, email, password, role) => {
    setLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await axios.post('https://localhost:8443/api/auth/register', { username, email, password, role: role || 'USER'});

      const { token, username: userName, role: userRole} = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('username', userName);
      localStorage.setItem('role', userRole);

      setUser({ username: userName, token: token, role: userRole });
      setMessage(`Добро пожаловать, ${userName}!`);

      return true;
    } catch (error) {
      console.error('Ошибка регистрации: ', error);
      console.error('Ответ сервера: ', error.response?.data);
      console.error('Статус ошибки:', error.response?.status);

      if (error.response?.status === 400) {
        const errorData = error.response.data;

        if (typeof errorData === 'object' && !Array.isArray(errorData)) {
          if (errorData.username || errorData.email || errorData.password) {
            setErrors(errorData);
            setMessage('❌ Проверьте правильность заполнения полей');
          } else {
            setMessage('❌ ' + (errorData.message || 'Ошибка регистрации'));
          }
        } else {
          setMessage('❌ ' + (errorData?.message || 'Ошибка регистрации'));
        }
      } else if (error.response?.status === 409) {
        setMessage('❌ Пользователь с таким именем или email уже существует');
      } else {
        setMessage('❌ Ошибка подключения к серверу');
      }
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');

    setUser(null);
    setMessage('Вы вышли из системы');
    setErrors({});
  };

  const checkProtected = async () => {
    const token = localStorage.getItem('token');

    if(!token) {
      setMessage('⚠️ Сначала войдите в систему');
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const response = await axios.get('https://localhost:8443/api/user/profile', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      setMessage(`✔️ Сервер ответил: ${JSON.stringify(response.data)}`);
    } catch (error) {
      console.error('Ошибка проверки:', error);

      if (error.response?.status === 403 || error.response?.status === 401) {
        setMessage('❌ Сессия истекла. Войдите заново.');
        logout();
      } else {
        setMessage(`❌ Ошибка: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const switchToLogin = () => {
    setIsLogin(true);
    setErrors({});
    setMessage('');
  };

  const switchToRegister = () => {
    setIsLogin(false);
    setErrors({});
    setMessage('');
  };  

  return {
    user,
    message,
    loading,
    isLogin,
    errors,
    setErrors,
    switchToLogin,
    switchToRegister,
    setMessage,
    login,
    register,
    logout,
    checkProtected
  };
};