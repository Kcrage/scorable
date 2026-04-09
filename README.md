# Grocery App (MVP)

A production-ready MVP grocery web application with:
- React frontend
- Node.js + Express backend
- MongoDB + Mongoose database
- REST API

## Project Structure

```
scorable/
├── backend/
│   ├── config/
│   │   └── db.js
│   ├── controllers/
│   │   ├── cartController.js
│   │   ├── orderController.js
│   │   └── productController.js
│   ├── data/
│   │   └── defaultProducts.js
│   ├── models/
│   │   ├── Cart.js
│   │   ├── Order.js
│   │   └── Product.js
│   ├── routes/
│   │   ├── cartRoutes.js
│   │   ├── orderRoutes.js
│   │   └── productRoutes.js
│   ├── .env.example
│   ├── package.json
│   └── server.js
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Cart.jsx
│   │   │   ├── Checkout.jsx
│   │   │   └── ProductList.jsx
│   │   ├── App.css
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   └── package.json
└── README.md
```

## API Endpoints

### `GET /api/products`
Returns all products.

### `POST /api/cart`
Add or remove cart items.

Request body:
```json
{
  "productId": "<product-object-id>",
  "quantity": 1
}
```
- Use positive `quantity` to add.
- Use negative `quantity` to remove.

### `GET /api/cart`
Returns current cart and total amount.

### `POST /api/order`
Places an order using current cart and clears cart.

## Local Setup

### 1) Database setup
- Ensure MongoDB is running locally on default port `27017`.
- Default URI used by backend: `mongodb://127.0.0.1:27017/grocery_app`

### 2) Backend setup
```bash
cd /home/runner/work/scorable/scorable/backend
cp .env.example .env
npm install
npm run dev
```
Backend runs at: `http://localhost:5000`

### 3) Frontend setup
```bash
cd /home/runner/work/scorable/scorable/frontend
npm install
npm run dev
```
Frontend runs at: `http://localhost:5173`

If needed, configure API URL in frontend:
```bash
VITE_API_URL=http://localhost:5000/api
```
