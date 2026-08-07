import React, { useState } from 'react';
import { FaRegEye, FaRegEyeSlash } from 'react-icons/fa6';
import { motion } from 'framer-motion';
import { api } from '../api';
import { useNavigate } from 'react-router-dom';
import MDButton from './ui/MDButton';

const AdminLogin = () => {
  const navigate = useNavigate();
  const [hide, setHide] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState({ email: '', password: '' });

  const handleHide = () => setHide(h => !h);
  const handleForm = (e) => { setError(''); setData({ ...data, [e.target.name]: e.target.value }); };

  const handleSubmit = async () => {
    const { email, password } = data;
    if (!email || !password) { setError('Please fill in all fields.'); return; }
    try {
      const res = await api.post('/api/admin/login', { email, password });
      if (res.data.token) localStorage.setItem('adminToken', res.data.token);
      navigate('/admin/doctors/pending');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed');
    }
  };

  return (
    <div className="md-app-wrapper items-center justify-center flex p-6">
      <div className="w-full max-w-md md-card fade-in">
        <h1 className="md-headline center mb-4">Admin Login</h1>
        {error && <div className="md-chip status-rejected mb-4">{error}</div>}

        <div className="md-input-group">
          <label htmlFor="email" className="md-label">Email</label>
          <input id="email" name="email" type="email" className="md-input" value={data.email} onChange={handleForm} />
        </div>
        <div className="md-input-group relative">
          <label htmlFor="password" className="md-label">Password</label>
          <input id="password" name="password" type={hide ? 'password' : 'text'} className="md-input pr-10" value={data.password} onChange={handleForm} />
          <motion.button type="button" onClick={handleHide} whileTap={{ scale: 1.2 }} className="absolute right-3 top-9 text-teal-700" style={{ background: 'transparent' }}>
            {hide ? <FaRegEye size={18} /> : <FaRegEyeSlash size={18} />}
          </motion.button>
        </div>

        <MDButton onClick={handleSubmit} className="w-full mb-3">Login as Admin</MDButton>
        <MDButton onClick={() => navigate('/admin/signup')} className="w-full" variant="outlined">Create Admin</MDButton>
      </div>
    </div>
  );
};

export default AdminLogin;
