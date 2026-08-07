import React from "react";
import Loginpage from "./components/Loginpage";
import Signup from "./components/Signup";
import Home from "./components/Home";
import Profile from "./components/Profile";
import GoogleSuccess from "./components/GoogleSuccess";
import QuickAppointment from "./components/QuickAppointment";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import OtpVerifyPage from "./components/otp";
import DoctorLoginPage from "./components/DoctorLoginPage";
import DoctorRegistrationForm from "./components/DoctorRegistrationForm";
import AdminPendingDoctorsPage from "./components/AdminPendingDoctorsPage";
import AdminSignup from "./components/AdminSignup";
import AdminLogin from "./components/AdminLogin";
import DoctorHome from "./components/DoctorHome";
import DoctorProfile from "./components/DoctorProfile";
import DoctorAvailability from "./components/DoctorAvailability";
import DoctorAppointments from "./components/DoctorAppointments";
import DoctorDashboard from "./components/DoctorDashboard";
// import AvailableSlots from "./components/test";

const App = () => {
  return (
    <>
      <BrowserRouter>
        {/* <Navbar/> */}
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Loginpage />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/otpVerify" element={<OtpVerifyPage />} />
          <Route path="/google-success" element={<GoogleSuccess />}></Route>
          <Route path="/profile" element={<Profile />} />
          <Route path="/quick-appointment" element={<QuickAppointment />} />
          <Route path="/doctor/login" element={<DoctorLoginPage />} />
          <Route path="/doctor/register" element={<DoctorRegistrationForm />} />
          <Route path="/doctor/home" element={<DoctorHome />} />
          <Route path="/doctor/profile" element={<DoctorProfile />} />
          <Route path="/docter/profile" element={<Navigate to="/doctor/profile" replace />} />
          <Route path="/doctor/availability" element={<DoctorAvailability />} />
          <Route path="/doctor/appointments" element={<DoctorAppointments />} />
          <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
          <Route path="/admin/doctors/pending" element={<AdminPendingDoctorsPage />} />
          <Route path="/admin/signup" element={<AdminSignup />} />
          <Route path="/admin/login" element={<AdminLogin />} />
          {/* <Route path="/appointments/:doctorId" element={<AvailableSlots />} /> */}

        </Routes>
      </BrowserRouter>
    </>
  );
};

export default App;
