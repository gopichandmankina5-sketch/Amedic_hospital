import React, { useState } from "react";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa6";
import { api } from "../api";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import MDButton from './ui/MDButton';

function Signup() {
  const navigate = useNavigate();
  const [hide, setHide] = useState(true);
  const [hided, setHided] = useState(true);
  const [err, setErr] = useState("");

  const [data, setData] = useState({
    name: "",
    email: "",
    password: "",
    confirmpass: "",
  });

  const handleHide = () => setHide(!hide);
  const handleHided = () => setHided(!hided);

  const handleForm = (e) => {
    setData({ ...data, [e.target.name]: e.target.value });
    setErr("");
  };

  const handleSubmit = async () => {
    const { name, email, password, confirmpass } = data;

    if (!name || !email || !password || !confirmpass) {
      setErr("Please fill all fields");
      return;
    }

    if (password !== confirmpass) {
      setErr("Passwords do not match");
      return;
    }

    try {
      const response = await api.post(
        "/user/signup",
        { name, email, password }
      );

      console.log("Successfully registered:", response.data);
      navigate("/otpVerify", { state: { email } });
    } catch (error) {
      console.log(error);
      setErr(error.response?.data?.message || "Signup failed");
    }
  };

  const pageVariants = {
    initial: { opacity: 0, scale: 0.9 },
    animate: { opacity: 1, scale: 1, transition: { duration: 0.6, ease: "easeOut" } },
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
      className="md-app-wrapper flex items-center justify-center"
    >
      <div className="w-full max-w-lg md-card fade-in">
        <motion.h1
          initial={{ y: -30, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.5 }}
          className="text-3xl font-extrabold text-center text-teal-700 mb-6"
        >
          Create Your Account
        </motion.h1>

        {err && (
          <motion.div
            variants={errorVariants}
            initial="hidden"
            animate="visible"
            className="md-chip md-chip-error mb-4"
          >
            {err}
          </motion.div>
        )}

        {["name", "email"].map((field, index) => (
          <motion.div
            key={field}
            custom={index}
            variants={inputVariants}
            initial="hidden"
            animate="visible"
            className="md-input-group"
          >
            <label
              htmlFor={field}
              className="md-label capitalize"
            >
              {field}
            </label>
            <input
              id={field}
              name={field}
              type={field === "email" ? "email" : "text"}
              value={data[field]}
              onChange={handleForm}
              className="md-input"
            />
          </motion.div>
        ))}

        {/* Password */}
        <motion.div
          custom={2}
          variants={inputVariants}
          initial="hidden"
          animate="visible"
          className="md-input-group"
        >
          <label
            htmlFor="password"
            className="md-label"
          >
            Password
          </label>
          <div className="relative">
            <input
              id="password"
              name="password"
              type={hide ? "password" : "text"}
              value={data.password}
              onChange={handleForm}
              className="md-input pr-10"
            />
            <motion.button
              type="button"
              onClick={handleHide}
              whileTap={{ scale: 1.2, rotate: 10 }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-teal-700"
              style={{ background: 'transparent' }}
            >
              {hide ? <FaRegEye size={20} /> : <FaRegEyeSlash size={20} />}
            </motion.button>
          </div>
        </motion.div>

        {/* Confirm Password */}
        <motion.div
          custom={3}
          variants={inputVariants}
          initial="hidden"
          animate="visible"
          className="md-input-group"
        >
          <label
            htmlFor="confirmpass"
            className="md-label"
          >
            Confirm Password
          </label>
          <div className="relative">
            <input
              id="confirmpass"
              name="confirmpass"
              type={hided ? "password" : "text"}
              value={data.confirmpass}
              onChange={handleForm}
              className="md-input pr-10"
            />
            <motion.button
              type="button"
              onClick={handleHided}
              whileTap={{ scale: 1.2, rotate: -10 }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-teal-700"
              style={{ background: 'transparent' }}
            >
              {hided ? <FaRegEye size={20} /> : <FaRegEyeSlash size={20} />}
            </motion.button>
          </div>
        </motion.div>

        {/* Signup button */}
        <MDButton onClick={handleSubmit} className="w-full mb-4">Signup</MDButton>

        {/* Navigate to Doctor Signup */}
        <MDButton onClick={() => navigate("/doctor/register")} className="w-full mb-4" style={{ borderRadius: '28px' }}>I am a Doctor</MDButton>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1.2 }}
          className="text-center"
        >
          <p className="text-gray-700">
            Already have an account?{" "}
            <span
              onClick={() => navigate("/login")}
              className="text-teal-600 cursor-pointer hover:underline font-semibold"
            >
              Login
            </span>
          </p>
        </motion.div>
      </div>
    </motion.div>
  );
}

export default Signup;
