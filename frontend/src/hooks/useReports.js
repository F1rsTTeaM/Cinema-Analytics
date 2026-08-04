import { useState, useCallback, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'https://localhost:8443/api';

export const useReports = () => {
  const [reportData, setReportData] = useState(null);
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

  const generateReport = useCallback(async (category, reportType, startDate, endDate) => {
    setLoading(true);
    setError(null);
    setMessage('');
    setReportData(null);

    try {
      const token = localStorage.getItem('token');
      
      const endpoints = {
        tickets: {
          summary: '/reports/tickets/summary',
          movies: '/reports/tickets/movies',
          halls: '/reports/tickets/halls',
          daily: '/reports/tickets/daily'
        },
        products: {
          summary: '/reports/products/summary',
          list: '/reports/products/list',
          daily: '/reports/products/daily'
        }
      };

      const endpoint = endpoints[category]?.[reportType];
      if (!endpoint) {
        throw new Error('Неизвестный тип отчета');
      }

      const start = startDate + 'T00:00:00';
      const end = endDate + 'T23:59:59';
      
      const response = await axios.get(
        `${API_URL}${endpoint}`,
        {
          params: { start, end },
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      
      setReportData(response.data);
      setMessage('✔️ Отчет успешно сгенерирован!');
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка генерации отчета';
      setError(errorMsg);
      setMessage('❌ ' + errorMsg);
      console.error('Error generating report:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const clearReport = useCallback(() => {
    setReportData(null);
    setError(null);
    setMessage('');
  }, []);

  return {
    reportData,
    loading,
    error,
    message,
    setMessage,
    generateReport,
    clearReport
  };
};