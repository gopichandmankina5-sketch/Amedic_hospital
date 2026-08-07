import React, { useEffect, useState } from 'react';
import { api, API_BASE } from '../api';
import { motion } from 'framer-motion';

const NMC_URL = 'https://www.nmc.org.in/information-desk/indian-medical-register/';

const AdminPendingDoctorsPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [reasonMap, setReasonMap] = useState({});

  const token = localStorage.getItem('adminToken');

  const fetchPending = async () => {
    setLoading(true); setError('');
    try {
      const res = await api.get('/api/admin/doctors/pending', {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      setDoctors(res.data.doctors || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load');
    } finally {
      setLoading(false);
    }
  };

  useEffect(()=> { fetchPending(); }, []);

  const approve = async (id) => {
    try {
      await api.patch(`/api/admin/doctor/approve/${id}`, {}, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
      setDoctors(d => d.filter(doc => doc._id !== id));
    } catch (err) { alert(err.response?.data?.message || 'Approve failed'); }
  };

  const reject = async (id) => {
    const reason = reasonMap[id] || prompt('Reason (optional):') || '';
    try {
      await api.patch(`/api/admin/doctor/reject/${id}`, { reason }, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
      setDoctors(d => d.filter(doc => doc._id !== id));
    } catch (err) { alert(err.response?.data?.message || 'Reject failed'); }
  };

  const updateReason = (id,val)=> setReasonMap(r => ({...r, [id]: val}));

  return (
    <motion.div className='min-h-screen bg-gradient-to-br from-gray-50 to-teal-50 p-6'>
      <div className='max-w-6xl mx-auto bg-white shadow rounded-lg p-6'>
        <h1 className='text-2xl font-bold text-teal-700 mb-4'>Pending Doctor Verifications</h1>
        <div className='mb-4 flex gap-3'>
          <button onClick={fetchPending} className='px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-700'>Refresh</button>
          <a href={NMC_URL} target='_blank' rel='noopener noreferrer' className='px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700'>Verify on NMC Website</a>
        </div>
        {loading && <p>Loading...</p>}
        {error && <p className='text-red-600'>{error}</p>}
        {!loading && doctors.length === 0 && <p>No pending doctors.</p>}
        {doctors.length > 0 && (
          <div className='overflow-x-auto'>
            <table className='min-w-full text-sm'>
              <thead>
                <tr className='bg-teal-100'>
                  <th className='p-2 text-left'>Name</th>
                  <th className='p-2 text-left'>Email</th>
                  <th className='p-2 text-left'>Reg. Number</th>
                  <th className='p-2 text-left'>Specialization</th>
                  <th className='p-2 text-left'>Docs</th>
                  <th className='p-2 text-left'>Actions</th>
                </tr>
              </thead>
              <tbody>
                {doctors.map(doc => (
                  <tr key={doc._id} className='border-b hover:bg-teal-50'>
                    <td className='p-2'>{doc.name}</td>
                    <td className='p-2'>{doc.email}</td>
                    <td className='p-2'>{doc.registrationNumber}</td>
                    <td className='p-2'>{doc.specialization}</td>
                    <td className='p-2'>
                      <div className='flex flex-col gap-1'>
                        {doc.documents?.medicalRegistrationCertificate && <a className='text-teal-600 underline' href={`${API_BASE}/profile-photo/${doc.documents.medicalRegistrationCertificate}`} target='_blank'>Registration Cert</a>}
                        {doc.documents?.degreeCertificate && <a className='text-teal-600 underline' href={`${API_BASE}/profile-photo/${doc.documents.degreeCertificate}`} target='_blank'>Degree Cert</a>}
                        {doc.documents?.govtIdProof && <a className='text-teal-600 underline' href={`${API_BASE}/profile-photo/${doc.documents.govtIdProof}`} target='_blank'>Govt ID</a>}
                        {doc.profilePhoto && <a className='text-teal-600 underline' href={`${API_BASE}/profile-photo/${doc.profilePhoto}`} target='_blank'>Profile Photo</a>}
                      </div>
                    </td>
                    <td className='p-2'>
                      <div className='flex flex-col gap-2'>
                        <button onClick={()=>approve(doc._id)} className='px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700'>Approve</button>
                        <button onClick={()=>reject(doc._id)} className='px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700'>Reject</button>
                        <input
                          type='text'
                          placeholder='Rejection reason'
                          value={reasonMap[doc._id] || ''}
                          onChange={e=>updateReason(doc._id,e.target.value)}
                          className='p-1 border rounded'
                        />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </motion.div>
  );
};

export default AdminPendingDoctorsPage;
