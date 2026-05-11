# Frontend to Backend Endpoint Audit

## A. Purpose

This audit compares the Angular frontend's current data and API usage against the Spring Boot backend's implemented controller surface for El Silencio Koffee.

The goal is to document, with code-based evidence, where the frontend and backend already align, where they only partially align, and where frontend features still depend on mocks or hardcoded data with no real backend support.

## B. Scope Reviewed

### Frontend services and modules reviewed

- `ElSilencioKoffee-Frontend/src/app/core/services/api.service.ts`
- `ElSilencioKoffee-Frontend/src/app/core/services/auth.service.ts`
- `ElSilencioKoffee-Frontend/src/app/core/interceptors/auth.interceptor.ts`
- `ElSilencioKoffee-Frontend/src/app/features/auth/services/auth-facade.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/orders/services/orders.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/orders/services/users.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/products/services/products.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/cart/services/cart-state.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/dashboard/services/dashboard.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/production/services/production.service.ts`
- `ElSilencioKoffee-Frontend/src/app/features/environment-monitoring/services/environment-monitoring.service.ts`

### Frontend pages and components reviewed

- Auth:
  - `features/auth/components/auth-container.component.ts`
  - `features/auth/components/login-form.component.ts`
  - `features/auth/components/register-form.component.ts`
  - `features/auth/pages/password-recovery-page.component.ts`
  - `features/auth/pages/change-password-page.component.ts`
  - `features/auth/login.routes.ts`
  - `features/auth/register.routes.ts`
  - `features/auth/password-recovery.routes.ts`
  - `features/auth/change-password.routes.ts`
- Store and products:
  - `features/store/pages/home-page.component.ts`
  - `features/products/pages/products-page.component.ts`
  - `features/products/pages/product-detail-page.component.ts`
  - `features/products/components/product-route-entry.component.ts`
  - `features/products/components/product-modal.component.ts`
- Cart and orders:
  - `features/cart/components/cart-page.component.ts`
  - `features/cart/components/cart-drawer.component.ts`
  - `features/cart/components/cart-route-entry.component.ts`
  - `features/orders/pages/orders-page.component.ts`
- Dashboard, production, and environment:
  - `features/dashboard/pages/dashboard-home-page.component.ts`
  - `features/dashboard/pages/dashboard-sales-page.component.ts`
  - `features/dashboard/pages/dashboard-users-page.component.ts`
  - `features/environment-monitoring/pages/environment-monitoring-page.component.ts`
  - `features/production/pages/production-page.component.ts`

### Backend controllers and supporting contracts reviewed

- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/AuthController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/UsuarioController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/RolController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/UsuarioRolController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/OrderController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/UserOrderController.java`
- `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/controllers/GlobalExceptionHandler.java`
- Relevant DTOs under `ElSilencioKoffee-Backend/src/main/java/ElSilencioKoffee_Backend/dto`
- Security files:
  - `security/SecurityConfig.java`
  - `security/JwtFilter.java`
  - `security/JwtUtil.java`
- Supporting backend contracts:
  - `entities/OrderStatus.java`
  - `entities/Order.java`
  - `repository/OrderRepository.java`
  - `repository/UsuarioRepository.java`
  - `services/impl/AuthServiceImpl.java`
  - `services/impl/OrderServiceImpl.java`
  - `services/impl/UsuarioServiceImpl.java`

## C. Summary of Findings

### Audit totals

- Frontend flows reviewed: `21`
- Backend controllers reviewed: `6`
- Backend endpoints reviewed: `23`
- Matches: `5`
- Partial matches: `3`
- Mismatches: `1`
- Frontend-only flows: `12`
- Backend-only endpoints: `14`

### Integration mode overview

| Frontend Service / Module | Key Methods / Flows | Primary Consumers | Current Mode | Notes |
| --- | --- | --- | --- | --- |
| `AuthService` | `login`, `register`, `passwordRecovery`, `changePassword` | `AuthContainerComponent`, `PasswordRecoveryPageComponent`, `ChangePasswordPageComponent` | Real backend API | Uses `authApiUrl` and `bypassMock: true`, so these flows call Spring Boot even while global mock mode is enabled. |
| `UsersService` | `listUsers` | `OrdersPageComponent` admin branch | Real backend API | Uses `authApiUrl` and `bypassMock: true`. |
| `OrdersService` | `listOrders`, `getOrder`, `createOrderFromCart` | `OrdersPageComponent`, `CartStateService` | Mixed mode | `listOrders` bypasses mocks; `getOrder` becomes real only when `isMockMode` is false; `createOrderFromCart` is mock-backed now but posts a real payload shape when mocks are disabled. |
| `ProductsService` | `listProducts`, `listFeaturedProducts`, `listCollectionProducts`, `getProduct` | `HomePageComponent`, `ProductsPageComponent`, `ProductRouteEntryComponent`, `ProductDetailPageComponent` | Hardcoded local data | Does not use `ApiService` or `HttpClient`; flipping `isMockMode` does not integrate products automatically. |
| `CartStateService` | `loadCart`, `addItem`, `updateQuantity`, `removeItem`, `checkout` | `CartDrawerComponent`, `CartPageComponent`, `ProductsPageComponent`, `HomePageComponent`, `ProductDetailPageComponent`, `ProductModalComponent` | Mock API wrapper | Uses `ApiService` mock responses for `/cart*`; checkout delegates to mixed `OrdersService#createOrderFromCart`. |
| `DashboardService` | `getMetrics`, `getSalesMetrics`, `getTopBuyers` | Dashboard pages | Mock API wrapper | Uses explicit endpoint strings but still mock-backed. |
| `ProductionService` | `listProduction` | `ProductionPageComponent` | Mock API wrapper | Uses explicit endpoint string but still mock-backed. |
| `EnvironmentMonitoringService` | `listReadings`, `getLatestReading` | `EnvironmentMonitoringPageComponent` | Mock API wrapper | Uses explicit endpoint strings but still mock-backed. |
| `ApiService` + `authInterceptor` | Shared request building and auth header injection | All `ApiService` callers | Shared infrastructure | `apiUrl` points to `/api/v1`; `authApiUrl` points to `http://localhost:8080`; backend currently has no `/api/v1` base path configured. |

### Highest-risk issues

1. Real frontend API calls are still configured to hit `http://localhost:8080` even in `environment.production.ts`, so auth and order-related flows are not production deployable as configured.
2. Checkout is contract-incompatible: frontend posts `{ items: [...] }`, while backend `POST /orders` requires `OrderCreateRequest.totalAmount`.
3. Products, cart, dashboard, production, and environment screens have no implemented Spring controllers, even though the UI and `database.sql` imply those domains exist.
4. Mock-backed modules assume `apiUrl` routes under `/api/v1`, but the backend exposes root-level mappings such as `/auth`, `/users`, and `/orders` with no `/api/v1` prefix.
5. `PATCH /orders/{id}/status` is guarded with `hasAnyRole('USER','ADMIN')`, which is more permissive than the documented admin-only behavior.

### Additional observations

- No bean validation annotations such as `@Valid`, `@NotBlank`, `@NotNull`, or `@Email` were found on the audited backend DTOs or controller method parameters.
- The backend auth response is intentionally minimal: it returns `token`, `username`, `email`, and `roles`, but not `refreshToken` or a nested `user` object.
- The routed login and register experience uses `AuthContainerComponent`; `login-page.component.ts` and `register-page.component.ts` exist but are not the active route components.
- `/cart` and `/product/:id` are route-entry flows, not stable standalone pages:
  - `/cart` opens the drawer and redirects back.
  - `/product/:id` resolves a product, opens the modal, and redirects back to a catalog route.

## D. Detailed Comparison Table

### Frontend flow to backend implementation matrix

