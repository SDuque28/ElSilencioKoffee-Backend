# El Silencio Koffee Backend

Spring Boot backend for the El Silencio Koffee e-commerce and admin platform.

This service is responsible for:

- authentication and JWT issuance
- product catalog and stock/inventory management
- cart and checkout flows
- user and admin order management
- production and environment metrics modules
- role-based access control for admin functionality

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (`jjwt`)
- Flyway
- MySQL
- H2 for tests

## Project Layout

The codebase is organized by feature first.

```text
src/main/java/ElSilencioKoffee_Backend
  auth/
  cart/
  checkout/
  environment/
  inventory/
  orders/
  production/
  products/
  roles/
  security/
  shared/
  userroles/
  users/
```

Most feature modules follow the same pattern:

- `controllers/`: HTTP entrypoints
- `dto/`: request/response payloads
- `entities/`: JPA entities
- `repositories/`: Spring Data repositories
- `services/`: business logic interfaces and implementations

Cross-cutting pieces live in:

- `security/`: JWT filter, security config, user details integration
- `shared/`: common responses, exception handling, reusable utilities

## Main Modules

- `auth`: register, login, password recovery, password change
- `products`: public product browsing plus admin CRUD
- `inventory`: stock lookup, stock movements, quantity changes
- `cart`: authenticated cart operations
- `checkout`: order creation from cart
- `orders`: user order history, order details, admin order management
- `production`: production batch management
- `environment`: environment metrics for monitored sections
- `users`, `roles`, `userroles`: user and permission administration

## Request Flow

Typical request path in this backend:

1. A controller receives the HTTP request.
2. DTOs validate or shape incoming data.
3. A service implements business rules.
4. A repository reads/writes persistent state.
5. The controller returns DTOs or shared responses.

This means:

- controllers should stay thin
- services own business logic
- repositories should stay persistence-focused

## Security Model

Security is configured in [`src/main/java/ElSilencioKoffee_Backend/security/config/SecurityConfig.java`](./src/main/java/ElSilencioKoffee_Backend/security/config/SecurityConfig.java).

Current behavior:

- JWT authentication is stateless
- `/auth/**` is public, except `/auth/change-password` which requires authentication
- `GET /products` and `GET /products/**` are public
- all other routes require authentication unless method-level authorization relaxes or narrows access
- admin-only functionality is enforced with `@PreAuthorize("hasRole('ADMIN')")`

JWT handling:

- tokens are generated in `auth`
- tokens are parsed by `security/jwt/JwtFilter`
- secret and expiration are environment-driven

## Key API Areas

This is a quick map for navigation, not a full contract reference.

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/password-recovery`
- `POST /auth/change-password`

### Products

- `GET /products`
- `GET /products/{id}`
- `POST /products` admin
- `PUT /products/{id}` admin
- `DELETE /products/{id}` admin

### Inventory

- `GET /inventory`
- `GET /inventory/{id}`
- `GET /inventory/products/{productId}`
- `GET /inventory/{id}/movements`
- `POST /inventory/{id}/increase` admin
- `POST /inventory/{id}/decrease` admin
- `POST /inventory/{id}/movements` admin

### Cart

- `GET /cart`
- `POST /cart/items`
- `PUT /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`
- `DELETE /cart`

### Checkout

- `POST /api/v1/checkout`

### Orders

- `GET /users/me/orders`
- `POST /orders`
- `GET /orders/{id}`
- `PATCH /orders/{id}/status`
- `POST /orders/{id}/pay`
- `GET /api/v1/admin/orders`
- `GET /api/v1/admin/orders/{id}`
- `PATCH /api/v1/admin/orders/{id}/delivery-status`

## Configuration

Runtime configuration is loaded from environment variables, with local support through:

- `.env`
- `src/main/resources/application.properties`
- `src/main/resources/application-prod.properties`

Use [`.env.example`](./.env.example) as the starting point.

Important variables:

```bash
SPRING_APPLICATION_NAME=ElSilencioKoffee-Backend
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/silencio_koffee_db
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_OPEN_IN_VIEW=false
SERVER_PORT=8080
JWT_SECRET=replace-with-a-32-byte-minimum-secret
JWT_EXPIRATION=3600000
SPRING_PROFILES_ACTIVE=default
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

