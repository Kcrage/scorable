import { useEffect, useState } from 'react';
import axios from 'axios';
import ProductList from './components/ProductList';
import Cart from './components/Cart';
import Checkout from './components/Checkout';
import './App.css';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://127.0.0.1:5000/api';

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [ordering, setOrdering] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const fetchProducts = async () => {
    try {
      setLoadingProducts(true);
      const response = await axios.get(`${API_BASE_URL}/products`);
      setProducts(response.data.data);
    } catch {
      setError('Failed to load products. Please try again.');
    } finally {
      setLoadingProducts(false);
    }
  };

  const fetchCart = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/cart`);
      setCart(response.data.data);
    } catch {
      setError('Failed to load cart. Please refresh the page.');
    }
  };

  useEffect(() => {
    fetchProducts();
    fetchCart();
  }, []);

  const updateCartItem = async (productId, quantity) => {
    setError('');
    setMessage('');

    try {
      const response = await axios.post(`${API_BASE_URL}/cart`, { productId, quantity });
      setCart(response.data.data);
    } catch (requestError) {
      const apiMessage = requestError?.response?.data?.message;
      setError(apiMessage || 'Failed to update cart.');
    }
  };

  const placeOrder = async () => {
    setError('');
    setMessage('');

    try {
      setOrdering(true);
      const response = await axios.post(`${API_BASE_URL}/order`);
      setMessage(`Order placed successfully! Order ID: ${response.data.data.orderId}`);
      await fetchCart();
    } catch (requestError) {
      const apiMessage = requestError?.response?.data?.message;
      setError(apiMessage || 'Failed to place order.');
    } finally {
      setOrdering(false);
    }
  };

  return (
    <main className="container">
      <h1>Grocery App</h1>

      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      <section>
        <h2>Products</h2>
        <ProductList products={products} loading={loadingProducts} onAddToCart={(id) => updateCartItem(id, 1)} />
      </section>

      <section>
        <h2>Cart</h2>
        <Cart cart={cart} onAdd={(id) => updateCartItem(id, 1)} onRemove={(id) => updateCartItem(id, -1)} />
      </section>

      <section>
        <h2>Checkout</h2>
        <Checkout hasItems={cart.items.length > 0} ordering={ordering} onCheckout={placeOrder} />
      </section>
    </main>
  );
}

export default App;
