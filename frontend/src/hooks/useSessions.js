import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useSessions = () => {
  const [sessions, setSessions] = useState([]);
  const [movies, setMovies] = useState([]);
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

  const fetchSessions = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/sessions`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSessions(response.data);
    } catch (err) {
      setError('Ошибка загрузки сеансов');
      setMessage('❌ Ошибка загрузки сеансов');
      console.error('Error fetching sessions:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchMovies = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/movies`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMovies(response.data);
    } catch (err) {
      console.error('Error fetching movies:', err);
    }
  }, []);

  const fetchHalls = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/halls`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setHalls(response.data);
    } catch (err) {
      console.error('Error fetching halls:', err);
    }
  }, []);

  const createSession = async (sessionData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_URL}/sessions`,
        sessionData,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setMessage('✔️ Сеанс успешно создан!');
      await fetchSessions();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка создания сеанса';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateSessionStatus = async (id, status) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.patch(
        `${API_URL}/sessions/${id}/status?status=${status}`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setMessage(`✔️ Статус изменён на "${status}"`);
      await fetchSessions();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка изменения статуса';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteSession = async (id) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_URL}/sessions/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Сеанс удалён');
      await fetchSessions();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка удаления сеанса';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const purchaseTickets = async (sessionId, seats) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_URL}/sessions/purchase`,
        { sessionId, seats },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setMessage(`✔️ ${seats.length} билетов успешно куплено!`);
      await fetchSessions();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка покупки билетов';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchSessionById = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/sessions/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (err) {
      setError('Ошибка загрузки сеанса');
      setMessage('❌ Ошибка загрузки сеанса');
      console.error('Error fetching session:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([
        fetchSessions(),
        fetchMovies(),
        fetchHalls()
      ]);
    };
    loadData();
  }, [fetchSessions, fetchMovies, fetchHalls]);

  return {
    sessions,
    movies,
    halls,
    loading,
    error,
    message,
    setMessage,
    fetchSessions,
    fetchMovies,
    fetchHalls,
    createSession,
    updateSessionStatus,
    deleteSession,
    purchaseTickets,
    fetchSessionById
  };
};