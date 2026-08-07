const mongoose = require("mongoose");

const docterSchema = new mongoose.Schema({
    name: { type: String, required: true, trim: true },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true, match: /.+@.+\..+/ },
    password: { type: String, required: true },
    phone: { type: String, trim: true },
    specialization: { type: String, trim: true },
    experience: { type: Number, min: 0 },
    registrationNumber: { type: String, trim: true },
    registrationCouncil: { type: String, trim: true },
    registrationYear: { type: Number },
    clinic: {
        name: { type: String, trim: true },
        address: { type: String, trim: true },
        city: { type: String, trim: true },
        state: { type: String, trim: true },
        pincode: { type: String, trim: true }
    },
    availability: [
        {
            day: { type: Number, min: 0, max: 6, required: true }, // 0=Sun
            slots: [{ type: String }]
        }
    ],
    documents: {
        medicalRegistrationCertificate: { type: String },
        degreeCertificate: { type: String },
        govtIdProof: { type: String }
    },
    profilePhoto: { type: String },
    verificationStatus: { type: String, enum: ["pending", "approved", "rejected"], default: "pending" },
    rejectionReason: { type: String, trim: true },
    onDuty: { type: Boolean, default: false }
}, { timestamps: true });

// Basic projection helper (optional future use)
docterSchema.methods.toSafeObject = function () {
    return {
        id: this._id,
        name: this.name,
        email: this.email,
        specialization: this.specialization,
        registrationNumber: this.registrationNumber,
        verificationStatus: this.verificationStatus,
        onDuty: this.onDuty
    };
};

const DocterModel = mongoose.model("Doctor", docterSchema);

module.exports = DocterModel;
