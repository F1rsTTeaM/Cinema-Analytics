import React, { useState } from 'react';
import { useReports } from '../../hooks/useReports';
import styles from './ReportsPage.module.css';

function ReportsPage() {
  const {
    reportData,
    loading,
    message,
    setMessage,
    generateReport,
    exportReport,
    sendReportEmail,
    clearReport
  } = useReports();

  const [category, setCategory] = useState('tickets');
  const [reportType, setReportType] = useState('summary');
  const [startDate, setStartDate] = useState(() => {
    const date = new Date();
    date.setDate(date.getDate() - 30);
    return date.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });

  const [showEmailForm, setShowEmailForm] = useState(false);
  const [email, setEmail] = useState('');
  const [emailSubject, setEmailSubject] = useState('');
  const [emailMessage, setEmailMessage] = useState('');
  const [emailFormat, setEmailFormat] = useState('pdf');
  const [sending, setSending] = useState(false);

  const reportConfig = {
    tickets: {
      label: 'Билеты',
      types: {
        summary: {
          label: 'Общая статистика',
          endpoint: '/reports/tickets/summary',
          isSummary: true
        },
        movies: {
          label: 'По фильмам',
          endpoint: '/reports/tickets/movies',
          dataKey: 'movieStats',
          columns: [
            { key: 'movieTitle', label: 'Фильм' },
            { key: 'sessionsCount', label: 'Сеансов' },
            { key: 'ticketsSold', label: 'Билетов' },
            { key: 'totalRevenue', label: 'Выручка', format: 'currency' }
          ]
        },
        halls: {
          label: 'По залам',
          endpoint: '/reports/tickets/halls',
          dataKey: 'hallStats',
          columns: [
            { key: 'hallName', label: 'Зал' },
            { key: 'capacity', label: 'Вместимость' },
            { key: 'sessionsCount', label: 'Сеансов' },
            { key: 'ticketsSold', label: 'Билетов' },
            { key: 'totalRevenue', label: 'Выручка', format: 'currency' },
            { key: 'occupancyPercent', label: 'Заполняемость %', format: 'percent' }
          ]
        },
        daily: {
          label: 'По дням',
          endpoint: '/reports/tickets/daily',
          dataKey: 'dailyStats',
          columns: [
            { key: 'date', label: 'Дата', format: 'date' },
            { key: 'sessionsCount', label: 'Сеансов' },
            { key: 'ticketsSold', label: 'Билетов' },
            { key: 'revenue', label: 'Выручка', format: 'currency' }
          ]
        }
      }
    },
    products: {
      label: 'Товары',
      types: {
        summary: {
          label: 'Общая статистика',
          endpoint: '/reports/products/summary',
          isSummary: true
        },
        list: {
          label: 'По товарам',
          endpoint: '/reports/products/list',
          dataKey: 'productStats',
          columns: [
            { key: 'productName', label: 'Товар' },
            { key: 'totalSold', label: 'Продано' },
            { key: 'totalRevenue', label: 'Выручка', format: 'currency' }
          ]
        },
        daily: {
          label: 'По дням',
          endpoint: '/reports/products/daily',
          dataKey: 'dailyStats',
          columns: [
            { key: 'date', label: 'Дата', format: 'date' },
            { key: 'itemsSold', label: 'Товаров' },
            { key: 'revenue', label: 'Выручка', format: 'currency' }
          ]
        }
      }
    }
  };

  const formatPrice = (price) => {
    if (price === undefined || price === null) return '0 ₽';
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(price);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const formatPercent = (value) => {
    if (value === undefined || value === null) return '0%';
    return Math.round(value) + '%';
  };

  const formatValue = (value, format) => {
    switch (format) {
      case 'currency':
        return formatPrice(value);
      case 'date':
        return formatDate(value);
      case 'percent':
        return formatPercent(value);
      default:
        return value;
    }
  };

  const renderSummaryReport = (data) => {
    const stats = data.data;
    const isTickets = category === 'tickets';

    return (
      <div className={styles.summaryStats}>
        <div className={styles.statGrid}>
          <div className={styles.statCard}>
            <div className={styles.statValue}>{formatPrice(stats.totalRevenue)}</div>
            <div className={styles.statLabel}>Общая выручка</div>
          </div>

          <div className={styles.statCard}>
            <div className={styles.statValue}>
              {isTickets ? stats.totalTickets : stats.totalItems}
            </div>
            <div className={styles.statLabel}>
              {isTickets ? 'Продано билетов' : 'Продано товаров'}
            </div>
          </div>

          {isTickets && stats.totalSessions !== undefined && (
            <div className={styles.statCard}>
              <div className={styles.statValue}>{stats.totalSessions || 0}</div>
              <div className={styles.statLabel}>Всего сеансов</div>
            </div>
          )}

          {isTickets && stats.topMovie && (
            <div className={styles.statCard}>
              <div className={styles.statValue}>{stats.topMovie || '—'}</div>
              <div className={styles.statLabel}>Самый популярный фильм</div>
              <div className={styles.statSub}>{stats.topMovieTickets || 0} билетов</div>
            </div>
          )}

          {isTickets && stats.topHall && (
            <div className={styles.statCard}>
              <div className={styles.statValue}>{stats.topHall || '—'}</div>
              <div className={styles.statLabel}>Самый популярный зал</div>
              <div className={styles.statSub}>{stats.topHallTickets || 0} билетов</div>
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderTableReport = (data, columns) => {
    const config = reportConfig[category].types[reportType];
    const items = data.data[config.dataKey];

    if (!items || items.length === 0) {
      return <div className={styles.emptyTable}>Нет данных за выбранный период</div>;
    }

    return (
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              {columns.map(col => (
                <th key={col.key}>{col.label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={index}>
                {columns.map(col => (
                  <td key={col.key}>
                    {col.format ? formatValue(item[col.key], col.format) : item[col.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const renderCharts = (charts) => {
    if (!charts || charts.length === 0) return null;

    return charts.map((chart, index) => {
      const maxValue = Math.max(...chart.values.map(v => Number(v)), 1);

      return (
        <div key={index} className={styles.chartContainer}>
          <h3>{chart.title}</h3>
          <div className={styles.chart}>
            <div className={styles.chartBars}>
              {chart.labels.map((label, i) => {
                const percentage = (Number(chart.values[i]) / maxValue) * 100;
                const color = `hsl(${(i * 360) / chart.labels.length}, 70%, 50%)`;

                return (
                  <div key={i} className={styles.chartBarWrapper}>
                    <div className={styles.chartBarLabel}>{label}</div>
                    <div className={styles.chartBarTrack}>
                      <div
                        className={styles.chartBarFill}
                        style={{
                          height: `${Math.max(percentage, 5)}%`,
                          backgroundColor: color
                        }}
                      />
                    </div>
                    <div className={styles.chartBarValue}>
                      {typeof chart.values[i] === 'number' && chart.values[i] > 1000
                        ? formatPrice(chart.values[i])
                        : chart.values[i]}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      );
    });
  };

  const renderReport = () => {
    if (!reportData) return null;

    const { reportName, data, charts } = reportData;
    const config = reportConfig[category].types[reportType];

    return (
      <div className={styles.report}>
        <div className={styles.reportHeader}>
          <h2>{reportName}</h2>
          <div className={styles.reportPeriod}>
            {formatDate(startDate)} — {formatDate(endDate)}
          </div>
          <div className={styles.reportActions}>
            <button onClick={() => exportReport(category, reportType, startDate, endDate, 'csv')} className={styles.exportButton}>
              CSV
            </button>
            <button onClick={() => exportReport(category, reportType, startDate, endDate, 'json')} className={styles.exportButton}>
              JSON
            </button>
            <button onClick={() => exportReport(category, reportType, startDate, endDate, 'pdf')} className={styles.exportButton}>
              PDF
            </button>
            <button
              onClick={() => setShowEmailForm(true)}
              className={`${styles.exportButton} ${styles.emailButton}`}
            >
              Отправить
            </button>
          </div>
        </div>

        {renderCharts(charts)}

        {config.isSummary && renderSummaryReport(reportData)}

        {!config.isSummary && config.columns && renderTableReport(reportData, config.columns)}

        <div className={styles.reportFooter}>
          <span>Сгенерирован: {new Date(reportData.generatedAt).toLocaleString('ru-RU')}</span>
        </div>
      </div>
    );
  };

  const handleGenerate = async () => {
    await generateReport(category, reportType, startDate, endDate);
  };

  const handleCategoryChange = (e) => {
    setCategory(e.target.value);
    setReportType('summary');
    clearReport();
  };

  const handleTypeChange = (e) => {
    setReportType(e.target.value);
    clearReport();
  };

  const handleSendEmail = async () => {
    if (!email) {
      setMessage('Пожалуйста, укажите email');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setMessage('Введите корректный email');
      return;
    }

    setSending(true);
    setMessage('');

    try {
      const reportTypeFull = category + '-' + reportType;
      await sendReportEmail(
        email,
        reportTypeFull,
        emailFormat,
        startDate,
        endDate,
        emailSubject || `Отчет: ${reportConfig[category].types[reportType].label}`,
        emailMessage || ''
      );
      setShowEmailForm(false);
      setEmail('');
      setEmailSubject('');
      setEmailMessage('');
    } catch (error) {
    } finally {
      setSending(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Отчеты и аналитика</h1>

      {message && (
        <div className={`${styles.message} ${message.includes('Ошибка') ? styles.error : styles.success}`}>
          {message}
          <button className={styles.closeMessage} onClick={() => setMessage('')}>×</button>
        </div>
      )}

      <div className={styles.controls}>
        <div className={styles.controlGroup}>
          <label className={styles.label}>Категория</label>
          <select
            value={category}
            onChange={handleCategoryChange}
            className={styles.select}
          >
            <option value="tickets">Билеты</option>
            <option value="products">Товары</option>
          </select>
        </div>

        <div className={styles.controlGroup}>
          <label className={styles.label}>Тип отчета</label>
          <select
            value={reportType}
            onChange={handleTypeChange}
            className={styles.select}
          >
            {Object.entries(reportConfig[category].types).map(([key, { label }]) => (
              <option key={key} value={key}>{label}</option>
            ))}
          </select>
        </div>

        <div className={styles.controlGroup}>
          <label className={styles.label}>С</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className={styles.input}
          />
        </div>

        <div className={styles.controlGroup}>
          <label className={styles.label}>По</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className={styles.input}
          />
        </div>

        <button
          onClick={handleGenerate}
          className={styles.generateButton}
          disabled={loading}
        >
          {loading ? 'Генерация...' : 'Сгенерировать'}
        </button>
      </div>

      {renderReport()}

      {showEmailForm && (
        <div className={styles.overlay}>
          <div className={styles.modal}>
            <h2 className={styles.modalTitle}>Отправить отчет на почту</h2>

            <div className={styles.formGroup}>
              <label className={styles.label}>Email получателя</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="example@mail.com"
                className={styles.input}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Тема письма</label>
              <input
                type="text"
                value={emailSubject}
                onChange={(e) => setEmailSubject(e.target.value)}
                placeholder="Тема письма"
                className={styles.input}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Сообщение</label>
              <textarea
                value={emailMessage}
                onChange={(e) => setEmailMessage(e.target.value)}
                placeholder="Дополнительное сообщение..."
                className={styles.textarea}
                rows={3}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Формат файла</label>
              <select
                value={emailFormat}
                onChange={(e) => setEmailFormat(e.target.value)}
                className={styles.select}
              >
                <option value="pdf">PDF</option>
                <option value="csv">CSV</option>
                <option value="json">JSON</option>
              </select>
            </div>

            <div className={styles.modalActions}>
              <button
                onClick={handleSendEmail}
                className={styles.submitButton}
                disabled={sending || !email}
              >
                {sending ? 'Отправка...' : 'Отправить'}
              </button>
              <button
                onClick={() => {
                  setShowEmailForm(false);
                  setEmail('');
                  setEmailSubject('');
                  setEmailMessage('');
                }}
                className={styles.cancelButton}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ReportsPage;