// routes/userRoutes.js
const express = require('express');
const getAvailableSlots = express.Router();
const Appointment = require('../model/AppointmentModel');

// Predefined daily time slots
const DAILY_SLOTS = [
  "09:00", "10:00", "11:00",
  "14:00", "15:00", "16:00"
];

// GET /user/appointments/slots?doctorId=xxx&date=YYYY-MM-DD
getAvailableSlots.get('/appointments/slots', async (req, res) => {
  const { doctorId, date } = req.query;

  if (!doctorId || !date) {
    return res.status(400).json({ message: "doctorId and date are required" });
  }

  try {
    // Get all booked slots for this doctor on the given date
    const appointments = await Appointment.find({ doctorId, date });

    const bookedSlots = appointments.map((a) => a.timeSlot);

    // Filter out booked slots
    const availableSlots = DAILY_SLOTS.filter(slot => !bookedSlots.includes(slot));

    res.status(200).json(availableSlots);
  } catch (error) {
    console.error("Error fetching slots:", error);
    res.status(500).json({ message: "Server error" });
  }
});

module.exports = getAvailableSlots;
