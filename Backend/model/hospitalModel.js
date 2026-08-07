    const mongoose = require("mongoose");

    const hospitalSchema = mongoose.Schema({
    name: {
        type: String,
        required: true,
    },
    address: {
        type: String,
        required: true,
    },
    contactNumber: {
        type: String,
        required: true,
    },
    email: {
        type: String,
        required: true,
        unique: true,
    },
    doctorList: [
        {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Doctor",
        },
    ],
    departments: [
        {
        type: String,
        },
    ],
    services: [
        {
        type: String,
        },
    ],
    rating: {
        type: Number,
        min: [1, "Rating cannot be less than 1"],
        max: [5, "Top Rating is 5"],
    },
    dateEstablished: {
        type: Date,
        required: true,
    },
    isActive: {
        type: Boolean,
        default: true,
    },
    });

    const HospitalModel = mongoose.model("Hospital", hospitalSchema);

    module.exports = HospitalModel;
