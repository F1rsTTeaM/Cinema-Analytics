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
      setMessage('Отчет успешно сгенерирован');
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка генерации отчета';
      setError(errorMsg);
      setMessage('Ошибка генерации отчета');
      console.error('Error generating report:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const exportReport = useCallback(async (category, reportType, startDate, endDate, format) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const start = startDate + 'T00:00:00';
      const end = endDate + 'T23:59:59';
      
      const reportTypeFull = category + '-' + reportType;
      
      const response = await axios.get(
        `${API_URL}/reports/export/${reportTypeFull}/${format}`,
        {
          params: { start, end },
          headers: { 
            Authorization: `Bearer ${token}`,
            'Accept': 'application/octet-stream'
          },
          responseType: 'blob'
        }
      );

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      
      const contentDisposition = response.headers['content-disposition'];
      let filename = `report.${format}`;
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="?([^"]+)"?/);
        if (match) filename = match[1];
      }
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setMessage(`Отчет экспортирован в ${format.toUpperCase()}`);
      return true;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка экспорта отчета';
      setError(errorMsg);
      setMessage('Ошибка экспорта отчета');
      console.error('Error exporting report:', err);
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

  const sendReportEmail = useCallback(async (toEmail, reportType, format, startDate, endDate, subject, message) => {
    setLoading(true);
    setError(null);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const start = startDate + 'T00:00:00';
      const end = endDate + 'T23:59:59';
      
      const requestData = {
        toEmail,
        reportType,
        format,
        startDate: start,
        endDate: end,
        subject,
        message
      };

      const response = await axios.post(
        `${API_URL}/reports/send-email`,
        requestData,
        {
          headers: { 
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      setMessage(response.data.message || 'Отчет успешно отправлен');
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Ошибка отправки отчета';
      setError(errorMsg);
      setMessage('Ошибка отправки: ' + errorMsg);
      console.error('Error sending report:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    reportData,
    loading,
    error,
    message,
    setMessage,
    generateReport,
    exportReport,
    sendReportEmail,
    clearReport
  };
};