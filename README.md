<div align="center">

# 🌙 Moonlight Stays

### Airbnb-style Hotel Booking Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![AWS](https://img.shields.io/badge/AWS-Amplify%20%7C%20EB%20%7C%20RDS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
[![Stripe](https://img.shields.io/badge/Stripe-Payments-635BFF?style=for-the-badge&logo=stripe&logoColor=white)](https://stripe.com/)

**Full-stack hotel booking application** with dynamic pricing, Stripe payments, role-based access, and production AWS deployment.

[Live Demo](https://main.d30tl6vi1qydms.amplifyapp.com) · [API Docs](http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html) · [Report Bug](https://github.com/Snehil208001/MoonLight-Stays/issues) · [Request Feature](https://github.com/Snehil208001/MoonLight-Stays/issues)

</div>

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Live Demo](#-live-demo)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Deployment](#-deployment)
- [API Overview](#-api-overview)
- [Documentation](#-documentation)
- [License](#-license)

---

## 🎯 About the Project

**Moonlight Stays** is a production-ready, full-stack hotel booking platform inspired by Airbnb. Built from scratch with modern technologies, it demonstrates end-to-end ownership from UI design to database schema, payment integration, and cloud deployment.

### Key Highlights

| Aspect | Implementation |
|--------|----------------|
| **Backend** | Java 17, Spring Boot 3.5, Spring Data JPA, Spring Security (JWT) |
| **Frontend** | Next.js 14 (App Router), React 18, TypeScript, Redux Toolkit, Tailwind CSS, Framer Motion |
| **Database** | PostgreSQL with complex relational models (hotels, rooms, bookings, reviews, promo codes) |
| **Payments** | Stripe Checkout with webhook verification |
| **Auth** | JWT with refresh tokens, role-based access (Guest / Hotel Manager) |
| **Design Patterns** | Strategy pattern for dynamic room pricing (surge, holiday, occupancy-based) |
| **DevOps** | AWS Amplify (frontend), Elastic Beanstalk (backend), RDS (database), custom deployment scripts |

---

## 🚀 Live Demo

| Resource | URL |
|----------|-----|
| **Frontend** | [https://main.d30tl6vi1qydms.amplifyapp.com](https://main.d30tl6vi1qydms.amplifyapp.com) |
| **Backend API** | [http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1](http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1) |
| **API Docs (Swagger)** | [http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html](http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html) |

---

## ✨ Features

### For Guests 👤

| Feature | Description |
|---------|-------------|
| **Search Hotels** | Filter by city, check-in/out dates, rooms, guests, price range, amenities |
| **Hotel Details** | Photos, amenities, room types, dynamic pricing based on dates |
| **Bookings** | Multi-step flow with guest details, promo codes, and Stripe checkout |
| **Reviews & Ratings** | Add and view hotel reviews with star ratings |
| **Favorites** | Save hotels for later with persistent storage |
| **My Bookings** | View, filter, and cancel bookings with status tracking |

### For Hotel Managers 🏨

| Feature | Description |
|---------|-------------|
| **Admin Dashboard** | Manage hotels, rooms, and promo codes from a dedicated interface |
| **CRUD Operations** | Create, update, delete hotels and rooms with validation |
| **Surge Pricing** | Set dynamic pricing for date ranges (holidays, events) |
| **Image Uploads** | Add hotel and room photos with preview |
| **Promo Codes** | Create discount codes with percentage or fixed amount |

### Technical Features ⚙️

- **Strategy Design Pattern** — Handles complex, dynamic room pricing (surge, holiday, occupancy-based)
- **JWT Authentication** — Secure auth with refresh token rotation
- **Stripe Webhooks** — Server-side payment confirmation
- **CORS & Proxy** — Next.js rewrites proxy API calls to avoid mixed-content issues
- **Responsive UI** — Dark-mode glassmorphism design with Framer Motion animations

---

## 🛠 Tech Stack

### Backend

| Technology | Purpose |
|------------|---------|
| **Java 17** | Core language |
| **Spring Boot 3.5** | REST API framework |
| **Spring Data JPA** | Database access, repositories |
| **Spring Security** | JWT auth, role-based access |
| **PostgreSQL** | Relational database |
| **Stripe Java SDK** | Payment processing |
| **SpringDoc OpenAPI** | Swagger/OpenAPI documentation |

### Frontend

| Technology | Purpose |
|------------|---------|
| **Next.js 14** | React framework, App Router |
| **TypeScript** | Type safety |
| **Redux Toolkit** | State management |
| **Tailwind CSS** | Styling |
| **Framer Motion** | Animations |
| **Lucide React** | Icons |

### DevOps & Deployment

| Service | Purpose |
|---------|---------|
| **AWS Amplify** | Frontend hosting, CDN, HTTPS |
| **AWS Elastic Beanstalk** | Backend hosting, EC2, load balancer |
| **Amazon RDS** | Managed PostgreSQL |
| **Stripe** | Payment processing |

---

## 🏗 Architecture

```
┌─────────────────────┐         HTTPS          ┌──────────────────────┐         HTTP          ┌─────────────────────────┐
│   AWS Amplify       │ ◄────────────────────► │  Next.js Frontend    │ ◄───────────────────► │  Elastic Beanstalk      │
│   (CDN + Hosting)   │                         │  /api/v1 → proxy     │                       │  (Spring Boot API)      │
└─────────────────────┘                         └──────────────────────┘                       └────────────┬────────────┘
                                                                                                                    │
                                                                                                                    │ JDBC
                                                                                                                    ▼
                                                                                                         ┌─────────────────────────┐
                                                                                                         │   Amazon RDS             │
                                                                                                         │   (PostgreSQL)           │
                                                                                                         └─────────────────────────┘
```

### Request Flow

1. **User** visits Amplify URL (HTTPS) → Next.js serves the React app
2. **API calls** go to `/api/v1/*` → Next.js rewrites proxy to Elastic Beanstalk backend
3. **Backend** validates JWT, processes request, queries RDS
4. **Stripe** webhooks notify backend on payment completion

### Why This Architecture?

- **Next.js proxy** — Avoids CORS and mixed-content (HTTPS frontend → HTTP backend)
- **RDS** — Managed PostgreSQL with backups and scaling
- **Elastic Beanstalk** — Handles EC2, load balancer, auto-scaling for Java

---

## 📁 Project Structure

```
MoonLight-Stays/
├── airBnbApp/                      # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/moonlight/project/airBnbApp/
│   │       ├── config/             # Security, CORS, Stripe, OpenAPI
│   │       ├── controller/         # REST controllers
│   │       ├── entity/             # JPA entities
│   │       ├── repository/         # Spring Data repositories
│   │       ├── service/             # Business logic (Strategy pattern for pricing)
│   │       └── security/            # JWT, WebSecurityConfig
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-prod.properties
│   ├── .ebextensions/              # EB config (swap, timeout, env)
│   ├── Procfile                    # Java run command
│   └── pom.xml
│
├── moonlight-stays/                # Next.js frontend
│   ├── src/
│   │   ├── app/                    # Pages, layouts (App Router)
│   │   ├── components/             # React components
│   │   └── lib/                    # API client, utilities
│   ├── public/
│   └── package.json
│
├── amplify.yml                     # Amplify build config
├── deploy-eb.ps1                   # Build + create EB deployment zip
├── AWS_DEPLOYMENT.md               # Full AWS deployment guide
├── MANUAL_DEPLOYMENT.md            # Manual deployment steps
├── EB_ENV_VARIABLES.md             # Environment variables reference
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Node.js** 18+ and npm
- **Java** 17 and Maven
- **PostgreSQL** (or H2 for dev)
- **Stripe CLI** (optional, for local webhook testing)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/Snehil208001/MoonLight-Stays.git
cd MoonLight-Stays

# Install dependencies and run everything
npm install
npm run dev
```

This starts:
- **Backend** — `http://localhost:8080`
- **Frontend** — `http://localhost:3000`
- **Stripe webhook listener** — For local payment testing

### Run Individually

```bash
# Backend only
cd airBnbApp && mvn spring-boot:run

# Frontend only
cd moonlight-stays && npm run dev

# Stripe webhooks (for payments)
stripe listen --forward-to localhost:8080/api/v1/webhooks/payment
```

### Environment Variables

Create `moonlight-stays/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

---

## ☁️ Deployment

### AWS Services

| Service | Purpose |
|---------|---------|
| **AWS Amplify** | Hosts Next.js frontend, builds on GitHub push, serves over HTTPS |
| **Elastic Beanstalk** | Runs Spring Boot backend on EC2 with load balancer |
| **Amazon RDS** | Managed PostgreSQL for production data |

### Deploy Backend (Elastic Beanstalk)

```powershell
# From project root
.\deploy-eb.ps1
```

Then upload `airBnbApp/target/airbnb-eb-deploy.zip` via EB Console → **Upload and deploy**.

### Deploy Frontend (Amplify)

Push to GitHub — Amplify auto-builds from `amplify.yml`.

### Region & URLs

- **Region**: `ap-south-1` (Mumbai)
- **Frontend**: https://main.d30tl6vi1qydms.amplifyapp.com
- **Backend**: http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com

📖 **Detailed guides**: [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) · [MANUAL_DEPLOYMENT.md](./MANUAL_DEPLOYMENT.md) · [EB_ENV_VARIABLES.md](./EB_ENV_VARIABLES.md)

---

## 📡 API Overview

| Area | Endpoints |
|------|-----------|
| **Auth** | `POST /auth/signup`, `POST /auth/login`, `POST /auth/refresh` |
| **Hotels** | `POST /hotels/search`, `GET /hotels/{id}/info` |
| **Bookings** | `POST /bookings/init`, `POST /bookings/{id}/payments` |
| **Admin** | `POST /admin/hotels`, `GET /admin/hotels`, `PATCH /admin/hotels/{id}/surge` |

**Full API docs**: [Swagger UI](http://v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html)

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) | Step-by-step AWS deployment (RDS, EB, Amplify) |
| [MANUAL_DEPLOYMENT.md](./MANUAL_DEPLOYMENT.md) | Manual deployment with deploy-eb.ps1 |
| [EB_ENV_VARIABLES.md](./EB_ENV_VARIABLES.md) | Elastic Beanstalk environment variables |
| [RDS_SECURITY_GROUP_SETUP.md](./RDS_SECURITY_GROUP_SETUP.md) | RDS security group for EB access |
| [ZIP_DEPLOY_CHECKLIST.md](./ZIP_DEPLOY_CHECKLIST.md) | Zip structure checklist |
| [DEV_SETUP.md](./DEV_SETUP.md) | Local development setup |
| [ROLE_BASED_FUNCTIONALITY.md](./ROLE_BASED_FUNCTIONALITY.md) | API endpoints by user role |
| [API_INTEGRATION_PLAN.md](./API_INTEGRATION_PLAN.md) | API integration details |

---

## 🌟 Highlights for Recruiters

- **Full-stack ownership** — UI, API, database, deployment
- **Production deployment** — Live on AWS (Amplify, EB, RDS, S3, EC2, Load Balancer)
- **Modern stack** — Spring Boot 3, Next.js 14, TypeScript
- **Payments** — Stripe Checkout with webhook integration
- **Design patterns** — Strategy pattern for dynamic pricing
- **Auth** — JWT with refresh tokens, role-based access
- **Responsive UI** — Dark-mode glassmorphism, Framer Motion

---

## 📄 License

Private project — All rights reserved.

---

<div align="center">

**Built with ❤️ by [Snehil](https://github.com/Snehil208001)**

[⬆ Back to Top](#-moonlight-stays)

</div>
