# Backend Setup for Google Sign-In & MongoDB

This snippet demonstrates how a Node.js/Express backend handles the Google `idToken` sent from the Android app, verifies it using the official Google Auth Library, and saves the user data to a MongoDB Atlas database using Mongoose.

## 1. Install Dependencies
```bash
npm install express mongoose google-auth-library dotenv
```

## 2. Mongoose User Model (`models/User.js`)
```javascript
const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
    googleId: { type: String, required: true, unique: true },
    email: { type: String, required: true, unique: true },
    name: { type: String, required: true },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', UserSchema);
```

## 3. Express Route for Authentication (`server.js`)
```javascript
require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const { OAuth2Client } = require('google-auth-library');
const User = require('./models/User');

const app = express();
app.use(express.json());

// Initialize Google OAuth Client with your Web Client ID
const client = new OAuth2Client(process.env.GOOGLE_WEB_CLIENT_ID);

// Connect to MongoDB Atlas
mongoose.connect(process.env.MONGODB_URI, { useNewUrlParser: true, useUnifiedTopology: true })
    .then(() => console.log('Connected to MongoDB Atlas'))
    .catch(err => console.error('MongoDB connection error:', err));

// Route mapping to @POST("api/auth/google") in Retrofit
app.post('/api/auth/google', async (req, res) => {
    const { idToken } = req.body;

    if (!idToken) {
        return res.status(400).json({ success: false, message: 'idToken is required' });
    }

    try {
        // 1. Verify the Google ID Token
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: process.env.GOOGLE_WEB_CLIENT_ID, 
        });
        
        const payload = ticket.getPayload();
        const googleId = payload['sub'];
        const email = payload['email'];
        const name = payload['name'];

        // 2. Upsert User in MongoDB Atlas
        let user = await User.findOne({ googleId });
        
        if (!user) {
            // Create a new user if they don't exist
            user = new User({ googleId, email, name });
            await user.save();
        }

        // 3. Respond with Success
        return res.status(200).json({
            success: true,
            message: 'Authentication successful',
            userId: user._id,
            email: user.email,
            name: user.name
        });

    } catch (error) {
        console.error('Error verifying token:', error);
        return res.status(401).json({ success: false, message: 'Invalid or expired token' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
```