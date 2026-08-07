const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const { upload } = require('../middleware/multer');
const DocterModel = require('../model/docterModel');
const catchAsyncError = require('../middleware/catchAsyncError');
const ErrorHandler = require('../utils/errorhadler');
const { sendMail } = require('../utils/mail');
const jwt = require('jsonwebtoken');
const { adminAuth } = require('../middleware/adminAuth');
require('dotenv').config();

const JWT_SECRET = process.env.SECRET || process.env.JWT_SECRET || 'dev_secret';
const JWT_EXPIRES = process.env.JWT_EXPIRES || '1h';
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '10', 10);

function signDoctorToken(doctor) {
  return jwt.sign({ id: doctor._id, role: 'doctor' }, JWT_SECRET, { expiresIn: JWT_EXPIRES });
}

// Multer fields for doctor registration
const registrationUpload = upload.fields([
  { name: 'medicalRegistrationCertificate', maxCount: 1 },
  { name: 'degreeCertificate', maxCount: 1 },
  { name: 'govtIdProof', maxCount: 1 },
  { name: 'profilePhoto', maxCount: 1 }
]);

// Doctor registration (verificationStatus defaults to pending)
router.post('/register', registrationUpload, catchAsyncError(async (req, res, next) => {
  let { fullName, email, password, phone, specialization, registrationNumber, registrationCouncil, registrationYear } = req.body;
  if (!fullName || !email || !password || !specialization || !registrationNumber) {
    return next(new ErrorHandler('Missing required fields', 400));
  }
  email = email.toLowerCase().trim();
  const existing = await DocterModel.findOne({ email });
  if (existing) return next(new ErrorHandler('Doctor already exists', 409));

  const hashed = await bcrypt.hash(password, BCRYPT_ROUNDS);

  const files = req.files || {};
  const doctor = await DocterModel.create({
    name: fullName.trim(),
    email,
    password: hashed,
    phone: phone?.trim(),
    specialization: specialization?.trim(),
    registrationNumber: registrationNumber?.trim(),
    registrationCouncil: registrationCouncil?.trim(),
    registrationYear: registrationYear ? parseInt(registrationYear, 10) : undefined,
    documents: {
      medicalRegistrationCertificate: files.medicalRegistrationCertificate?.[0]?.filename,
      degreeCertificate: files.degreeCertificate?.[0]?.filename,
      govtIdProof: files.govtIdProof?.[0]?.filename
    },
    profilePhoto: files.profilePhoto?.[0]?.filename,
    verificationStatus: 'pending'
  });

  // Send notification email (SMS omitted - require free service integration if needed)
  try {
    await sendMail({
      email: doctor.email,
      subject: 'AmedicK Doctor Registration Received',
      message: 'Your documents are under review. We will notify you once verified.'
    });
  } catch (e) {
    console.warn('Email send failed (continuing):', e.message);
  }

  res.status(201).json({ message: 'Registration submitted. Await verification.', doctor: doctor.toSafeObject() });
}));

// List pending doctors (admin)
router.get('/admin/doctors/pending', adminAuth, catchAsyncError(async (req, res) => {
  const pending = await DocterModel.find({ verificationStatus: 'pending' }).select('-password');
  res.json({ count: pending.length, doctors: pending });
}));

// Approve doctor
router.patch('/admin/doctor/approve/:id', adminAuth, catchAsyncError(async (req, res, next) => {
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

// Reject doctor (optional reason)
// Reject doctor: now deletes doctor record entirely per new requirement
router.patch('/admin/doctor/reject/:id', adminAuth, catchAsyncError(async (req, res, next) => {
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

// Doctor login only if approved
router.post('/login', catchAsyncError(async (req, res, next) => {
  let { email, password } = req.body;
  if (!email || !password) return next(new ErrorHandler('Email and password required', 400));
  email = email.toLowerCase().trim();
  const doctor = await DocterModel.findOne({ email });
  if (!doctor) return next(new ErrorHandler('Doctor not found', 404));
  if (doctor.verificationStatus !== 'approved') {
    return next(new ErrorHandler('Account not verified yet', 403));
  }
  const valid = await bcrypt.compare(password, doctor.password);
  if (!valid) return next(new ErrorHandler('Invalid credentials', 401));
  const token = signDoctorToken(doctor);
  // Do not set cookies; return token only for Authorization header usage
  res.json({ message: 'Login successful', token, doctor: doctor.toSafeObject() });
}));

module.exports = router;
