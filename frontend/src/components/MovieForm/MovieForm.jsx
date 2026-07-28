import React, { useState, useEffect } from 'react';
import styles from './MovieForm.module.css';

function MovieForm({ movie, onSubmit, onCancel, loading }) {
    const [formData, setFormData] = useState({
        title: '',
        genre: '',
        durationMinutes: '',
        releaseDate: ''
    });

    useEffect(() => {
        if (movie) {
            setFormData({
                title: movie.title || '',
                genre: movie.genre || '',
                durationMinutes: movie.durationMinutes || '',
                releaseDate: movie.releaseDate ? movie.releaseDate.substring(0, 10) : ''
            });
        }
    }, [movie]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const submitData = {
            ...formData,
            durationMinutes: parseInt(formData.durationMinutes),
            releaseDate: formData.releaseDate + 'T00:00:00'
        };

        onSubmit(submitData);
    };

    return (
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <h2 className={styles.title}>
                    {movie ? 'Редактировать фильм' : 'Добавить фильм'}
                </h2>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Название фильма</label>
                        <input
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            placeholder="Введите название"
                            required
                            className={styles.input}
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
                            required
                            className={styles.input}
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
                            required
                            min="1"
                            className={styles.input}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Дата релиза</label>
                        <input
                            type="date"
                            name="releaseDate"
                            value={formData.releaseDate}
                            onChange={handleChange}
                            required
                            className={styles.input}
                        />
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            type="submit"
                            className={styles.submitButton}
                            disabled={loading}
                        >
                            {loading ? 'Загрузка...' : (movie ? 'Сохранить' : 'Создать')}
                        </button>
                        <button
                            type="button"
                            onClick={onCancel}
                            className={styles.cancelButton}
                        >
                            Отмена
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default MovieForm;