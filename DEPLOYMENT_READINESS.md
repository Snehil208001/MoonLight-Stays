# Deployment Readiness Report

**Status: NOT READY** — Critical issues must be fixed before production deployment.

## Critical (Must Fix)

1. **Hardcoded Secrets** — Move DB password, JWT secret, Stripe keys, email credentials to environment variables
2. **CORS** — Add production frontend URL
3. **Frontend Config** — Use env vars for API URL in next.config.js
4. **Stripe Production** — Create real webhook endpoint in Stripe Dashboard
5. **Database** — Use `ddl-auto=validate` and `show-sql=false` in production
6. **File Uploads** — Use configurable path or object storage

## High Priority

- Disable data seeding in production (`app.seed.enabled=false`)
- Restrict or disable Swagger in production
- Sanitize error messages in 500 responses

## Pre-Deployment Checklist

- [ ] Move all secrets to environment variables
- [ ] Configure CORS for production frontend URL
- [ ] Set `NEXT_PUBLIC_API_URL` for production
- [ ] Create Stripe production webhook
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` in prod
- [ ] Set `app.seed.enabled=false` in prod
- [ ] Use production Stripe keys (not `sk_test_`)
