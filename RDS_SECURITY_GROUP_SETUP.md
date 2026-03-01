# RDS Security Group Setup (Allow EB to Reach Database)

If the app can't reach the database, it will fail on startup. Follow these steps to allow your **v2-0** Elastic Beanstalk instances to connect to **airbnb-db**.

---

## Step 1: Find the v2-0 Security Group

### Option A: Via EC2 Instances
1. Open **AWS Console** → **EC2** → **Instances**
2. Find the instance for v2-0 (Instance ID: `i-07ff962b312d4b71b` or search by name containing `v2-0`)
3. Select the instance
4. Open the **Security** tab (bottom panel)
5. Under **Security groups**, click the security group link (e.g. `sg-xxxxxxxxx`)
6. **Copy the Security group ID** (e.g. `sg-042d5586c29ec48f1`)

### Option B: Via Elastic Beanstalk
1. **Elastic Beanstalk** → **Environments** → **v2-0**
2. Click **Configuration** (left sidebar)
3. Under **Security**, click **Edit**
4. Note the **EC2 security groups** listed
5. Or go to **EC2** → **Security Groups** and search for one associated with the v2-0 environment

---

## Step 2: Edit the RDS Security Group

1. Open **AWS Console** → **RDS** → **Databases**
2. Click **airbnb-db**
3. In **Connectivity & security**, find **VPC security groups**
4. Click the security group (e.g. `airbnb-db-sg` or `sg-067baf1c5a6ae1373`)
5. The Security Group page opens

---

## Step 3: Add Inbound Rule

1. In the security group page, select the **Inbound rules** tab
2. Click **Edit inbound rules**
3. Click **Add rule**
4. Set:
   - **Type:** `PostgreSQL` (or Custom TCP)
   - **Port range:** `5432`
   - **Source:** Choose **Custom** → paste the **v2-0 security group ID** (e.g. `sg-042d5586c29ec48f1`)
     - Or select it from the dropdown if it appears
5. **Description (optional):** `EB v2-0 access`
6. Click **Save rules**

---

## Step 4: Verify

The RDS security group should now have an inbound rule like:

| Type       | Port | Source              |
|------------|------|---------------------|
| PostgreSQL | 5432 | sg-xxxxxxxxx (v2-0) |

---

## Step 5: Restart the Environment (Optional)

After adding the rule, the app may need a restart to retry the DB connection:

1. **Elastic Beanstalk** → **v2-0** → **Actions** → **Restart app server(s)**

Or redeploy:

1. **Upload and deploy** → select your zip → Deploy

---

## Quick Reference

| Item              | Value                          |
|-------------------|--------------------------------|
| RDS DB            | airbnb-db                      |
| RDS Port          | 5432                           |
| EB Instance       | i-07ff962b312d4b71b            |
| EB Environment     | v2-0                           |
| EB Domain         | v2-0.eba-hk6i6byc.ap-south-1.elasticbeanstalk.com |
