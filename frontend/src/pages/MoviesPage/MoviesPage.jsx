import React, { useState, useEffect } from 'react';
import { useMovies } from '../../hooks/useMovies';
import MovieForm from '../../components/MovieForm/MovieForm';
import styles from './MoviesPage.module.css';

function MoviesPage() {
    const {
        movies,
        loading,
        message,
        setMessage,
        createMovie,
        updateMovie,
        deleteMovie,
        searchMovies
    } = useMovies();

    const [showForm, setShowForm] = useState(false);
    const [editingMovie, setEditingMovie] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [formLoading, setFormLoading] = useState(false);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage('');
            }, 1500);
            
            return () => clearTimeout(timer);
        }
    }, [message, setMessage]);

    const handleCreate = async (data) => {
        setFormLoading(true);
        try {
            await createMovie(data);
            setShowForm(false);
        } catch (error) {
        } finally {
            setFormLoading(false);
        }
    };

    const handleUpdate = async (data) => {
        setFormLoading(true);
        try {
            await updateMovie(editingMovie.id, data);
            setEditingMovie(null);
            setShowForm(false);
        } catch (error) {
        } finally {
            setFormLoading(false);
        }
    };

    const handleDelete = async (id, title) => {
        if (window.confirm(`Вы уверены, что хотите удалить фильм "${title}"?`)) {
            await deleteMovie(id);
        }
    };

    const handleSearch = (e) => {
        const query = e.target.value;
        setSearchQuery(query);
        searchMovies(query);
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h1 className={styles.title}>Управление фильмами</h1>
                <button
                    className={styles.addButton}
                    onClick={() => {
                        setEditingMovie(null);
                        setShowForm(true);
                    }}
                >
                    + Добавить фильм
                </button>
            </div>

            <div className={styles.toolbar}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={handleSearch}
                    placeholder="Поиск фильмов..."
                    className={styles.searchInput}
                />
                <button
                    className={styles.refreshButton}
                    onClick={() => {
                        searchMovies('');
                        setSearchQuery('');
                    }}
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
            ) : movies.length === 0 ? (
                <div className={styles.emptyState}>
                    <p>Нет фильмов</p>
                    <p className={styles.emptyHint}>Добавьте первый фильм, нажав кнопку "Добавить фильм"</p>
                </div>
            ) : (
                <div className={styles.grid}>
                    {movies.map((movie) => (
                        <div key={movie.id} className={styles.card}>
                            <div className={styles.cardContent}>
                                <div className={styles.cardRow}>
                                    <div className={styles.cardInfo}>
                                        <h3 className={styles.movieTitle}>{movie.title}</h3>
                                        <div className={styles.movieInfo}>
                                            <span className={styles.genre}>{movie.genre}</span>
                                            <span className={styles.duration}>⏱ {movie.durationMinutes} мин</span>
                                        </div>
                                        <div className={styles.movieDate}>
                                            {new Date(movie.releaseDate).toLocaleDateString('ru-RU')}
                                        </div>
                                    </div>
                                    <div className={styles.cardActions}>
                                        <button
                                            className={styles.editButton}
                                            onClick={() => {
                                                setEditingMovie(movie);
                                                setShowForm(true);
                                            }}
                                            title="Редактировать"
                                        >
                                            ✏️
                                        </button>
                                        <button
                                            className={styles.deleteButton}
                                            onClick={() => handleDelete(movie.id, movie.title)}
                                            title="Удалить"
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showForm && (
                <MovieForm
                    movie={editingMovie}
                    onSubmit={editingMovie ? handleUpdate : handleCreate}
                    onCancel={() => {
                        setShowForm(false);
                        setEditingMovie(null);
                    }}
                    loading={formLoading}
                />
            )}
        </div>
    );
}

export default MoviesPage;