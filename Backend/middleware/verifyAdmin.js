const jwt = require('jsonwebtoken');
const ErrorHandler = require('../utils/errorhadler');

module.exports = function verifyAdmin(req, res, next) {
  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) {
    return next(new ErrorHandler('Authorization header missing', 401));
  }
  const token = auth.split(' ')[1];
  jwt.verify(token, process.env.SECRET || process.env.JWT_SECRET || 'dev_secret', (err, decoded) => {
    if (err) return next(new ErrorHandler('Invalid or expired token', 401));
    if (decoded.role !== 'admin') return next(new ErrorHandler('Forbidden: admin only', 403));
    req.admin = decoded;
    next();
  });
};