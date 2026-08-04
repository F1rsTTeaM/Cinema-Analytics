import React, { useState } from 'react';
import { useSessions } from '../../hooks/useSessions';
import styles from './SessionsPage.module.css';

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
  const [formData, setFormData] = useState({
    movieId: '',
    hallId: '',
    startTime: '',
    endTime: '',
    ticketPrice: ''
  });

  const validateForm = () => {
  const errors = {};
  let isValid = true;

  if (!formData.movieId) {
    errors.movieId = 'Выберите фильм';
    isValid = false;
  }

  if (!formData.hallId) {
    errors.hallId = 'Выберите зал';
    isValid = false;
  }

  if (!formData.startTime) {
    errors.startTime = 'Укажите время начала';
    isValid = false;
  }

  if (!formData.endTime) {
    errors.endTime = 'Укажите время окончания';
    isValid = false;
  }

  if (formData.startTime && formData.endTime) {
    const start = new Date(formData.startTime);
    const end = new Date(formData.endTime);
    
    if (end <= start) {
      errors.endTime = 'Время окончания должно быть позже времени начала';
      isValid = false;
    } else {
      const durationMinutes = (end - start) / (1000 * 60);
      if (durationMinutes < 30) {
        errors.endTime = 'Минимальная длительность сеанса — 30 минут';
        isValid = false;
      }
    }
  }

  if (!formData.ticketPrice) {
    errors.ticketPrice = 'Укажите цену билета';
    isValid = false;
  } else if (parseFloat(formData.ticketPrice) < 0) {
    errors.ticketPrice = 'Цена не может быть отрицательной';
    isValid = false;
  }

  setFormErrors(errors);
  return isValid;
};

  const handleCreate = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setFormLoading(true);
    setFormErrors({});

    try {
      const data = {
        ...formData,
        movieId: parseInt(formData.movieId),
        hallId: parseInt(formData.hallId),
        ticketPrice: parseFloat(formData.ticketPrice)
      };

      await createSession(data);
      setShowForm(false);
      setFormData({ movieId: '', hallId: '', startTime: '', endTime: '', ticketPrice: '' });
      setFormErrors({});
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || 'Ошибка создания сеанса';
      if (errorMsg.includes('пересекается') || errorMsg.includes('уже есть сеанс')) {
        setFormErrors({ hallId: errorMsg });
      } else if (errorMsg.includes('минимальная длительность')) {
        setFormErrors({ endTime: errorMsg });
      } else {
        console.error('Error creating session:', errorMsg);
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleStatusChange = async (id, currentStatus) => {
    const statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'SOLD_OUT'];
    const currentIndex = statuses.indexOf(currentStatus);
    const nextIndex = (currentIndex + 1) % statuses.length;
    const newStatus = statuses[nextIndex];

    await updateSessionStatus(id, newStatus);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этот сеанс?')) {
      await deleteSession(id);
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
      prev.includes(seat)
        ? prev.filter(s => s !== seat)
        : [...prev, seat]
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

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const isPurchasable = (session) => {
    return session.status !== 'CANCELLED' && 
           session.status !== 'COMPLETED' && 
           session.status !== 'SOLD_OUT';
  };

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
            <form onSubmit={handleCreate} className={styles.form}>
              <div className={styles.inputGroup}>
                <label className={styles.label}>Фильм</label>
                <select
                  name="movieId"
                  value={formData.movieId}
                  onChange={(e) => {
                    setFormData({ ...formData, movieId: e.target.value });
                    if (formErrors.movieId) {
                      setFormErrors({ ...formErrors, movieId: '' });
                    }
                  }}
                  required
                  className={`${styles.select} ${formErrors.movieId ? styles.inputError : ''}`}
                >
                  <option value="">Выберите фильм</option>
                  {movies.map(movie => (
                    <option key={movie.id} value={movie.id}>
                      {movie.title}
                    </option>
                  ))}
                </select>
                {formErrors.movieId && (
                  <div className={styles.fieldError}>{formErrors.movieId}</div>
                )}
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Зал</label>
                <select
                  name="hallId"
                  value={formData.hallId}
                  onChange={(e) => {
                    setFormData({ ...formData, hallId: e.target.value });
                    if (formErrors.hallId) {
                      setFormErrors({ ...formErrors, hallId: '' });
                    }
                  }}
                  required
                  className={`${styles.select} ${formErrors.hallId ? styles.inputError : ''}`}
                >
                  <option value="">Выберите зал</option>
                  {halls.map(hall => (
                    <option key={hall.id} value={hall.id}>
                      {hall.name} ({hall.capacity} мест)
                    </option>
                  ))}
                </select>
                {formErrors.hallId && (
                  <div className={styles.fieldError}>{formErrors.hallId}</div>
                )}
              </div>

              <div className={styles.row}>
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Начало</label>
                  <input
                    type="datetime-local"
                    name="startTime"
                    value={formData.startTime}
                    onChange={(e) => {
                      setFormData({ ...formData, startTime: e.target.value });
                      if (formErrors.startTime) {
                        setFormErrors({ ...formErrors, startTime: '' });
                      }
                    }}
                    required
                    className={`${styles.input} ${formErrors.startTime ? styles.inputError : ''}`}
                  />
                  {formErrors.startTime && (
                    <div className={styles.fieldError}>{formErrors.startTime}</div>
                  )}
                </div>
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Конец</label>
                  <input
                    type="datetime-local"
                    name="endTime"
                    value={formData.endTime}
                    onChange={(e) => {
                      setFormData({ ...formData, endTime: e.target.value });
                      if (formErrors.endTime) {
                        setFormErrors({ ...formErrors, endTime: '' });
                      }
                    }}
                    required
                    className={`${styles.input} ${formErrors.endTime ? styles.inputError : ''}`}
                  />
                  {formErrors.endTime && (
                    <div className={styles.fieldError}>{formErrors.endTime}</div>
                  )}
                </div>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Цена билета (₽)</label>
                <input
                  type="number"
                  name="ticketPrice"
                  value={formData.ticketPrice}
                  onChange={(e) => {
                    setFormData({ ...formData, ticketPrice: e.target.value });
                    if (formErrors.ticketPrice) {
                      setFormErrors({ ...formErrors, ticketPrice: '' });
                    }
                  }}
                  placeholder="15.99"
                  required
                  min="0"
                  step="0.01"
                  className={`${styles.input} ${formErrors.ticketPrice ? styles.inputError : ''}`}
                />
                {formErrors.ticketPrice && (
                  <div className={styles.fieldError}>{formErrors.ticketPrice}</div>
                )}
              </div>

              <div className={styles.buttonGroup}>
                <button type="submit" className={styles.submitButton} disabled={formLoading}>
                  {formLoading ? 'Создание...' : 'Создать сеанс'}
                </button>
                <button type="button" onClick={() => setShowForm(false)} className={styles.cancelButton}>
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