| Frontend Module | Frontend File | Frontend Method / Usage | Expected Endpoint | Expected Method | Expected Request | Expected Response | Backend Controller | Implemented Endpoint | Implemented Method | Actual Request DTO | Actual Response DTO | Auth / Role Notes | Match Status | Impact | Recommendation |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Auth | `core/services/auth.service.ts` | `login(payload)` used by `auth-container.component.ts` | `/auth/login` via `authApiUrl` | `POST` | `{ username, password }` | `token`, `username`, `email`, `roles` | `AuthController#login` | `/auth/login` | `POST` | `LoginRequest` | `AuthResponse` | Public endpoint; auth header not required | MATCH | Current login contract is compatible. | Keep contract stable; update deployment config so production does not point to localhost. |
| Auth | `core/services/auth.service.ts` | `register(payload)` used by `auth-container.component.ts` | `/auth/register` via `authApiUrl` | `POST` | `{ username, email, password }` | `token`, `username`, `email`, `roles` | `AuthController#register` | `/auth/register` | `POST` | `RegisterRequest` | `AuthResponse` | Public endpoint; default role assignment depends on `ROLE_USER` existing in DB | MATCH | Registration works contract-wise, but bootstrapping depends on seeded roles. | Ensure production seed/migration creates `ROLE_USER`. |
| Auth | `core/services/auth.service.ts` | `passwordRecovery(payload)` used by `password-recovery-page.component.ts` | `/auth/password-recovery` via `authApiUrl` | `POST` | `{ username, email, newPassword, confirmPassword }` | `{ message }` | `AuthController#passwordRecovery` | `/auth/password-recovery` | `POST` | `PasswordRecoveryRequest` | `MessageResponse` | Public endpoint; no token required | MATCH | Request and response shapes align. | Add backend bean validation and rate limiting before production hardening. |
| Auth | `core/services/auth.service.ts` | `changePassword(payload)` used by `change-password-page.component.ts` | `/auth/change-password` via `authApiUrl` | `POST` | `{ currentPassword, newPassword, confirmPassword }` | `{ message }` | `AuthController#changePassword` | `/auth/change-password` | `POST` | `ChangePasswordRequest` | `MessageResponse` | Requires `Authorization: Bearer`; backend requires authenticated user | MATCH | Frontend and backend agree, but project docs still describe this as `PATCH`. | Keep FE and BE aligned; update API documentation to match actual `POST` behavior. |
| Orders admin enrichment | `features/orders/services/users.service.ts` | `listUsers()` used by `orders-page.component.ts` admin branch | `/users` via `authApiUrl` | `GET` | None | Array of `{ id, username, email, activo, createdAt }` | `UsuarioController#findAll` | `/users` | `GET` | None | `UserResponse[]` or `Page<UserResponse>` when paginated | Admin only; frontend only calls this in admin branch | MATCH | Admin orders page can resolve usernames from user IDs. | Consider pagination later if user volume grows. |
| Orders admin list | `features/orders/services/orders.service.ts` | `listOrders()` admin branch used by `orders-page.component.ts` | `/orders` via `authApiUrl` | `GET` | None currently; service can map array or page | Array or page of orders `{ id, orderDate, status, totalAmount, userId }` | `OrderController#findOrders` | `/orders` | `GET` | Query params `page` and `limit` optional together | `OrderResponse[]` or `Page<OrderResponse>` | Admin only on backend; frontend route also requires admin guard | PARTIAL MATCH | Current admin page works only because service tolerates array responses, but frontend does not use backend pagination and backend order status taxonomy is narrower than the frontend model/docs. | Align on a single paginated contract and finalize order status vocabulary. |
| Orders user list | `features/orders/services/orders.service.ts` | `listOrders()` user branch used by `orders-page.component.ts` | `/users/me/orders` via `authApiUrl` | `GET` | None | Array of orders `{ id, orderDate, status, totalAmount, userId }` | `UserOrderController#findMyOrders` | `/users/me/orders` | `GET` | None | `OrderResponse[]` | Backend requires `ROLE_USER`; frontend chooses this branch for non-admin sessions | PARTIAL MATCH | Current branch is functionally aligned, but backend only supports `NON PAID` and `PAID` while frontend models and docs still anticipate more statuses. | Decide final order lifecycle and update both model enums and backend enum values together. |
| Orders detail | `features/orders/services/orders.service.ts` | `getOrder(orderId)` defined in service, not currently used by routed UI | `/orders/{id}` via `authApiUrl` when `isMockMode` is false | `GET` | Path param `id` | Order object `{ id, orderDate, status, totalAmount, userId }` | `OrderController#findOrderById` | `/orders/{id}` | `GET` | Path variable `Long id` | `OrderResponse` | Backend allows `USER` and `ADMIN`, but user lookups are ownership-restricted | PARTIAL MATCH | Service method can consume the backend response, but no active UI uses it and callers must respect backend ownership rules. | Either wire this into a real order detail screen or remove until needed. |
| Checkout | `features/orders/services/orders.service.ts` | `createOrderFromCart(cart)` used through `CartStateService#checkout` | `/orders` via `authApiUrl` when mocks are disabled | `POST` | Frontend sends `{ items: [{ productId, quantity }] }` | Order object `{ id, userId, status, totalAmount, orderDate }` | `OrderController#createOrder` | `/orders` | `POST` | `OrderCreateRequest` with only `totalAmount` | `OrderResponse` | Backend allows `USER` and `ADMIN` | MISMATCH | Real checkout will fail because backend requires `totalAmount` and ignores line items, while frontend never sends `totalAmount`. | Redesign this contract first: either backend accepts order items or frontend calculates and sends the exact DTO backend requires. |
| Products catalog | `features/products/services/products.service.ts` | `listProducts()` used by `products-page.component.ts` | Intended `/products` contract; no HTTP call exists in service code | Intended `GET` | None | `ProductsListResponse { count, products[] }` from hardcoded local data | None found | None | None | None | None | Public feature in UI | FRONTEND ONLY | Products page is fully local-data driven; flipping `isMockMode` does not create real integration. | Implement a real products API and refactor `ProductsService` to use `ApiService` or `HttpClient`. |
| Product detail | `features/products/services/products.service.ts` | `getProduct(productId)` used by `product-detail-page.component.ts` and `product-route-entry.component.ts` | Intended `/products/{id}` contract; no HTTP call exists in service code | Intended `GET` | Path param `id` | `Product` from hardcoded local data | None found | None | None | None | None | Public feature in UI | FRONTEND ONLY | Product detail and modal route resolution have no backend support. | Implement `GET /products/{id}` and migrate the service away from hardcoded product arrays. |
| Cart | `features/cart/services/cart-state.service.ts` | `loadCart()` used by `cart-page.component.ts` and `cart-drawer.component.ts` | `/cart` via `apiUrl` | `GET` | None | `Cart { items, subtotal, shipping, total }` | None found | None | None | None | None | Frontend route is auth-guarded | FRONTEND ONLY | Cart exists only as frontend state plus mock API response; there is no server-side cart persistence. | Decide whether cart is session-based, DB-backed, or entirely client-side and implement the corresponding backend contract. |
| Cart | `features/cart/services/cart-state.service.ts` | `addItem(product, quantity)` used by store/product UIs | `/cart/items` via `apiUrl` | `POST` | `{ productId, quantity }` | `Cart` | None found | None | None | None | None | Frontend route and drawer assume authenticated shopping | FRONTEND ONLY | Add-to-cart success is mock-generated and hides missing backend support. | Implement a real cart item creation endpoint or explicitly declare cart as client-only. |
| Cart | `features/cart/services/cart-state.service.ts` | `updateQuantity(itemId, quantity)` used by cart drawer/page | `/cart/items/{itemId}` via `apiUrl` | `PATCH` | `{ quantity }` | `Cart` | None found | None | None | None | None | Auth implied by route usage | FRONTEND ONLY | Quantity changes are UI-only and cannot persist or validate against stock. | Implement cart item update endpoint and stock validation rules. |
| Cart | `features/cart/services/cart-state.service.ts` | `removeItem(itemId)` used by cart drawer/page | `/cart/items/{itemId}` via `apiUrl` | `DELETE` | Path param `itemId` | `Cart` | None found | None | None | None | None | Auth implied by route usage | FRONTEND ONLY | Removal is local-state only. | Implement cart item deletion endpoint if server-side cart is required. |
| Dashboard | `features/dashboard/services/dashboard.service.ts` | `getOverview()` used by `dashboard-home-page.component.ts` | `/orders` via `authApiUrl` | `GET` | None | `DashboardOverview { metrics, revenueSeries }` | `OrderController#findOrders` | `/orders` | `GET` | Optional `page`, `limit` supported by backend but not used here | `OrderResponse[]` or `Page<OrderResponse>` | Frontend admin-only route; backend admin-only route | MATCH WITH FRONTEND AGGREGATION | Overview cards and revenue trend are now computed on the frontend from real order data because `/dashboard/metrics` does not exist. | Keep this aggregation path until a dedicated backend dashboard module is intentionally introduced. |
| Dashboard | `features/dashboard/services/dashboard.service.ts` | `getOrderVolumeSeries(startDate, endDate)` used by `dashboard-sales-page.component.ts` | `/orders` via `authApiUrl` | `GET` | None | `ChartSeries { labels, values }` | `OrderController#findOrders` | `/orders` | `GET` | Optional `page`, `limit` supported by backend but not used here | `OrderResponse[]` or `Page<OrderResponse>` | Frontend admin-only route; backend admin-only route | MATCH WITH FRONTEND AGGREGATION | Sales chart now groups real orders by week on the client because `/dashboard/sales` does not exist. | Add a backend aggregation endpoint later only if the dataset becomes too large for client-side grouping. |
| Dashboard | `features/dashboard/services/dashboard.service.ts` | `getTopBuyers()` used by `dashboard-users-page.component.ts` | `/orders` and `/users` via `authApiUrl` | `GET`, `GET` | None | `TopBuyer[]` | `OrderController#findOrders`, `UsuarioController#findAll` | `/orders`, `/users` | `GET`, `GET` | None | `OrderResponse[]` or `Page<OrderResponse>`, `UserResponse[]` or `Page<UserResponse>` | Frontend admin-only route; backend admin-only routes | MATCH WITH FRONTEND AGGREGATION | Buyer ranking is now computed from live orders and enriched with usernames from `/users`, with safe ID fallback if user enrichment fails. | Add a dedicated backend ranking endpoint later only if pagination/sorting requirements grow. |
| Production | `features/production/services/production.service.ts` | `getProductionChartSeries(page, limit)` used by `production-page.component.ts` | `/production` via `apiUrl` | `GET` | None | `ProductionChartSeries { labels, quantities }` | `ProductionController#findAll` | `/production` | `GET` | None | `ProductionResponse[]` | Frontend admin-only route; backend controller currently exposes read access without admin guard | MATCH | Production dashboard now reads the live backend list and maps it into chart-safe labels and quantities on the frontend. | Consider aligning backend read security with admin-only dashboard intent in a separate story. |
| Environment | `features/environment-monitoring/services/environment-monitoring.service.ts` | `getChartSeries(page, limit)` used by `environment-monitoring-page.component.ts` | `/environment-metrics` via `apiUrl` | `GET` | None | `EnvironmentChartSeries { labels, temperatureValues, humidityValues }` | `EnvironmentMetricController#findAll` | `/environment-metrics` | `GET` | Optional `type`, `sectionId`, `fromDate`, `toDate` supported by backend but not used here | `EnvironmentMetricResponse[]` | Frontend admin-only route; backend requires authentication | MATCH | Environment charts now read the live metrics endpoint and pair temperature/humidity readings client-side by timestamp. | Keep the current pairing logic until the backend exposes a dedicated combined-reading DTO if needed. |
| Environment | `features/environment-monitoring/services/environment-monitoring.service.ts` | `getLatestReading()` currently defined but not used by page logic | `/environment-metrics` via `apiUrl` | `GET` | None | `EnvironmentReading` | `EnvironmentMetricController#findAll` | `/environment-metrics` | `GET` | Same as above | `EnvironmentMetricResponse[]` | Frontend admin-only route; backend requires authentication | PARTIAL MATCH | The helper derives the latest paired reading from the live list endpoint because there is no dedicated latest-reading route. | Keep as a derived helper or add a backend latest-reading endpoint later if a single-record contract is needed. |

