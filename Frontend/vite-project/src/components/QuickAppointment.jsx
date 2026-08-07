import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { FaCalendarPlus, FaArrowLeft } from "react-icons/fa";
import { api } from "../api";

const QuickAppointment = () => {
  const navigate = useNavigate();
  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState("");
  const [patientName, setPatientName] = useState("");
  const [patientEmail, setPatientEmail] = useState("");
  const [date, setDate] = useState("");
  const [availableSlots, setAvailableSlots] = useState([]);
  const [timeSlot, setTimeSlot] = useState("");
  const [symptoms, setSymptoms] = useState("");
  const [suggestion, setSuggestion] = useState("");
  const [message, setMessage] = useState("");

  // Decode JWT from localStorage
  const decodeJWT = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      ).join(''));
      return JSON.parse(jsonPayload);
    } catch (err) {
      console.error("Token decode error", err);
      return null;
    }
  };

  // Fetch doctors on load
  useEffect(() => {
    // Auth guard: require logged-in user (token) or doctorToken
    const token = localStorage.getItem("token") || localStorage.getItem("doctorToken");
    if (!token) {
      alert("Please login first.");
      navigate("/login");
      return;
    }

    api.get("/doctor/appointments/doctors")
      .then((res) => setDoctors(res.data))
      .catch((err) => console.error("Failed to load doctors", err));
  }, []);

  // Fetch slots on doctor/date change
  useEffect(() => {
    if (doctorId && date) {
      api.get(`/available/appointments/slots?doctorId=${doctorId}&date=${date}`)
        .then((res) => setAvailableSlots(res.data))
        .catch((err) => console.error("Failed to load slots", err));
    } else {
      setAvailableSlots([]);
      setTimeSlot("");
    }
  }, [doctorId, date]);

  // LLM autocomplete for symptoms input
  useEffect(() => {
    if (!symptoms || symptoms.trim().length < 3) {
      setSuggestion("");
      return;
    }
    const timeout = setTimeout(async () => {
      try {
        const res = await api.post('/api/ai/autocomplete', { text: symptoms });
        setSuggestion(res.data?.suggestion || "");
      } catch (err) {
        setSuggestion("");
      }
    }, 400);
    return () => clearTimeout(timeout);
  }, [symptoms]);

  const acceptSuggestion = () => {
    if (!suggestion) return;
    setSymptoms(suggestion);
    setSuggestion("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!doctorId || !date || !timeSlot) {
      return alert("Please fill all required fields!");
    }

    const token = localStorage.getItem("token");
    if (!token) {
      return alert("Please log in to book an appointment.");
    }

    const decoded = decodeJWT(token);
    const patientId = decoded?.id || decoded?._id;

    if (!patientId) {
      return alert("Invalid or expired token. Please login again.");
    }

    try {
      const res = await api.post(
        "/appointmentsbook/Appointment",
        {
          doctorId,
          patientId,
          date,
          time: timeSlot
        },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setMessage("✅ Appointment booked successfully!");
      setDoctorId("");
      setPatientName("");
      setPatientEmail("");
      setDate("");
      setTimeSlot("");
      setSymptoms("");
      setAvailableSlots([]);
    } catch (err) {
      console.error("Booking error", err.response?.data || err.message);
     setMessage(`❌ Failed to book appointment. ${err.response?.data?.message || err.message}`);

    }
  };

  return (
    <motion.div className="min-h-screen bg-gradient-to-br from-blue-100 to-teal-100 px-6 py-10"
      initial={{ opacity: 0 }} animate={{ opacity: 1 }}
    >
      <button
        onClick={() => navigate("/")}
        className="flex items-center text-teal-800 font-semibold mb-6 hover:underline"
      >
        <FaArrowLeft className="mr-2" /> Back to Home
      </button>

      <motion.h1
        className="text-4xl font-bold text-center text-teal-800 mb-8"
        initial={{ y: -30, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
      >
        Book a Quick Appointment
      </motion.h1>

      {message && (
        <div className="max-w-xl mx-auto mb-4 text-center text-lg font-medium text-green-700">
          {message}
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        className="max-w-xl mx-auto bg-white p-8 rounded-2xl shadow-xl border border-teal-100 space-y-6"
      >
        <div>
          <label className="block font-semibold text-gray-700 mb-2">
            Select Doctor <span className="text-red-500">*</span>
          </label>
          <select
            value={doctorId}
            onChange={(e) => setDoctorId(e.target.value)}
            className="w-full border border-gray-300 rounded-lg p-3"
          >
            <option value="">-- Choose Doctor --</option>
            {doctors.map((doc) => (
              <option key={doc._id} value={doc._id}>
                {doc.name}
              </option>
            ))}
          </select>
        </div>

        {/* <div>
          <label className="block font-semibold text-gray-700 mb-2">
            Your Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={patientName}
            onChange={(e) => setPatientName(e.target.value)}
            className="w-full border border-gray-300 rounded-lg p-3"
            placeholder="Enter your name"
          />
        </div> */}

        {/* <div>
          <label className="block font-semibold text-gray-700 mb-2">
            Your Email <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            value={patientEmail}
            onChange={(e) => setPatientEmail(e.target.value)}
            className="w-full border border-gray-300 rounded-lg p-3"
            placeholder="Enter your email"
          />
        </div> */}

        <div className="flex gap-4">
          <div className="flex-1">
            <label className="block font-semibold text-gray-700 mb-2">
              Date <span className="text-red-500">*</span>
            </label>
            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full border border-gray-300 rounded-lg p-3"
            />
          </div>

          <div className="flex-1">
            <label className="block font-semibold text-gray-700 mb-2">
              Time Slot <span className="text-red-500">*</span>
            </label>
            <select
              value={timeSlot}
              onChange={(e) => setTimeSlot(e.target.value)}
              className="w-full border border-gray-300 rounded-lg p-3"
              disabled={!availableSlots.length}
            >
              <option value="">-- Choose Time Slot --</option>
              {availableSlots.map((slot, index) => (
                <option key={index} value={slot}>
                  {slot}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block font-semibold text-gray-700 mb-2">
            Symptoms (optional)
          </label>
          <textarea
            value={symptoms}
            onChange={(e) => setSymptoms(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Tab" && suggestion) {
                e.preventDefault();
                acceptSuggestion();
              }
            }}
            rows={4}
            className="w-full border border-gray-300 rounded-lg p-3 resize-none"
            placeholder="Describe your symptoms"
          ></textarea>
          {suggestion && (
            <div className="text-sm text-gray-600 mt-2 flex items-center gap-3">
              <span>Suggested: {suggestion}</span>
              <button type="button" className="text-teal-700 underline" onClick={acceptSuggestion}>Use</button>
            </div>
          )}
        </div>

        <motion.button
          type="submit"
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          className="w-full bg-teal-600 text-white py-3 rounded-lg font-semibold shadow-md hover:bg-teal-700 transition"
        >
          <FaCalendarPlus className="inline mr-2" />
          Book Appointment
        </motion.button>
      </form>
    </motion.div>
  );
};

export default QuickAppointment;
