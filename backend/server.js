require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const path = require('path');
const Parser = require('rss-parser');
const User = require('./models/User');

const app = express();

// RSS Parser for Admin Dashboard
const rssParser = new Parser({
    customFields: {
        item: ['ht:approx_traffic', 'ht:news_item_title', 'ht:news_item_snippet', 'ht:picture']
    }
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public'))); // Serve static files for Admin Panel

// MongoDB Atlas Connection
const mongoUri = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/trendglance';

mongoose.connect(mongoUri)
    .then(() => console.log('✅ Connected to MongoDB Atlas successfully!'))
    .catch((err) => {
        console.error('❌ MongoDB Atlas connection error:', err.message);
    });

const JWT_SECRET = process.env.JWT_SECRET || 'trendglance_super_secret_key_123';

/**
 * Root Landing Route
 */
app.get('/', (req, res) => {
    res.status(200).json({
        service: 'TrendGlance Auth Backend Server',
        status: 'Active',
        endpoints: {
            health: 'GET /api/health',
            register: 'POST /api/auth/register',
            login: 'POST /api/auth/login'
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
 * Registration Endpoint: @POST("api/auth/register")
 */
app.post('/api/auth/register', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ success: false, message: 'Name, email, and password are required' });
        }

        if (mongoose.connection.readyState !== 1) {
            return res.status(500).json({ success: false, message: 'Database not connected' });
        }

        // Check if user already exists
        const existingUser = await User.findOne({ email: email.toLowerCase().trim() });
        if (existingUser) {
            return res.status(409).json({ success: false, message: 'Email already registered' });
        }

        // Hash password
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Create user
        const newUser = new User({
            name: name.trim(),
            email: email.toLowerCase().trim(),
            password: hashedPassword
        });

        await newUser.save();
        console.log(`👤 New user registered in MongoDB Atlas: ${newUser.email}`);

        // Generate Token
        const sessionToken = jwt.sign(
            { userId: newUser._id, email: newUser.email },
            JWT_SECRET,
            { expiresIn: '7d' }
        );

        return res.status(201).json({
            success: true,
            message: 'User registered successfully',
            userId: newUser._id,
            email: newUser.email,
            name: newUser.name,
            sessionToken: sessionToken
        });

    } catch (error) {
        console.error('❌ Error during registration:', error);
        return res.status(500).json({ success: false, message: 'Internal server error', error: error.message });
    }
});

/**
 * Login Endpoint: @POST("api/auth/login")
 */
app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ success: false, message: 'Email and password are required' });
        }

        if (mongoose.connection.readyState !== 1) {
            return res.status(500).json({ success: false, message: 'Database not connected' });
        }

        // Find user
        const user = await User.findOne({ email: email.toLowerCase().trim() });
        if (!user) {
            return res.status(401).json({ success: false, message: 'Invalid credentials' });
        }

        // Check password
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ success: false, message: 'Invalid credentials' });
        }

        // Update last login
        user.lastLogin = new Date();
        await user.save();
        console.log(`🔄 User logged in via MongoDB Atlas: ${user.email}`);

        // Generate Token
        const sessionToken = jwt.sign(
            { userId: user._id, email: user.email },
            JWT_SECRET,
            { expiresIn: '7d' }
        );

        return res.status(200).json({
            success: true,
            message: 'Login successful',
            userId: user._id,
            email: user.email,
            name: user.name,
            sessionToken: sessionToken
        });

    } catch (error) {
        console.error('❌ Error during login:', error);
        return res.status(500).json({ success: false, message: 'Internal server error', error: error.message });
    }
});

// ==========================================
// ADMIN DASHBOARD ENDPOINTS
// ==========================================

/**
 * Get all registered users and activity metrics
 */
app.get('/api/admin/users', async (req, res) => {
    try {
        if (mongoose.connection.readyState !== 1) {
            return res.status(500).json({ success: false, message: 'Database not connected' });
        }

        const users = await User.find().sort({ createdAt: -1 }).select('-password');
        const totalUsers = users.length;

        // Calculate active users (logged in within the last 24 hours)
        const activeUsers = users.filter(u => {
            const timeDiff = new Date() - new Date(u.lastLogin);
            return timeDiff < 24 * 60 * 60 * 1000;
        }).length;

        return res.status(200).json({
            success: true,
            totalUsers,
            activeUsers,
            users
        });
    } catch (error) {
        console.error('❌ Error fetching admin users:', error);
        return res.status(500).json({ success: false, error: error.message });
    }
});

/**
 * Get live Google Trends for the Admin Panel
 */
app.get('/api/admin/trends', async (req, res) => {
    try {
        const geo = req.query.geo || 'US';
        const feed = await rssParser.parseURL(`https://trends.google.com/trending/rss?geo=${geo}&hl=en`);

        const trends = feed.items.map(item => ({
            title: item.title,
            traffic: item['ht:approx_traffic'] || '100K+',
            newsTitle: item['ht:news_item_title'] || item.title,
            snippet: item['ht:news_item_snippet'] || 'No snippet available',
            pubDate: item.pubDate,
            link: item.link,
            picture: item['ht:picture'] || null
        }));

        return res.status(200).json({ success: true, trends });
    } catch (error) {
        console.error('❌ Error fetching admin trends:', error);
        return res.status(500).json({ success: false, error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`🚀 TrendGlance Auth Backend Server running on port ${PORT}`);
    console.log(`📡 API URL: http://localhost:${PORT}`);
    console.log(`📊 ADMIN DASHBOARD: http://localhost:${PORT}/admin`);
    console.log(`=======================================================`);
});
