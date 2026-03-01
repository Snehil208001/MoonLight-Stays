# Manual Deployment Guide

**CI/CD has been disabled** to avoid exhausting AWS free tier (CodePipeline, CodeBuild). Use manual deployment instead.

---

## Step 1: Disable CodePipeline (Stop Free Tier Usage)

**Important**: Do this first to stop automatic runs on every push.

1. **AWS Console** → **CodePipeline** → **airbnb-backend-pipeline**
2. **Option A – Disable trigger**: Edit → Triggers → Remove the Push trigger
3. **Option B – Delete pipeline**: Settings → Delete pipeline (stops all usage)
4. **Option C – Suspend GitHub App**: GitHub → Settings → Installed GitHub Apps → AWS Connector for GitHub → Configure → Suspend

---

## Step 2: Build and Create Deployment Package

From the **project root** (where `deploy-eb.ps1` lives):

```powershell
.\deploy-eb.ps1
```

This will:
- Build the JAR (`mvn clean package -DskipTests`)
- Create `airBnbApp\target\airbnb-eb-deploy.zip` with correct structure (files at root)

### ZIP Structure (Required by Elastic Beanstalk)

**When you open the zip, you must see these at the top level (no parent folder):**

| At root | Description |
|---------|--------------|
| `application.jar` | Spring Boot JAR (Maven outputs this via `finalName=application`) |
| `Procfile` | Must contain: `web: java -Xmx512m -Xms256m -jar application.jar` |
| `.ebextensions/` | Folder with `01_environment.config` |

**Wrong:** `airbnb-eb-deploy/` → `application.jar` (nested folder = deployment fails)  
**Right:** `application.jar`, `Procfile`, `.ebextensions/` directly visible when opening zip

---

## Step 3: Upload to Elastic Beanstalk

1. **AWS Console** → **Elastic Beanstalk** → **Environments** → **Airbnb-backend-env**
2. Click **Upload and deploy**
3. Choose the zip file: `airBnbApp\target\airbnb-eb-deploy.zip`
4. Version label: e.g. `v1-manual-2026-03-01`
5. Click **Deploy**
6. Wait 5–15 minutes for the deployment to complete

---

## Step 4: Verify

```powershell
# Health check
Invoke-RestMethod -Uri "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/actuator/health"

# Search hotels
Invoke-RestMethod -Uri "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/hotels/search" -Method Post -ContentType "application/json" -Body '{"checkInDate":"2026-03-15","endDate":"2026-03-16","guests":2}'
```

Or open: http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html

---

## Environment Variables

Ensure these are set in **EB → Configuration → Software → Environment properties**:

| Variable | Required |
|----------|----------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD` | Yes |
| `JWT_SECRET_KEY` | Yes |
| `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` | Optional |
| `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS` | Optional |

---

## Re-enable CI/CD Later

If you want to use CodePipeline again:
1. Re-create the pipeline (or re-enable if you only disabled it)
2. Ensure `buildspec.yml` is in the repo root
3. Re-add the Git trigger
