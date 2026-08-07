import React, { useState } from 'react';
import { api } from '../api';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';

const DoctorRegistrationForm = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    specialization: '',
    registrationNumber: '',
    registrationCouncil: '',
    registrationYear: '',
  });
  const [files, setFiles] = useState({
    medicalRegistrationCertificate: null,
    degreeCertificate: null,
    govtIdProof: null,
    profilePhoto: null,
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };
  const handleFile = (e) => {
    setFiles({ ...files, [e.target.name]: e.target.files[0] });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const required = ['fullName','email','password','specialization','registrationNumber'];
    for (const field of required) {
      if (!form[field]) { setError(`Missing field: ${field}`); return; }
    }

    const fd = new FormData();
    Object.entries(form).forEach(([k,v])=> fd.append(k,v));
    Object.entries(files).forEach(([k,v])=> { if(v) fd.append(k,v); });

    try {
      setLoading(true);
      const res = await api.post('/doctor/register', fd);
      setSuccess(res.data.message || 'Submitted');
      setTimeout(()=> navigate('/'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.div className='min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-teal-100 p-6'>
      <form onSubmit={handleSubmit} className='bg-white w-full max-w-2xl p-8 rounded-2xl shadow-xl space-y-4'>
        <h1 className='text-3xl font-bold text-teal-700 text-center'>Doctor Registration</h1>
        {error && <div className='bg-red-100 text-red-700 p-2 rounded'>{error}</div>}
        {success && <div className='bg-green-100 text-green-700 p-2 rounded'>{success}</div>}
        <div className='grid md:grid-cols-2 gap-4'>
          {['fullName','email','password','phone','specialization','registrationNumber','registrationCouncil','registrationYear'].map((f)=> (
            <div key={f}>
              <label className='block text-sm font-semibold mb-1 capitalize'>{f.replace(/([A-Z])/g,' $1')}</label>
              <input
                type={f==='password'?'password': (f==='registrationYear'?'number':'text')}
                name={f}
                value={form[f]}
                onChange={handleChange}
                className='w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-teal-500'
              />
            </div>
          ))}
        </div>

        <div className='grid md:grid-cols-2 gap-4 pt-4'>
          <div>
            <label className='block text-sm font-semibold mb-1'>Medical Registration Certificate</label>
            <input type='file' name='medicalRegistrationCertificate' onChange={handleFile} className='w-full' />
          </div>
          <div>
            <label className='block text-sm font-semibold mb-1'>Degree Certificate</label>
            <input type='file' name='degreeCertificate' onChange={handleFile} className='w-full' />
          </div>
          <div>
            <label className='block text-sm font-semibold mb-1'>Government ID Proof</label>
            <input type='file' name='govtIdProof' onChange={handleFile} className='w-full' />
          </div>
          <div>
            <label className='block text-sm font-semibold mb-1'>Profile Photo</label>
            <input type='file' name='profilePhoto' onChange={handleFile} className='w-full' />
          </div>
        </div>

        <button disabled={loading} className='w-full bg-teal-600 text-white py-3 rounded font-semibold hover:bg-teal-700 disabled:bg-teal-300'>
          {loading ? 'Submitting...' : 'Submit for Verification'}
        </button>
        <p className='text-center text-sm text-gray-600 pt-2'>Already registered? <span onClick={()=>navigate('/doctor/login')} className='text-teal-600 cursor-pointer font-semibold'>Login</span></p>
      </form>
    </motion.div>
  );
};

export default DoctorRegistrationForm;
