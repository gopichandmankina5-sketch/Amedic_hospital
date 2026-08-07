const express = require('express');
const router = express.Router();
const DocterModel = require('../model/docterModel');
const Appointment = require('../model/AppointmentModel');
const doctorAuth = require('../middleware/doctorAuth');
const { upload } = require('../middleware/multer');
const { sendMail } = require('../utils/mail');

// Helper: get weekday from date string YYYY-MM-DD
function getWeekday(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.getDay(); // 0-6
}

// Profile: Get
router.get('/profile', doctorAuth, async (req, res) => {
  const doc = await DocterModel.findById(req.doctor.id).select('-password');
  if (!doc) return res.status(404).json({ message: 'Doctor not found' });
  res.json({ doctor: doc });
});

// Profile: Update (supports basic fields and profilePhoto upload)
router.patch('/profile', doctorAuth, upload.single('profilePhoto'), async (req, res) => {
  const allowed = ['name', 'phone', 'specialization', 'experience', 'clinic'];
  const updates = {};
  for (const k of allowed) {
    if (req.body[k] !== undefined) {
      if (k === 'clinic' && typeof req.body[k] === 'string') {
        try { updates[k] = JSON.parse(req.body[k]); } catch { updates[k] = req.body[k]; }
      } else {
        updates[k] = req.body[k];
      }
    }
  }
  if (req.file) updates.profilePhoto = req.file.filename;
  const doc = await DocterModel.findByIdAndUpdate(req.doctor.id, updates, { new: true }).select('-password');
  res.json({ message: 'Profile updated', doctor: doc });
});

// Availability: Get
router.get('/availability', doctorAuth, async (req, res) => {
  const doc = await DocterModel.findById(req.doctor.id).select('availability');
  res.json({ availability: doc?.availability || [] });
});

// Availability: Upsert day slots { day: 0-6, slots: ["09:00","10:00"] }
router.post('/availability', doctorAuth, async (req, res) => {
  const { day, slots } = req.body;
  if (day === undefined || !Array.isArray(slots)) return res.status(400).json({ message: 'day and slots required' });
  const doc = await DocterModel.findById(req.doctor.id);
  if (!doc) return res.status(404).json({ message: 'Doctor not found' });
  if (!Array.isArray(doc.availability)) doc.availability = [];
  const idx = doc.availability.findIndex(d => d.day === Number(day));
  if (idx >= 0) {
    doc.availability[idx].slots = slots;
  } else {
    doc.availability.push({ day: Number(day), slots });
  }
  await doc.save();
  res.json({ message: 'Availability saved', availability: doc.availability });
});

// Availability: Delete a slot or whole day
router.delete('/availability/:day', doctorAuth, async (req, res) => {
  const day = Number(req.params.day);
  const { slot } = req.query;
  const doc = await DocterModel.findById(req.doctor.id);
  if (!doc) return res.status(404).json({ message: 'Doctor not found' });
  const idx = (doc.availability || []).findIndex(d => d.day === day);
  if (idx < 0) return res.status(404).json({ message: 'Day not found' });
  if (slot) {
    doc.availability[idx].slots = doc.availability[idx].slots.filter(s => s !== slot);
  } else {
    doc.availability.splice(idx, 1);
  }
  await doc.save();
  res.json({ message: 'Availability updated', availability: doc.availability });
});

// Available slots for a given date using doctor's availability minus booked
router.get('/available-slots', doctorAuth, async (req, res) => {
  const { date } = req.query;
  if (!date) return res.status(400).json({ message: 'date required' });
  const doc = await DocterModel.findById(req.doctor.id).select('availability');
  if (!doc) return res.status(404).json({ message: 'Doctor not found' });
  const weekday = getWeekday(date);
  const dayEntry = (doc.availability || []).find(d => d.day === weekday);
  const allowed = dayEntry?.slots || [];
  const booked = (await Appointment.find({ doctorId: req.doctor.id, date })).map(a => a.time);
  const available = allowed.filter(s => !booked.includes(s));
  res.json({ date, slots: available });
});

// Dashboard summary
router.get('/dashboard', doctorAuth, async (req, res) => {
  const todayStr = new Date().toISOString().slice(0,10);
  const [today, upcoming] = await Promise.all([
    Appointment.find({ doctorId: req.doctor.id, date: todayStr }).sort({ time: 1 }),
    Appointment.find({ doctorId: req.doctor.id, date: { $gt: todayStr } }).sort({ date: 1, time: 1 }).limit(10)
  ]);
  res.json({ todayCount: today.length, today, upcoming });
});

// Appointments list
router.get('/appointments', doctorAuth, async (req, res) => {
  const status = req.query.status; // optional filter
  const query = { doctorId: req.doctor.id };
  if (status) query.status = status;
  const appts = await Appointment.find(query).sort({ date: 1, time: 1 });
  res.json({ count: appts.length, appointments: appts });
});

// Update appointment status: accept | cancel | complete
router.patch('/appointment/:id/status', doctorAuth, async (req, res) => {
  const { id } = req.params; const { status } = req.body;
  const allowed = ['accepted','cancelled','completed'];
  if (!allowed.includes(status)) return res.status(400).json({ message: 'Invalid status' });
  const appt = await Appointment.findOne({ _id: id, doctorId: req.doctor.id });
  if (!appt) return res.status(404).json({ message: 'Appointment not found' });
  appt.status = status; await appt.save();
  res.json({ message: 'Status updated', appointment: appt });
});

// Delete appointment (hard delete)
router.delete('/appointment/:id', doctorAuth, async (req, res) => {
  const { id } = req.params;
  const deleted = await Appointment.findOneAndDelete({ _id: id, doctorId: req.doctor.id });
  if (!deleted) return res.status(404).json({ message: 'Appointment not found' });
  res.json({ message: 'Appointment deleted', appointment: deleted });
});

module.exports = router;
