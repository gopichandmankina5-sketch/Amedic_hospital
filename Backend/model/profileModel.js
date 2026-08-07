const mongoose = require("mongoose");

const ProfileSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
  name: String,
  gender: String,
  dob: String,
  phone: String,
  address: String,
   profilePhoto: {
     type: String 
    },
});

const Profile=mongoose.model("Profile", ProfileSchema);

module.exports = {Profile}
