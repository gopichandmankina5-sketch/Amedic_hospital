import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import {
  FaCalendarCheck,
  FaUserMd,
  FaHospitalUser,
  FaHistory,
  FaUserCircle,
  FaSignOutAlt,
  FaAddressCard,
} from "react-icons/fa";
import { api } from "../api";
import MDButton from './ui/MDButton';

const Home = () => {
  const navigate = useNavigate();
  const [showAppointments, setShowAppointments] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem("token");

  const decodeJWT = (token) => {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      return JSON.parse(jsonPayload);
    } catch (err) {
      console.error("Invalid token", err);
      return null;
    }
  };

  const toggleAppointments = async () => {
    const shouldShow = !showAppointments;
    setShowAppointments(shouldShow);
    setProfileMenuOpen(false);

    if (!shouldShow) return;

    const decoded = decodeJWT(token);
    const patientId = decoded?.id || decoded?._id;

    if (!patientId) return;

    try {
      setLoading(true);
      const response = await api.get(
        `/appointmentsbook/patient/${patientId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setAppointments(response.data);
    } catch (error) {
      console.error("Failed to fetch appointments", error);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    setShowAppointments(false);
    setProfileMenuOpen(false);
    navigate("/login");
  };

  const handleProfileClick = () => {
    setProfileMenuOpen(!profileMenuOpen);
  };

  const goToProfile = () => {
    setProfileMenuOpen(false);
    navigate("/profile");
  };

  return (
    <motion.div className="md-app-wrapper" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.6 }}>
      {/* Top Nav */}
      <div className="w-full md-card" style={{ position: 'sticky', top: 0, zIndex: 40 }}>
        <div className="max-w-6xl mx-auto px-6 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="md-title">AmedicK</div>
            <span className="md-chip cursor-default">Home</span>
          </div>
          <div className="flex items-center gap-2">
            <MDButton variant="text" onClick={() => navigate('/quick-appointment')}>Quick Appointment</MDButton>
            {token ? (
              <div className="relative">
                <button onClick={handleProfileClick}>
                  <FaUserCircle className="text-3xl text-teal-700 hover:text-teal-800 transition" />
                </button>
                {profileMenuOpen && (
                  <motion.div className="absolute right-0 mt-2 md-menu" initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
                    <button onClick={goToProfile} className="md-menu-item">
                      <FaAddressCard /> <span>Create/View Profile</span>
                    </button>
                    <button onClick={toggleAppointments} className="md-menu-item">
                      <FaHistory /> <span>Previous Appointments</span>
                    </button>
                    <button onClick={logout} className="md-menu-item" style={{ color: 'var(--md-error)' }}>
                      <FaSignOutAlt /> <span>Logout</span>
                    </button>
                  </motion.div>
                )}
              </div>
            ) : (
              <MDButton onClick={() => navigate('/login')}>Login</MDButton>
            )}
          </div>
        </div>
      </div>

      {/* Hero */}
      <div className="px-6 py-10">
        <div className="max-w-6xl mx-auto md-card fade-in">
          <div className="grid md:grid-cols-2 gap-6 items-center">
            <div>
              <motion.h1 className="md-headline mb-2" initial={{ y: -20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.1 }}>
                Your health, one tap away
              </motion.h1>
              <motion.p className="md-subtitle mb-lg" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.2 }}>
                Book appointments with trusted doctors quickly and securely.
              </motion.p>
              <div className="md-actions">
                <MDButton onClick={() => navigate('/quick-appointment')}>Book Now</MDButton>
                {token ? (
                  <MDButton variant="outlined" onClick={goToProfile}>My Profile</MDButton>
                ) : (
                  <MDButton variant="outlined" onClick={() => navigate('/signup')}>Create Account</MDButton>
                )}
              </div>
            </div>
            <div className="md-card-flat">
              <div className="md-title mb-sm">Why AmedicK?</div>
              <ul className="text-sm opacity-90 list-disc pl-5 space-y-1">
                <li>Instant booking—no waiting in lines</li>
                <li>Verified, experienced doctors</li>
                <li>Reminders and appointment history</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Feature Cards */}
      <div className="md-grid md-grid-3 max-w-6xl mx-auto mb-12 px-6">
        <motion.div whileHover={{ scale: 1.02 }} onClick={() => navigate('/quick-appointment')} className="md-card text-center cursor-pointer">
          <FaCalendarCheck className="text-4xl mx-auto mb-4 text-teal-600" />
          <h2 className="font-semibold text-lg">Quick Appointments</h2>
          <p className="text-gray-600 text-sm mt-2">Book instantly, no queues.</p>
        </motion.div>
        <motion.div whileHover={{ scale: 1.02 }} className="md-card text-center">
          <FaUserMd className="text-4xl mx-auto mb-4 text-teal-600" />
          <h2 className="font-semibold text-lg">Expert Doctors</h2>
          <p className="text-gray-600 text-sm mt-2">Certified and experienced.</p>
        </motion.div>
        <motion.div whileHover={{ scale: 1.02 }} className="md-card text-center">
          <FaHospitalUser className="text-4xl mx-auto mb-4 text-teal-600" />
          <h2 className="font-semibold text-lg">24/7 Help</h2>
          <p className="text-gray-600 text-sm mt-2">Always available for you.</p>
        </motion.div>
      </div>

      {/* Previous Appointments Button */}
      {token && (
        <div className="text-center mb-12 px-6">
          <MDButton onClick={toggleAppointments}>🕒 Previous Appointments</MDButton>
        </div>
      )}

      {/* Appointments Section */}
      <AnimatePresence>
        {showAppointments && (
          <motion.div
            className="max-w-4xl mx-auto md-card px-6"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 30 }}
            transition={{ duration: 0.4 }}
          >
            <h2 className="md-title mb-sm">Your Previous Appointments</h2>

            {loading ? (
              <p className="text-gray-500 text-center">Loading appointments...</p>
            ) : appointments.length === 0 ? (
              <p className="text-gray-500 italic text-center">No appointments to show.</p>
            ) : (
              <ul className="space-y-4">
                {appointments.map((appt) => (
                  <li key={appt._id} className="md-card-flat">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="text-lg font-semibold text-teal-700">
                          {appt.doctorId?.name || 'Doctor'}
                        </h3>
                        <p className="text-sm text-gray-600">
                          {appt.doctorId?.specialization || 'Specialist'} | {appt.date} @ {appt.time}
                        </p>
                      </div>
                      <span className={`md-chip status-${(appt.status||'').toLowerCase()}`}>{appt.status}</span>
                    </div>
                    {appt.symptoms && (
                      <p className="text-gray-700 mt-2">
                        <span className="font-medium">Symptom:</span> {appt.symptoms}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default Home;
