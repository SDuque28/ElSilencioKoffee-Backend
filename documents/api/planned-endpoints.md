# Planned Endpoints

This file lists endpoint groups that are implied by the Angular frontend, the SQL schema, or prior project documentation, but are **not currently implemented in the Spring Boot backend**.

These are not part of the live OpenAPI contract in `openapi.yaml`.

## Purpose

Use this file to:

- track future API scope,
- keep planned modules separate from live backend contract documentation,
- support backlog refinement without pretending these endpoints already exist.

## Planned module areas

### Products

Suggested future canonical paths:

- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products`
- `PATCH /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`
- `PATCH /api/v1/products/{id}/stock`

Why planned:

- the Angular frontend has product catalog and detail flows
- the SQL schema contains `products`
- no Spring controller exists yet

### Cart

Suggested future canonical paths:

- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/{itemId}`
- `DELETE /api/v1/cart/items/{itemId}`

Why planned:

- the Angular frontend includes a cart drawer and cart route flow
- no Spring controller exists yet

### Itemized order details

Suggested future canonical contract expansion:

- include line items in order creation and order responses
- persist through `orders_details`

Why planned:

- SQL schema includes `orders_details`
- current backend order implementation only persists order headers

### Dashboard

Suggested future canonical paths:

- `GET /api/v1/dashboard/metrics`
- `GET /api/v1/dashboard/sales`
- `GET /api/v1/dashboard/top-buyers`
- `GET /api/v1/dashboard/export`

Why planned:

- the Angular admin dashboard depends on these areas
- no Spring controller exists yet

### Production

Suggested future canonical paths:

- `GET /api/v1/production`
- `POST /api/v1/production`
- `GET /api/v1/production/{id}`
- `PATCH /api/v1/production/{id}`
- `DELETE /api/v1/production/{id}`

Why planned:

- SQL schema includes `production`
- frontend has a production page
- no backend controller exists yet

### Environment monitoring

Suggested future canonical paths:

- `POST /api/v1/environment/readings`
- `GET /api/v1/environment/readings`
- `GET /api/v1/environment/readings/latest`
- `GET /api/v1/environment/thresholds`
- `PATCH /api/v1/environment/thresholds`
- `GET /api/v1/environment/alerts`

Why planned:

- frontend has environment-monitoring UI
- project docs already describe these routes
- no backend controller exists yet

### Inventory

Suggested future canonical paths:

- `GET /api/v1/inventory`
- `PATCH /api/v1/inventory/{productId}`
- `GET /api/v1/inventory/movements`

Why planned:

- SQL schema includes `inventory` and `inventory_movements`
- no backend controller exists yet

## Rule for using this file

If code proves a planned endpoint has been implemented:

1. move it into `openapi.yaml`,
2. remove it from the planned-only list,
3. update `contract-gap-analysis.md`,
4. update frontend integration notes if applicable.
