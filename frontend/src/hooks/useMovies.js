import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useMovies = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');

  const fetchMovies = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/movies`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMovies(response.data);
    } catch (err) {
      setError('Ошибка загрузки фильмов');
      console.error('Ошибка загрузки фильмов:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const createMovie = async (movieData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`${API_URL}/movies`, movieData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Фильм успешно создан!');
      await fetchMovies();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка создания фильма';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateMovie = async (id, movieData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.put(`${API_URL}/movies/${id}`, movieData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Фильм успешно обновлён!');
      await fetchMovies();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка обновления фильма';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteMovie = async (id) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_URL}/movies/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Фильм успешно удалён!');
      await fetchMovies();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка удаления фильма';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const searchMovies = async (query) => {
    if (!query.trim()) {
      await fetchMovies();
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/movies/search?query=${query}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMovies(response.data);
    } catch (err) {
      setError('Ошибка поиска');
      console.error('Error searching movies:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, [fetchMovies]);

  return {
    movies,
    loading,
    error,
    message,
    setMessage,
    fetchMovies,
    createMovie,
    updateMovie,
    deleteMovie,
    searchMovies
  };
};