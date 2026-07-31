import React, { useState, useEffect } from 'react';
import { useProducts } from '../../hooks/useProducts';
import styles from './ProductsPage.module.css';

function ProductsPage() {
    const {
        products,
        loading,
        message,
        setMessage,
        fetchProducts,
        createProduct,
        updateProduct,
        deleteProduct,
        searchProducts
    } = useProducts();

    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [formLoading, setFormLoading] = useState(false);
    const [formErrors, setFormErrors] = useState({});
    const [formSuccess, setFormSuccess] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        price: ''
    });

    useEffect(() => {
        if (showForm) {
            setFormErrors({});
            setFormSuccess('');
        }
    }, [showForm]);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage('');
            }, 1500);

            return () => clearTimeout(timer);
        }
    }, [message, setMessage]);

    const validateForm = () => {
        const errors = {};
        let isValid = true;

        if (!formData.name.trim()) {
            errors.name = 'Название товара обязательно';
            isValid = false;
        } else if (formData.name.trim().length < 2) {
            errors.name = 'Название должно содержать минимум 2 символа';
            isValid = false;
        } else if (formData.name.trim().length > 100) {
            errors.name = 'Название не должно превышать 100 символов';
            isValid = false;
        }

        if (!formData.price) {
            errors.price = 'Цена обязательна';
            isValid = false;
        } else if (parseFloat(formData.price) < 0) {
            errors.price = 'Цена не может быть отрицательной';
            isValid = false;
        } else if (parseFloat(formData.price) > 100000) {
            errors.price = 'Цена не может превышать 100 000 ₽';
            isValid = false;
        }

        if (!editingProduct) {
            const existing = products.find(
                p => p.name.toLowerCase() === formData.name.trim().toLowerCase()
            );
            if (existing) {
                errors.name = 'Товар с таким названием уже существует';
                isValid = false;
            }
        } else {
            const existing = products.find(
                p => p.name.toLowerCase() === formData.name.trim().toLowerCase() && p.id !== editingProduct.id
            );
            if (existing) {
                errors.name = 'Товар с таким названием уже существует';
                isValid = false;
            }
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
        searchProducts(query);
    };

    const handleRefresh = () => {
        setSearchQuery('');
        if (fetchProducts) {
            fetchProducts();
        }
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
                price: parseFloat(formData.price)
            };

            if (editingProduct) {
                await updateProduct(editingProduct.id, data);
                setFormSuccess('✔️ Товар успешно обновлён');
            } else {
                await createProduct(data);
                setFormSuccess('✔️ Товар успешно создан');
            }

            setFormData({ name: '', price: '' });

            setShowForm(false);
            setEditingProduct(null);
            setFormData({ name: '', price: '' });
            setFormSuccess('');
            setFormErrors({});
        } catch (error) {
        } finally {
            setFormLoading(false);
        }
    };

    const handleEdit = (product) => {
        setEditingProduct(product);
        setFormData({
            name: product.name,
            price: product.price.toString()
        });
        setFormErrors({});
        setFormSuccess('');
        setShowForm(true);
    };

    const handleDelete = async (id, name) => {
        if (window.confirm(`Вы уверены, что хотите удалить товар "${name}"?`)) {
            await deleteProduct(id);
        }
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditingProduct(null);
        setFormData({ name: '', price: '' });
        setFormErrors({});
        setFormSuccess('');
        setMessage('');
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(price);
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h1 className={styles.title}>Управление товарами</h1>
                <button
                    className={styles.addButton}
                    onClick={() => {
                        setEditingProduct(null);
                        setFormData({ name: '', price: '' });
                        setFormErrors({});
                        setFormSuccess('');
                        setShowForm(true);
                    }}
                >
                    + Добавить товар
                </button>
            </div>

            <div className={styles.toolbar}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={handleSearch}
                    placeholder="Поиск товаров..."
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
            ) : products.length === 0 ? (
                <div className={styles.emptyState}>
                    <p>Нет товаров</p>
                    <p className={styles.emptyHint}>Добавьте первый товар, нажав кнопку "Добавить товар"</p>
                </div>
            ) : (
                <div className={styles.grid}>
                    {products.map((product) => (
                        <div key={product.id} className={styles.card}>
                            <div className={styles.cardContent}>
                                <div className={styles.cardRow}>
                                    <div className={styles.cardInfo}>
                                        <h3 className={styles.productName}>{product.name}</h3>
                                        <p className={styles.productPrice}>{formatPrice(product.price)}</p>
                                    </div>
                                    <div className={styles.cardActions}>
                                        <button
                                            className={styles.editButton}
                                            onClick={() => handleEdit(product)}
                                            title="Редактировать"
                                        >
                                            ✏️
                                        </button>
                                        <button
                                            className={styles.deleteButton}
                                            onClick={() => handleDelete(product.id, product.name)}
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
                <div className={styles.overlay}>
                    <div className={styles.modal}>
                        <h2 className={styles.modalTitle}>
                            {editingProduct ? 'Редактировать товар' : 'Добавить товар'}
                        </h2>

                        {formSuccess && (
                            <div className={styles.formSuccess}>
                                {formSuccess}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className={styles.form}>
                            <div className={styles.inputGroup}>
                                <label className={styles.label}>Название товара</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Попкорн большой"
                                    required
                                    className={`${styles.input} ${formErrors.name ? styles.inputError : ''}`}
                                />
                                {formErrors.name && (
                                    <div className={styles.fieldError}>{formErrors.name}</div>
                                )}
                                {!formErrors.name && formData.name && (
                                    <div className={styles.fieldHint}>
                                        {formData.name.length < 2 ? 'Минимум 2 символа' : '✔️ Название подходит'}
                                    </div>
                                )}
                            </div>

                            <div className={styles.inputGroup}>
                                <label className={styles.label}>Цена (₽)</label>
                                <input
                                    type="number"
                                    name="price"
                                    value={formData.price}
                                    onChange={handleChange}
                                    placeholder="99.99"
                                    required
                                    min="0"
                                    step="0.01"
                                    className={`${styles.input} ${formErrors.price ? styles.inputError : ''}`}
                                />
                                {formErrors.price && (
                                    <div className={styles.fieldError}>{formErrors.price}</div>
                                )}
                                {!formErrors.price && formData.price && (
                                    <div className={styles.fieldHint}>
                                        Цена: {formatPrice(parseFloat(formData.price))}
                                    </div>
                                )}
                            </div>

                            <div className={styles.buttonGroup}>
                                <button type="submit" className={styles.submitButton} disabled={formLoading}>
                                    {formLoading ? 'Сохранение...' : (editingProduct ? 'Сохранить' : 'Создать')}
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

export default ProductsPage;