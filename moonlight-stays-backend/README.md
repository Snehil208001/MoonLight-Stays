# Moonlight Stays Backend Engine

Enterprise Clean-Architecture Spring Boot 3.5.x platform targeting container runtime deployments across **Azure App Service + Azure PostgreSQL Flexible Server Instances**.

## Core Development Configuration Matrix

### Required Infrastructure Environment Flags
```bash
export DB_HOST="airbnb-db-server.postgres.database.azure.com"
export DB_PORT="5432"
export DB_NAME="moonlight_db"
export DB_USERNAME="your-azure-dbadmin-username"
export DB_PASSWORD="your-secure-azure-db-password"
export JWT_SECRET="your-ultra-secure-long-base64-encoded-jwt-passphrase-string"
```

### Direct Local Execution Pipeline
```bash
./gradlew clean bootRun --args='--spring.profiles.active=local'
```

### Local Container Ecosystem Execution
```bash
docker-compose up --build
```

### Azure App Service Deployment Sequence via Docker Containerization

1. **Verify Database Accessibility Connection Controls**: Ensure your target Azure Database for PostgreSQL Flexible Server Firewall settings explicitly authorize inbound connections from native Azure resource origins.

2. **Build and Tag Application Images**:
```bash
docker build -t moonlightregistry.azurecr.io/backend:v1 .
```

3. **Push to Target Container Registry**: Authenticate your environment terminal into your private Azure Container Registry (ACR) instance and distribute the runner artifact:
```bash
az acr login --name moonlightregistry
docker push moonlightregistry.azurecr.io/backend:v1
```

4. **Instantiate Web App Resource Configuration**: Spin up a Web App for Containers instance utilizing the production settings profiles. Supply your critical application infrastructure system settings directly within the App Service Configuration App Settings Dashboard Console to feed values securely into your environment targets at runtime.
