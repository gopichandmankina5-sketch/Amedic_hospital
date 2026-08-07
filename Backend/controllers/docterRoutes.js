const express = require("express");
// const router = express.Router();
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
require("dotenv").config();

const DocterModel = require("../model/docterModel");
const catchAsyncError = require("../middleware/catchAsyncError");
const ErrorHandler = require("../utils/errorhadler");
const e = require("express");

// Use env secret for JWT. Falls back to a dev default.
const JWT_SECRET = process.env.SECRET || process.env.JWT_SECRET || "dev_secret";
const JWT_EXPIRES = process.env.JWT_EXPIRES || "1h";
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || "10", 10);

const { auth } = require("../middleware/auth");

function signDoctorToken(doctor) {
  return jwt.sign(
    { id: doctor._id, role: "doctor" },
    JWT_SECRET,
    { expiresIn: JWT_EXPIRES }
  );
}


const doctorRouter = express.Router();
// ──────────────────────────────────────
// SIGNUP
// ──────────────────────────────────────
doctorRouter.post("/signup", catchAsyncError(async (req, res, next) => {
  let { name, email, password } = req.body;

  if (!name || !email || !password) {
    return next(new ErrorHandler("All fields (name, email, password) are required", 400));
  }
  email = email.toLowerCase().trim();

  const existingUser = await DocterModel.findOne({ email });
  if (existingUser) return next(new ErrorHandler("Doctor already exists", 409));

  if (password.length < 6) {
    return next(new ErrorHandler("Password must be at least 6 characters", 400));
  }

  const hashedPassword = await bcrypt.hash(password, BCRYPT_ROUNDS);
  const newDoctor = new DocterModel({ name: name.trim(), email, password: hashedPassword });
  await newDoctor.save();

  const token = signDoctorToken(newDoctor);
  res.cookie("accesstoken", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: process.env.NODE_ENV === "production" ? "none" : "lax",
    maxAge: 60 * 60 * 1000
  });

  res.status(201).json({
    message: "Doctor registered successfully",
    token,
    doctor: newDoctor.toSafeObject()
  });
}));

// ──────────────────────────────────────
// LOGIN
// ──────────────────────────────────────
doctorRouter.post("/login", catchAsyncError(async (req, res, next) => {
  let { email, password } = req.body;
  if (!email || !password) return next(new ErrorHandler("Email and password required", 400));
  email = email.toLowerCase().trim();

  const doctor = await DocterModel.findOne({ email });
  if (!doctor) return next(new ErrorHandler("Doctor not found", 404));

  const isPasswordValid = await bcrypt.compare(password, doctor.password);
  if (!isPasswordValid) return next(new ErrorHandler("Invalid credentials", 401));

  const token = signDoctorToken(doctor);
  res.cookie("accesstoken", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: process.env.NODE_ENV === "production" ? "none" : "lax",
    maxAge: 60 * 60 * 1000
  });

  res.status(200).json({
    message: "Login successful",
    token,
    doctor: doctor.toSafeObject()
  });
}));

// Logout route
doctorRouter.post("/logout", (req, res) => {
  res.clearCookie("accesstoken", {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: process.env.NODE_ENV === "production" ? "none" : "lax"
  });
  res.status(200).json({ message: "Logged out" });
});

// Authenticated doctor profile
doctorRouter.get("/me", auth, catchAsyncError(async (req, res, next) => {
  if (req.user_role !== "doctor") {
    return next(new ErrorHandler("Forbidden: doctor access only", 403));
  }
  const doctor = await DocterModel.findById(req.user_id);
  if (!doctor) return next(new ErrorHandler("Doctor not found", 404));
  res.json({ doctor: doctor.toSafeObject() });
}));


doctorRouter.get('/appointments/doctors', async (req, res) => {
  try {
    const doctors = await DocterModel.find({}, { name: 1 }); // return only name and _id
    res.status(200).json(doctors);
  } catch (error) {
    console.error("Error fetching doctors:", error);
    res.status(500).json({ message: 'Server error fetching doctors' });
  }
});

module.exports = doctorRouter;
