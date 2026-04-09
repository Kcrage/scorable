function Checkout({ hasItems, onCheckout, ordering }) {
  return (
    <div className="checkout-box">
      <p>Review your cart and place your order.</p>
      <button onClick={onCheckout} disabled={!hasItems || ordering}>
        {ordering ? 'Placing Order...' : 'Place Order'}
      </button>
    </div>
  );
}

export default Checkout;
