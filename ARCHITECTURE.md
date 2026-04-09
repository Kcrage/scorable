# Agent 1 Architecture Contract

## 1) Minimal but Complete Feature List
1. Browse available grocery products.
2. Add products to cart.
3. Remove products from cart.
4. View live cart totals.
5. Place order from cart.
6. Clear cart after successful order placement.

## 2) Database Schema

### Product
- `_id`: ObjectId
- `name`: String (required)
- `description`: String (required)
- `price`: Number (required, min 0)
- `imageUrl`: String (required)
- `category`: String (required)
- `inStock`: Boolean (default true)
- `createdAt`, `updatedAt`: Date

### Cart
- `_id`: ObjectId
- `sessionId`: String (default `default`, unique)
- `items`: Array of:
  - `product`: ObjectId (ref Product, required)
  - `quantity`: Number (required, min 1)
- `createdAt`, `updatedAt`: Date

### Order
- `_id`: ObjectId
- `items`: Array of:
  - `product`: ObjectId (ref Product)
  - `name`: String
  - `price`: Number
  - `quantity`: Number
  - `subtotal`: Number
- `totalAmount`: Number (required, min 0)
- `status`: Enum (`placed`)
- `createdAt`, `updatedAt`: Date

## 3) API Contracts

### `GET /api/products`
- Response 200:
  - `success`: Boolean
  - `data`: Product[]

### `POST /api/cart`
- Request body:
  - `productId`: String (ObjectId)
  - `quantity`: Integer (non-zero; positive add, negative remove)
- Response 200:
  - `success`: Boolean
  - `data`:
    - `id`: String
    - `items`: Array of:
      - `productId`: String
      - `name`: String
      - `price`: Number
      - `imageUrl`: String
      - `quantity`: Number
      - `subtotal`: Number
    - `totalAmount`: Number
- Error responses:
  - 400 invalid input
  - 404 product unavailable

### `GET /api/cart`
- Response 200:
  - `success`: Boolean
  - `data`: Cart summary (same shape as POST /api/cart response data)

### `POST /api/order`
- Request body: none
- Response 201:
  - `success`: Boolean
  - `message`: String
  - `data`:
    - `orderId`: String
    - `totalAmount`: Number
    - `status`: String
    - `createdAt`: Date string
- Error responses:
  - 400 when cart is empty

## 4) Folder Structure

### Backend (MVC)
- `backend/config`: DB connection setup
- `backend/models`: Mongoose schemas
- `backend/controllers`: Request handlers/business logic
- `backend/routes`: Route definitions
- `backend/data`: Seed data
- `backend/server.js`: App bootstrap and middleware wiring

### Frontend
- `frontend/src/components`: Reusable UI components
- `frontend/src/App.jsx`: Main page composition and state orchestration
- `frontend/src/App.css`: Basic styling
- `frontend/src/main.jsx`: React entry point