### Backend-only endpoints

The following implemented backend endpoints were not found in current frontend service usage.

| Backend Controller | Endpoint | Method | Current Frontend Usage | Notes |
| --- | --- | --- | --- | --- |
| `UsuarioController#findById` | `/users/{id}` | `GET` | None found | Could support admin user detail later. |
| `UsuarioController#update` | `/users/{id}` | `PATCH` | None found | No frontend admin edit user flow currently calls this. |
| `UsuarioController#softDelete` | `/users/{id}` | `DELETE` | None found | No frontend admin delete/deactivate flow currently calls this. |
| `UsuarioController#findOrdersByUserId` | `/users/{id}/orders` | `GET` | None found | Frontend admin orders page instead calls `/orders` plus `/users`. |
| `RolController#findAll` | `/roles` | `GET` | None found | No role management UI currently wired. |
| `RolController#findById` | `/roles/{id}` | `GET` | None found | No role detail UI currently wired. |
| `RolController#save` | `/roles` | `POST` | None found | No role creation UI currently wired. |
| `RolController#update` | `/roles/{id}` | `PUT` | None found | No role edit UI currently wired. |
| `RolController#delete` | `/roles/{id}` | `DELETE` | None found | No role delete UI currently wired. |
| `UsuarioRolController#findByUsuarioId` | `/usuario-roles/usuario/{usuarioId}` | `GET` | None found | No user-role admin UI currently wired. |
| `UsuarioRolController#findByRolId` | `/usuario-roles/rol/{rolId}` | `GET` | None found | No user-role admin UI currently wired. |
| `UsuarioRolController#save` | `/usuario-roles` | `POST` | None found | No user-role assignment UI currently wired. |
| `UsuarioRolController#delete` | `/usuario-roles/{usuarioId}/{rolId}` | `DELETE` | None found | No user-role revocation UI currently wired. |
| `OrderController#updateStatus` | `/orders/{id}/status` | `PATCH` | None found | Security is currently too permissive for an endpoint that docs describe as admin-only. |

