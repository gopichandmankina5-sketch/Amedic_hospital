import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, API_BASE } from '../api';
import MDButton from './ui/MDButton';
import { getDoctorToken } from "../tokenStore";

// API_BASE provided by shared api module

const DoctorProfile = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState('');
  const [file, setFile] = useState(null);
  const [currentPhoto, setCurrentPhoto] = useState('');
  const [previewUrl, setPreviewUrl] = useState('');
  const [form, setForm] = useState({
    name: '',
    phone: '',
    specialization: '',
    experience: '',
    clinic: {
      name: '',
      address: '',
      city: '',
      state: '',
      pincode: ''
    }
  });

  const token = getDoctorToken();

  useEffect(() => {
    if (!token) {
      navigate('/doctor/login');
      return;
    }
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/api/doctor/profile`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const d = res.data?.doctor || {};
        setForm({
          name: d.name || '',
          phone: d.phone || '',
          specialization: d.specialization || '',
          experience: d.experience || '',
          clinic: {
            name: d.clinic?.name || '',
            address: d.clinic?.address || '',
            city: d.clinic?.city || '',
            state: d.clinic?.state || '',
            pincode: d.clinic?.pincode || ''
          }
        });
        setCurrentPhoto(d.profilePhoto || '');
      } catch (e) {
        setMsg(e.response?.data?.message || 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [navigate, token]);

  const setClinic = (key, val) => setForm(prev => ({ ...prev, clinic: { ...prev.clinic, [key]: val } }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMsg('');
    try {
      const fd = new FormData();
      fd.append('name', form.name);
      fd.append('phone', form.phone);
      fd.append('specialization', form.specialization);
      fd.append('experience', form.experience);
      fd.append('clinic', JSON.stringify(form.clinic));
      if (file) fd.append('profilePhoto', file);

      const res = await api.patch(`/api/doctor/profile`, fd, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const updated = res.data?.doctor || {};
      setMsg('Profile updated');
      setCurrentPhoto(updated.profilePhoto || currentPhoto);
      setFile(null);
      setPreviewUrl('');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  const onFileChange = (e) => {
    const f = e.target.files?.[0] || null;
    setFile(f);
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(f ? URL.createObjectURL(f) : '');
  };

  if (loading) return <div className="p-6">Loading...</div>;

  return (
    <div className="md-app-wrapper p-6">
      <div className="max-w-3xl mx-auto md-card">
        <h2 className="md-headline mb-2">My Profile</h2>
        <p className="md-subtitle mb-lg">Keep your information up to date.</p>
        {msg && <div className="md-chip mb-md">{msg}</div>}
        <form onSubmit={onSubmit} className="space-y-md">
          <div className="md-input-group">
            <label>Name</label>
            <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="md-input-group">
            <label>Phone</label>
            <input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div className="md-input-group">
            <label>Specialization</label>
            <input value={form.specialization} onChange={e => setForm({ ...form, specialization: e.target.value })} />
          </div>
          <div className="md-input-group">
            <label>Experience (years)</label>
            <input type="number" min="0" value={form.experience} onChange={e => setForm({ ...form, experience: e.target.value })} />
          </div>

          <h3 className="md-title mt-lg">Clinic</h3>
          <div className="md-input-group"><label>Clinic Name</label><input value={form.clinic.name} onChange={e => setClinic('name', e.target.value)} /></div>
          <div className="md-input-group"><label>Address</label><input value={form.clinic.address} onChange={e => setClinic('address', e.target.value)} /></div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="md-input-group"><label>City</label><input value={form.clinic.city} onChange={e => setClinic('city', e.target.value)} /></div>
            <div className="md-input-group"><label>State</label><input value={form.clinic.state} onChange={e => setClinic('state', e.target.value)} /></div>
            <div className="md-input-group"><label>Pincode</label><input value={form.clinic.pincode} onChange={e => setClinic('pincode', e.target.value)} /></div>
          </div>

          <div className="md-input-group">
            <label>Profile Photo</label>
            <input type="file" accept="image/*" onChange={onFileChange} />
            <div className="flex items-center gap-4 mt-3">
              {(previewUrl || currentPhoto) && (
                <img
                  src={previewUrl || `${API_BASE}/profile-photo/${currentPhoto}`}
                  alt="Profile"
                  style={{ width: 88, height: 88, objectFit: 'cover', borderRadius: 12 }}
                  onError={(e) => { e.currentTarget.style.display = 'none'; }}
                />
              )}
              {!previewUrl && !currentPhoto && (
                <span className="text-sm opacity-70">No photo uploaded</span>
              )}
            </div>
          </div>

          <div className="md-actions">
            <MDButton type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</MDButton>
            <MDButton type="button" variant="outlined" onClick={() => navigate('/doctor/home')}>Back</MDButton>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DoctorProfile;
