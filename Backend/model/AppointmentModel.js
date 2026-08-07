const mongoose = require("mongoose");

const appointmentSchema = mongoose.Schema({
  doctorId: { type: mongoose.Schema.Types.ObjectId, ref: "Doctor", required: true },
  patientId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
  date: { type: String, required: true }, // "2025-08-05"
  time: { type: String, required: true }, // "09:30"
  status: { type: String, enum: ["booked", "accepted", "cancelled", "completed"], default: "booked" }
});

module.exports = mongoose.model("Appointment", appointmentSchema);
