import React, { useState } from "react";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa6";
import { api } from "../api";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import MDButton from './ui/MDButton';
import GoogleButton from "./GoogleButton";

function Loginpage() {
  const navigate = useNavigate();
  const [hide, setHide] = useState(true);
  const [error, setError] = useState("");
  const [data, setData] = useState({ email: "", password: "" });

  const handleHide = () => setHide((prev) => !prev);

  const handleForm = (e) => {
    setError("");
    setData({ ...data, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    const { email, password } = data;
    if (!email || !password) {
      setError("Please fill in all fields.");
      return;
    }

    try {
      const response = await api.post(
        "/user/login",
        { email, password }
      );
      if (response.data.token) {
        localStorage.setItem("token", response.data.token);
      }
      navigate("/");
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.message ||
        "Something went wrong. Please try again.";
      setError(message);
    }
  };

  const pageVariants = {
    initial: { opacity: 0, scale: 0.9 },
    animate: {
      opacity: 1,
      scale: 1,
      transition: { duration: 0.6, ease: "easeOut" },
    },
    exit: { opacity: 0, scale: 0.7, transition: { duration: 0.3 } },
  };

  const inputVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: (i) => ({
      opacity: 1,
      y: 0,
      transition: { delay: i * 0.2 },
    }),
  };

  const errorVariants = {
    hidden: { x: 0 },
    visible: {
      x: [0, -8, 8, -6, 6, -3, 3, 0],
      transition: { duration: 0.6 },
    },
  };

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
      className="md-app-wrapper items-center justify-center flex"
    >
      <div className="w-full max-w-md md-card fade-in">
        {/* User login only; admin login moved to a separate route */}
        <motion.h1
          initial={{ y: -30, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.5 }}
          className="text-3xl font-extrabold text-center text-teal-700 mb-6"
        >
          Welcome Back 👋
        </motion.h1>

        {error && (
          <motion.div
            variants={errorVariants}
            initial="hidden"
            animate="visible"
            className="md-chip status-rejected mb-4"
          >
            {error}
          </motion.div>
        )}

        <motion.label
          htmlFor="email"
          className="block text-gray-700 font-semibold mb-2"
          variants={inputVariants}
          initial="hidden"
          animate="visible"
          custom={0}
        >
          Email Address
        </motion.label>
        <div className="md-input-group">
          <label htmlFor="email" className="md-label">Email</label>
          <motion.input
            id="email"
            type="email"
            name="email"
            value={data.email}
            onChange={handleForm}
            className="md-input"
            variants={inputVariants}
            initial="hidden"
            animate="visible"
            custom={0.1}
          />
        </div>

        <motion.label
          htmlFor="password"
          className="block text-gray-700 font-semibold mb-2"
          variants={inputVariants}
          initial="hidden"
          animate="visible"
          custom={0.2}
        >
          Password
        </motion.label>
        <motion.div
          className="relative"
          variants={inputVariants}
          initial="hidden"
          animate="visible"
          custom={0.3}
        >
          <div className="md-input-group">
            <label htmlFor="password" className="md-label">Password</label>
            <input
              id="password"
              name="password"
              type={hide ? "password" : "text"}
              value={data.password}
              onChange={handleForm}
              required
              className="md-input"
            />
            <motion.button
              type="button"
              onClick={handleHide}
              whileTap={{ scale: 1.2, rotate: 15 }}
              aria-label="Toggle password visibility"
              className="absolute right-4 top-9 text-teal-700"
              style={{ background: 'transparent' }}
            >
              {hide ? <FaRegEye size={18} /> : <FaRegEyeSlash size={18} />}
            </motion.button>
          </div>
          <motion.button
            type="button"
            onClick={handleHide}
            whileTap={{ scale: 1.2, rotate: 15 }}
            aria-label="Toggle password visibility"
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-teal-600 hover:text-teal-800"
          >
            {hide ? <FaRegEye size={20} /> : <FaRegEyeSlash size={20} />}
          </motion.button>
        </motion.div>

        <MDButton onClick={handleSubmit} className="w-full mb-4">Login</MDButton>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1 }}
          className="text-center"
        >
          <div className="text-gray-700">
            Don’t have an account?{" "}
            <span
              onClick={() => navigate("/signup")}
              className="cursor-pointer font-semibold text-teal-700 hover:text-teal-800"
            >
              Sign up
            </span>
            <GoogleButton/>
          </div>
          <div className="center mt-lg">
            <MDButton variant="outlined" onClick={() => navigate('/doctor/login')}>
              I am a Doctor — Login
            </MDButton>
          </div>
          
        </motion.div>
      </div>
    </motion.div>
  );
}

export default Loginpage;
