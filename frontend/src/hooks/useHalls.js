import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useHalls = () => {
  const [halls, setHalls] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        setMessage('');
      }, 1500);
      
      return () => clearTimeout(timer);
    }
  }, [message]);

  const fetchHalls = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/halls`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setHalls(response.data);
    } catch (err) {
      setError('Ошибка загрузки залов');
      setMessage('❌ Ошибка загрузки залов');
      console.error('Error fetching halls:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const createHall = async (hallData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_URL}/halls`,
        hallData,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setMessage(`✔️ Зал "${response.data.name}" успешно создан`);
      await fetchHalls();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка создания зала';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteHall = async (id, name) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_URL}/halls/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage(`✔️ Зал "${name}" удалён`);
      await fetchHalls();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка удаления зала';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const searchHalls = async (query) => {
    if (!query.trim()) {
      await fetchHalls();
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/halls/search?query=${query}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setHalls(response.data);
    } catch (err) {
      setError('Ошибка поиска');
      setMessage('❌ Ошибка поиска');
      console.error('Error searching halls:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchHallById = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/halls/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (err) {
      setError('Ошибка загрузки зала');
      setMessage('❌ Ошибка загрузки зала');
      console.error('Error fetching hall:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHalls();
  }, [fetchHalls]);

  return {
    halls,
    loading,
    error,
    message,
    setMessage,
    fetchHalls,
    createHall,
    deleteHall,
    searchHalls,
    fetchHallById
  };
};