import React, { useState, useEffect } from 'react';
import { useHalls } from '../../hooks/useHalls';
import styles from './HallsPage.module.css';

const MAX_NAME = 100;
const MIN_NAME = 2;
const MAX_ROWS = 20;
const MAX_SEATS = 30;

function HallsPage() {
  const {
    halls,
    loading,
    message,
    setMessage,
    createHall,
    deleteHall,
    searchHalls
  } = useHalls();

  const [showForm, setShowForm] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const [formMessage, setFormMessage] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    rowsCount: '',
    seatsPerRow: ''
  });
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (formMessage) {
      const timer = setTimeout(() => setFormMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [formMessage]);

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(''), 1500);
      return () => clearTimeout(timer);
    }
  }, [message, setMessage]);

  const handleChange = (e) => {
    const { name, value } = e.target;
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

  const handleSearch = (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    searchHalls(query);
  };

  const handleRefresh = () => {
    setSearchQuery('');
    searchHalls('');
  };

  const validateForm = () => {
    const invalid = {};
    const errors = [];

    const name = formData.name.trim();
    if (!name) {
      invalid.name = true;
      errors.push('Название зала обязательно');
    } else if (name.length < MIN_NAME) {
      invalid.name = true;
      errors.push(`Название должно содержать минимум ${MIN_NAME} символа`);
    } else if (name.length > MAX_NAME) {
      invalid.name = true;
      errors.push(`Название не должно превышать ${MAX_NAME} символов`);
    } else {
      const existing = halls.find(
        h => h.name.toLowerCase() === name.toLowerCase()
      );
      if (existing) {
        invalid.name = true;
        errors.push(`Зал с именем "${name}" уже существует`);
      }
    }

    const rows = parseInt(formData.rowsCount, 10);
    if (!formData.rowsCount) {
      invalid.rowsCount = true;
      errors.push('Укажите количество рядов');
    } else if (Number.isNaN(rows) || rows < 1) {
      invalid.rowsCount = true;
      errors.push('Количество рядов должно быть больше 0');
    } else if (rows > MAX_ROWS) {
      invalid.rowsCount = true;
      errors.push(`Максимальное количество рядов — ${MAX_ROWS}`);
    }

    const seats = parseInt(formData.seatsPerRow, 10);
    if (!formData.seatsPerRow) {
      invalid.seatsPerRow = true;
      errors.push('Укажите количество мест в ряду');
    } else if (Number.isNaN(seats) || seats < 1) {
      invalid.seatsPerRow = true;
      errors.push('Количество мест должно быть больше 0');
    } else if (seats > MAX_SEATS) {
      invalid.seatsPerRow = true;
      errors.push(`Максимальное количество мест в ряду — ${MAX_SEATS}`);
    }

    if (Object.keys(invalid).length === 0) return null;
    return { invalid, msg: errors.join('; ') };
  };

  const handleSubmit = async (e) => {
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
        name: formData.name.trim(),
        rowsCount: parseInt(formData.rowsCount, 10),
        seatsPerRow: parseInt(formData.seatsPerRow, 10)
      };

      await createHall(data);

      setShowForm(false);
      setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
      setFormErrors({});
      setFormMessage('');
    } catch (err) {
      const data = err.response?.data;
      let msg = 'Ошибка создания зала';
      const invalid = {};

      if (typeof data === 'string') {
        msg = data;
      } else if (data && typeof data === 'object') {
        if (typeof data.message === 'string' && data.message.trim()) {
          msg = data.message;
        }

        const fieldsSource = data.errors && typeof data.errors === 'object'
          ? data.errors
          : data;

        Object.entries(fieldsSource).forEach(([field, value]) => {
          if (['name', 'rowsCount', 'seatsPerRow'].includes(field)
            && typeof value === 'string') {
            invalid[field] = true;
          }
        });

        if (msg === 'Ошибка создания зала' && data.errors) {
          const values = Object.values(data.errors).filter(v => typeof v === 'string');
          if (values.length) msg = values.join('; ');
        } else if (msg === 'Ошибка создания зала') {
          const values = Object.values(fieldsSource).filter(v => typeof v === 'string');
          if (values.length) msg = values.join('; ');
        }
      }

      setFormErrors(invalid);
      setFormMessage('❌ ' + msg);
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
    setFormErrors({});
    setFormMessage('');
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Вы уверены, что хотите удалить зал "${name}"?`)) {
      try {
        await deleteHall(id, name);
      } catch (err) {
      }
    }
  };

  const getTotalSeats = (rows, seatsPerRow) => rows * seatsPerRow;

  const inputClass = (field) =>
    `${styles.input} ${formErrors[field] ? styles.inputError : ''}`;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>Управление залами</h1>
        <button
          className={styles.addButton}
          onClick={() => {
            setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
            setFormErrors({});
            setFormMessage('');
            setShowForm(true);
          }}
        >
          + Создать зал
        </button>
      </div>

      <div className={styles.toolbar}>
        <input
          type="text"
          value={searchQuery}
          onChange={handleSearch}
          placeholder="Поиск залов..."
          className={styles.searchInput}
        />
        <button className={styles.refreshButton} onClick={handleRefresh}>
          Обновить
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
      ) : halls.length === 0 ? (
        <div className={styles.emptyState}>
          <p>Нет залов</p>
          <p className={styles.emptyHint}>Создайте первый зал, нажав кнопку "Создать зал"</p>
        </div>
      ) : (
        <div className={styles.grid}>
          {halls.map((hall) => (
            <div key={hall.id} className={styles.card}>
              <div className={styles.cardHeader}>
                <h3 className={styles.hallName}>{hall.name}</h3>
                <span className={styles.hallCapacity}>{hall.capacity} мест</span>
              </div>
              <div className={styles.cardInfo}>
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>Рядов:</span>
                  <span className={styles.infoValue}>{hall.rowsCount}</span>
                </div>
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>Мест в ряду:</span>
                  <span className={styles.infoValue}>{hall.seatsPerRow}</span>
                </div>
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>Всего мест:</span>
                  <span className={styles.infoValue}>{hall.capacity}</span>
                </div>
              </div>
              <div className={styles.cardActions}>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDelete(hall.id, hall.name)}
                >
                  🗑️ Удалить
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <div className={styles.overlay}>
          <div className={styles.modal}>
            <h2 className={styles.modalTitle}>Создать новый зал</h2>

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

            <form onSubmit={handleSubmit} className={styles.form} noValidate>
              <div className={styles.inputGroup}>
                <label className={styles.label}>Название зала</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Зал 1"
                  className={inputClass('name')}
                />
                {!formErrors.name && formData.name && (
                  <div className={styles.fieldHint}>
                    {halls.some(h => h.name.toLowerCase() === formData.name.trim().toLowerCase())
                      ? '⚠️ Зал с таким именем уже существует'
                      : '✔️ Имя доступно'}
                  </div>
                )}
              </div>

              <div className={styles.row}>
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Количество рядов</label>
                  <input
                    type="number"
                    name="rowsCount"
                    value={formData.rowsCount}
                    onChange={handleChange}
                    placeholder="5"
                    min="1"
                    max={MAX_ROWS}
                    className={inputClass('rowsCount')}
                  />
                  {!formErrors.rowsCount && formData.rowsCount && (
                    <div className={styles.fieldHint}>
                      Допустимо от 1 до {MAX_ROWS} рядов
                    </div>
                  )}
                </div>

                <div className={styles.inputGroup}>
                  <label className={styles.label}>Мест в ряду</label>
                  <input
                    type="number"
                    name="seatsPerRow"
                    value={formData.seatsPerRow}
                    onChange={handleChange}
                    placeholder="10"
                    min="1"
                    max={MAX_SEATS}
                    className={inputClass('seatsPerRow')}
                  />
                  {!formErrors.seatsPerRow && formData.seatsPerRow && (
                    <div className={styles.fieldHint}>
                      Допустимо от 1 до {MAX_SEATS} мест
                    </div>
                  )}
                </div>
              </div>

              <div className={styles.preview}>
                <p>
                  Вместимость: <strong>{getTotalSeats(
                    parseInt(formData.rowsCount, 10) || 0,
                    parseInt(formData.seatsPerRow, 10) || 0
                  )}</strong> зрителей
                </p>
              </div>

              <div className={styles.buttonGroup}>
                <button
                  type="submit"
                  className={styles.submitButton}
                  disabled={formLoading}
                >
                  {formLoading ? 'Создание...' : 'Создать зал'}
                </button>
                <button
                  type="button"
                  onClick={handleCancel}
                  className={styles.cancelButton}
                >
                  Отмена
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default HallsPage;