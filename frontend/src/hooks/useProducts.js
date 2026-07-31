import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/products`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProducts(response.data);
    } catch (err) {
      setError('Ошибка загрузки товаров');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const createProduct = async (productData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`${API_URL}/products`, productData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Товар успешно создан!');
      await fetchProducts();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка создания товара';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateProduct = async (id, productData) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.put(`${API_URL}/products/${id}`, productData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Товар успешно обновлён!');
      await fetchProducts();
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка обновления товара';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteProduct = async (id) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_URL}/products/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setMessage('✔️ Товар успешно удалён!');
      await fetchProducts();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка удаления товара';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const searchProducts = async (query) => {
    if (!query.trim()) {
      await fetchProducts();
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/products/search?query=${query}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProducts(response.data);
    } catch (err) {
      setError('Ошибка поиска');
      console.error('Error searching products:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  return {
    products,
    loading,
    error,
    message,
    setMessage,
    fetchProducts,
    createProduct,
    updateProduct,
    deleteProduct,
    searchProducts
  };
};