import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import MDButton from './ui/MDButton';
import { getDoctorToken } from "../tokenStore";

// Using shared api base

const DoctorDashboard = () => {
  const navigate = useNavigate();
  const token = getDoctorToken();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({ todayCount: 0, today: [], upcoming: [] });
  const [msg, setMsg] = useState('');

  useEffect(() => {
    if (!token) return navigate('/doctor/login');
    const load = async () => {
      setMsg('');
      try {
        const res = await api.get(`/api/doctor/dashboard`, { headers: { Authorization: `Bearer ${token}` } });
        setData({ todayCount: res.data?.todayCount || 0, today: res.data?.today || [], upcoming: res.data?.upcoming || [] });
      } catch (e) {
        setMsg(e.response?.data?.message || 'Failed to load dashboard');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [navigate, token]);

  if (loading) return <div className="p-6">Loading...</div>;

  const todayCount = data.todayCount || 0;

  return (
    <div className="md-app-wrapper p-6">
      <div className="max-w-5xl mx-auto grid gap-4 md:grid-cols-2">
        <div className="md-card">
          <h3 className="md-title mb-md">Today's Summary</h3>
          {msg && <div className="md-chip mb-md">{msg}</div>}
          <div className="md-card-flat">
            <div className="md-subtitle">Total appointments today</div>
            <div className="md-display">{todayCount}</div>
          </div>
          <div className="md-actions mt-lg">
            <MDButton onClick={() => navigate('/doctor/appointments')}>Manage Appointments</MDButton>
          </div>
        </div>
        <div className="md-card">
          <h3 className="md-title mb-md">Upcoming</h3>
          <div className="grid gap-2">
            {(data.upcoming || []).length === 0 && <div className="md-card-flat">No upcoming appointments.</div>}
            {(data.upcoming || []).map(u => (
              <div className="md-card-flat" key={u._id}>
                <div className="flex items-center justify-between">
                  <div className="md-subtitle">{u.patientId?.name || 'Patient'}</div>
                  <div className="text-sm opacity-80">{(u.date || '').toString()} {u.time ? `• ${u.time}` : ''}</div>
                </div>
              </div>
            ))}
          </div>
          <div className="md-actions mt-lg">
            <MDButton variant="outlined" onClick={() => navigate('/doctor/home')}>Back</MDButton>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;
