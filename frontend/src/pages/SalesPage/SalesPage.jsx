import React, { useState, useEffect } from 'react';
import { useSales } from '../../hooks/useSales';
import styles from './SalesPage.module.css';

function SalesPage() {
  const {
    sales,
    products,
    loading,
    message,
    stats,
    setMessage,
    createSale
  } = useSales();

  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [formLoading, setFormLoading] = useState(false);
  const [formSuccess, setFormSuccess] = useState('');
  const [formError, setFormError] = useState('');

  useEffect(() => {
    if (formSuccess) {
      const timer = setTimeout(() => setFormSuccess(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [formSuccess]);

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        setMessage('');
      }, 1500);

      return () => clearTimeout(timer);
    }
  }, [message, setMessage]);

  useEffect(() => {
  console.log('Sales data:', sales);
}, [sales]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    if (!selectedProductId) {
      setFormError('Выберите товар');
      return;
    }

    if (quantity < 1) {
      setFormError('Количество должно быть больше 0');
      return;
    }

    setFormLoading(true);

    try {
      await createSale(parseInt(selectedProductId), parseInt(quantity));
      setFormSuccess('✔️ Продажа успешно зарегистрирована');
      setSelectedProductId('');
      setQuantity(1);
    } catch (error) {
      setFormError('❌ Ошибка при создании продажи');
    } finally {
      setFormLoading(false);
    }
  };

  const formatPrice = (price) => {
    const numPrice = typeof price === 'string' ? parseFloat(price) : price;
    
    if (numPrice === null || numPrice === undefined || isNaN(numPrice)) {
      return '—';
    }
    
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(numPrice);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    try {
      return new Date(dateString).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return '—';
    }
  };

  const selectedProduct = products.find(p => p.id === parseInt(selectedProductId));

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Продажа продуктов</h1>

      {message && (
        <div className={`${styles.message} ${message.includes('✔️') ? styles.success : styles.error}`}>
          {message}
          <button className={styles.closeMessage} onClick={() => setMessage('')}>×</button>
        </div>
      )}

      <div className={styles.stats}>
        <div className={styles.statCard}>
          <div className={styles.statInfo}>
            <div className={styles.statValue}>{stats.totalSales}</div>
            <div className={styles.statLabel}>Всего продаж</div>
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statInfo}>
            <div className={styles.statValue}>{formatPrice(stats.totalRevenue)}</div>
            <div className={styles.statLabel}>Общая выручка</div>
          </div>
        </div>
      </div>

      <div className={styles.content}>
        <div className={styles.saleForm}>
          <h2>Новая продажа</h2>

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.inputGroup}>
              <label className={styles.label}>Выберите товар</label>
              <select
                value={selectedProductId}
                onChange={(e) => setSelectedProductId(e.target.value)}
                className={styles.select}
                required
              >
                <option value="">-- Выберите товар --</option>
                {products.map(product => (
                  <option key={product.id} value={product.id}>
                    {product.name} — {formatPrice(product.price)}
                  </option>
                ))}
              </select>
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>Количество</label>
              <div className={styles.quantityControl}>
                <button
                  type="button"
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className={styles.qtyButton}
                >
                  −
                </button>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  min="1"
                  className={styles.qtyInput}
                />
                <button
                  type="button"
                  onClick={() => setQuantity(quantity + 1)}
                  className={styles.qtyButton}
                >
                  +
                </button>
              </div>
            </div>

            {selectedProduct && (
              <div className={styles.totalPreview}>
                Итого: <strong>{formatPrice(selectedProduct.price * quantity)}</strong>
              </div>
            )}

            <button 
              type="submit" 
              className={styles.submitButton}
              disabled={formLoading || !selectedProductId}
            >
              {formLoading ? 'Обработка...' : 'Продать'}
            </button>
          </form>
        </div>

        <div className={styles.salesHistory}>
          <h2>История продаж</h2>
          
          {loading ? (
            <div className={styles.loading}>Загрузка...</div>
          ) : sales.length === 0 ? (
            <div className={styles.emptyState}>
              <p>Нет продаж</p>
              <p className={styles.emptyHint}>Зарегистрируйте первую продажу</p>
            </div>
          ) : (
            <div className={styles.tableWrapper}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Товар</th>
                    <th>Кол-во</th>
                    <th>Цена</th>
                    <th>Сумма</th>
                    <th>Дата</th>
                  </tr>
                </thead>
                <tbody>
                  {sales.map((sale) => (
                    <tr key={sale.id}>
                      <td>{sale.productName || 'Товар удалён'}</td>
                      <td>{sale.count}</td>
                      <td>{formatPrice(sale.productPrice)}</td>
                      <td>{formatPrice(sale.totalAmount)}</td>
                      <td>{formatDate(sale.saleDate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default SalesPage;