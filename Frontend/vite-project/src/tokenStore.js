let _doctorToken = null;

export const setDoctorToken = (t) => { _doctorToken = t || null; };
export const getDoctorToken = () => _doctorToken;
export const clearDoctorToken = () => { _doctorToken = null; };
