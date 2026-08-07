// controllers/bookAppointment.js
const express = require("express");
const Appointment = require("../model/AppointmentModel");
const DoctorModel = require("../model/docterModel");
const AppointmentRouter = express.Router();

AppointmentRouter.post("/Appointment", async (req, res, next) => {
  try {
    const { doctorId, patientId, date, time } = req.body;

    // Check if slot is already booked
    const exists = await Appointment.findOne({ doctorId, date, time });
    if (exists) {
      return res.status(400).json({ message: "Time slot already booked" });
    }

    const appointment = await Appointment.create({
      doctorId,
      patientId,
      date,
      time,
      status: "booked"
    });

    res.status(201).json({ message: "Appointment booked", appointment });
  } catch (err) {
    next(err);
  }
});


// controllers/getAppointments.js
AppointmentRouter.get("/patient/:patientId", async (req, res) => {
  try {
    const appointments = await Appointment.find({ patientId: req.params.patientId })
       .populate("doctorId")
  .exec();

    res.json(appointments);
  } catch (err) {
    res.status(500).json({ message: "Error fetching appointments", error: err.message });
  }
});


// ✅ NEW - Fetches appointments for a doctor
AppointmentRouter.get("/doctor/:doctorId", async (req, res) => {
  try {
    const appointments = await Appointment.find({ doctorId: req.params.doctorId })
      .populate("patientId", "name email phone") // optional: populate patient info
      .sort({ date: 1, time: 1 });

    res.json(appointments);
  } catch (err) {
    res.status(500).json({ message: "Error fetching doctor's appointments", error: err.message });
  }
});






module.exports =  AppointmentRouter ;
