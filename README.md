# BazaarHub — Ecommerce Multi-Vendor Platform

BazaarHub is a full-stack multi-vendor e-commerce marketplace that lets customers shop across seller stores, enables sellers to manage inventory and orders, and gives administrators control over approvals, categories, and coupons. The platform includes JWT-secured REST APIs, Razorpay and Stripe payment integration (sandbox-friendly), transactional email, and a **rule-based chatbot** for order, cart, catalog, and FAQ assistance—without requiring an LLM.

---

## Features

### Customer
- Browse and search products by name or category
- Cart, wishlist, shipping addresses, and checkout
- Order history, status tracking, and cancellation (when allowed)
- Reviews on purchased products
- In-app rule-based shopping assistant

### Seller
- Shop profile creation (admin approval required)
- Product and inventory management
- Order fulfillment (ship / deliver)
- Dashboard with earnings and order trends (charts)

### Admin
- Approve or reject seller shops
- Manage categories and discount coupons
- Platform overview dashboard (orders by status, pending approvals)

### Platform
- Role-based access: `CUSTOMER`, `SELLER`, `ADMIN`
- JWT authentication and Spring Security
- Payments: Razorpay, Stripe, and Cash on Delivery
- Email notifications via Java Mail (MailHog in local/Docker)
- OpenAPI / Swagger documentation
- Docker Compose one-command local deployment

---

## Tech stack

| Layer | Technologies |
| --- | --- |
| Backend | Java 21, Spring Boot 4, Spring Security, JWT (jjwt), Spring Data JPA, MySQL, Java Mail, springdoc OpenAPI |
| Frontend | React 19, TypeScript, Redux Toolkit, Material UI, Tailwind CSS 4, Formik, Yup, React Router, Axios, Recharts |
| Payments | Razorpay Java SDK, Stripe Java SDK (mock fallback when keys are placeholders) |
| Tooling | Gradle, Vite, Docker Compose, JUnit |

---

## Architecture (high level)

```
Browser (React SPA)
        │  HTTP + JWT
        ▼
   Nginx / Vite  ──────►  Spring Boot API (:8080)
                              │            │
                              ▼            ▼
                           MySQL        MailHog (SMTP)
                              │
                    Razorpay / Stripe (optional sandbox)
```

Detailed design and module documentation:

- [`docs/EcommerceMultiVendor-Architecture.pdf`](docs/EcommerceMultiVendor-Architecture.pdf)
- [`docs/EcommerceMultiVendor-Modules.pdf`](docs/EcommerceMultiVendor-Modules.pdf)

Source Markdown: [`docs/architecture.md`](docs/architecture.md), [`docs/modules.md`](docs/modules.md).

---

## Prerequisites

- **JDK 21+**
- **Node.js 20+** and npm
- **Docker Desktop** (for Compose) *or* a local MySQL 8 instance
- Free host ports: **3000** (UI), **8080** (API), **3306** (MySQL), **8025** (MailHog UI)

> If `8080` or `3000` are already in use (for example by another project), stop those containers or change the published ports in `docker-compose.yml`.

Check a port on macOS/Linux:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

On Windows (PowerShell):

```powershell
netstat -ano | findstr :8080
```

---

## Quick start (Docker)

### macOS / Linux

```bash
cd EcommerceMultiVendor

cd backend && ./gradlew bootJar && cd ..
cd frontend && npm install && npm run build && cd ..

docker compose up --build -d
```

Or with Make:

```bash
make up
```

### Windows (Command Prompt)

```bat
cd EcommerceMultiVendor

cd backend
gradlew.bat bootJar
cd ..

cd frontend
npm install
npm run build
cd ..

docker compose up --build -d
```

### Application URLs

| Service | URL |
| --- | --- |
| Storefront | http://localhost:3000 |
| API base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MailHog | http://localhost:8025 |

Stop the stack:

```bash
docker compose down
```

---

## Local development (without full Compose)

### 1. Infrastructure

```bash
docker compose up -d mysql mailhog
```

