import React, { useState } from 'react';
import { useHalls } from '../../hooks/useHalls';
import styles from './HallsPage.module.css';

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
  const [formSuccess, setFormSuccess] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    rowsCount: '',
    seatsPerRow: ''
  });
  const [searchQuery, setSearchQuery] = useState('');

  const validateForm = () => {
    const errors = {};
    let isValid = true;

    if (!formData.name.trim()) {
      errors.name = 'Название зала обязательно';
      isValid = false;
    } else if (formData.name.trim().length < 2) {
      errors.name = 'Название должно содержать минимум 2 символа';
      isValid = false;
    } else {
      const existing = halls.find(
        h => h.name.toLowerCase() === formData.name.trim().toLowerCase()
      );
      if (existing) {
        errors.name = `Зал с именем "${formData.name.trim()}" уже существует`;
        isValid = false;
      }
    }

    const rows = parseInt(formData.rowsCount);
    if (!formData.rowsCount) {
      errors.rowsCount = 'Укажите количество рядов';
      isValid = false;
    } else if (isNaN(rows) || rows < 1) {
      errors.rowsCount = 'Количество рядов должно быть больше 0';
      isValid = false;
    } else if (rows > 20) {
      errors.rowsCount = 'Максимальное количество рядов - 20';
      isValid = false;
    }

    const seats = parseInt(formData.seatsPerRow);
    if (!formData.seatsPerRow) {
      errors.seatsPerRow = 'Укажите количество мест в ряду';
      isValid = false;
    } else if (isNaN(seats) || seats < 1) {
      errors.seatsPerRow = 'Количество мест должно быть больше 0';
      isValid = false;
    } else if (seats > 30) {
      errors.seatsPerRow = 'Максимальное количество мест в ряду - 30';
      isValid = false;
    }

    setFormErrors(errors);
    return isValid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: '' }));
    }
    setFormSuccess('');
    setMessage('');
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

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setFormLoading(true);
    setFormSuccess('');
    setFormErrors({});

    try {
      const data = {
        name: formData.name.trim(),
        rowsCount: parseInt(formData.rowsCount),
        seatsPerRow: parseInt(formData.seatsPerRow)
      };

      const result = await createHall(data);
      
      setFormSuccess(`✔️ Зал "${result.name}" успешно создан!`);
      setFormData({ name: '', rowsCount: '', seatsPerRow: '' });

      setShowForm(false);
      setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Ошибка создания зала';
      if (errorMsg.includes('существует')) {
        setFormErrors({ name: errorMsg });
      } else {
        setMessage('❌ ' + errorMsg);
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
    setFormErrors({});
    setFormSuccess('');
    setMessage('');
  };

  const handleDelete = (id, name) => {
    if (window.confirm(`Вы уверены, что хотите удалить зал "${name}"?`)) {
      deleteHall(id, name);
    }
  };

  const getTotalSeats = (rows, seatsPerRow) => {
    return rows * seatsPerRow;
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>Управление залами</h1>
        <button
          className={styles.addButton}
          onClick={() => {
            setFormData({ name: '', rowsCount: '', seatsPerRow: '' });
            setFormErrors({});
            setFormSuccess('');
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
        <button
          className={styles.refreshButton}
          onClick={handleRefresh}
        >
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
                <span className={styles.hallCapacity}>
                  {hall.capacity} мест
                </span>
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

            {formSuccess && (
              <div className={styles.formSuccess}>{formSuccess}</div>
            )}

            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.inputGroup}>
                <label className={styles.label}>Название зала</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Зал 1"
                  required
                  className={`${styles.input} ${formErrors.name ? styles.inputError : ''}`}
                />
                {formErrors.name && (
                  <div className={styles.fieldError}>{formErrors.name}</div>
                )}
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
                    required
                    min="1"
                    max="20"
                    className={`${styles.input} ${formErrors.rowsCount ? styles.inputError : ''}`}
                  />
                  {formErrors.rowsCount && (
                    <div className={styles.fieldError}>{formErrors.rowsCount}</div>
                  )}
                  {!formErrors.rowsCount && formData.rowsCount && (
                    <div className={styles.fieldHint}>Допустимо от 1 до 20 рядов</div>
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
                    required
                    min="1"
                    max="30"
                    className={`${styles.input} ${formErrors.seatsPerRow ? styles.inputError : ''}`}
                  />
                  {formErrors.seatsPerRow && (
                    <div className={styles.fieldError}>{formErrors.seatsPerRow}</div>
                  )}
                  {!formErrors.seatsPerRow && formData.seatsPerRow && (
                    <div className={styles.fieldHint}>Допустимо от 1 до 30 мест</div>
                  )}
                </div>
              </div>

              <div className={styles.preview}>
                <p>
                  Вместимость: <strong>{getTotalSeats(
                    parseInt(formData.rowsCount) || 0,
                    parseInt(formData.seatsPerRow) || 0
                  )}</strong> зрителей
                </p>
                {getTotalSeats(
                  parseInt(formData.rowsCount) || 0,
                  parseInt(formData.seatsPerRow) || 0
                ) > 200 && (
                  <p className={styles.previewWarning}>⚠️ Большой зал! Максимальная вместимость — 200 мест</p>
                )}
              </div>

              <div className={styles.buttonGroup}>
                <button
                  type="submit"
                  className={styles.submitButton}
                  disabled={formLoading || !!formErrors.name}
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