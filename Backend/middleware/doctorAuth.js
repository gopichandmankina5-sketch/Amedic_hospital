const jwt = require('jsonwebtoken');

module.exports = function doctorAuth(req, res, next) {
  const token = req.headers.authorization && req.headers.authorization.startsWith('Bearer ')
    ? req.headers.authorization.split(' ')[1]
    : null;
  if (!token) return res.status(401).json({ message: 'Authentication required' });
  jwt.verify(token, process.env.SECRET || process.env.JWT_SECRET || 'dev_secret', (err, decoded) => {
    if (err) return res.status(401).json({ message: 'Invalid or expired token' });
    if (decoded.role !== 'doctor') return res.status(403).json({ message: 'Doctor access only' });
    req.doctor = decoded;
    next();
  });
}