## E. Mismatch Analysis

### Critical

- Checkout request contract is incompatible.
  - Frontend source: `features/orders/services/orders.service.ts#createOrderFromCart`
  - Frontend payload: `{ items: [{ productId, quantity }] }`
  - Backend source: `controllers/OrderController.java#createOrder`
  - Backend DTO: `OrderCreateRequest` with only `totalAmount`
  - Impact: the first real checkout request will fail or be rejected because the backend-required field is absent.

- Production configuration still points direct API consumers to localhost.
  - Frontend source: `src/environments/environment.production.ts`
  - `authApiUrl` remains `http://localhost:8080`
  - Affected real flows: login, register, password recovery, change password, orders, user lookup
  - Impact: any production-deployed frontend build would attempt to call a local machine instead of the deployed backend.

### High

- Frontend has broad domain coverage that the backend does not implement.
  - Missing backend domains for active UI: products, cart, dashboard, production, environment
  - Evidence:
    - Frontend services reference `/products`, `/cart`, `/dashboard`, `/production`, `/environment`
    - No Spring controllers exist for those routes
  - Impact: most non-auth screens cannot be integrated by configuration alone.

- Future real integrations assume `/api/v1`, but backend controllers are mounted at root paths.
  - Frontend source: `core/services/api.service.ts` plus environment files
  - Default `apiUrl`: `https://elsilenciokofee.com/api/v1`
  - Backend mappings: `/auth`, `/users`, `/orders`, `/roles`, `/usuario-roles`
  - Impact: once mocks are disabled for cart, dashboard, production, or environment flows, calls will target `/api/v1/...` and likely 404 unless a gateway or reverse proxy rewrites them.

