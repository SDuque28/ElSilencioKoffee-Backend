# Contract Gap Analysis

## Summary

This document summarizes the current gaps between:

- the implemented Spring Boot backend,
- the Angular frontend integration needs, and
- the intended project scope suggested by the database schema and UI.

## Summary counts

- Implemented backend endpoints documented in the live contract: `23`
- Partially aligned implemented endpoints: `4`
- Planned module areas not yet implemented in backend: `7`
- Frontend modules currently depending on mocks or local data instead of real backend support: `5`

## Implemented endpoint groups

### Fully implemented and generally aligned

- Auth:
  - `POST /auth/register`
  - `POST /auth/login`
  - `POST /auth/password-recovery`
  - `POST /auth/change-password`
- Users:
  - `GET /users`
  - `GET /users/{id}`
  - `PATCH /users/{id}`
  - `DELETE /users/{id}`
  - `GET /users/{id}/orders`
- Roles:
  - `GET /roles`
  - `GET /roles/{id}`
  - `POST /roles`
  - `PUT /roles/{id}`
  - `DELETE /roles/{id}`
- User Roles:
  - `GET /usuario-roles/usuario/{usuarioId}`
  - `GET /usuario-roles/rol/{rolId}`
  - `POST /usuario-roles`
  - `DELETE /usuario-roles/{usuarioId}/{rolId}`
- Orders:
  - `GET /orders`
  - `GET /orders/{id}`
  - `GET /users/me/orders`

## Partially aligned endpoints

### 1. `POST /orders`

Status: `PARTIAL / HIGH RISK`

Current backend behavior:

- accepts only `totalAmount`
- creates only an order header row
- sets status to `NON PAID`

Current frontend need:

- checkout flow builds `items[]`
- frontend cart is multi-item

Gap:

- payload mismatch
- no order-detail persistence
- backend does not reflect the frontend cart model or `orders_details` schema

### 2. `PATCH /orders/{id}/status`

Status: `PARTIAL / HIGH RISK`

Current backend behavior:

- implementation allows `USER` and `ADMIN`

Project documentation and likely product intent:

- should be admin-only

Gap:

- security contract mismatch

### 3. `GET /users`

Status: `PARTIAL / MEDIUM RISK`

Current backend behavior:

- returns either:
  - plain array when pagination params are omitted
  - Spring Page object when both `page` and `limit` are provided

Frontend impact:

- Angular already compensates for mixed shapes in orders, but mixed list response shapes increase integration complexity

Gap:

- dual response shape is supported but not ideal for long-term contract stability

### 4. `GET /orders`

Status: `PARTIAL / MEDIUM RISK`

Current backend behavior:

- returns either:
  - plain array when pagination params are omitted
  - Spring Page object when both `page` and `limit` are provided

Frontend impact:

- Angular `OrdersService` explicitly normalizes both shapes

Gap:

- live compatibility exists, but this is not the cleanest stable public contract

## Frontend expectations without backend support

These areas exist in the Angular app but do not yet have corresponding Spring controllers:

### Products

Frontend has:

- product catalog
- product detail flow
- product modal
- store home product sections

Backend support:

- none

### Cart

Frontend has:

- cart drawer
- cart route behavior
- add/update/remove item flows

Backend support:

- none

### Dashboard

Frontend has:

- metrics page
- sales charts
- user ranking page

Backend support:

- none

### Production

Frontend has:

- production chart page

Backend support:

- none

### Environment monitoring

Frontend has:

- readings chart page
- latest reading service method

Backend support:

- none

### Inventory

Implied by schema:

- inventory
- inventory movements

Backend support:

- none

### Itemized order details

Implied by:

- cart model
- `orders_details` table

Backend support:

- none in the live order create/read contract

## Backend endpoints currently unused by frontend

These implemented backend endpoints are not actively used by the current Angular service layer:

- `GET /users/{id}`
- `PATCH /users/{id}`
- `DELETE /users/{id}`
- `GET /users/{id}/orders`
- `GET /roles`
- `GET /roles/{id}`
- `POST /roles`
- `PUT /roles/{id}`
- `DELETE /roles/{id}`
- `GET /usuario-roles/usuario/{usuarioId}`
- `GET /usuario-roles/rol/{rolId}`
- `POST /usuario-roles`
- `DELETE /usuario-roles/{usuarioId}/{rolId}`
- `PATCH /orders/{id}/status`

## Auth and response-shape gaps

### Auth response gap

Current backend returns:

- `token`
- `username`
- `email`
- `roles`

Frontend model tolerates more shapes:

- nested `user`
- `userId`
- `id`
- optional `refreshToken`

Impact:

- current frontend auth normalization succeeds, but project docs previously described a richer response than the backend actually returns

### Success envelope gap

Current backend commonly returns:

- raw DTO objects
- raw arrays
- raw Spring Page objects

Frontend `ApiService` can normalize raw responses, but some project docs still describe a standard success envelope:

```json
{ "success": true, "data": {}, "message": "OK" }
```

Impact:

- the frontend can tolerate current backend responses
- project documentation and future contracts should stop assuming the envelope exists unless it is intentionally implemented

## Versioning and path gaps

Current backend:

- unversioned root-level paths such as `/auth` and `/orders`

Current frontend documentation and environment assumptions:

- `/api/v1/...`

Impact:

- contract documentation must clearly distinguish:
  - current live routes
  - future canonical versioned routes

## Top risks

1. Order creation contract is not safe for real checkout integration.
2. Several visible frontend modules still have no backend implementation at all.
3. Security expectations and actual implementation differ for order status update.
4. Current backend path strategy is unversioned while the project intends to move to `/api/v1/...`.
5. Mixed list response shapes for `/users` and `/orders` increase client complexity.

## Recommended next actions

1. Stabilize the live contract around implemented modules first:
   - auth
   - users
   - roles
   - user roles
   - orders

2. Resolve the order creation contract before broader checkout integration.

3. Decide whether `GET /users` and `GET /orders` should remain dual-shape or become consistently paginated.

4. Tighten `PATCH /orders/{id}/status` security to match intended behavior.

5. Introduce `/api/v1/...` paths as the canonical public API and migrate frontend services accordingly.

6. Implement backend modules for the highest-value missing frontend areas in priority order:
   - products
   - cart
   - itemized order details
   - dashboard metrics
   - production
   - environment monitoring
