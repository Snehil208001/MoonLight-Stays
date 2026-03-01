# EB Zip Deploy Checklist

Use this when building the deployment zip manually (or to verify `.\deploy-eb.ps1` output).

---

## Required Zip Structure

**Open the zip — these must be at the top level (no parent folder):**

```
airbnb-eb-deploy.zip
├── application.jar      ← JAR file (not airBnbApp-0.0.1-SNAPSHOT.jar)
├── Procfile             ← Exact content below
└── .ebextensions/
    └── 01_environment.config
```

---

## Procfile (exact content)

```
web: java -Xms256m -Xmx384m -XX:+UseG1GC -jar application.jar
```

*384MB max heap for t3.micro (1GB RAM) - prevents OOM. Swap file added via .ebextensions/02_setup_swap.config*

- The JAR name in Procfile (`application.jar`) must match the file inside the zip.
- Maven builds `application.jar` (pom.xml has `<finalName>application</finalName>`).

---

## Manual Build Steps

1. **Build JAR:** `cd airBnbApp` → `mvn clean package -DskipTests`
2. **Output:** `airBnbApp/target/application.jar`
3. **Create zip:** Put `application.jar`, `Procfile`, and `.ebextensions` folder at the **root** of the zip (no wrapper folder).
4. **Verify:** Double-click the zip — you should see the 3 items immediately.

---

## Deploy

1. EB → **v2-0** (or your env) → **Upload and deploy**
2. Select the zip
3. Version label: `v2-fixed-zip` (or similar)
4. Click **Deploy**
