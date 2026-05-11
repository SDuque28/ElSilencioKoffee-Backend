# API Contract Guide

## Purpose

This folder contains the canonical backend API contract for El Silencio Koffee.

Its purpose is to give backend, frontend, QA, and delivery work a single source of truth for:

- current implemented backend endpoints
- request and response shapes
- authentication requirements
- known integration gaps
- planned-but-not-yet-implemented API areas

## Files in this folder

- `openapi.yaml`
  - OpenAPI 3.x contract for the currently implemented backend endpoints
- `contract-gap-analysis.md`
  - Current mismatch and risk analysis between Angular needs and backend reality
- `planned-endpoints.md`
  - Future API areas implied by the frontend and database schema but not yet implemented
- `frontend-backend-endpoint-audit.md`
  - Detailed endpoint usage audit
- `api-versioning-strategy.md`
  - Versioning and backward-compatibility policy

## Scope covered by the live OpenAPI spec

The live spec covers only backend areas proven by code:

- Auth
- Users
- Roles
- User Roles
- Orders

## Implemented vs planned

### Implemented

Included as active paths in `openapi.yaml`:

- `/auth/*`
- `/users/*`
- `/roles/*`
- `/usuario-roles/*`
- `/orders/*`
- `/users/me/orders`

### Planned / not yet implemented

These are intentionally **not** listed as active paths in the main OpenAPI contract:

- products
- cart
- dashboard
- production
- environment monitoring
- inventory
- itemized order details

These areas are tracked separately in `planned-endpoints.md` and in the gap analysis.

## Important versioning note

The OpenAPI spec documents the **current live backend routes**, which are still unversioned.

Current live examples:

- `/auth/login`
- `/orders`
- `/users/me/orders`

Project migration target:

- `/api/v1/auth/login`
- `/api/v1/orders`
- `/api/v1/users/me/orders`

Why the spec still uses current live paths:

- those are the routes the Spring Boot backend actually exposes today
- using them in the spec keeps the contract executable and evidence-based
- the migration to `/api/v1/...` is documented in `api-versioning-strategy.md`

## Authentication overview

Authentication is JWT-based.

### Public endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/password-recovery`

### Authenticated endpoints

- `POST /auth/change-password`
- all `/users/*`
- all `/roles/*`
- all `/usuario-roles/*`
- all `/orders/*`
- `GET /users/me/orders`

### Bearer token usage

For protected endpoints, clients should send:

```http
Authorization: Bearer <jwt>
```

## How frontend should consume this contract

1. Treat `openapi.yaml` as the source of truth for currently live backend routes and payloads.
2. Do not assume mock-only frontend modules already exist in backend.
3. Use `contract-gap-analysis.md` to understand what still needs backend implementation.
4. Follow the versioning strategy when switching frontend services to the future `/api/v1/...` base path.

## How QA can use this contract

QA can use these documents to:

- design endpoint-level API tests
- validate auth and role restrictions
- verify status codes and error behavior
- separate live backend coverage from planned future modules

## Practical guidance

- If a route is in `openapi.yaml`, it exists in the backend today.
- If a route is only in `planned-endpoints.md`, it is not live yet.
- If a route is in the gap analysis as partially aligned, it exists but needs contract or implementation refinement.
