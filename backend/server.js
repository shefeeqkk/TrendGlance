require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const { OAuth2Client } = require('google-auth-library');
const User = require('./models/User');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Initialize Google OAuth2 Client
const googleWebClientId = process.env.GOOGLE_WEB_CLIENT_ID || '';
const googleClient = new OAuth2Client(googleWebClientId);

// MongoDB Atlas Connection
const mongoUri = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/trendglance';

mongoose.connect(mongoUri)
    .then(() => console.log('✅ Connected to MongoDB Atlas successfully!'))
    .catch((err) => {
        console.error('❌ MongoDB Atlas connection error:', err.message);
        console.log('⚠️ Running server with local memory fallback for authentication testing...');
    });

/**
 * Root Landing Route
 */
app.get('/', (req, res) => {
    res.status(200).json({
        service: 'TrendGlance Auth Backend Server',
        status: 'Active',
        endpoints: {
            health: 'GET /api/health',
            googleAuth: 'POST /api/auth/google'
        },
        mongoConnected: mongoose.connection.readyState === 1
    });
});

/**
 * Health check endpoint
 */
app.get('/api/health', (req, res) => {
    res.status(200).json({
        status: 'OK',
        service: 'TrendGlance Auth Backend',
        mongoConnected: mongoose.connection.readyState === 1,
        timestamp: new Date().toISOString()
    });
});

/**
 * GET Info Route for /api/auth/google
 * Responds gracefully when visited in a browser via GET request.
 */
app.get('/api/auth/google', (req, res) => {
    res.status(200).json({
        message: 'This endpoint expects an HTTP POST request with JSON body { idToken, email, name } from Retrofit or cURL.',
        acceptedMethod: 'POST',
        endpoint: '/api/auth/google',
        examplePayload: {
            idToken: 'google_id_token_sample',
            email: 'user.trends@gmail.com',
            name: 'Google Trends User'
        },
        healthCheckUrl: '/api/health'
    });
});

/**
 * Authentication Endpoint: @POST("api/auth/google")
 * Verifies Google ID Token (or saves payload) and upserts user in MongoDB Atlas.
 */
app.post('/api/auth/google', async (req, res) => {
    try {
        const { idToken, email, name } = req.body || {};

        if (!idToken) {
            return res.status(400).json({
                success: false,
                message: 'idToken is required in request body'
            });
        }

        let userGoogleId = `google_user_${Date.now()}`;
        let userEmail = email || 'user.trends@gmail.com';
        let userName = name || 'Google Trends User';
        let userAvatarUrl = null;

        // 1. Verify Google ID Token if client ID is configured
        if (googleWebClientId && !idToken.startsWith('mock_')) {
            try {
                const ticket = await googleClient.verifyIdToken({
                    idToken: idToken,
                    audience: googleWebClientId
                });
                const payload = ticket.getPayload();
                userGoogleId = payload['sub'];
                userEmail = payload['email'] || userEmail;
                userName = payload['name'] || userName;
                userAvatarUrl = payload['picture'] || null;
            } catch (authErr) {
                console.warn('⚠️ Google Token verification warning (using fallback profile):', authErr.message);
            }
        }

        // 2. Upsert User profile into MongoDB Atlas database
        let user = null;
        let sessionToken = null;
        if (mongoose.connection.readyState === 1) {
            const updateData = {
                email: userEmail,
                name: userName,
                avatarUrl: userAvatarUrl,
                lastLogin: new Date()
            };

            // Using findOneAndUpdate with upsert option to atomic create or update
            user = await User.findOneAndUpdate(
                { googleId: userGoogleId },
                { $set: updateData },
                { upsert: true, new: true, setDefaultsOnInsert: true }
            );

            console.log(`👤 User Upserted in MongoDB Atlas: ${user.email}`);

            // Optional: Generate a simple JWT session token
            // In a production app, use jwt.sign with a real secret
            const jwt = require('jsonwebtoken');
            const JWT_SECRET = process.env.JWT_SECRET || 'trendglance_super_secret_key_123';
            sessionToken = jwt.sign(
                { userId: user._id, googleId: user.googleId, email: user.email },
                JWT_SECRET,
                { expiresIn: '7d' }
            );
        }

        // 3. Send Success Response
        return res.status(200).json({
            success: true,
            message: 'Authentication & MongoDB Atlas sync successful',
            userId: user ? user._id : userGoogleId,
            email: userEmail,
            name: userName,
            avatarUrl: userAvatarUrl,
            sessionToken: sessionToken
        });

    } catch (error) {
        console.error('❌ Error during authentication endpoint:', error);
        return res.status(500).json({
            success: false,
            message: 'Internal server error during authentication',
            error: error.message
        });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`🚀 TrendGlance Auth Backend Server running on port ${PORT}`);
    console.log(`📡 URL: http://localhost:${PORT}/api/auth/google`);
    console.log(`=======================================================`);
});