- Order status update endpoint is too permissive.
  - Backend source: `controllers/OrderController.java#updateStatus`
  - Security: `@PreAuthorize("hasAnyRole('USER','ADMIN')")`
  - Documented intent in `documentation/routes.md`: admin only
  - Impact: a normal authenticated user could potentially update order statuses if they know an order ID.

### Medium

- Order lifecycle taxonomy is not aligned across the stack.
  - Frontend models and docs allow: `NON PAID`, `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`
  - Backend enum `OrderStatus` only allows: `PAID`, `NON PAID`
  - Impact: order screens can render strings, but the intended business lifecycle is not implemented end to end.

- Products module is hardcoded, not mock-over-HTTP.
  - Frontend source: `features/products/services/products.service.ts`
  - It returns local arrays via `of(...).pipe(delay(...))`
  - Impact: unlike cart/dashboard/environment/production, this module will not even attempt real API calls when `isMockMode` is disabled.

- Backend validation is minimal and mostly manual.
  - No `@Valid` controller parameters or bean validation annotations were found in the audited DTOs.
  - Impact: malformed requests rely on service-level checks and generic exception handling rather than consistent contract validation.

- Admin orders page relies on unpaginated list behavior.
  - Frontend admin orders page does not pass `page` and `limit`
  - Backend supports both array and pageable responses
  - Impact: compatibility exists today, but the UI is not aligned to a production-ready paginated contract.

