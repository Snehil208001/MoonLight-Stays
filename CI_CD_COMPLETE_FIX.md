# CI/CD Complete Fix Guide — MoonLight Stays Backend

This guide walks you through fixing the CI/CD pipeline so deployments succeed consistently.

---

## Part 1: Elastic Beanstalk Environment Variables (Critical)

Deployment fails when required variables are missing or wrong. Set these in **EB → Configuration → Software → Environment properties**:

### Required (app will not start without these)

| Variable | Example | Notes |
|----------|---------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production config |
| `RDS_HOSTNAME` | `your-db.xxxxx.ap-south-1.rds.amazonaws.com` | RDS endpoint |
| `RDS_PORT` | `5432` | PostgreSQL port |
| `RDS_DB_NAME` | `postgres` or `airBnb` | Database name |
| `RDS_USERNAME` | `postgres` | DB username |
| `RDS_PASSWORD` | `your-secure-password` | DB password |
| `JWT_SECRET_KEY` | `your-32-char-secret` | Min 256 bits for HS256 |

### Optional (app starts without; features fail at runtime)

| Variable | Purpose |
|----------|---------|
| `STRIPE_SECRET_KEY` | Stripe payments |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhooks |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Booking confirmation emails |
| `FRONTEND_URL` | `https://main.d30tl6vi1qydms.amplifyapp.com` |
| `CORS_ALLOWED_ORIGINS` | `https://main.d30tl6vi1qydms.amplifyapp.com` |

---

## Part 2: RDS Security Group (Critical)

EB instances must reach RDS on port 5432.

1. **RDS** → Your database → **Connectivity & security** → **VPC security group**
2. Open that security group → **Edit inbound rules**
3. Add rule:
   - Type: **PostgreSQL**
   - Port: **5432**
   - Source: **EB environment security group** (or the security group of your EB instances)

To find the EB security group: **EB → Configuration → Security** → note the EC2 security group, then use it as the source in the RDS rule.

---

## Part 3: Re-enable Pipeline Trigger (After Fixing EB)

If you disabled the Git trigger to stop the restart loop:

1. **CodePipeline** → **airbnb-backend-pipeline** → **Edit**
2. Go to **Triggers** (or **Edit** on Source stage)
3. **Add trigger** → **Push** → **Include branches: main**
4. Save

---

## Part 4: Verify Deployment

### 1. Trigger a deployment

- Push to `main`, or
- **CodePipeline** → **Release change**

### 2. Watch the pipeline

- **Source** → **Build** → **Deploy** should all turn green
- Deploy typically takes 5–15 minutes

### 3. Test the API

```powershell
# Health check
Invoke-RestMethod -Uri "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/actuator/health"

# Search hotels
Invoke-RestMethod -Uri "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/hotels/search" -Method Post -ContentType "application/json" -Body '{"checkInDate":"2026-03-15","endDate":"2026-03-16","guests":2}'
```

Or open: http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html

---

## Part 5: If Deployment Still Fails

### Get logs

1. **CloudWatch** → **Log groups** → search `elasticbeanstalk` or `Airbnb-backend-env`
2. Open the latest log stream
3. Look for:
   - `Connection refused` → RDS security group or wrong host/port
   - `Authentication failed` → Wrong RDS username/password
   - `JWT_SECRET_KEY` or `Could not resolve placeholder` → Missing env var
   - `OutOfMemoryError` → Consider increasing instance size or JVM heap

### EB configuration

- **EB → Configuration → Capacity** → Ensure at least 1 instance
- **EB → Configuration → Updates, monitoring, and logging** → Health reporting: **Enhanced**

---

## Summary Checklist

- [ ] All required env vars set in EB
- [ ] RDS security group allows EB instances on port 5432
- [ ] Pipeline trigger re-enabled (if you disabled it)
- [ ] Push or Release change to deploy
- [ ] API responds at `/api/v1/actuator/health` and `/api/v1/hotels/search`
