# Musify

Full-stack music streaming platform built with Kotlin.

## Structure

- **[backend/](backend/)** — Ktor REST API with PostgreSQL, Redis, JWT auth, Stripe payments, S3 storage, and real-time streaming
- **[frontend/](frontend/)** — Android app with Jetpack Compose, ExoPlayer, Hilt DI, and Material3

## Quick Start

### Backend
```bash
cd backend
./gradlew run
```
Server starts on http://localhost:8080

### Frontend
Open `frontend/` in Android Studio and run on an emulator. The debug build connects to `http://10.0.2.2:8080` (emulator's host loopback).

See each directory's README for detailed setup instructions.
