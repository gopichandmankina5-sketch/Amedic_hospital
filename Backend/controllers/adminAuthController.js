const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');
const ErrorHandler = require('../utils/errorhadler');
const catchAsyncError = require('../middleware/catchAsyncError');

const JWT_SECRET = process.env.SECRET || process.env.JWT_SECRET || 'dev_secret';
const JWT_EXPIRES = process.env.JWT_EXPIRES || '1h';

function signAdmin(admin) {
  return jwt.sign({ id: admin._id, role: 'admin' }, JWT_SECRET, { expiresIn: JWT_EXPIRES });
}

exports.signup = catchAsyncError(async (req, res, next) => {
  let { name, email, password } = req.body;
  if (!name || !email || !password) return next(new ErrorHandler('All fields required', 400));
  email = email.toLowerCase().trim();
  const existing = await Admin.findOne({ email });
  if (existing) return next(new ErrorHandler('Admin already exists', 409));
  const admin = await Admin.create({ name: name.trim(), email, password });
  res.status(201).json({ message: 'Admin registered successfully', admin: admin.toSafeObject() });
});

exports.login = catchAsyncError(async (req, res, next) => {
  let { email, password } = req.body;
  if (!email || !password) return next(new ErrorHandler('Email and password required', 400));
  email = email.toLowerCase().trim();
  const admin = await Admin.findOne({ email });
  if (!admin) return next(new ErrorHandler('Admin not found', 404));
  const valid = await admin.comparePassword(password);
  if (!valid) return next(new ErrorHandler('Invalid credentials', 401));
  const token = signAdmin(admin);
  res.json({ message: 'Login successful', token, admin: admin.toSafeObject() });
});