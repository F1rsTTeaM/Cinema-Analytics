import React, { useState, useEffect } from 'react';
import { useSales } from '../../hooks/useSales';
import styles from './SalesPage.module.css';

const MAX_COUNT = 10;
const MIN_COUNT = 1;

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
  const [invalidFields, setInvalidFields] = useState({});

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        setMessage('');
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [message, setMessage]);

  const handleProductChange = (e) => {
    setSelectedProductId(e.target.value);
    setInvalidFields(prev => {
      const next = { ...prev };
      delete next.product;
      return next;
    });
  };

  const handleQuantityChange = (e) => {
    setQuantity(e.target.value);
    setInvalidFields(prev => {
      const next = { ...prev };
      delete next.quantity;
      return next;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const invalid = {};
    const errors = [];

    if (!selectedProductId) {
      invalid.product = true;
      errors.push('Выберите товар');
    }

    const qty = parseInt(quantity, 10);
    if (Number.isNaN(qty) || qty < MIN_COUNT) {
      invalid.quantity = true;
      errors.push('Количество должно быть больше 0');
    } else if (qty > MAX_COUNT) {
      invalid.quantity = true;
      errors.push(`За одну продажу можно продать не более ${MAX_COUNT} единиц товара`);
    }

    if (Object.keys(invalid).length > 0) {
      setInvalidFields(invalid);
      setMessage('❌ ' + errors.join('; '));
      return;
    }

    setInvalidFields({});
    setFormLoading(true);

    try {
      await createSale(parseInt(selectedProductId, 10), qty);
      setSelectedProductId('');
      setQuantity(1);
    } catch (err) {
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

  const selectedProduct = products.find(p => p.id === parseInt(selectedProductId, 10));

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

          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.inputGroup}>
              <label className={styles.label}>Выберите товар</label>
              <select
                value={selectedProductId}
                onChange={handleProductChange}
                className={`${styles.select} ${invalidFields.product ? styles.inputError : ''}`}
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
              <div className={`${styles.quantityControl} ${invalidFields.quantity ? styles.inputError : ''}`}>
                <button
                  type="button"
                  onClick={() => setQuantity(q => Math.max(MIN_COUNT, (parseInt(q, 10) || MIN_COUNT) - 1))}
                  className={styles.qtyButton}
                >
                  −
                </button>
                <input
                  type="number"
                  value={quantity}
                  onChange={handleQuantityChange}
                  min={MIN_COUNT}
                  max={MAX_COUNT}
                  className={styles.qtyInput}
                />
                <button
                  type="button"
                  onClick={() => setQuantity(q => Math.min(MAX_COUNT, (parseInt(q, 10) || MIN_COUNT) + 1))}
                  className={styles.qtyButton}
                >
                  +
                </button>
              </div>
            </div>

            {selectedProduct && (
              <div className={styles.totalPreview}>
                Итого: <strong>{formatPrice(selectedProduct.price * (parseInt(quantity, 10) || 0))}</strong>
              </div>
            )}

            <button
              type="submit"
              className={styles.submitButton}
              disabled={formLoading}
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