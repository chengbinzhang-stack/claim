# Deploy to Render + Vercel Guide

## Step 1: Create PostgreSQL on Render

1. Go to https://dashboard.render.com
2. Click **New → PostgreSQL**
3. Name: `insurance-db`
4. Select **Free** instance
5. Wait for creation, then copy **Internal Database URL**

## Step 2: Initialize Database

1. In Render PostgreSQL dashboard, click **Shell**
2. Copy content from `init.sql` and paste
3. Run to create tables and sample data

## Step 3: Deploy Backend to Render

### claim-api (Port 8082)

1. **New → Web Service**
2. Connect GitHub repo: `chengbinzhang-stack/claim`
3. Settings:
   - Name: `claim-api`
   - Root Directory: `claim-api`
   - Runtime: **Docker**
   - Branch: `main`
4. Environment Variables:
   ```
   SPRING_DATASOURCE_URL = postgresql://user:pass@host:5432/dbname
   SPRING_DATASOURCE_USERNAME = your_db_user
   SPRING_DATASOURCE_PASSWORD = your_db_password
   PORT = 8082
   ```
5. Click **Create Web Service**

### policy-service (Port 8083)

Repeat steps above with:
- Name: `policy-service`
- Root Directory: `policy-service`
- PORT = 8083

### notification-service (Port 8084)

Repeat steps above with:
- Name: `notification-service`
- Root Directory: `notification-service`
- PORT = 8084

## Step 4: Update Frontend API URL

After backend is deployed, update `react-web/src/services/api.ts`:

```typescript
const API_BASE_URL = 'https://claim-api.onrender.com'; // Your actual URL
```

## Step 5: Deploy Frontend to Vercel

1. Go to https://vercel.com
2. Import GitHub repo: `chengbinzhang-stack/claim`
3. Settings:
   - Framework Preset: **Vite**
   - Root Directory: `react-web`
   - Build Command: `npm run build`
   - Output Directory: `dist`
4. Environment Variables:
   ```
   VITE_API_URL = https://claim-api.onrender.com
   ```
5. Click **Deploy**

## Step 6: Update CORS (if needed)

In Render backend services, add your Vercel URL to CORS allowed origins.

## Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| john.doe | password123 | CUSTOMER |
| mike.johnson | password123 | ADJUSTER |
| admin | password123 | ADMIN |
