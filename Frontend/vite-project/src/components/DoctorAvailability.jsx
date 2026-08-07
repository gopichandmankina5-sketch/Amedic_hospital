import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, API_BASE } from '../api';
import MDButton from './ui/MDButton';
import { getDoctorToken } from "../tokenStore";

// API_BASE provided by shared api module
const DAYS = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'];
const dayNameToNum = { Sunday: 0, Monday: 1, Tuesday: 2, Wednesday: 3, Thursday: 4, Friday: 5, Saturday: 6 };
const dayNumToName = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

const DoctorAvailability = () => {
  const navigate = useNavigate();
  const token = getDoctorToken();
  const [availability, setAvailability] = useState([]);
  const [selectedDay, setSelectedDay] = useState(DAYS[0]);
  const [slotsInput, setSlotsInput] = useState('');
  const [selectedDate, setSelectedDate] = useState(''); // YYYY-MM-DD
  const [dateSlots, setDateSlots] = useState([]);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    if (!token) return navigate('/doctor/login');
    const load = async () => {
      try {
        const res = await api.get(`/api/doctor/availability`, { headers: { Authorization: `Bearer ${token}` } });
        setAvailability(res.data?.availability || []);
      } catch (e) {
        setMsg(e.response?.data?.message || 'Failed to load availability');
      }
    };
    load();
  }, [navigate, token]);

  // Normalize slots input: accepts ranges "HH:mm-HH:mm"; expands to 30-min steps (end exclusive)
  const expandRange = (start, end) => {
    const out = [];
    const toMin = (s) => {
      const [H, M] = s.split(':').map(Number);
      return H * 60 + M;
    };
    const pad = (n) => n.toString().padStart(2, '0');
    let a = toMin(start);
    const b = toMin(end);
    while (a < b) {
      const h = Math.floor(a / 60);
      const m = a % 60;
      out.push(`${pad(h)}:${pad(m)}`);
      a += 30;
    }
    return out;
  };

  const parseSlotsInput = () => {
    const tokens = slotsInput.split(',').map(s => s.trim()).filter(Boolean);
    const slots = [];
    const hhmm = /^([01]\d|2[0-3]):([0-5]\d)$/;
    for (const t of tokens) {
      if (!t.includes('-')) continue; // only ranges allowed
      const [ra, rb] = t.split('-').map(s => s.trim());
      if (!hhmm.test(ra) || !hhmm.test(rb)) continue;
      // ensure start < end
      const toMin = (s) => { const [H, M] = s.split(':').map(Number); return H*60+M; };
      if (toMin(ra) >= toMin(rb)) continue;
      slots.push(...expandRange(ra, rb));
    }
    // de-dup and sort
    return Array.from(new Set(slots)).sort();
  };

  const addAvailability = async () => {
    setMsg('');
    const slots = parseSlotsInput();
    if (!slots.length) { setMsg('Please enter at least one valid time, e.g. 09:00, 09:30 or 09:00-11:00'); return; }
    try {
      const dayNum = dayNameToNum[selectedDay] ?? 1;
      await api.post(`/api/doctor/availability`, { day: dayNum, slots }, { headers: { Authorization: `Bearer ${token}` } });
      const res = await api.get(`/api/doctor/availability`, { headers: { Authorization: `Bearer ${token}` } });
      setAvailability(res.data?.availability || []);
      setSlotsInput('');
      setMsg('Availability saved');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to save availability');
    }
  };

  const deleteDay = async (day) => {
    setMsg('');
    try {
      await api.delete(`/api/doctor/availability/${encodeURIComponent(day)}`, { headers: { Authorization: `Bearer ${token}` } });
      setAvailability(prev => prev.filter(a => a.day !== day));
      setMsg('Deleted');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to delete');
    }
  };

  const deleteSlot = async (day, slot) => {
    setMsg('');
    try {
      await api.delete(`/api/doctor/availability/${encodeURIComponent(day)}?slot=${encodeURIComponent(slot)}`, { headers: { Authorization: `Bearer ${token}` } });
      setAvailability(prev => prev.map(a => a.day === day ? { ...a, slots: (a.slots || []).filter(s => s !== slot) } : a));
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to delete slot');
    }
  };

  const deleteRange = async (day, range) => {
    setMsg('');
    const toMin = (s) => {
      const [H, M] = s.split(':').map(Number);
      return H * 60 + M;
    };
    const toHHMM = (m) => {
      const pad = (n) => n.toString().padStart(2, '0');
      const H = Math.floor(m / 60);
      const M = m % 60;
      return `${pad(H)}:${pad(M)}`;
    };
    try {
      for (let a = toMin(range.start); a < toMin(range.end); a += 30) {
        const slot = toHHMM(a);
        await api.delete(`/api/doctor/availability/${encodeURIComponent(day)}?slot=${encodeURIComponent(slot)}`, { headers: { Authorization: `Bearer ${token}` } });
      }
      // reload day availability from server to ensure consistency
      const res = await api.get(`/api/doctor/availability`, { headers: { Authorization: `Bearer ${token}` } });
      setAvailability(res.data?.availability || []);
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to delete range');
    }
  };

  // Calendar: load available slots for specific date
  const loadDateSlots = async (date) => {
    if (!date) { setDateSlots([]); return; }
    try {
      const res = await api.get(`/api/doctor/available-slots?date=${encodeURIComponent(date)}`, { headers: { Authorization: `Bearer ${token}` } });
      setDateSlots(res.data?.slots || []);
    } catch (e) {
      setDateSlots([]);
    }
  };

  useEffect(() => { loadDateSlots(selectedDate); /* eslint-disable-next-line */ }, [selectedDate]);

  const selectedDateWeekday = useMemo(() => {
    if (!selectedDate) return '';
    const d = new Date(`${selectedDate}T00:00:00`);
    return dayNumToName[d.getDay()];
  }, [selectedDate]);

  return (
    <div className="md-app-wrapper p-6">
      <div className="max-w-4xl mx-auto md-card">
        <h2 className="md-headline mb-2">Manage Availability</h2>
        <p className="md-subtitle mb-lg">Add your weekly slots for bookings.</p>
        {msg && <div className="md-chip mb-md">{msg}</div>}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-lg">
          <div className="md-input-group">
            <label>Calendar (date)</label>
            <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)} />
            {selectedDate && <div className="text-sm opacity-80 mt-1">Weekday: {selectedDateWeekday}</div>}
          </div>
          <div className="md-input-group">
            <label>Day</label>
            <select value={selectedDay} onChange={e => setSelectedDay(e.target.value)}>
              {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
          </div>
          <div className="md-input-group md-col-span-2">
            <label>Slots (ranges only, comma separated)</label>
            <input value={slotsInput} onChange={e => setSlotsInput(e.target.value)} placeholder="09:00-11:00, 14:30-16:00" />
          </div>
        </div>
        <div className="md-actions mb-xl">
          <MDButton onClick={addAvailability}>Add/Replace</MDButton>
          <MDButton variant="outlined" onClick={() => navigate('/doctor/home')}>Back</MDButton>
        </div>

        {selectedDate && (
          <div className="md-card-flat mb-xl">
            <div className="md-title mb-sm">Available on {selectedDate} ({selectedDateWeekday || '-'})</div>
            <div className="text-sm opacity-80 mb-sm">Derived from your weekly availability minus booked slots.</div>
            <div className="flex flex-wrap gap-2">
              {dateSlots.length === 0 ? (
                <span className="opacity-60">No available slots for this date.</span>
              ) : (
                dateSlots.map(s => <span key={s} className="md-chip">{s}</span>)
              )}
            </div>
          </div>
        )}

        <h3 className="md-title mb-md">Current Availability</h3>
        <div className="grid gap-3">
          {availability.length === 0 && <div className="md-card-flat">No availability set yet.</div>}
          {availability.map((a) => (
            <div className="md-card-flat flex items-center justify-between" key={a.day}>
              <div>
                <div className="md-title">{dayNumToName[a.day]}</div>
                <RangesView slots={a.slots || []} onDeleteRange={(range) => deleteRange(a.day, range)} />
              </div>
              <MDButton variant="outlined" onClick={() => deleteDay(a.day)}>Delete</MDButton>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// Compress sorted HH:mm slots (30-min step) into ranges
function compressToRanges(slots) {
  const toMin = (s) => {
    const [H, M] = s.split(':').map(Number);
    return H * 60 + M;
  };
  const toHHMM = (m) => {
    const pad = (n) => n.toString().padStart(2, '0');
    const H = Math.floor(m / 60);
    const M = m % 60;
    return `${pad(H)}:${pad(M)}`;
  };
  const sorted = [...new Set(slots)].sort();
  const result = [];
  let i = 0;
  while (i < sorted.length) {
    let start = toMin(sorted[i]);
    let end = start + 30; // exclusive end
    let j = i + 1;
    while (j < sorted.length && toMin(sorted[j]) === end) {
      end += 30; j++;
    }
    result.push({ start: toHHMM(start), end: toHHMM(end) });
    i = j;
  }
  return result;
}

const RangesView = ({ slots, onDeleteRange }) => {
  const ranges = compressToRanges(slots);
  return (
    <div className="text-sm opacity-80 flex flex-wrap gap-2 mt-1">
      {ranges.map(r => (
        <span key={`${r.start}-${r.end}`} className="md-chip flex items-center gap-2">
          {r.start}-{r.end}
          {onDeleteRange && <button type="button" onClick={() => onDeleteRange(r)} className="opacity-70 hover:opacity-100">×</button>}
        </span>
      ))}
    </div>
  );
};

export default DoctorAvailability;
