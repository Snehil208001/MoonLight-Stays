# Elastic Beanstalk Deployment Failure - eb-engine.log

## Error
"Engine execution has encountered an error. Instance deployment failed. For details, see 'eb-engine.log'."

---

## Stop the Restart Loop

If CodePipeline keeps triggering new deployments after you abort:

1. **CodePipeline** → **airbnb-backend-pipeline**
2. If a run is in progress, click **Stop execution**
3. **Edit** the pipeline → **Source** stage → **Edit stage**
4. Disable or remove the **Source trigger** (GitHub webhook)
5. Save. Re-enable after fixing the issue.

---

## How to Get the Logs

### Option A: Elastic Beanstalk (may fail with "Failed to retrieve requested logs")
1. **Elastic Beanstalk** → **Environments** → **Airbnb-backend-env**
2. Click **Logs** (left sidebar)
3. Click **Request Logs** → **Last 100 Lines** (or **Full Logs**)
4. Wait for the log bundle to generate, then **Download**
5. Extract the zip and open **var/log/web.stdout.log** (app) and **var/log/eb-engine.log** (deployment)

### Option B: CloudWatch (when EB logs fail)
1. **CloudWatch** → **Log groups**
2. Search for `/aws/elasticbeanstalk/Airbnb-backend-env`
3. Open the log group → select the latest **Log stream**
4. Look for startup errors, stack traces, DB connection failures

---

## Common Causes

| Cause | Fix |
|-------|-----|
| **Missing env vars** | EB → Configuration → Software → Environment properties. Ensure `SPRING_PROFILES_ACTIVE`, `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`, `JWT_SECRET_KEY` are set |
| **Database connection failed** | Verify RDS is running, security group allows EB instances to reach RDS on port 5432 |
| **Port mismatch** | `application-prod.properties` has `server.port=5000` – EB Java platform expects 5000 |
| **Command timeout** | App takes >10 min to start. `.ebextensions` sets Timeout=900. Check DB/RDS latency. |
| **JAR not found** | Buildspec outputs `application.jar` and `Procfile` at artifact root |

---

## Quick Check: Environment Variables

In **EB → Configuration → Software → Environment properties**, you must have at minimum:
- `SPRING_PROFILES_ACTIVE` = `prod`
- `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`
- `JWT_SECRET_KEY`
