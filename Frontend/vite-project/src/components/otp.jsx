import { useState, useEffect } from "react";
import { api } from "../api";
import { useNavigate, useLocation } from "react-router-dom";
import { motion } from "framer-motion";

const OtpVerifyPage = () => {
  const [otp, setOtp] = useState(Array(6).fill(""));
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email;

  useEffect(() => {
    if (!email) {
      setMessage("No email found. Please go back to the signup page.");
    }
    document.getElementById("otp-input-0")?.focus();
  }, [email]);

  const handleChange = (e, index) => {
    const value = e.target.value.replace(/\D/g, ""); // Allow only digits
    const newOtp = [...otp];
    newOtp[index] = value.slice(-1); // Limit to 1 digit
    setOtp(newOtp);

    if (value && index < otp.length - 1) {
      document.getElementById(`otp-input-${index + 1}`)?.focus();
    }
  };

  const handleVerify = async (otpValue) => {
    if (!email) {
      setMessage("Email is missing");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const response = await api.post(
        "/user/verify-otp",
        { email, otp: otpValue }
      );
      setMessage(response.data.message);
      navigate("/login");
    } catch (error) {
      setMessage(
        error.response?.data?.message || "Invalid OTP, please try again"
      );
      setOtp(Array(6).fill(""));
      document.getElementById("otp-input-0")?.focus();
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.6 }}
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        height: "100vh",
        width: "100vw",
        background: "linear-gradient(to right, #e0f7fa, #ffffff)",
      }}
    >
      <motion.div
        initial={{ y: 50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ type: "spring", stiffness: 100 }}
        style={{
          width: "400px",
          backgroundColor: "white",
          borderRadius: "16px",
          boxShadow: "0px 10px 30px rgba(0,0,0,0.2)",
          padding: "40px",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          textAlign: "center",
        }}
      >
        <h2
          style={{
            fontSize: "1.8rem",
            fontWeight: "bold",
            color: "#0f766e",
            marginBottom: "20px",
          }}
        >
          OTP Verification
        </h2>

        <div style={{ display: "flex", gap: "12px", marginBottom: "24px" }}>
          {otp.map((digit, index) => (
            <motion.input
              key={index}
              type="text"
              id={`otp-input-${index}`}
              value={digit}
              onChange={(e) => handleChange(e, index)}
              maxLength="1"
              required
              whileFocus={{ scale: 1.1 }}
              style={{
                width: "48px",
                height: "48px",
                textAlign: "center",
                fontSize: "1.5rem",
                borderRadius: "8px",
                border: "2px solid #94a3b8",
                outline: "none",
                transition: "border-color 0.3s",
              }}
            />
          ))}
        </div>

        {message && (
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            style={{
              color: message.toLowerCase().includes("success")
                ? "green"
                : "red",
              fontWeight: "bold",
              marginBottom: "16px",
            }}
          >
            {message}
          </motion.p>
        )}

        {loading && (
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            style={{ color: "blue", marginBottom: "10px" }}
          >
            Verifying...
          </motion.p>
        )}

        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          disabled={otp.some((d) => d === "") || loading}
          onClick={() => handleVerify(otp.join(""))}
          style={{
            backgroundColor: "#14b8a6",
            color: "white",
            padding: "10px 24px",
            border: "none",
            borderRadius: "8px",
            fontWeight: "600",
            fontSize: "1rem",
            cursor: loading ? "not-allowed" : "pointer",
            boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
          }}
        >
          Verify OTP
        </motion.button>
      </motion.div>
    </motion.div>
  );
};

export default OtpVerifyPage;
