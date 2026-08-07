import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { FaUserCircle } from "react-icons/fa";
import { api, API_BASE } from "../api";

const Profile = () => {
  const [profile, setProfile] = useState({
    name: "",
    gender: "",
    dob: "",
    phone: "",
    address: "",
    photo: null,
  });

  const [existingProfile, setExistingProfile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);

  // Fetch profile on mount
  useEffect(() => {
    api
      .get("/user/upload")
      .then((res) => {
        if (res.data.success && res.data.profile) {
          const data = res.data.profile;
          setExistingProfile(data);
          setProfile({
            name: data.name || "",
            gender: data.gender || "",
            dob: data.dob?.substring(0, 10) || "", // Format date
            phone: data.phone || "",
            address: data.address || "",
            photo: null,
          });
          if (data.photoUrl) setPreviewUrl(`${API_BASE}${data.photoUrl}`);
        }
      })
      .catch((err) => {
        console.log("No profile found or error fetching:", err.message);
      });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile({ ...profile, [name]: value });
  };

  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    setProfile({ ...profile, photo: file });
    setPreviewUrl(URL.createObjectURL(file));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const formData = new FormData();
    Object.entries(profile).forEach(([key, value]) => {
      if (value) formData.append(key, value);
    });

    try {
      const res = await api.post(
        "/user/upload",
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );
      alert("Profile saved successfully!");
      setExistingProfile(res.data.profile);
    } catch (err) {
      alert("Error saving profile");
      console.error(err);
    }
  };

  return (
    <motion.div
      className="min-h-screen bg-gradient-to-br from-blue-50 to-teal-100 px-6 py-10 flex justify-center"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.8 }}
    >
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-2xl shadow-2xl max-w-xl w-full space-y-6"
      >
        <h2 className="text-3xl font-bold text-teal-800 text-center">
          {existingProfile ? "Edit Your Profile" : "Create Your Profile"}
        </h2>

        {/* Profile Picture */}
        <div className="flex flex-col items-center">
          {previewUrl ? (
            <img
              src={previewUrl}
              alt="Preview"
              className="w-32 h-32 rounded-full object-cover border-2 border-teal-600"
            />
          ) : (
            <FaUserCircle className="text-7xl text-gray-400 mb-2" />
          )}
          <input
            type="file"
            accept="image/*"
            onChange={handlePhotoChange}
            className="mt-2 text-sm"
          />
        </div>

        {/* Name */}
        <div>
          <label className="block text-teal-700 font-medium mb-1">Name</label>
          <input
            type="text"
            name="name"
            value={profile.name}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 rounded-lg border border-teal-300 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
        </div>

        {/* Gender */}
        <div>
          <label className="block text-teal-700 font-medium mb-1">Gender</label>
          <select
            name="gender"
            value={profile.gender}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 rounded-lg border border-teal-300 focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Select gender</option>
            <option value="male">Male</option>
            <option value="female">Female</option>
            <option value="other">Other</option>
          </select>
        </div>

        {/* DOB */}
        <div>
          <label className="block text-teal-700 font-medium mb-1">
            Date of Birth
          </label>
          <input
            type="date"
            name="dob"
            value={profile.dob}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 rounded-lg border border-teal-300 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
        </div>

        {/* Phone */}
        <div>
          <label className="block text-teal-700 font-medium mb-1">Phone</label>
          <input
            type="tel"
            name="phone"
            value={profile.phone}
            onChange={handleChange}
            placeholder="e.g. 9876543210"
            className="w-full px-4 py-2 rounded-lg border border-teal-300 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
        </div>

        {/* Address */}
        <div>
          <label className="block text-teal-700 font-medium mb-1">
            Address
          </label>
          <textarea
            name="address"
            value={profile.address}
            onChange={handleChange}
            rows={3}
            placeholder="Enter your address"
            className="w-full px-4 py-2 rounded-lg border border-teal-300 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
        </div>

        {/* Submit Button */}
        <div className="text-center">
          <motion.button
            type="submit"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="bg-teal-600 text-white px-6 py-3 rounded-lg font-semibold shadow-md hover:bg-teal-700 transition"
          >
            {existingProfile ? "Update Profile" : "Save Profile"}
          </motion.button>
        </div>
      </form>
    </motion.div>
  );
};

export default Profile;
