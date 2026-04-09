const mongoose = require('mongoose');
const Cart = require('../models/Cart');
const Product = require('../models/Product');

const SESSION_ID = 'default';

const calculateCartTotal = (items) =>
  items.reduce((total, item) => total + item.product.price * item.quantity, 0);

const formatCartResponse = (cart) => ({
  id: cart._id,
  items: cart.items.map((item) => ({
    productId: item.product._id,
    name: item.product.name,
    price: item.product.price,
    imageUrl: item.product.imageUrl,
    quantity: item.quantity,
    subtotal: item.product.price * item.quantity,
  })),
  totalAmount: calculateCartTotal(cart.items),
});

const getOrCreateCart = async () => {
  let cart = await Cart.findOne({ sessionId: SESSION_ID }).populate('items.product');

  if (!cart) {
    cart = await Cart.create({ sessionId: SESSION_ID, items: [] });
    cart = await Cart.findById(cart._id).populate('items.product');
  }

  return cart;
};

const getCart = async (_req, res, next) => {
  try {
    const cart = await getOrCreateCart();

    return res.status(200).json({
      success: true,
      data: formatCartResponse(cart),
    });
  } catch (error) {
    return next(error);
  }
};

const updateCart = async (req, res, next) => {
  try {
    const { productId, quantity } = req.body;

    if (!mongoose.Types.ObjectId.isValid(productId)) {
      return res.status(400).json({ success: false, message: 'Invalid productId' });
    }

    if (!Number.isInteger(quantity) || quantity === 0) {
      return res.status(400).json({ success: false, message: 'Quantity must be a non-zero integer' });
    }

    const product = await Product.findById(productId);
    if (!product || !product.inStock) {
      return res.status(404).json({ success: false, message: 'Product not available' });
    }

    const cart = await getOrCreateCart();
    const itemIndex = cart.items.findIndex((item) => item.product._id.toString() === productId);

    if (itemIndex === -1 && quantity > 0) {
      cart.items.push({ product: product._id, quantity });
    } else if (itemIndex !== -1) {
      const nextQuantity = cart.items[itemIndex].quantity + quantity;

      if (nextQuantity <= 0) {
        cart.items.splice(itemIndex, 1);
      } else {
        cart.items[itemIndex].quantity = nextQuantity;
      }
    }

    await cart.save();
    await cart.populate('items.product');

    return res.status(200).json({
      success: true,
      data: formatCartResponse(cart),
    });
  } catch (error) {
    return next(error);
  }
};

module.exports = {
  getCart,
  updateCart,
};
