const Cart = require('../models/Cart');
const Order = require('../models/Order');

const SESSION_ID = 'default';

const placeOrder = async (_req, res, next) => {
  try {
    const cart = await Cart.findOne({ sessionId: SESSION_ID }).populate('items.product');

    if (!cart || cart.items.length === 0) {
      return res.status(400).json({ success: false, message: 'Cart is empty' });
    }

    const orderItems = cart.items.map((item) => ({
      product: item.product._id,
      name: item.product.name,
      price: item.product.price,
      quantity: item.quantity,
      subtotal: item.product.price * item.quantity,
    }));

    const totalAmount = orderItems.reduce((total, item) => total + item.subtotal, 0);

    const order = await Order.create({
      items: orderItems,
      totalAmount,
    });

    cart.items = [];
    await cart.save();

    return res.status(201).json({
      success: true,
      message: 'Order placed successfully',
      data: {
        orderId: order._id,
        totalAmount: order.totalAmount,
        status: order.status,
        createdAt: order.createdAt,
      },
    });
  } catch (error) {
    return next(error);
  }
};

module.exports = {
  placeOrder,
};
