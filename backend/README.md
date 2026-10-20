# TrendGlance Auth & MongoDB Atlas Backend Server 🚀

This is the Node.js / Express backend server for **TrendGlance**, providing the `@POST("api/auth/google")` API endpoint that receives Google authentication tokens from the Android app and persists user profiles in **MongoDB Atlas**.

---

## 🛠️ Features

- 🔐 **Google OAuth Token Verification**: Verifies `idToken` using official `google-auth-library`.
- 🍃 **MongoDB Atlas Mongoose Database**: Upserts user accounts (`googleId`, `email`, `name`, `lastLogin`).
- ⚡ **Resilient Fallback**: Handles test tokens and developer fallback authentication seamlessly.

---

## 🚀 Quick Start Instructions

### 1. Install Dependencies
Navigate to the `backend` folder and install Node packages:
```bash
cd backend
npm install
```

### 2. Configure Environment Variables
Edit `.env` (or copy from `.env.example`):
```env
PORT=3000
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/trendglance?retryWrites=true&w=majority
GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
```

### 3. Start the Server
Run in production or development mode:
```bash
# Production mode
npm start

# Development mode (with auto-reload)
npm run dev
```

The server will start at `http://localhost:3000`.
Health check endpoint: `http://localhost:3000/api/health`
Auth endpoint: `http://localhost:3000/api/auth/google`
