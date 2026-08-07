const jwt = require('jsonwebtoken');
require('dotenv').config();

// Ensures token exists and decoded role === 'admin'
module.exports.adminAuth = (req, res, next) => {
  const bearer = req.headers.authorization && req.headers.authorization.startsWith('Bearer ')
    ? req.headers.authorization.split(' ')[1]
    : null;
  const token = req.cookies?.accesstoken || bearer;
  if (!token) {
    return res.status(401).json({ message: 'Authentication required' });
  }
  jwt.verify(token, process.env.SECRET || process.env.JWT_SECRET, (err, decoded) => {
    if (err) return res.status(401).json({ message: 'Invalid or expired token' });
    if (decoded.role !== 'admin') return res.status(403).json({ message: 'Admin access only' });
    req.admin_id = decoded.id;
    next();
  });
};
