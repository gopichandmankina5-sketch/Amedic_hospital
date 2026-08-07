import React, { useState } from "react";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa6";
import { api } from "../api";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";

function DoctorSignupPage() {
  const navigate = useNavigate();
  const [hide, setHide] = useState(true);
  const [error, setError] = useState("");
  const [data, setData] = useState({ name: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);

  const handleHide = () => setHide((prev) => !prev);
  const handleForm = (e) => {
    setError("");
    setData({ ...data, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    const { name, email, password } = data;
    if (!name || !email || !password) {
      setError("Please fill in all fields.");
      return;
    }
    if (password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    try {
      setLoading(true);
      const response = await api.post(
        "/doctor/signup",
        { name, email, password }
      );

      if (response.data.token) {
        localStorage.setItem("doctorToken", response.data.token);
      }

      navigate("/");
    } catch (err) {
      const message =
        err.response?.data?.message || err.message || "Signup failed. Try again.";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const pageVariants = {
    initial: { opacity: 0, scale: 0.9 },
    animate: { opacity: 1, scale: 1, transition: { duration: 0.6, ease: "easeOut" } },
    exit: { opacity: 0, scale: 0.7, transition: { duration: 0.3 } }
  };

  const inputVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: (i) => ({
      opacity: 1,
      y: 0,
      transition: { delay: i * 0.2 }
    })
  };

  const errorVariants = {
    hidden: { x: 0 },
    visible: {
      x: [0, -8, 8, -6, 6, -3, 3, 0],
      transition: { duration: 0.6 }
    }
  };

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
      className="flex justify-center items-center min-h-screen bg-gradient-to-br from-blue-50 to-teal-100"
    >
      <div className="w-full sm:w-[400px] bg-white p-8 rounded-2xl shadow-xl border border-blue-100">
        <motion.h1
          initial={{ y: -30, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.5 }}
          className="text-3xl font-extrabold text-center text-teal-700 mb-6"
        >
          Doctor Signup 🩺
        </motion.h1>

        {error && (
          <motion.div
            variants={errorVariants}
            initial="hidden"
            animate="visible"
            className="bg-red-100 border border-red-400 text-red-700 p-2 rounded-md mb-4 text-center"
          >
            {error}
          </motion.div>
        )}

        {['name','email'].map((field,i)=>(
          <motion.div
            key={field}
            custom={i}
            variants={inputVariants}
            initial="hidden"
            animate="visible"
          >
            <label htmlFor={field} className="block text-gray-700 font-semibold mb-2 capitalize">{field}</label>
            <input
              id={field}
              name={field}
              type={field === 'email' ? 'email' : 'text'}
              value={data[field]}
              onChange={handleForm}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-teal-500 bg-blue-50"
            />
          </motion.div>
        ))}

        <motion.div
          custom={2}
          variants={inputVariants}
          initial="hidden"
          animate="visible"
        >
          <label htmlFor="password" className="block text-gray-700 font-semibold mb-2">Password</label>
          <div className="relative">
            <input
              id="password"
              name="password"
              type={hide ? "password" : "text"}
              value={data.password}
              onChange={handleForm}
              className="w-full p-3 border border-teal-300 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-teal-500 bg-blue-50"
            />
            <motion.button
              type="button"
              onClick={handleHide}
              whileTap={{ scale: 1.2, rotate: 10 }}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-teal-600 hover:text-teal-800"
            >
              {hide ? <FaRegEye size={20} /> : <FaRegEyeSlash size={20} />}
            </motion.button>
          </div>
        </motion.div>

        <motion.button
          type="button"
          onClick={handleSubmit}
          disabled={loading}
          whileHover={{ scale: !loading ? 1.03 : 1 }}
          whileTap={{ scale: 0.95 }}
          className="w-full p-3 bg-teal-600 text-white font-semibold rounded-lg mb-4 hover:bg-teal-700 transition shadow-md disabled:bg-teal-300"
        >
          {loading ? 'Signing up...' : 'Signup as Doctor'}
        </motion.button>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1 }}
          className="text-center"
        >
          <div className="text-gray-700">
            Already a doctor?{' '}<span onClick={()=>navigate('/doctor/login')} className="text-teal-600 cursor-pointer hover:underline font-semibold">Login</span>
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
}

export default DoctorSignupPage;
