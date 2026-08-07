import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FiUser, FiCalendar, FiClock, FiBarChart2 } from "react-icons/fi";
import MDButton from "./ui/MDButton";
import { getDoctorToken, clearDoctorToken } from "../tokenStore";

const DoctorHome = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const token = getDoctorToken();
    if (!token) navigate("/doctor/login");
  }, [navigate]);

  const logout = () => {
    clearDoctorToken();
    navigate("/doctor/login");
  };

  return (
    <div className="bg-gray-100 min-h-screen">
      {/* TOP NAV */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-6xl mx-auto flex items-center justify-between py-4 px-4">
          <h2 className="text-2xl font-bold text-teal-700 tracking-wide">
            AmedicK <span className="text-gray-700 text-lg">• Doctor</span>
          </h2>

          <div className="flex items-center gap-4">
            <MDButton variant="text" onClick={() => navigate("/doctor/profile")}>Profile</MDButton>
            <MDButton variant="text" onClick={() => navigate("/doctor/availability")}>Availability</MDButton>
            <MDButton variant="text" onClick={() => navigate("/doctor/appointments")}>Appointments</MDButton>
            <MDButton variant="text" onClick={() => navigate("/doctor/dashboard")}>Analytics</MDButton>
            <MDButton variant="outlined" onClick={logout}>Logout</MDButton>
          </div>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <div className="max-w-6xl mx-auto p-6 grid gap-6 md:grid-cols-3">

        {/* ------------------------------------ */}
        {/* MAIN DASHBOARD HEADER CARD */}
        {/* ------------------------------------ */}
        <div className="md-card md-col-span-2 p-6">
          <h1 className="text-3xl font-bold text-teal-800 mb-2">Doctor Dashboard</h1>
          <p className="text-gray-600 mb-6">A quick overview of your activities</p>

          <div className="grid gap-5 md:grid-cols-2">

            {/* Profile */}
            <div className="md-card-flat p-5 hover:shadow-lg transition cursor-pointer"
              onClick={() => navigate("/doctor/profile")}>
              <div className="flex items-center gap-3 mb-2">
                <FiUser className="text-teal-600 text-2xl" />
                <h3 className="text-lg font-semibold text-gray-800">Profile</h3>
              </div>
              <p className="text-gray-600 text-sm mb-3">
                Update your personal & professional information.
              </p>
              <MDButton>View / Edit</MDButton>
            </div>

            {/* Availability */}
            <div className="md-card-flat p-5 hover:shadow-lg transition cursor-pointer"
              onClick={() => navigate("/doctor/availability")}>
              <div className="flex items-center gap-3 mb-2">
                <FiClock className="text-teal-600 text-2xl" />
                <h3 className="text-lg font-semibold text-gray-800">Availability</h3>
              </div>
              <p className="text-gray-600 text-sm mb-3">
                Set your weekly time slots for patient appointments.
              </p>
              <MDButton>Manage</MDButton>
            </div>

          </div>
        </div>

        {/* ------------------------------------ */}
        {/* QUICK LINKS */}
        {/* ------------------------------------ */}
        <div className="md-card p-6">
          <h3 className="text-xl font-semibold text-gray-800 mb-3">Quick Links</h3>

          <div className="flex flex-col gap-3">
            <button className="quick-link" onClick={() => navigate("/doctor/appointments")}>
              <FiCalendar className="text-lg" /> Appointments
            </button>

            <button className="quick-link" onClick={() => navigate("/doctor/dashboard")}>
              <FiBarChart2 className="text-lg" /> Analytics
            </button>

            <button className="quick-link" onClick={() => navigate("/")}>
              🏠 Patient Home
            </button>
          </div>
        </div>

        {/* ------------------------------------ */}
        {/* APPOINTMENTS */}
        {/* ------------------------------------ */}
        <div className="md-card p-6 md-col-span-2">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-xl font-semibold text-gray-800">Appointments</h3>
            <MDButton variant="outlined" onClick={() => navigate("/doctor/appointments")}>
              Open
            </MDButton>
          </div>

          <p className="text-gray-600 text-sm">
            View upcoming appointments, accept or cancel patient bookings.
          </p>
        </div>

        {/* ------------------------------------ */}
        {/* ANALYTICS */}
        {/* ------------------------------------ */}
        <div className="md-card p-6">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-xl font-semibold text-gray-800">Analytics</h3>
            <MDButton variant="outlined" onClick={() => navigate("/doctor/dashboard")}>
              View
            </MDButton>
          </div>

          <p className="text-gray-600 text-sm">
            Track your patient flow, performance, and schedule insights.
          </p>
        </div>

      </div>
    </div>
  );
};

export default DoctorHome;
