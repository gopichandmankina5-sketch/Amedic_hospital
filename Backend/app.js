const express = require("express");
const ErrorMiddleware = require("./middleware/error");
const userRouter = require('./controllers/userRoutes');
const doctorRouter = require("./controllers/docterRoutes");
const doctorVerificationRouter = require('./controllers/doctorVerificationRoutes');
const adminAuthRoutes = require('./routes/adminAuthRoutes');
const doctorFeatureRoutes = require('./controllers/doctorFeatureRoutes');
const AppointmentRouter = require("./controllers/bookAppointment");
const getAvailableSlotsRouter = require("./controllers/getAvailableSlots"); // ✅
const aiAutocompleteRouter = require('./controllers/aiAutocomplete');
const cookieParser = require('cookie-parser');
const cors = require("cors");
const path = require('path');

const app = express();
app.use(express.json());
app.use(cookieParser());
require('./config/passport');

app.use(cors({
  credentials: true
}));

app.use("/appointmentsbook", AppointmentRouter);
app.use("/available", getAvailableSlotsRouter); // ✅ This line fixed your issue
app.use('/api/ai', aiAutocompleteRouter);

app.use("/user", userRouter);
app.use('/doctor', doctorRouter); // legacy doctor routes
app.use('/doctor', doctorVerificationRouter); // verification + registration/login
app.use('/api/admin', adminAuthRoutes); // admin authentication & protected doctor mgmt
app.use('/api/doctor', doctorFeatureRoutes); // doctor profile, availability, dashboard
app.use('/profile-photo', express.static(path.join(__dirname, 'upload')));
app.use(ErrorMiddleware);

module.exports = { app };
