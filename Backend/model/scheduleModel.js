const mongoose = require("mongoose");

const scheduleSchema = mongoose.Schema({
  doctorId: { type: mongoose.Schema.Types.ObjectId, ref: "Doctor", required: true },
  date: { type: String, required: true }, // e.g., "2025-08-05"
  slots: [String] // e.g., ["09:00", "09:30", "10:00"]
});

module.exports = mongoose.model("Schedule", scheduleSchema);
