# Elastic Beanstalk Environment Variables

Add these in **Configuration → Software → Environment properties** → **Edit**.

---

## Memory (for t3.micro - prevents OOM)

| Name | Value | Purpose |
|------|-------|---------|
| `JAVA_OPTS` | `-Xms256m -Xmx384m` | Limit JVM heap so OS/EB agents have RAM |

*Also set in `.ebextensions/01_environment.config`. Procfile uses same limits.*

---

## Required (app won't start without these)

| Name | Value | Where to get it |
|------|-------|----------------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Fixed value |
| `RDS_HOSTNAME` | `your-db.xxxxx.ap-south-1.rds.amazonaws.com` | RDS → Databases → your DB → Endpoint |
| `RDS_PORT` | `5432` | Fixed (PostgreSQL) |
| `RDS_DB_NAME` | `airBnb` | Your DB name (or what you used in RDS) |
| `RDS_USERNAME` | `postgres` | RDS master username |
| `RDS_PASSWORD` | *(your password)* | RDS master password |
| `JWT_SECRET_KEY` | *(32+ char random string)* | Generate: `openssl rand -base64 32` |

---

## Optional (app starts without; add for full features)

| Name | Value | Purpose |
|------|-------|---------|
| `STRIPE_SECRET_KEY` | `sk_live_...` or `sk_test_...` | Stripe payments |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` | Stripe webhooks |
| `FRONTEND_URL` | `https://your-frontend.com` | Stripe redirects |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.com` | CORS for API calls |
| `MAIL_USERNAME` | your-email@gmail.com | Email notifications |
| `MAIL_PASSWORD` | app-specific password | Email (Gmail: use App Password) |

---

## Quick copy-paste checklist

```
SPRING_PROFILES_ACTIVE = prod
RDS_HOSTNAME = <from RDS console>
RDS_PORT = 5432
RDS_DB_NAME = airBnb
RDS_USERNAME = postgres
RDS_PASSWORD = <your password>
JWT_SECRET_KEY = <generate with: openssl rand -base64 32>
```

---

## Generate JWT_SECRET_KEY (PowerShell)

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

Or use any 32+ character random string (letters + numbers).
