# AWS CodePipeline CI/CD Setup

This guide sets up an automated CI/CD pipeline for the **AirBnb Backend** (Spring Boot) using AWS CodePipeline, CodeBuild, and Elastic Beanstalk.

---

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
2. **CodeCommit** repository, or **GitHub** connected to AWS (via OAuth or connection)
3. **IAM permissions** for CodePipeline, CodeBuild, and Elastic Beanstalk

---

## Step 1: Create CodeBuild Project

1. Go to **AWS Console → CodeBuild → Build projects → Create build project**

2. **Project configuration**
   - Project name: `airbnb-backend-build`
   - Description: (optional)

3. **Source**
   - Source provider: **AWS CodeCommit** or **GitHub**
   - Repository: Select your repo (e.g. `AirBnb_BackEnd`)
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
   - Source provider: **AWS CodeCommit** or **GitHub (Version 2)**
   - Repository: Your repo
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

## Step 3: Elastic Beanstalk Deployment Package Format

Elastic Beanstalk Java platform expects a **zip file** containing the JAR. Update `buildspec.yml` if your deploy fails:

```yaml
post_build:
  commands:
    - cp target/application.jar ../application.jar
    - cd ..
    - zip -j deploy.zip application.jar

artifacts:
  files:
    - deploy.zip
```

If EB accepts the JAR directly, the current `buildspec.yml` is fine. If you get deployment errors, switch to the zip format above.

---

## Step 4: IAM Permissions (if using custom roles)

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

## Step 5: Environment Variables (Secrets)

**Do not** put secrets in `buildspec.yml`. Set them in:

1. **CodeBuild → Project → Edit → Environment → Additional configuration**
   - Add environment variables (e.g. for build-time config if needed)

2. **Elastic Beanstalk → Configuration → Software → Environment properties**
   - `SPRING_PROFILES_ACTIVE`, `JWT_SECRET_KEY`, `RDS_*`, etc. (see `AWS_DEPLOYMENT.md`)

---

## Step 6: Trigger Pipeline

- **Automatic**: Pipeline runs on every push to `main` (if change detection is enabled)
- **Manual**: CodePipeline Console → **Release change**

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails: "buildspec.yml not found" | Ensure `buildspec.yml` is in repo root and committed |
| Build fails: Maven/Java not found | Use `runtime-versions: java: corretto17` in buildspec |
| Deploy fails: Invalid deployment package | Use zip format (see Step 3) |
| Deploy fails: Environment not found | Verify EB app and environment names in deploy stage |
| Permission denied | Check IAM roles for CodePipeline and CodeBuild |

---

## Optional: Add Frontend to Pipeline

To also build and deploy the Next.js frontend via CodePipeline (instead of Amplify):

1. Add a second build stage for `moonlight-stays` (Node.js runtime)
2. Deploy to S3 + CloudFront, or trigger Amplify build via API

For most setups, **Amplify's built-in Git integration** is simpler for the frontend. This pipeline focuses on the backend only.
