# WardedLock

Identity and Access Management (IAM) service built on a high-performance, multi-module architecture.

## Prerequisites
- **Java 25**: Required for the backend services.
- **Docker & Docker Compose**: For backing infrastructure.
- **Node.js & NPM (v18+)**: Used for the development service runner.
- **Git Bash / WSL** (Windows only): Required for the initial bootstrap script.

---

## 🚀 Setup & Running Locally

### 1. Environment Configuration
Initialize your local environment variables and generate required secrets (RSA keys, HMAC keys, etc.):
```bash
cp .env.example .env
npm run bootstrap
```

### 2. Install Dependencies
Install the Node.js process orchestrator:
```bash
npm install
```

### 3. Start Infrastructure
Launch PostgreSQL, Redis, MinIO (S3), MailPit (SMTP), and Prometheus:
```bash
docker compose up -d
```

### 4. Execute Services
You can run all microservices at once or start them individually.

**Run All Services (Concurrent):**
```bash
npm run dev
```

**Run Individual Services:**
```bash
npm run <service-name>
# Examples: npm run auth, npm run gateway, npm run account
```

---

## 🛠 Service Architecture

| Service | Default Port | Description |
| :--- | :--- | :--- |
| **Gateway** | `18080` | API Gateway and Request Router |
| **Auth** | `8081` | Authentication & Token Management |
| **Account** | `8082` | User Profile & Account Management |
| **Role** | `8083` | RBAC & Permission Management |
| **App Mgmt** | `8084` | Client Application Management |
| **Notification**| `8085` | Email & SMS Delivery |

---

## 🧪 Verification & Testing
- **Full Check**: `./gradlew check --no-daemon` (Runs all tests and static analysis)
- **Unit Tests**: `./gradlew test --no-daemon`
- **Integration Tests**: `./gradlew integrationTest --no-daemon`

---

## 🔍 Troubleshooting

- **Port Conflicts**: If a service fails to bind, check for existing processes on the port:
  - **Windows**: `netstat -ano | findstr :<PORT>`
  - **Linux/macOS**: `lsof -i :<PORT>`
- **Database Connectivity**: If you change passwords in `.env` and the DB fails to connect, reset the volumes:
  ```bash
  docker compose down -v && docker compose up -d
  ```
- **Windows CRLF issues**: Ensure Git preserves Unix line endings (LF) for scripts:
  ```bash
  git config core.autocrlf input
  ```
- **Linux Docker Access**: Prometheus connects to host-running services via `host.docker.internal`.
