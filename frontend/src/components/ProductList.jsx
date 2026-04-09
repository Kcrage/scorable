function ProductList({ products, onAddToCart, loading }) {
  if (loading) {
    return <p className="empty-state">Loading products...</p>;
  }

  if (products.length === 0) {
    return <p className="empty-state">No products available.</p>;
  }

  return (
    <div className="product-grid">
      {products.map((product) => (
        <article key={product._id} className="card">
          <img src={product.imageUrl} alt={product.name} className="card-image" />
          <div className="card-body">
            <h3>{product.name}</h3>
            <p>{product.description}</p>
            <div className="card-meta">
              <span>${product.price.toFixed(2)}</span>
              <button onClick={() => onAddToCart(product._id)}>Add to Cart</button>
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}

export default ProductList;
