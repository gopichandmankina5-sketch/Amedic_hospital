const express = require('express');
const router = express.Router();
const { signup, login } = require('../controllers/adminAuthController');
const verifyAdmin = require('../middleware/verifyAdmin');
const DocterModel = require('../model/docterModel');
const { sendMail } = require('../utils/mail');
const catchAsyncError = require('../middleware/catchAsyncError');
const ErrorHandler = require('../utils/errorhadler');

// Public auth endpoints
router.post('/signup', signup); // POST /api/admin/signup
router.post('/login', login);   // POST /api/admin/login

// Protected doctor verification endpoints (admin only)
router.get('/doctors/pending', verifyAdmin, catchAsyncError(async (req, res) => {
  const pending = await DocterModel.find({ verificationStatus: 'pending' }).select('-password');
  res.json({ count: pending.length, doctors: pending });
}));

router.patch('/doctor/approve/:id', verifyAdmin, catchAsyncError(async (req, res, next) => {
  const { id } = req.params;
  const doctor = await DocterModel.findById(id);
  if (!doctor) return next(new ErrorHandler('Doctor not found', 404));
  doctor.verificationStatus = 'approved';
  doctor.rejectionReason = undefined;
  await doctor.save();
  try {
    await sendMail({
      email: doctor.email,
      subject: 'AmedicK Doctor Account Approved',
      message: `Hello Dr. ${doctor.name},\n\nYour account has been approved. You can now log in and start using the platform.\n\nRegards,\nAmedicK Team`
    });
  } catch (e) {
    console.warn('Approval email failed:', e.message);
  }
  res.json({ message: 'Doctor approved', doctor: doctor.toSafeObject() });
}));

router.patch('/doctor/reject/:id', verifyAdmin, catchAsyncError(async (req, res, next) => {
  const { id } = req.params;
  const { reason } = req.body;
  const doctor = await DocterModel.findById(id);
  if (!doctor) return next(new ErrorHandler('Doctor not found', 404));
  const email = doctor.email;
  const name = doctor.name;
  try {
    await sendMail({
      email,
      subject: 'AmedicK Doctor Application Rejected',
      message: `Hello Dr. ${name},\n\nWe regret to inform you that your application has been rejected. Reason: ${reason || 'No reason provided.'}\n\nYou may re-apply with corrected documents if applicable.\n\nRegards,\nAmedicK Team`
    });
  } catch (e) {
    console.warn('Rejection email failed:', e.message);
  }
  await doctor.deleteOne();
  res.json({ message: 'Doctor rejected and deleted', reason: reason || null });
}));

module.exports = router;