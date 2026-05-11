# API Versioning Strategy

## A. Purpose

El Silencio Koffee needs a clear API versioning strategy because:

- the current Spring Boot backend exposes unversioned public routes,
- the frontend documentation and some environment settings already assume a versioned API base,
- the project still has active contract mismatches and unfinished modules,
- the backend will need to evolve without repeatedly breaking Angular integrations.

This document defines a practical versioning policy for the current codebase and a safe migration path toward a production-ready `/api/v1/...` API.

## B. Current State

### Current implemented backend route structure

The audited Spring Boot controllers currently expose these root-level paths:

- `/auth`
- `/users`
- `/roles`
- `/usuario-roles`
- `/orders`
- `/users/me/orders`

### Current controller mappings found

| Controller | Current Base Path | Notes |
| --- | --- | --- |
| `AuthController` | `/auth` | Unversioned auth routes. |
| `UsuarioController` | `/users` | Unversioned admin user management routes. |
| `RolController` | `/roles` | Unversioned admin role management routes. |
| `UsuarioRolController` | `/usuario-roles` | Unversioned and naming is mixed-language. |
| `OrderController` | `/orders` | Unversioned order management routes. |
| `UserOrderController` | `/users/me/orders` | Unversioned nested route for current-user orders. |

### Current route examples

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/password-recovery`
- `POST /auth/change-password`
- `GET /users`
- `GET /users/{id}`
- `PATCH /users/{id}`
- `DELETE /users/{id}`
- `GET /users/{id}/orders`
- `GET /users/me/orders`
- `GET /roles`
- `POST /roles`
- `POST /orders`
- `GET /orders`
- `GET /orders/{id}`
- `PATCH /orders/{id}/status`

### Current versioning state

- There is **no controller-level API version prefix** in the backend.
- `SecurityConfig` currently also matches unversioned auth paths such as `/auth/**`.
- The backend therefore behaves as an unversioned API today.

### Current frontend assumptions

The frontend already contains a partial assumption that the API should be versioned:

- `environment.ts` uses `apiUrl: 'https://elsilenciokofee.com/api/v1'`
- `environment.production.ts` uses `apiUrl: 'https://elsilenciokofee.com/api/v1'`
- `documentation/routes.md` declares the base URL as `https://elsilenciokofee.com/api/v1`

However:

- auth and order-related real calls still use `authApiUrl`, not `apiUrl`
- current backend routes are still root-level and unversioned
- this creates a split between the documented future shape and the actual deployed backend shape

### Current consistency assessment

#### What is consistent

- Most route names are REST-like and resource-oriented:
  - `/users`
  - `/orders`
  - `/roles`
- Nested current-user route `/users/me/orders` is understandable and frontend-friendly.

#### What is inconsistent or risky

- There is no global version prefix.
- `usuario-roles` is mixed-language, while most route names are English.
- Security path matching is tied to current unversioned auth routes.
- Frontend coupling risk is highest for routes already used in real backend mode:
  - `/auth/login`
  - `/auth/register`
  - `/auth/password-recovery`
  - `/auth/change-password`
  - `/orders`
  - `/orders/{id}`
  - `/users/me/orders`
  - `/users`

### Current state conclusion

The project is currently in an **unversioned implementation state with versioned documentation expectations**. That mismatch should be resolved before broader frontend integration and production rollout.

## C. Recommended Versioning Approach

## Recommended strategy

Use **path-based API versioning** with the canonical public base path:

- `/api/v1/...`

### Why path-based versioning is recommended here

This project should prefer path-based versioning over header-based or media-type-based versioning because:

1. it is simple to understand for both backend and Angular developers,
2. it matches the frontend's current documented base URL expectation,
3. it is easy to configure in environment files and test suites,
4. it is practical for a student/project-to-production codebase without extra gateway complexity,
5. it makes parallel support for legacy and new paths easier during migration.

### Canonical versioning model

- Public API base path: `/api`
- Major version segment: `/v1`
- Resource path after version: `/auth`, `/users`, `/orders`, etc.

Canonical pattern:

```text
/api/v1/<resource-or-feature-path>
```

### Why `/api/v1/...` instead of `/v1/...`

Use `/api/v1/...` rather than `/v1/...` because:

- it clearly distinguishes API routes from frontend application routes,
- it matches the frontend environment configuration already present,
- it leaves room for hosting the Angular SPA and API under the same domain,
- it is the most practical convention for future proxying and deployment.

## D. Standard Route Convention

### Canonical route pattern

All public backend endpoints should use:

```text
/api/v1/<resource>
```

### Canonical path examples

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/auth/password-recovery`
- `/api/v1/auth/change-password`
- `/api/v1/users`
- `/api/v1/users/{id}`
- `/api/v1/users/me`
- `/api/v1/users/me/orders`
- `/api/v1/orders`
- `/api/v1/orders/{id}`
- `/api/v1/orders/{id}/status`
- `/api/v1/roles`

### Current vs recommended route examples

| Current Route | Recommended Canonical Route | Notes |
| --- | --- | --- |
| `/auth/login` | `/api/v1/auth/login` | Safe and direct versioned migration. |
| `/auth/register` | `/api/v1/auth/register` | Safe and direct versioned migration. |
| `/users` | `/api/v1/users` | Safe and direct versioned migration. |
| `/orders` | `/api/v1/orders` | Safe and direct versioned migration. |
| `/users/me/orders` | `/api/v1/users/me/orders` | Keep route shape, add only the versioned API prefix. |
| `/usuario-roles` | `/api/v1/usuario-roles` initially | Safe short-term migration; route naming cleanup should be treated separately as a breaking change. |

### Naming convention rules

Within `v1`, use these conventions:

- lowercase path segments
- kebab-case for multi-word segments
- plural resource names for collections where appropriate
- stable route nouns rather than action verbs, except for auth operations where login/register are already practical endpoint actions

Examples:

- good: `/api/v1/orders`
- good: `/api/v1/users/me/orders`
- acceptable for auth: `/api/v1/auth/login`
- avoid introducing new mixed-language public route names

## E. Backward Compatibility Rules

## Compatibility policy for `v1`

Within `v1`, preserve backward compatibility wherever practical.

### Non-breaking changes allowed within `v1`

The following changes are allowed in `v1`:

- adding new endpoints
- adding optional request fields
- adding optional response fields
- adding optional query parameters
- adding new filter/sort parameters when optional
- improving internal implementation without changing externally visible contract behavior
- adding new HTTP response codes only when they do not break existing successful flows and are properly documented
- adding new enum values only if existing clients can safely ignore or tolerate them

### Breaking changes that require `v2`

The following changes require a new major version such as `v2` unless the old behavior remains supported in parallel:

- removing an endpoint
- renaming an endpoint path
- changing the base route structure
- removing response fields
- renaming response fields
- renaming request fields
- changing field data types
- changing field meaning/semantics
- making an optional request field required
- changing auth requirements in a way that breaks existing clients
- changing enum meanings
- removing enum values that clients may depend on
- changing success response shape incompatibly
- changing error handling in a way that breaks client-side parsing assumptions

### Rules for specific contract areas

#### DTO fields

- Adding optional fields in request or response DTOs is allowed in `v1`.
- Removing fields is not allowed in `v1`.
- Renaming fields is not allowed in `v1`.
- Changing `string` to `number`, `number` to `string`, or similar type changes is not allowed in `v1`.

#### Validation rules

- Tightening validation in a way that rejects requests that were previously valid is a breaking change.
- Minor clarifications that do not invalidate previously valid requests may remain in `v1`.
- Example:
  - changing a max length from 500 to 100 and rejecting existing payloads is breaking
  - trimming whitespace internally is non-breaking

#### Enum values

- Adding enum values in `v1` is allowed only if clients can safely tolerate unknown values.
- Renaming enum values is breaking.
- Removing enum values is breaking.
- Changing the meaning of an existing enum value is breaking.

#### Path and query parameters

- Adding a new optional query parameter is non-breaking.
- Making an existing query parameter mandatory is breaking.
- Renaming path variables or path segments is breaking.

#### Response ordering

- Clients should not rely on incidental JSON field ordering.
- However, changes in collection sorting behavior that materially affect client behavior should be treated as contract-sensitive and documented.
- If clients depend on a current default ordering, changing that ordering should be treated as a compatibility risk and handled carefully.

### Concrete examples

#### Example non-breaking change

Adding an optional `phone` field to a user response in `v1`:

```json
{
  "id": 5,
  "username": "juan",
  "email": "juan@mail.com",
  "phone": "+57-300-000-0000"
}
```

This is allowed in `v1` because existing clients can ignore the new optional field.

#### Example breaking change

Renaming `totalAmount` to `grandTotal` in the order response:

```json
{
  "id": 123,
  "grandTotal": 54000
}
```

This is breaking unless `totalAmount` remains supported in parallel.

## F. Deprecation Policy

### How endpoints should be deprecated

Deprecated endpoints should:

1. remain operational for a defined transition period,
2. be marked as deprecated in documentation,
3. be noted in migration release notes,
4. be covered by tests until removal,
5. be removed only after dependent clients have been updated.

### Practical deprecation process for this project

1. Introduce the new versioned route.
2. Keep the old unversioned route temporarily available.
3. Update Angular services and environment configuration.
4. Update documentation and test references.
5. Mark the old route as deprecated in internal docs and release notes.
6. Remove the old route only after frontend and test migration is complete.

### Recommended deprecation window

For this project, a practical migration window is:

- short-lived parallel support during active integration work
- removal of legacy unversioned routes before production hardening is finalized

Because the project is not yet in stable public production, the goal is not long-term dual maintenance. The goal is safe migration while integration is still in progress.

## G. Migration Plan for Current Controllers

## Recommended migration decision

The project should move to:

- `/api/v1/...` as the canonical public API

and should **temporarily support both unversioned and versioned paths** during migration.

### Why dual-path support is recommended temporarily

This is the safest practical path because:

- real frontend flows already call current unversioned backend routes,
- the docs and `apiUrl` already assume `/api/v1`,
- changing everything in one step would create avoidable integration risk,
- security rules and tests currently reference unversioned routes.

### Migration phases

#### Phase 1: Declare `/api/v1` as canonical

- Update documentation to state that `/api/v1/...` is the official API surface.
- Keep existing unversioned routes operational temporarily.

#### Phase 2: Add versioned route support

Recommended implementation direction:

- expose each controller under `/api/v1/...`
- temporarily preserve legacy route access for currently used paths

Example migration target:

- `/auth/login` and `/api/v1/auth/login`
- `/orders` and `/api/v1/orders`
- `/users/me/orders` and `/api/v1/users/me/orders`

#### Phase 3: Migrate clients and docs

- Update Angular services to use centralized versioned base paths consistently.
- Stop using separate unversioned assumptions in service code.
- Update all internal docs and route inventories to only show `/api/v1/...` as canonical.

#### Phase 4: Migrate tests

- Update backend integration tests, frontend E2E assumptions, and any API docs/tests to the versioned paths.
- Keep temporary compatibility tests for legacy routes only while the migration window is open.

#### Phase 5: Deprecate and remove unversioned routes

- Once Angular services and automated tests are fully moved, remove the legacy root-level routes.

### Guidance for the current frontend

Angular services should:

- consume centralized environment-driven base URLs,
- target `/api/v1/...` consistently,
- avoid hardcoding route roots in a way that bypasses versioning policy.

Current project-specific recommendation:

- unify the real backend path strategy so auth and orders also align with `/api/v1`
- remove split assumptions between:
  - `apiUrl = /api/v1`
  - `authApiUrl = localhost root`

### Guidance for current documentation

All route documentation should:

- present `/api/v1/...` as canonical,
- note any temporary legacy compatibility paths only in migration notes,
- stop presenting unversioned root paths as the long-term contract.

### Guidance for current security config

When versioned routes are introduced, security path matching should also be updated accordingly.

Example:

- current: `/auth/**`
- target canonical matchers: `/api/v1/auth/**`

Legacy route matchers may remain temporarily during the migration window if both path sets are supported.

## H. Examples

### Example canonical paths

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/users/me`
- `/api/v1/orders`
- `/api/v1/users/me/orders`

### Example current route to versioned route migration

| Current | Target |
| --- | --- |
| `/auth/login` | `/api/v1/auth/login` |
| `/auth/register` | `/api/v1/auth/register` |
| `/orders` | `/api/v1/orders` |
| `/users/me/orders` | `/api/v1/users/me/orders` |

### Example non-breaking change in `v1`

Adding an optional `phone` field to a user response:

Before:

```json
{
  "id": 7,
  "username": "maria",
  "email": "maria@mail.com"
}
```

After:

```json
{
  "id": 7,
  "username": "maria",
  "email": "maria@mail.com",
  "phone": "+57-301-000-0000"
}
```

This remains `v1` because existing clients can ignore the new field.

### Example breaking change requiring new version

If an order response changes from:

```json
{
  "id": 123,
  "totalAmount": 54000
}
```

to:

```json
{
  "id": 123,
  "grandTotal": 54000
}
```

that is a breaking change and should require `v2` unless `totalAmount` remains supported in parallel.

### Example migration recommendation

If current controllers expose:

- `/auth`
- `/orders`

the project should introduce:

- `/api/v1/auth`
- `/api/v1/orders`

then:

1. update Angular services,
2. update documentation,
3. update tests,
4. deprecate legacy unversioned paths,
5. remove legacy paths after migration completes.

## I. Versioning Governance Rules

All future public API work should follow these rules:

1. All public endpoints must live under the canonical versioned base path.
   - Current standard: `/api/v1/...`

2. DTO and response changes must be reviewed for compatibility impact before merge.

3. Frontend services must use centralized environment-based API base URLs.

4. Deprecated endpoints must be documented and tracked until removal.

5. Breaking API changes require:
   - an explicit major version bump,
   - migration notes,
   - updated documentation,
   - updated tests.

6. New modules added later, such as products, cart, dashboard, production, or environment APIs, must be introduced directly under the versioned base path rather than adding new unversioned routes.

7. Route naming consistency should be improved over time, but naming cleanup itself must be treated as contract-sensitive.
   - For example, replacing `/usuario-roles` with `/user-roles` should not happen silently inside `v1` without parallel support.

8. Documentation must clearly distinguish:
   - canonical current versioned routes,
   - temporary legacy compatibility paths,
   - planned future versions.

## J. Recommended Next Actions

1. Adopt `/api/v1/...` as the official canonical public API base path immediately in documentation and planning.

2. Introduce versioned controller mappings for currently implemented modules:
   - auth
   - users
   - roles
   - usuario-roles
   - orders
   - users/me/orders

3. Temporarily support legacy unversioned routes during migration because some Angular flows already depend on them.

4. Update Spring Security path matchers to cover canonical versioned routes and any temporary legacy aliases.

5. Standardize Angular service configuration so all real backend calls consistently target the versioned API base.

6. Update route documentation and any future OpenAPI definition to use `/api/v1/...` as the source of truth.

7. Update tests to reference versioned routes before removing legacy ones.

8. After frontend migration is complete, remove unversioned legacy paths before production hardening is finalized.

## Final Recommendation

For El Silencio Koffee, the most practical and safest strategy is:

- path-based versioning
- canonical base path: `/api/v1/...`
- backward compatibility preserved within `v1` wherever practical
- breaking changes moved to `v2`
- temporary support for both legacy and versioned paths during migration

This strategy is simple enough for the current project, aligns with the Angular frontend’s documented expectations, and gives the backend a governed path toward production-safe evolution.
