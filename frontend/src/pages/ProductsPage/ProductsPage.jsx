import React, { useState, useEffect } from 'react';
import { useProducts } from '../../hooks/useProducts';
import styles from './ProductsPage.module.css';

const MAX_NAME = 100;
const MIN_NAME = 2;
const MAX_PRICE = 5000;

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
    const [formMessage, setFormMessage] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [formData, setFormData] = useState({ name: '', price: '' });

    useEffect(() => {
        if (formMessage) {
            const timer = setTimeout(() => setFormMessage(''), 3000);
            return () => clearTimeout(timer);
        }
    }, [formMessage]);

    useEffect(() => {
        if (!showForm) {
            setFormErrors({});
            setFormMessage('');
        }
    }, [showForm]);

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
        searchProducts(query);
    };

    const handleRefresh = () => {
        setSearchQuery('');
        if (fetchProducts) fetchProducts();
    };

    const validateForm = () => {
        const invalid = {};
        const messages = [];

        const name = formData.name.trim();
        if (!name) {
            invalid.name = true;
            messages.push('Название товара обязательно');
        } else if (name.length < MIN_NAME) {
            invalid.name = true;
            messages.push(`Название должно содержать минимум ${MIN_NAME} символа`);
        } else if (name.length > MAX_NAME) {
            invalid.name = true;
            messages.push(`Название не должно превышать ${MAX_NAME} символов`);
        } else {
            const existing = products.find(p =>
                p.name.toLowerCase() === name.toLowerCase() &&
                (!editingProduct || p.id !== editingProduct.id)
            );
            if (existing) {
                invalid.name = true;
                messages.push('Товар с таким названием уже существует');
            }
        }

        if (formData.price === '' || formData.price === null) {
            invalid.price = true;
            messages.push('Цена обязательна');
        } else {
            const price = parseFloat(formData.price);
            if (Number.isNaN(price)) {
                invalid.price = true;
                messages.push('Цена должна быть числом');
            } else if (price <= 0) {
                invalid.price = true;
                messages.push('Цена должна быть больше 0');
            } else if (price > MAX_PRICE) {
                invalid.price = true;
                messages.push(`Цена не может превышать ${MAX_PRICE.toLocaleString('ru-RU')} ₽`);
            }
        }

        if (messages.length === 0) return null;
        return { invalid, msg: messages.join('; ') };
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
                price: parseFloat(formData.price)
            };

            if (editingProduct) {
                await updateProduct(editingProduct.id, data);
            } else {
                await createProduct(data);
            }

            handleCancel();
        } catch (err) {
            const data = err.response?.data;
            let msg = 'Ошибка сохранения';
            let invalid = {};

            if (typeof data === 'string') {
                msg = data;
            } else if (data && typeof data === 'object') {
                const src = data.errors || data;
                Object.entries(src).forEach(([field, value]) => {
                    if (typeof value === 'string') invalid[field] = true;
                });

                if (data.message) {
                    msg = data.message;
                } else {
                    const values = Object.values(src).filter(v => typeof v === 'string');
                    if (values.length) msg = values.join('; ');
                }
            }

            setFormErrors(invalid);
            setFormMessage('❌ ' + msg);
        } finally {
            setFormLoading(false);
        }
    };

    const handleEdit = (product) => {
        setEditingProduct(product);
        setFormData({ name: product.name, price: product.price.toString() });
        setFormErrors({});
        setFormMessage('');
        setShowForm(true);
    };

    const handleDelete = async (id, name) => {
        if (window.confirm(`Вы уверены, что хотите удалить товар "${name}"?`)) {
            try {
                await deleteProduct(id);
            } catch (err) {
            }
        }
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditingProduct(null);
        setFormData({ name: '', price: '' });
        setFormErrors({});
        setFormMessage('');
    };

    const formatPrice = (price) =>
        new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(price);

    const inputClass = (field) =>
        `${styles.input} ${formErrors[field] ? styles.inputError : ''}`;

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
                        setFormMessage('');
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
                                <label className={styles.label}>Название товара</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Попкорн большой"
                                    className={inputClass('name')}
                                />
                            </div>

                            <div className={styles.inputGroup}>
                                <label className={styles.label}>Цена (₽)</label>
                                <input
                                    type="number"
                                    name="price"
                                    value={formData.price}
                                    onChange={handleChange}
                                    placeholder="99.99"
                                    min="0.01"
                                    max={MAX_PRICE}
                                    step="0.01"
                                    className={inputClass('price')}
                                />
                            </div>

                            <div className={styles.buttonGroup}>
                                <button type="submit" className={styles.submitButton} disabled={formLoading}>
                                    {formLoading ? 'Сохранение...' : (editingProduct ? 'Сохранить' : 'Создать')}
                                </button>
                                <button type="button" onClick={handleCancel} className={styles.cancelButton}>
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