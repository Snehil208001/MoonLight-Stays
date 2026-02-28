# AWS Deployment Guide (RDS + Elastic Beanstalk)

Deploy the backend to **AWS RDS** (PostgreSQL) + **AWS Elastic Beanstalk** (Java). No Docker required.

---

## Prerequisites

- AWS account
- AWS CLI installed and configured
- Maven 3.x
- Java 17

---

## Step 1: Create RDS PostgreSQL Database

1. Go to **AWS Console → RDS → Create database**
2. Choose **PostgreSQL**
3. Template: **Free tier** (or Production if needed)
4. Settings:
   - DB instance identifier: `airbnb-db`
   - Master username: `postgres` (or your choice)
   - Master password: **(save this — you'll need it)**
5. Instance: `db.t3.micro` (free tier)
6. Storage: 20 GB
7. **Connectivity**: 
   - VPC: Default
   - Public access: **Yes** (or No if using same VPC as EB)
   - VPC security group: Create new or use existing
8. Database name: `airBnb` (or match `RDS_DB_NAME`)
9. Create database
10. Wait for status **Available**
11. Note the **Endpoint** (e.g. `airbnb-db.xxxxx.us-east-1.rds.amazonaws.com`)

---

## Step 2: Create Elastic Beanstalk Environment

1. Go to **AWS Console → Elastic Beanstalk → Create application**
2. Application name: `airbnb-backend`
3. Environment: **Web server environment**
4. Platform: **Java** → **Corretto 17**
5. Application code: **Upload your code**
6. Create environment (we'll upload the JAR next)

---

## Step 3: Build and Deploy

### Build the JAR

```bash
cd airBnbApp
mvn clean package -DskipTests
```

Output: `target/application.jar`

### Create deployment package (for RDS-linked EB)

If you **add RDS through Elastic Beanstalk** (recommended):

1. In your EB environment → **Configuration** → **Database**
2. Click **Edit** → Add RDS
3. EB will auto-inject `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`

If using **standalone RDS** (created in Step 1), set these manually in **Environment properties**.

### Upload and deploy

**Option A: AWS Console**
1. EB environment → **Upload and deploy**
2. Upload `target/application.jar` (or zip containing it)
3. Deploy

**Option B: AWS CLI**
```bash
cd airBnbApp
mvn clean package -DskipTests
aws elasticbeanstalk create-application-version \
  --application-name airbnb-backend \
  --version-label v1 \
  --source-bundle S3Bucket=your-bucket,S3Key=application.jar
aws elasticbeanstalk update-environment \
  --environment-name your-env-name \
  --version-label v1
```

---

## Step 4: Set Environment Variables

In **Elastic Beanstalk → Configuration → Software → Environment properties**, add:

| Name | Value |
|------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET_KEY` | *(your JWT secret)* |
| `STRIPE_SECRET_KEY` | *(Stripe live key)* |
| `STRIPE_WEBHOOK_SECRET` | *(Stripe prod webhook secret)* |
| `MAIL_USERNAME` | *(your email)* |
| `MAIL_PASSWORD` | *(app password)* |
| `FRONTEND_URL` | `https://your-frontend-domain.com` |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend-domain.com` |

**If using standalone RDS** (not linked through EB), also add:

| Name | Value |
|------|-------|
| `RDS_HOSTNAME` | `your-db.xxxxx.us-east-1.rds.amazonaws.com` |
| `RDS_PORT` | `5432` |
| `RDS_DB_NAME` | `airBnb` |
| `RDS_USERNAME` | `postgres` |
| `RDS_PASSWORD` | *(your DB password)* |

---

## Step 5: RDS Security Group

Ensure your RDS security group allows inbound **PostgreSQL (5432)** from:
- Your EB environment's security group, OR
- Your IP (for debugging)

---

## Step 6: Stripe Webhook (Production)

1. Stripe Dashboard → Developers → Webhooks
2. Add endpoint: `https://your-eb-url/api/v1/webhooks/payment`
3. Select events: `checkout.session.completed`, etc.
4. Copy **Signing secret** → set as `STRIPE_WEBHOOK_SECRET`

---

## Quick Deploy Script (PowerShell)

```powershell
cd airBnbApp
mvn clean package -DskipTests
# Then upload target/application.jar via EB Console
```

---

## Post-Deploy

- Your API will be at: `http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1`
- **Frontend integration**: In `moonlight-stays/.env.local`, set:
  ```
  NEXT_PUBLIC_API_URL=http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1
  ```
- **CORS**: For local frontend dev, `http://localhost:3000` is allowed by default. For a deployed frontend, add its URL to `CORS_ALLOWED_ORIGINS` in EB.
- Test: `curl https://your-env.elasticbeanstalk.com/api/v1/hotels/search -X POST -H "Content-Type: application/json" -d '{"city":"","checkInDate":"2026-03-01","endDate":"2026-03-02","roomsCount":1,"page":0,"size":5}'`

---

---

## Amplify Frontend (Next.js Monorepo)

The frontend lives in `moonlight-stays/`. For monorepo deployments, Amplify needs `AMPLIFY_MONOREPO_APP_ROOT` to find the build output and generate `deploy-manifest.json`.

### Fix: "Failed to find deploy-manifest.json"

1. **In `amplify.yml`** (already set): `AMPLIFY_MONOREPO_APP_ROOT: moonlight-stays` under `env.variables`.
2. **In Amplify Console** (required for existing apps):
   - Amplify Console → Your app → **Hosting** → **Environment variables** → **Manage variables**
   - Add: `AMPLIFY_MONOREPO_APP_ROOT` = `moonlight-stays`
   - Save and **Redeploy** the app.

### Environment variables for frontend

In Amplify → Environment variables, add:

| Name | Value |
|------|-------|
| `NEXT_PUBLIC_API_URL` | `http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1` |

For production with HTTPS, use your backend URL (e.g. `https://api.yourdomain.com/api/v1`).

---

## Notes

- **File uploads**: Stored in `uploads/` on the EB instance. Ephemeral — lost on redeploy. For production, consider S3.
- **HTTPS**: EB provides a URL; for custom domain + SSL, use a load balancer or CloudFront.
