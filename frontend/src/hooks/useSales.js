import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useSales = () => {
  const [sales, setSales] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');
  const [stats, setStats] = useState({
    totalSales: 0,
    totalRevenue: 0
  });

  const fetchSales = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/sales`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSales(response.data);
    } catch (err) {
      setError('Ошибка загрузки продаж');
      console.error('Error fetching sales:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchProducts = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/products`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProducts(response.data);
    } catch (err) {
      console.error('Error fetching products:', err);
    }
  }, []);

  const fetchStats = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_URL}/sales/stats`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setStats({
        totalSales: response.data.totalSales || 0,
        totalRevenue: response.data.totalRevenue || 0
      });
    } catch (err) {
      console.error('Error fetching stats:', err);
    }
  }, []);

  const createSale = async (productId, count) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_URL}/sales`,
        { productId, count },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setMessage('✔️ Продажа успешно зарегистрирована!');
      
      await Promise.all([
        fetchSales(),
        fetchStats()
      ]);
      
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка регистрации продажи';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([
        fetchSales(),
        fetchStats(),
        fetchProducts()
      ]);
    };
    loadData();
  }, [fetchSales, fetchStats, fetchProducts]);

  return {
    sales,
    products,
    loading,
    error,
    message,
    stats,
    setMessage,
    fetchSales,
    createSale
  };
};