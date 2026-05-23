# 🏦 VaultX — Secure Online Banking System

A production-grade full-stack banking system built with Spring Boot and React.js.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![JWT](https://img.shields.io/badge/Auth-JWT-blue)
![AES](https://img.shields.io/badge/Encryption-AES--256--GCM-red)

## ✨ Features
- 🔐 JWT Authentication with refresh token rotation
- 🔒 AES-256-GCM encryption on all transaction data
- 🤖 Real-time fraud detection engine
- 👥 Role-based access control (USER / ADMIN / AUDITOR)
- 💳 Multi-account management (Savings / Checking)
- 📊 Admin dashboard with fraud alerts
- 📝 Full immutable audit trail

## 🛠️ Tech Stack
| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.4.5 |
| Security | Spring Security, JWT, BCrypt, AES-256-GCM |
| Database | PostgreSQL / H2 |
| ORM | Hibernate JPA |
| Frontend | React.js, HTML5, CSS3 |
| Build | Maven |

## 🚀 Getting Started
```bash
cd backend
mvn clean package -DskipTests
java -jar target/secure-banking-1.0.0.jar
```
Open `http://localhost:8080/index.html`

## 📡 API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/auth/register | Register user |
| POST | /api/v1/auth/login | Login |
| GET | /api/v1/accounts | Get all accounts |
| POST | /api/v1/transactions/transfer | Transfer funds |
| POST | /api/v1/transactions/deposit | Deposit funds |
| GET | /api/v1/transactions/history/{id} | Transaction history |