### 2. Backend

```bash
cp .env.example .env
cd backend
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

API: http://localhost:8080

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Dev UI: http://localhost:5173 (Vite proxies `/api` to the backend).

### 4. Tests

```bash
cd backend
./gradlew test             # Windows: gradlew.bat test
```

---

## Demo accounts

Seed data is loaded automatically when the database is empty (`APP_SEED_ENABLED=true`).

| Email | Password | Role |
| --- | --- | --- |
| `customer@shop.com` | `password123` | Customer |
| `seller@shop.com` | `password123` | Seller (approved shop + sample products) |
| `admin@shop.com` | `password123` | Admin |

Sample coupon: **`WELCOME10`** (10% discount).

---

## API overview

| Area | Endpoints |
| --- | --- |
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Catalog | `GET /api/v1/products`, `GET /api/v1/categories` |
| Cart / wishlist | `/api/v1/cart`, `/api/v1/wishlist/{productId}` |
| Orders | `/api/v1/addresses`, `/api/v1/checkout`, `/api/v1/orders` |
| Payments | `POST /api/v1/payments`, `POST /api/v1/payments/confirm` |
| Seller | `/api/v1/seller/shop`, `/products`, `/orders`, `/dashboard` |
| Admin | `/api/v1/admin/dashboard`, `/shops/pending`, `/coupons`, `/categories` |
| Chatbot | `POST /api/v1/chat` |

Interactive docs: [Swagger UI](http://localhost:8080/swagger-ui.html).

---

## Rule-based chatbot

The assistant uses keyword and regex intent matching (no external LLM or API keys). Supported capabilities include:

- Greetings, help menu, and FAQs (returns, payments, shipping)
- Order list, detail, status, and cancel
- Cart summary and wishlist
- Product search and category browse
- Seller summaries (pending orders, low stock)
- Admin summaries (pending shop approvals)

Authenticated users can use the floating chat widget in the storefront.

---

## Project structure

```text
EcommerceMultiVendor/
├── backend/                 # Spring Boot API
│   ├── src/main/java/...    # Domain, security, services, controllers, chatbot
│   ├── src/test/java/...    # JUnit tests
│   ├── Dockerfile
│   └── build.gradle
├── frontend/                # React + TypeScript SPA
│   ├── src/pages/           # Storefront, seller, admin views
│   ├── src/components/      # Layout, chat widget
│   ├── src/store/           # Redux Toolkit slices
│   ├── Dockerfile
│   └── nginx.conf
├── docs/                    # Architecture & module PDFs + Markdown
├── docker-compose.yml
├── .env.example
├── Makefile
└── README.md
```

---

## Configuration

Copy [`.env.example`](.env.example) and adjust as needed.

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | HS256 signing secret (use a long random value in real deployments) |
| `SPRING_DATASOURCE_*` | MySQL connection |
| `MAIL_HOST` / `MAIL_PORT` | SMTP (MailHog: `localhost:1025`) |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins |
| `RAZORPAY_*` / `STRIPE_*` | Payment sandbox keys |
| `APP_SEED_ENABLED` | Seed demo users and catalog on empty DB |

When payment keys contain placeholders, the API returns **mock** provider references so checkout demos work offline. Replace with real sandbox credentials to call Razorpay/Stripe directly.

---

## Troubleshooting

| Issue | Resolution |
| --- | --- |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | Free the port or remap in `docker-compose.yml` (e.g. `"8081:8080"`) |
| Backend / frontend stuck in `Created` | Usually a port conflict; fix ports, then `docker compose up -d` |
| MailHog platform warning on Apple Silicon | Harmless for local use; mail capture still works on port 8025 |
| Empty catalog / no login users | Ensure `APP_SEED_ENABLED=true` and the DB volume is fresh, or recreate: `docker compose down -v && docker compose up --build -d` |

---

## License

This project is provided for educational and demonstration purposes. Add a license file (for example MIT) if you intend to redistribute it publicly.
