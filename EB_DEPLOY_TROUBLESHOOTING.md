# Elastic Beanstalk Deployment Failure - eb-engine.log

## Error
"Engine execution has encountered an error. Instance deployment failed. For details, see 'eb-engine.log'."

## How to Get the Logs

1. Go to **Elastic Beanstalk** → **Environments** → **Airbnb-backend-env**
2. Click **Logs** (left sidebar)
3. Click **Request Logs** → **Last 100 Lines** (or **Full Logs**)
4. Wait for the log bundle to generate, then **Download**
5. Extract the zip and open **eb-engine.log**

## Common Causes

| Cause | Fix |
|-------|-----|
| **Missing env vars** | EB → Configuration → Software → Environment properties. Ensure `SPRING_PROFILES_ACTIVE`, `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`, `JWT_SECRET_KEY` are set |
| **Database connection failed** | Verify RDS is running, security group allows EB, credentials correct |
| **Port mismatch** | `application-prod.properties` has `server.port=5000` – EB Java platform expects 5000 |
| **JAR not found** | Buildspec now includes Procfile; ensure deploy.zip has `application.jar` at root |

## Quick Check: Environment Variables

In **EB → Configuration → Software → Environment properties**, you must have at minimum:
- `SPRING_PROFILES_ACTIVE` = `prod`
- `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`
- `JWT_SECRET_KEY`