### Low

- API documentation drift exists even where frontend and backend currently agree.
  - `documentation/routes.md` still describes:
    - `/auth/change-password` as `PATCH`
    - auth responses with `refreshToken` and nested `user`
    - `SYSTEM` role for environment ingestion
  - Current frontend/backend implementation does not expose those documented shapes.
  - Impact: onboarding and future integration work will be slowed by conflicting sources of truth.

- Unused frontend auth pages exist beside the routed auth container flow.
  - `features/auth/pages/login-page.component.ts`
  - `features/auth/pages/register-page.component.ts`
  - Actual routes use `components/auth-container.component.ts`
  - Impact: low runtime risk, but it can confuse future maintenance and test coverage.

## F. Recommended Next Actions

1. Resolve the checkout contract first.
   - Define the authoritative request and response for order creation.
   - Decide whether the backend should accept line items, or whether the frontend should send `totalAmount` only.

2. Stabilize environment configuration and base-path strategy.
   - Remove `localhost` from production config.
   - Decide whether the backend will expose `/api/v1` directly or whether an API gateway/reverse proxy will rewrite paths.

3. Lock the order domain contract.
   - Finalize allowed order statuses.
   - Align frontend types, backend enum values, and route documentation.

4. Fix backend security on unused but risky endpoints before wider integration.
   - Tighten `PATCH /orders/{id}/status` to the intended role.
   - Review other admin-only endpoints for principle-of-least-privilege consistency.

5. Decide which frontend modules are part of the near-term MVP.
   - Products
   - Cart
   - Dashboard
   - Production
   - Environment
   - Inventory
   - Then prioritize backend implementation accordingly.

6. Convert the products module from hardcoded local data to a real API integration path.
   - This should happen before broad mock-mode removal because products currently bypass the shared API layer.

7. Add bean validation to the backend contract surface.
   - Apply request validation annotations to DTOs.
   - Use `@Valid` in controllers.
   - Standardize validation errors before expanding the API.

8. Update project documentation after contract decisions are made.
   - Make `documentation/routes.md` match the actual implemented contract.
   - Document which screens are still mock-backed versus production-backed.

9. Re-run this audit after the first round of contract fixes.
   - The highest-value follow-up audit should confirm:
     - checkout alignment
     - environment/base URL alignment
     - mock removal progress
     - corrected security rules
