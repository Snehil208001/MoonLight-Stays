# AWS CodePipeline CI/CD Setup

This guide sets up an automated CI/CD pipeline for the **Moonlight Stays Backend** (Spring Boot) using AWS CodePipeline, CodeBuild, and Elastic Beanstalk.

**Repository:** [https://github.com/Snehil208001/MoonLight-Stays](https://github.com/Snehil208001/MoonLight-Stays)

> **Note:** Ensure `buildspec.yml` exists in the MoonLight-Stays repo root. Copy it from this project if needed.

## Architecture

```
[GitHub/CodeCommit] → [CodePipeline] → [CodeBuild] → [Elastic Beanstalk]
       Source              Build           Deploy
```

- **Source**: Your Git repository (GitHub or AWS CodeCommit)
- **Build**: CodeBuild runs `buildspec.yml` → produces `application.jar`
- **Deploy**: Elastic Beanstalk receives the JAR and deploys

---

## Prerequisites

1. **Elastic Beanstalk** application and environment already created (see `AWS_DEPLOYMENT.md`)
2. **GitHub** connected to AWS CodePipeline (OAuth or connection for `Snehil208001/MoonLight-Stays`)
3. **`buildspec.yml`** in the repo root (same structure as this project: `airBnbApp/`, `moonlight-stays/`)
4. **IAM permissions** for CodePipeline, CodeBuild, and Elastic Beanstalk

---

## Step 1: Create CodeBuild Project

1. Go to **AWS Console → CodeBuild → Build projects → Create build project**

2. **Project configuration**
   - Project name: `airbnb-backend-build`
   - Description: (optional)

3. **Source**
   - Source provider: **GitHub (Version 2)**
   - Repository: `Snehil208001/MoonLight-Stays` or connect and select it
   - Reference type: **Branch**
   - Branch: `main` (or your default branch)

4. **Environment**
   - Environment image: **Managed image**
   - Operating system: **Amazon Linux 2**
   - Runtime: **Standard**
   - Image: **aws/codebuild/amazonlinux2-x86_64-standard:5.0**
   - Privileged: **No**
   - Service role: **New service role** (or existing with required permissions)
   - Role name: `codebuild-airbnb-backend-service-role`

5. **Buildspec**
   - Build specifications: **Use a buildspec file**
   - Buildspec name: `buildspec.yml` (root of repo)

6. **Artifacts**
   - Type: **No artifacts** (CodePipeline will handle artifacts via buildspec output)

7. **Logs**
   - CloudWatch logs: **Enabled** (recommended)

8. Click **Create build project**

---

## Step 2: Create CodePipeline

1. Go to **AWS Console → CodePipeline → Pipelines → Create pipeline**

2. **Pipeline settings**
   - Pipeline name: `airbnb-backend-pipeline`
   - Service role: **New service role**
   - Role name: `codepipeline-airbnb-backend-role`
   - Artifact store: **Default location** (S3 bucket created automatically)
   - Encryption: **Default (AWS managed key)**

3. **Add source stage**
   - Source provider: **GitHub (Version 2)**
   - Repository: `Snehil208001/MoonLight-Stays`
   - Branch: `main`
   - Change detection: **Amazon CloudWatch Events** (recommended)
   - Output artifact format: **CodePipeline default**
   - Click **Next**

4. **Add build stage**
   - Build provider: **AWS CodeBuild**
   - Region: Same as pipeline (e.g. `ap-south-1`)
   - Project name: `airbnb-backend-build`
   - Build specification: **Use the buildspec in the source code root**
   - Input artifact: Select the source output (e.g. `SourceArtifact`)
   - Output artifact name: `BuildArtifact`
   - Click **Next**

5. **Add deploy stage**
   - Deploy provider: **AWS Elastic Beanstalk**
   - Application name: `airbnb-backend` (or your EB app name)
   - Environment name: Your EB environment (e.g. `Airbnb-backend-env`)
   - Input artifact: `BuildArtifact`
   - Click **Next**

6. **Review** and click **Create pipeline**

---

## Step 3: IAM Permissions (if using custom roles)

Ensure the **CodeBuild service role** has:
- `codebuild:*`
- `s3:GetObject`, `s3:PutObject` on the pipeline artifact bucket
- `logs:CreateLogGroup`, `logs:CreateLogStream`, `logs:PutLogEvents`

Ensure the **CodePipeline service role** has:
- `codepipeline:*`
- `codebuild:BatchGetBuilds`, `codebuild:StartBuild`
- `elasticbeanstalk:*`
- `s3:GetObject`, `s3:PutObject` on the artifact bucket
- `iam:PassRole` (to pass role to CodeBuild)

---

## Step 4: Environment Variables (Secrets)

**Do not** put secrets in `buildspec.yml`. Set them in:

1. **CodeBuild → Project → Edit → Environment → Additional configuration**
   - Add environment variables (e.g. for build-time config if needed)

2. **Elastic Beanstalk → Configuration → Software → Environment properties**
   - `SPRING_PROFILES_ACTIVE`, `JWT_SECRET_KEY`, `RDS_*`, etc. (see `AWS_DEPLOYMENT.md`)

---

## Step 5: Trigger Pipeline

- **Automatic**: Pipeline runs on every push to `main` (if change detection is enabled)
- **Manual**: CodePipeline Console → **Release change**

---

## Step 6: Test the CI/CD Pipeline

### Trigger a run
1. **Push to GitHub**: Make any change, commit, and push to `main`
2. **Or manual**: CodePipeline → **Release change**

### Verify each stage
1. **CodePipeline** → Open your pipeline → Watch Source → Build → Deploy turn green
2. **CodeBuild** → Build history → Click latest build → View logs
3. **Elastic Beanstalk** → Events → Look for "Environment update completed successfully"

### Test the live API
```powershell
# Search hotels (no auth required)
Invoke-WebRequest -Uri "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/hotels/search" -Method POST -ContentType "application/json" -Body '{"city":"","checkInDate":"2026-03-15","endDate":"2026-03-16","roomsCount":1}' -UseBasicParsing
```

Or run `.\test-api.ps1` from the project root, or open [Swagger UI](http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html)

---

## Elastic Beanstalk Environment Variables (Reference)

Set these in **EB → Configuration → Software → Environment properties**:

| Variable | Example / Description |
|----------|------------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `RDS_HOSTNAME` | `postgres-spring.xxxxx.ap-south-1.rds.amazonaws.com` |
| `RDS_PORT` | `5432` |
| `RDS_DB_NAME` | `postgres` or `airBnb` |
| `RDS_USERNAME` | Your DB username |
| `RDS_PASSWORD` | Your DB password |
| `JWT_SECRET_KEY` | (your secret) |
| `STRIPE_SECRET_KEY` | (Stripe live key) |
| `STRIPE_WEBHOOK_SECRET` | (Stripe webhook secret) |
| `MAIL_USERNAME` | (your email) |
| `MAIL_PASSWORD` | (app password) |
| `FRONTEND_URL` | `https://main.d30tl6vi1qydms.amplifyapp.com` |
| `CORS_ALLOWED_ORIGINS` | `https://main.d30tl6vi1qydms.amplifyapp.com` |

> **Note:** Use a **standalone RDS** (not linked via EB) so your database survives redeployments.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails: "buildspec.yml not found" | Ensure `buildspec.yml` is in repo root and committed |
| Build fails: Maven/Java not found | Use `runtime-versions: java: corretto17` in buildspec |
| Deploy fails: Invalid deployment package | Ensure `buildspec.yml` outputs `deploy.zip` (already configured) |
| Deploy fails: Environment not found | Verify EB app and environment names in deploy stage |
| Permission denied | Check IAM roles for CodePipeline and CodeBuild |

---

## Optional: Add Frontend to Pipeline

To also build and deploy the Next.js frontend via CodePipeline (instead of Amplify):

1. Add a second build stage for `moonlight-stays` (Node.js runtime)
2. Deploy to S3 + CloudFront, or trigger Amplify build via API

For most setups, **Amplify's built-in Git integration** is simpler for the frontend. This pipeline focuses on the backend only.
