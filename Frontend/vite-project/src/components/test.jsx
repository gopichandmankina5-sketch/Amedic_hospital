// import React, { useEffect, useState } from "react";
// import { useParams } from "react-router-dom";
// import { motion } from "framer-motion";

// const AvailableSlots = () => {
//   const { doctorId } = useParams();
//   const [date, setDate] = useState("");
//   const [slots, setSlots] = useState([]);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState("");
//   const [selectedSlot, setSelectedSlot] = useState(null);

//   const fetchSlots = async () => {
//     if (!date) return;
//     setLoading(true);
//     try {
//       const res = await fetch(
//         `http://localhost:9090/appointments/available/${doctorId}?date=${date}`
//       );
//       const data = await res.json();
//       if (res.ok) {
//         setSlots(data.availableSlots);
//         setError("");
//       } else {
//         setError(data.message || "Failed to load slots");
//         setSlots([]);
//       }
//     } catch (err) {
//       setError("Network error");
//       setSlots([]);
//     }
//     setLoading(false);
//   };

//   const handleBook = async () => {
//     const token = localStorage.getItem("token");
//     if (!token || !selectedSlot) return;

//     try {
//       const res = await fetch("http://localhost:9090/appointments/book", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json",
//           Authorization: `Bearer ${token}`,
//         },
//         body: JSON.stringify({
//           doctorId,
//           date,
//           time: selectedSlot,
//         }),
//       });
//       const data = await res.json();
//       if (res.ok) {
//         alert("Appointment booked successfully!");
//         setSelectedSlot(null);
//         fetchSlots();
//       } else {
//         alert(data.message || "Booking failed");
//       }
//     } catch (err) {
//       alert("Network error while booking");
//     }
//   };

//   return (
//     <div className="min-h-screen bg-blue-50 px-6 py-10">
//       <h1 className="text-3xl font-bold text-teal-800 mb-6 text-center">
//         Available Slots
//       </h1>

//       <div className="max-w-md mx-auto mb-6">
//         <input
//           type="date"
//           className="w-full px-4 py-2 border rounded-lg"
//           value={date}
//           onChange={(e) => setDate(e.target.value)}
//         />
//         <button
//           onClick={fetchSlots}
//           className="mt-4 w-full bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700"
//         >
//           Check Availability
//         </button>
//       </div>

//       {loading && <p className="text-center">Loading slots...</p>}
//       {error && <p className="text-center text-red-500">{error}</p>}

//       <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-2xl mx-auto">
//         {slots.map((slot, index) => (
//           <motion.button
//             key={index}
//             whileHover={{ scale: 1.05 }}
//             whileTap={{ scale: 0.95 }}
//             onClick={() => setSelectedSlot(slot)}
//             className={`px-4 py-2 rounded-lg border transition ${
//               selectedSlot === slot
//                 ? "bg-teal-700 text-white"
//                 : "bg-white hover:bg-teal-100"
//             }`}
//           >
//             {slot}
//           </motion.button>
//         ))}
//       </div>

//       {selectedSlot && (
//         <div className="text-center mt-8">
//           <p className="text-lg mb-2">Selected Slot: <strong>{selectedSlot}</strong></p>
//           <button
//             onClick={handleBook}
//             className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700"
//           >
//             Book Appointment
//           </button>
//         </div>
//       )}
//     </div>
//   );
// };

// export default AvailableSlots;
