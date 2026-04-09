function Cart({ cart, onAdd, onRemove }) {
  if (!cart || cart.items.length === 0) {
    return <p className="empty-state">Your cart is empty.</p>;
  }

  return (
    <div className="cart-list">
      {cart.items.map((item) => (
        <div key={item.productId} className="cart-item">
          <div>
            <h4>{item.name}</h4>
            <p>
              ${item.price.toFixed(2)} × {item.quantity}
            </p>
          </div>
          <div className="cart-actions">
            <button onClick={() => onRemove(item.productId)}>-</button>
            <span>{item.quantity}</span>
            <button onClick={() => onAdd(item.productId)}>+</button>
          </div>
        </div>
      ))}
      <div className="cart-total">Total: ${cart.totalAmount.toFixed(2)}</div>
    </div>
  );
}

export default Cart;
