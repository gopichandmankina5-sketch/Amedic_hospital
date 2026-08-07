    const mongoose = require("mongoose");
      const DocterModel = require("../model/docterModel");

    const userSchema = mongoose.Schema({
      name: {
        type: String,
        required: true,
      },
      email: {
        type: String,
        required: true,
        unique: true,
      },
      password: {
        type: String,
        required: false,
      },
      phone: {
        type: String,
        match: [/^[6-9]\d{9}$/, "Please enter a valid 10-digit Indian phone number"],
        unique: true,
      },
        doctorDetails: {
          type: mongoose.Schema.Types.ObjectId,
          ref: "Doctor",
        },
      dateJoined: {
        type: Date,
        default: Date.now,
      },
      isActivated: {
        type: Boolean,
        default: false,
      },
    });

    const userModel = mongoose.model("User", userSchema);
    module.exports = {userModel};
