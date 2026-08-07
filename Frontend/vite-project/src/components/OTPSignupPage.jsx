import React, { useState } from "react";
import { api } from "../api";
import { useNavigate } from "react-router-dom";

function OTPSignupPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmpass, setConfirmpass] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSendOTP = async () => {
    setError("");
    setSuccess("");

    if (!name || !email || !password || !confirmpass) {
      setError("Please fill all fields");
      return;
    }
    if (password !== confirmpass) {
      setError("Passwords do not match");
      return;
    }

    try {
      const res = await api.post("/user/send-otp", {
        name,
        email,
        password,
      });

      setSuccess(res.data.message || "OTP sent to your email");
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to send OTP");
    }
  };

  const handleVerifyOTP = async () => {
    setError("");
    setSuccess("");

    if (!otp) {
      setError("Please enter OTP");
      return;
    }

    try {
      const res = await api.post("/user/verify-otp", {
        email,
        otp,
      });

      setSuccess(res.data.message || "OTP verified successfully");
      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } catch (err) {
      setError(err.response?.data?.message || "Invalid OTP");
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-blue-50 to-teal-100">
      <div className="w-full sm:w-[400px] bg-white p-8 rounded-2xl shadow-xl border border-blue-100">
        <h1 className="text-3xl font-extrabold text-center text-teal-700 mb-6">
          {step === 1 ? "Sign Up - Step 1" : "Verify OTP"}
        </h1>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 p-2 rounded-md mb-4 text-center">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 p-2 rounded-md mb-4 text-center">
            {success}
          </div>
        )}

        {step === 1 ? (
          <>
            <input
              type="text"
              placeholder="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 bg-blue-50"
            />
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 bg-blue-50"
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 bg-blue-50"
            />
            <input
              type="password"
              placeholder="Confirm Password"
              value={confirmpass}
              onChange={(e) => setConfirmpass(e.target.value)}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 bg-blue-50"
            />
            <button
              onClick={handleSendOTP}
              className="w-full p-3 bg-teal-600 text-white font-semibold rounded-lg hover:bg-teal-700 transition shadow-md"
            >
              Send OTP
            </button>
          </>
        ) : (
          <>
            <input
              type="text"
              placeholder="Enter OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 bg-blue-50"
            />
            <button
              onClick={handleVerifyOTP}
              className="w-full p-3 bg-teal-600 text-white font-semibold rounded-lg hover:bg-teal-700 transition shadow-md"
            >
              Verify OTP
            </button>
          </>
        )}
      </div>
    </div>
  );
}

export default OTPSignupPage;
