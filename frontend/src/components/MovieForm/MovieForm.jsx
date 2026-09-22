import React, { useState, useEffect } from 'react';
import styles from './MovieForm.module.css';

const MAX_TITLE = 256;
const MAX_GENRE = 256;
const MAX_DURATION = 240;
const MIN_DATE = '1895-01-01';

function MovieForm({ movie, onSubmit, onCancel, loading }) {
    const [formData, setFormData] = useState({
        title: '',
        genre: '',
        durationMinutes: '',
        releaseDate: ''
    });

    const [message, setMessage] = useState('');
    const [invalidFields, setInvalidFields] = useState({});

    useEffect(() => {
        if (movie) {
            setFormData({
                title: movie.title || '',
                genre: movie.genre || '',
                durationMinutes: movie.durationMinutes ?? '',
                releaseDate: movie.releaseDate ? movie.releaseDate.substring(0, 10) : ''
            });
        } else {
            setFormData({ title: '', genre: '', durationMinutes: '', releaseDate: '' });
        }
        setMessage('');
        setInvalidFields({});
    }, [movie]);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage('');
            }, 3000);

            return () => clearTimeout(timer);
        }
    }, [message]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));

        if (invalidFields[name]) {
            setInvalidFields(prev => {
                const next = { ...prev };
                delete next[name];
                return next;
            });
        }
        if (message) setMessage('');
    };

    const validate = () => {
        const invalid = {};
        const messages = [];

        if (!formData.title.trim()) {
            invalid.title = true;
            messages.push('Введите название фильма');
        } else if (formData.title.length > MAX_TITLE) {
            invalid.title = true;
            messages.push(`Название не должно превышать ${MAX_TITLE} символов`);
        }

        if (!formData.genre.trim()) {
            invalid.genre = true;
            messages.push('Введите жанр');
        } else if (formData.genre.length > MAX_GENRE) {
            invalid.genre = true;
            messages.push(`Жанр не должен превышать ${MAX_GENRE} символов`);
        }

        const duration = Number(formData.durationMinutes);
        if (!formData.durationMinutes || duration <= 0) {
            invalid.durationMinutes = true;
            messages.push('Длительность должна быть больше 0');
        } else if (duration > MAX_DURATION) {
            invalid.durationMinutes = true;
            messages.push(`Длительность не должна превышать ${MAX_DURATION} минут`);
        }

        if (!formData.releaseDate) {
            invalid.releaseDate = true;
            messages.push('Укажите дату релиза');
        } else if (formData.releaseDate < MIN_DATE) {
            invalid.releaseDate = true;
            messages.push('Дата не может быть раньше 1895 года');
        } else if (formData.releaseDate > new Date().toISOString().substring(0, 10)) {
            invalid.releaseDate = true;
            messages.push('Дата не может быть в будущем');
        }

        if (messages.length === 0) return null;
        return { invalid, msg: messages.join('; ') };
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');

        const result = validate();
        if (result) {
            setInvalidFields(result.invalid);
            setMessage('❌ ' + result.msg);
            return;
        }
        setInvalidFields({});

        const submitData = {
            ...formData,
            durationMinutes: parseInt(formData.durationMinutes, 10),
            releaseDate: formData.releaseDate + 'T00:00:00'
        };

        try {
            await onSubmit(submitData);
        } catch (err) {
            const data = err.response?.data;
            let msg = 'Ошибка сохранения';
            let invalid = {};

            if (typeof data === 'string') {
                msg = data;
            } else if (data && typeof data === 'object') {
                const src = data.errors || data;

                Object.entries(src).forEach(([field, value]) => {
                    if (typeof value === 'string') {
                        invalid[field] = true;
                    }
                });

                if (data.message) {
                    msg = data.message;
                } else {
                    const values = Object.values(src).filter(v => typeof v === 'string');
                    if (values.length) msg = values.join('; ');
                }
            }

            setInvalidFields(invalid);
            setMessage('❌ ' + msg);
        }
    };

    const inputClass = (field) =>
        `${styles.input} ${invalidFields[field] ? styles.inputError : ''}`;

    return (
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <h2 className={styles.title}>
                    {movie ? 'Редактировать фильм' : 'Добавить фильм'}
                </h2>

                {message && (
                    <div className={`${styles.message} ${message.includes('❌') ? styles.error : styles.success}`}>
                        {message}
                        <button
                            type="button"
                            className={styles.closeMessage}
                            onClick={() => setMessage('')}
                        >
                            ×
                        </button>
                    </div>
                )}

                <form onSubmit={handleSubmit} className={styles.form} noValidate>
                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Название фильма</label>
                        <input
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            placeholder="Введите название"
                            className={inputClass('title')}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Жанр</label>
                        <input
                            type="text"
                            name="genre"
                            value={formData.genre}
                            onChange={handleChange}
                            placeholder="Введите жанр"
                            className={inputClass('genre')}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Длительность (минуты)</label>
                        <input
                            type="number"
                            name="durationMinutes"
                            value={formData.durationMinutes}
                            onChange={handleChange}
                            placeholder="120"
                            min="1"
                            max={MAX_DURATION}
                            className={inputClass('durationMinutes')}
                        />
                        {!invalidFields.durationMinutes && formData.durationMinutes && (
                            <div className={styles.fieldHint}>
                                От 1 до {MAX_DURATION} минут
                            </div>
                        )}
                    </div>

                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Дата релиза</label>
                        <input
                            type="date"
                            name="releaseDate"
                            value={formData.releaseDate}
                            onChange={handleChange}
                            min={MIN_DATE}
                            max={new Date().toISOString().substring(0, 10)}
                            className={inputClass('releaseDate')}
                        />
                    </div>

                    <div className={styles.buttonGroup}>
                        <button type="submit" className={styles.submitButton} disabled={loading}>
                            {loading ? 'Загрузка...' : (movie ? 'Сохранить' : 'Создать')}
                        </button>
                        <button type="button" onClick={onCancel} className={styles.cancelButton}>
                            Отмена
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default MovieForm;