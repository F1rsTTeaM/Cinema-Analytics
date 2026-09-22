import React, { useState, useEffect } from 'react';
import { useSessions } from '../../hooks/useSessions';
import styles from './SessionsPage.module.css';

const MIN_PRICE = 100;
const MAX_PRICE = 10000;
const MIN_DURATION_MINUTES = 60;
const MAX_MONTHS_AHEAD = 3;

function SessionsPage() {
  const {
    sessions,
    movies,
    halls,
    loading,
    message,
    setMessage,
    createSession,
    updateSessionStatus,
    deleteSession,
    purchaseTickets
  } = useSessions();

  const [showForm, setShowForm] = useState(false);
  const [showSeatSelection, setShowSeatSelection] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [formLoading, setFormLoading] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const [formMessage, setFormMessage] = useState('');
  const [formData, setFormData] = useState({
    movieId: '',
    hallId: '',
    startTime: '',
    endTime: '',
    ticketPrice: ''
  });

  // Автоскрытие сообщения в форме
  useEffect(() => {
    if (formMessage) {
      const timer = setTimeout(() => setFormMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [formMessage]);

  // Автоскрытие сообщения страницы
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(''), 1500);
      return () => clearTimeout(timer);
    }
  }, [message, setMessage]);

  // Сброс ошибок/сообщений при закрытии формы
  useEffect(() => {
    if (!showForm) {
      setFormErrors({});
      setFormMessage('');
    }
  }, [showForm]);

  const todayStr = new Date().toISOString().slice(0, 16);
  const maxDateStr = new Date(Date.now() + 1000 * 60 * 60 * 24 * 30 * MAX_MONTHS_AHEAD)
    .toISOString().slice(0, 16);

  const handleChange = (name, value) => {
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors(prev => {
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }
    if (formMessage) setFormMessage('');
  };

  const validateForm = () => {
    const invalid = {};
    const errors = [];

    if (!formData.movieId) {
      invalid.movieId = true;
      errors.push('Выберите фильм');
    }
    if (!formData.hallId) {
      invalid.hallId = true;
      errors.push('Выберите зал');
    }

    const now = new Date();
    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + MAX_MONTHS_AHEAD);

    if (!formData.startTime) {
      invalid.startTime = true;
      errors.push('Укажите время начала');
    } else {
      const start = new Date(formData.startTime);
      const todayStart = new Date();
      todayStart.setHours(0, 0, 0, 0);

      if (start < todayStart) {
        invalid.startTime = true;
        errors.push('Начало сеанса не может быть раньше сегодняшнего дня');
      } else if (start > maxDate) {
        invalid.startTime = true;
        errors.push(`Начало сеанса не может быть позже ${MAX_MONTHS_AHEAD} месяцев от текущей даты`);
      }
    }

    if (!formData.endTime) {
      invalid.endTime = true;
      errors.push('Укажите время окончания');
    } else {
      const end = new Date(formData.endTime);
      const todayStart = new Date();
      todayStart.setHours(0, 0, 0, 0);

      if (end < todayStart) {
        invalid.endTime = true;
        errors.push('Окончание сеанса не может быть раньше сегодняшнего дня');
      } else if (end > maxDate) {
        invalid.endTime = true;
        errors.push(`Окончание сеанса не может быть позже ${MAX_MONTHS_AHEAD} месяцев от текущей даты`);
      }
    }

    if (formData.startTime && formData.endTime) {
      const start = new Date(formData.startTime);
      const end = new Date(formData.endTime);

      if (end <= start) {
        invalid.endTime = true;
        errors.push('Время окончания должно быть позже времени начала');
      } else {
        const minutes = (end - start) / (1000 * 60);
        if (minutes < MIN_DURATION_MINUTES) {
          invalid.endTime = true;
          errors.push(`Минимальная длительность сеанса — ${MIN_DURATION_MINUTES} минут`);
        }
      }
    }

    if (!formData.ticketPrice && formData.ticketPrice !== 0) {
      invalid.ticketPrice = true;
      errors.push('Укажите цену билета');
    } else {
      const price = parseFloat(formData.ticketPrice);
      if (Number.isNaN(price)) {
        invalid.ticketPrice = true;
        errors.push('Цена должна быть числом');
      } else if (price < MIN_PRICE) {
        invalid.ticketPrice = true;
        errors.push(`Цена билета должна быть не менее ${MIN_PRICE} ₽`);
      } else if (price > MAX_PRICE) {
        invalid.ticketPrice = true;
        errors.push(`Цена билета не должна превышать ${MAX_PRICE.toLocaleString('ru-RU')} ₽`);
      }
    }

    if (Object.keys(invalid).length === 0) return null;
    return { invalid, msg: errors.join('; ') };
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormMessage('');

    const result = validateForm();
    if (result) {
      setFormErrors(result.invalid);
      setFormMessage('❌ ' + result.msg);
      return;
    }
    setFormErrors({});

    setFormLoading(true);

    try {
      const data = {
        movieId: parseInt(formData.movieId, 10),
        hallId: parseInt(formData.hallId, 10),
        startTime: formData.startTime + ':00',
        endTime: formData.endTime + ':00',
        ticketPrice: parseFloat(formData.ticketPrice)
      };

      await createSession(data);

      setShowForm(false);
      setFormData({ movieId: '', hallId: '', startTime: '', endTime: '', ticketPrice: '' });
      setFormErrors({});
      setFormMessage('');
    } catch (err) {
      const data = err.response?.data;
      let msg = 'Ошибка создания сеанса';
      const invalid = {};

      if (typeof data === 'string') {
        msg = data;
      } else if (data && typeof data === 'object') {
        if (typeof data.message === 'string' && data.message.trim()) {
          msg = data.message;
        }

        const src = data.errors && typeof data.errors === 'object' ? data.errors : data;
        Object.entries(src).forEach(([field, value]) => {
          if (['movieId', 'hallId', 'startTime', 'endTime', 'ticketPrice'].includes(field)
              && typeof value === 'string') {
            invalid[field] = true;
          }
        });

        if (msg === 'Ошибка создания сеанса') {
          const values = Object.values(src).filter(v => typeof v === 'string');
          if (values.length) msg = values.join('; ');
        }

        // Эвристика: сервер вернул что-то про пересечение — подсветим зал
        if (/пересека|занят|уже есть сеанс/i.test(msg)) {
          invalid.hallId = true;
        }
        if (/длительн|1 час|60 мин/i.test(msg)) {
          invalid.endTime = true;
        }
      }

      setFormErrors(invalid);
      setFormMessage('❌ ' + msg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleStatusChange = async (id, currentStatus) => {
    const statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SOLD_OUT'];
    const currentIndex = statuses.indexOf(currentStatus);
    const nextIndex = (currentIndex + 1) % statuses.length;
    await updateSessionStatus(id, statuses[nextIndex]);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этот сеанс?')) {
      try {
        await deleteSession(id);
      } catch (err) {
        // message уже показан через хук
      }
    }
  };

  const handlePurchase = async () => {
    if (selectedSeats.length === 0) {
      setMessage('❌ Выберите хотя бы одно место');
      return;
    }
    await purchaseTickets(selectedSession.id, selectedSeats);
    setSelectedSeats([]);
    setShowSeatSelection(false);
  };

  const toggleSeat = (seat) => {
    setSelectedSeats(prev =>
      prev.includes(seat) ? prev.filter(s => s !== seat) : [...prev, seat]
    );
  };

  const getStatusLabel = (status) => {
    const labels = {
      SCHEDULED: 'Запланирован',
      IN_PROGRESS: 'Идёт',
      COMPLETED: 'Завершён',
      CANCELLED: 'Отменён',
      SOLD_OUT: 'Распродан'
    };
    return labels[status] || status;
  };

  const getStatusColor = (status) => {
    const colors = {
      SCHEDULED: '#2196F3',
      IN_PROGRESS: '#FF9800',
      COMPLETED: '#4CAF50',
      CANCELLED: '#f44336',
      SOLD_OUT: '#9E9E9E'
    };
    return colors[status] || '#999';
  };

  const formatDate = (dateString) =>
    new Date(dateString).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

  const isPurchasable = (session) =>
    session.status !== 'CANCELLED' &&
    session.status !== 'COMPLETED' &&
    session.status !== 'SOLD_OUT';

  const inputClass = (field) =>
    `${styles.input} ${formErrors[field] ? styles.inputError : ''}`;

  const selectClass = (field) =>
    `${styles.select} ${formErrors[field] ? styles.inputError : ''}`;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>Управление сеансами</h1>
        <button className={styles.addButton} onClick={() => setShowForm(true)}>
          + Добавить сеанс
        </button>
      </div>

      {message && (
        <div className={`${styles.message} ${message.includes('✔️') ? styles.success : styles.error}`}>
          {message}
          <button className={styles.closeMessage} onClick={() => setMessage('')}>×</button>
        </div>
      )}

      {loading ? (
        <div className={styles.loading}>Загрузка...</div>
      ) : sessions.length === 0 ? (
        <div className={styles.emptyState}>
          <p>Нет сеансов</p>
          <p className={styles.emptyHint}>Создайте первый сеанс, нажав кнопку "Добавить сеанс"</p>
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Фильм</th>
                <th>Зал</th>
                <th>Начало</th>
                <th>Конец</th>
                <th>Цена</th>
                <th>Билеты</th>
                <th>Статус</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {sessions.map((session) => (
                <tr key={session.id}>
                  <td>{session.movieTitle}</td>
                  <td>{session.hallName}</td>
                  <td>{formatDate(session.startTime)}</td>
                  <td>{formatDate(session.endTime)}</td>
                  <td>{session.ticketPrice} ₽</td>
                  <td>{session.soldCount}/{session.capacity}</td>
                  <td>
                    <span
                      className={styles.statusBadge}
                      style={{ backgroundColor: getStatusColor(session.status) }}
                    >
                      {getStatusLabel(session.status)}
                    </span>
                  </td>
                  <td>
                    <div className={styles.actions}>
                      <button
                        className={styles.buyButton}
                        onClick={() => {
                          setSelectedSession(session);
                          setSelectedSeats([]);
                          setShowSeatSelection(true);
                        }}
                        disabled={!isPurchasable(session)}
                        title={!isPurchasable(session) ? 'Билеты недоступны' : 'Купить билеты'}
                      >
                        🎫
                      </button>
                      <button
                        className={styles.statusButton}
                        onClick={() => handleStatusChange(session.id, session.status)}
                        title="Изменить статус"
                      >
                        🔄
                      </button>
                      <button
                        className={styles.deleteButton}
                        onClick={() => handleDelete(session.id)}
                        title="Удалить"
                      >
                        🗑️
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <div className={styles.overlay}>
          <div className={styles.modal}>
            <h2 className={styles.modalTitle}>Добавить сеанс</h2>

            {formMessage && (
              <div className={`${styles.message} ${formMessage.includes('❌') ? styles.error : styles.success}`}>
                {formMessage}
                <button
                  type="button"
                  className={styles.closeMessage}
                  onClick={() => setFormMessage('')}
                >
                  ×
                </button>
              </div>
            )}

            <form onSubmit={handleCreate} className={styles.form} noValidate>
              <div className={styles.inputGroup}>
                <label className={styles.label}>Фильм</label>
                <select
                  name="movieId"
                  value={formData.movieId}
                  onChange={(e) => handleChange('movieId', e.target.value)}
                  className={selectClass('movieId')}
                >
                  <option value="">Выберите фильм</option>
                  {movies.map(movie => (
                    <option key={movie.id} value={movie.id}>{movie.title}</option>
                  ))}
                </select>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Зал</label>
                <select
                  name="hallId"
                  value={formData.hallId}
                  onChange={(e) => handleChange('hallId', e.target.value)}
                  className={selectClass('hallId')}
                >
                  <option value="">Выберите зал</option>
                  {halls.map(hall => (
                    <option key={hall.id} value={hall.id}>
                      {hall.name} ({hall.capacity} мест)
                    </option>
                  ))}
                </select>
              </div>

              <div className={styles.row}>
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Начало</label>
                  <input
                    type="datetime-local"
                    name="startTime"
                    value={formData.startTime}
                    onChange={(e) => handleChange('startTime', e.target.value)}
                    min={todayStr}
                    max={maxDateStr}
                    className={inputClass('startTime')}
                  />
                  {!formErrors.startTime && formData.startTime && (
                    <div className={styles.fieldHint}>
                      Не раньше сегодня и не позже {MAX_MONTHS_AHEAD} месяцев вперёд
                    </div>
                  )}
                </div>

                <div className={styles.inputGroup}>
                  <label className={styles.label}>Конец</label>
                  <input
                    type="datetime-local"
                    name="endTime"
                    value={formData.endTime}
                    onChange={(e) => handleChange('endTime', e.target.value)}
                    min={todayStr}
                    max={maxDateStr}
                    className={inputClass('endTime')}
                  />
                  {!formErrors.endTime && formData.endTime && (
                    <div className={styles.fieldHint}>
                      Не раньше начала и не позже {MAX_MONTHS_AHEAD} месяцев вперёд, минимум +1 час
                    </div>
                  )}
                </div>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Цена билета (₽)</label>
                <input
                  type="number"
                  name="ticketPrice"
                  value={formData.ticketPrice}
                  onChange={(e) => handleChange('ticketPrice', e.target.value)}
                  placeholder="500"
                  min={MIN_PRICE}
                  max={MAX_PRICE}
                  step="0.01"
                  className={inputClass('ticketPrice')}
                />
                {!formErrors.ticketPrice && formData.ticketPrice && (
                  <div className={styles.fieldHint}>
                    От {MIN_PRICE} до {MAX_PRICE.toLocaleString('ru-RU')} ₽
                  </div>
                )}
              </div>

              <div className={styles.buttonGroup}>
                <button type="submit" className={styles.submitButton} disabled={formLoading}>
                  {formLoading ? 'Создание...' : 'Создать сеанс'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowForm(false);
                    setFormData({ movieId: '', hallId: '', startTime: '', endTime: '', ticketPrice: '' });
                    setFormErrors({});
                    setFormMessage('');
                  }}
                  className={styles.cancelButton}
                >
                  Отмена
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showSeatSelection && selectedSession && (
        <div className={styles.overlay}>
          <div className={styles.seatModal}>
            <h2 className={styles.modalTitle}>Выбор мест</h2>
            <p className={styles.seatInfo}>
              {selectedSession.movieTitle} — {selectedSession.hallName}
              <br />
              <span className={styles.seatSubInfo}>
                Свободно: {selectedSession.availableCount} / {selectedSession.capacity}
              </span>
            </p>

            <div className={styles.seatGrid}>
              {selectedSession.availableSeats?.map((seat) => (
                <button
                  key={seat}
                  className={`${styles.seatButton} ${selectedSeats.includes(seat) ? styles.selected : ''}`}
                  onClick={() => toggleSeat(seat)}
                >
                  {seat}
                </button>
              ))}
            </div>

            <div className={styles.seatSummary}>
              Выбрано: <strong>{selectedSeats.length}</strong> мест
              <br />
              Итого: <strong>{(selectedSeats.length * selectedSession.ticketPrice).toFixed(2)} ₽</strong>
            </div>

            <div className={styles.buttonGroup}>
              <button className={styles.submitButton} onClick={handlePurchase}>
                Купить билеты
              </button>
              <button
                className={styles.cancelButton}
                onClick={() => {
                  setShowSeatSelection(false);
                  setSelectedSeats([]);
                }}
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

export default SessionsPage;