### Production Notes

- activate the production profile with `SPRING_PROFILES_ACTIVE=prod`
- configure `CORS_ALLOWED_ORIGINS` explicitly in production
- use a strong `JWT_SECRET` with at least 32 bytes
- keep `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` or another explicitly chosen value for deployed environments

## Database and Migrations

This backend uses Flyway migrations from:

- [`src/main/resources/db/migration`](./src/main/resources/db/migration)

Current migration chain:

- `V1__create_users_and_roles.sql`
- `V2__create_sections_and_varieties.sql`
- `V3__create_production_and_catalog_tables.sql`
- `V4__create_inventory_tables.sql`
- `V5__create_orders_tables.sql`
- `V6__create_supporting_indexes.sql`
- `V7__create_cart_tables.sql`
- `V8__enforce_inventory_product_uniqueness.sql`
- `V9__create_environment_metrics_table.sql`
- `V10__update_order_status_to_pending.sql`
- `V11__add_checkout_payment_and_delivery_tables.sql`

Before running locally:

1. Create the MySQL database.
2. Copy `.env.example` to `.env`.
3. Fill in database credentials and JWT values.
4. Start the app and let Flyway validate/migrate the schema.

## Local Development

### Start the app

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Default port:

- `8080`

### Run tests

```bash
./mvnw test
```

Tests use H2 and do not require a running MySQL instance.

### Build the jar

```bash
./mvnw package
```

Artifact output:

- `target/ElSilencioKoffee-Backend-0.0.1-SNAPSHOT.jar`

## Working With the Code

Recommended entry points when exploring the code:

- application bootstrap:
  [`ElSilencioKoffeeBackendApplication.java`](./src/main/java/ElSilencioKoffee_Backend/ElSilencioKoffeeBackendApplication.java)
- security setup:
  [`SecurityConfig.java`](./src/main/java/ElSilencioKoffee_Backend/security/config/SecurityConfig.java)
- JWT logic:
  [`JwtFilter.java`](./src/main/java/ElSilencioKoffee_Backend/security/jwt/JwtFilter.java)
- shared exception behavior:
  `shared/controllers/GlobalExceptionHandler.java`

If you are tracing a feature:

1. start from the controller
2. follow into the service interface and implementation
3. inspect the DTO mapping
4. inspect repository queries if persistence behavior matters

## Common Backend Tasks

### Add a new endpoint

1. Add or update DTOs.
2. Add controller method.
3. Implement service logic.
4. Reuse or extend repository methods.
5. Add tests.

### Change stock behavior

Look in:

- `products/`
- `inventory/`

Product edits and inventory state are related, but stock persistence is ultimately inventory-backed.

### Change order behavior

Look in:

- `orders/`
- `checkout/`
- `cart/`

### Change access control

Look in:

- `security/config/SecurityConfig.java`
- controller `@PreAuthorize(...)` annotations

## Related Docs

Additional project documentation lives in the repo-level [`../docs`](../docs) folder, including:

- database migration notes
- security/environment refactor notes
- checkout and order flow documentation

## Troubleshooting

### App fails on startup with JWT error

- make sure `JWT_SECRET` is defined
- make sure it is at least 32 bytes long

### App cannot connect to MySQL

- verify `SPRING_DATASOURCE_URL`
- verify username/password
- verify the database exists
- verify MySQL is running

### Frontend cannot call backend

- verify `CORS_ALLOWED_ORIGINS`
- verify frontend runtime config points to the right backend URL
- verify the backend is running on the expected port

### Flyway validation fails

- check whether the local schema matches the committed migrations
- avoid changing old migration files after they have been applied in shared environments
