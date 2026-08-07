import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import MDButton from './ui/MDButton';
import { getDoctorToken } from "../tokenStore";

// Using shared api base
const STATUSES = ['all','booked','accepted','cancelled','completed'];

const DoctorAppointments = () => {
  const navigate = useNavigate();
  const token = getDoctorToken();
  const [filter, setFilter] = useState('all');
  const [list, setList] = useState([]);
  const [msg, setMsg] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    if (!token) return navigate('/doctor/login');
    setLoading(true);
    setMsg('');
    try {
      const params = filter !== 'all' ? { status: filter } : {};
      const res = await api.get('/api/doctor/appointments', { params, headers: { Authorization: `Bearer ${token}` } });
      setList(res.data?.appointments || []);
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [filter]);

  const updateStatus = async (id, status) => {
    setMsg('');
    try {
      await api.patch(`/api/doctor/appointment/${id}/status`, { status }, { headers: { Authorization: `Bearer ${token}` } });
      await load();
      setMsg('Updated');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to update');
    }
  };

  const deleteAppointment = async (id) => {
    setMsg('');
    const ok = window.confirm('Delete this appointment permanently?');
    if (!ok) return;
    try {
      await api.delete(`/api/doctor/appointment/${id}`, { headers: { Authorization: `Bearer ${token}` } });
      await load();
      setMsg('Deleted');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to delete');
    }
  };

  const actionButtons = (a) => {
    const buttons = [];
    if (a.status === 'booked') {
      buttons.push(<MDButton key="accept" onClick={() => updateStatus(a._id, 'accepted')}>Accept</MDButton>);
      buttons.push(<MDButton key="cancel" variant="outlined" onClick={() => updateStatus(a._id, 'cancelled')}>Cancel</MDButton>);
    }
    if (a.status === 'accepted') {
      buttons.push(<MDButton key="complete" onClick={() => updateStatus(a._id, 'completed')}>Complete</MDButton>);
      buttons.push(<MDButton key="cancel" variant="outlined" onClick={() => updateStatus(a._id, 'cancelled')}>Cancel</MDButton>);
    }

    if (a.status === 'cancelled' || a.status === 'completed') {
      buttons.push(<MDButton key="delete" variant="outlined" onClick={() => deleteAppointment(a._id)}>Delete</MDButton>);
    }
    return <div className="md-actions">{buttons}</div>;
  };

  return (
    <div className="md-app-wrapper p-6">
      <div className="max-w-5xl mx-auto md-card">
        <div className="flex items-center justify-between mb-lg">
          <div>
            <h2 className="md-headline mb-1">Appointments</h2>
            <p className="md-subtitle">Review and manage your bookings.</p>
          </div>
          <div className="md-input-group">
            <label>Filter</label>
            <select value={filter} onChange={e => setFilter(e.target.value)}>
              {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>
        {msg && <div className="md-chip mb-md">{msg}</div>}
        {loading ? (
          <div>Loading...</div>
        ) : (
          <div className="grid gap-3">
            {list.length === 0 && <div className="md-card-flat">No appointments found.</div>}
            {list.map(a => (
              <div className="md-card-flat" key={a._id}>
                <div className="flex items-center justify-between">
                  <div>
                    <div className="md-title">{a.patientId?.name || 'Patient'}</div>
                    <div className="text-sm opacity-80">{(a.date || '').toString()} {a.time ? `• ${a.time}` : ''} • {a.status}</div>
                  </div>
                  {actionButtons(a)}
                </div>
                {a.reason && <div className="text-sm mt-2">Reason: {a.reason}</div>}
              </div>
            ))}
          </div>
        )}
        <div className="md-actions mt-lg">
          <MDButton variant="outlined" onClick={() => navigate('/doctor/home')}>Back</MDButton>
        </div>
      </div>
    </div>
  );
};

export default DoctorAppointments;
