import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { api } from '../api';
import { useNavigate } from 'react-router-dom';
import MDButton from './ui/MDButton';

const AdminSignup = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const onChange = (e) => { setError(''); setSuccess(''); setForm({ ...form, [e.target.name]: e.target.value }); };

  const submit = async () => {
    const { name, email, password } = form;
    if (!name || !email || !password) { setError('All fields are required'); return; }
    try {
      const res = await api.post('/api/admin/signup', { name, email, password });
      setSuccess(res.data?.message || 'Admin created');
      setTimeout(() => navigate('/login'), 800);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Signup failed');
    }
  };

  return (
    <div className="md-app-wrapper flex items-center justify-center p-6">
      <div className="w-full max-w-md md-card fade-in">
        <h1 className="md-headline center mb-4">Create Admin</h1>

        {error && <div className="md-chip md-chip-error mb-4">{error}</div>}
        {success && <div className="md-chip mb-4">{success}</div>}

        <div className="md-input-group">
          <label htmlFor="name" className="md-label">Name</label>
          <input id="name" name="name" className="md-input" value={form.name} onChange={onChange} />
        </div>
        <div className="md-input-group">
          <label htmlFor="email" className="md-label">Email</label>
          <input id="email" name="email" type="email" className="md-input" value={form.email} onChange={onChange} />
        </div>
        <div className="md-input-group">
          <label htmlFor="password" className="md-label">Password</label>
          <input id="password" name="password" type="password" className="md-input" value={form.password} onChange={onChange} />
        </div>

        <MDButton onClick={submit} className="w-full mb-3">Create Admin</MDButton>
        <MDButton onClick={() => navigate('/login')} className="w-full" variant="outlined">Back to Login</MDButton>
      </div>
    </div>
  );
};

export default AdminSignup;
