const Product = require('../models/Product');

const getProducts = async (_req, res, next) => {
  try {
    const products = await Product.find().sort({ createdAt: 1 });

    return res.status(200).json({
      success: true,
      data: products,
    });
  } catch (error) {
    return next(error);
  }
};

module.exports = {
  getProducts,
};
