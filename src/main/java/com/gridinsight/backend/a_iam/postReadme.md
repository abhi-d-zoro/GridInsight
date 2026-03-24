
# IAM & Audit API Test Cases (US001–US004)

**Generated:** 2026-03-16T10:51:41

This document lists detailed, ready-to-run test cases for all IAM & Audit APIs. Use these as QA acceptance tests in Postman/Newman. Each API includes positive and negative scenarios, prerequisites, sample payloads, and expected results.

---

## Conventions
- **Auth:** Unless marked *Public*, calls require a valid **Bearer {{accessToken}}** header.
- **Env Vars (Postman):** `baseUrl`, `accessToken`, `refreshToken`, `resetToken`, `adminAccessToken`
- **HTTP Codes:** 2xx success, 4xx client errors, 5xx unexpected server errors
- **Headers:**
    - `Content-Type: application/json`
    - Optional for traceability: `X-Correlation-ID`
    - Optional for IP forwarding tests: `X-Forwarded-For`

---

# 1) AUTH CONTROLLER — `/auth`

## 1.1 POST /auth/login — Login (Public)
**Purpose:** Login with email/phone + password; issue access & refresh tokens.

### Positive
- **TC-LOGIN-001** Valid email + password
    - **Request**
      ```http
      POST {{baseUrl}}/auth/login
      Content-Type: application/json
  
      {
        "identifier": "admin@grid.com",
        "password": "Admin@123"
      }
      ```
    - **Expect** `200 OK` with body containing fields: `accessToken`, `refreshToken`, `tokenType=Bearer`, `expiresIn=600`.
    - **Postman Tests**
      ```javascript
      pm.test('200 OK', () => pm.response.to.have.status(200));
      const json = pm.response.json();
      pm.expect(json).to.have.property('accessToken');
      pm.expect(json).to.have.property('refreshToken');
      pm.environment.set('accessToken', json.accessToken);
      pm.environment.set('refreshToken', json.refreshToken);
      ```

### Negative
- **TC-LOGIN-002** Invalid password (increments failed attempts)
    - **Expect** `401 Unauthorized`.
- **TC-LOGIN-003** Locked account after 5 failures
    - Send 5 wrong passwords, then retry with correct password **before 15 minutes**.
    - **Expect** `401/423` (locked) with message about lockout.

---

## 1.2 POST /auth/refresh — Refresh Tokens (Public)
**Purpose:** Rotate refresh token; enforce idle timeout (30 minutes).

### Positive
- **TC-REFRESH-001** Valid refresh token
    - **Request**
      ```http
      POST {{baseUrl}}/auth/refresh
      Content-Type: application/json
  
      { "refreshToken": "{{refreshToken}}" }
      ```
    - **Expect** `200 OK` with **new** `accessToken` and **new** `refreshToken` (old one revoked).

### Negative
- **TC-REFRESH-002** Reuse old refresh token after rotation
    - **Expect** `401 Unauthorized`.
- **TC-REFRESH-003** Idle timeout exceeded (> 30 min since lastUsedAt)
    - **Expect** `401 Unauthorized` with idle-timeout message.

---

## 1.3 POST /auth/password/otp — Request Password Reset (Public)
**Purpose:** Generate reset token for email/phone; log audit.

### Positive
- **TC-RESET-REQ-001** Existing user identifier
    - **Expect** `200 OK` with neutral message; in dev, may return `token` field. Save token to `{{resetToken}}` if present.

### Negative
- **TC-RESET-REQ-002** Unknown identifier
    - **Expect** `200 OK` same neutral message (no user enumeration).

---

## 1.4 POST /auth/password/reset — Confirm Password Reset (Public)
**Purpose:** Reset using valid token; enforce password policy; audit success/failure.

### Positive
- **TC-RESET-CNF-001** Valid token + strong password
    - **Request**
      ```http
      POST {{baseUrl}}/auth/password/reset
      Content-Type: application/json
  
      {
        "token": "{{resetToken}}",
        "newPassword": "NewStrong@123"
      }
      ```
    - **Expect** `200 OK` (or `204 No Content` depending on implementation). Audit `PASSWORD_RESET_SUCCESS`.

### Negative
- **TC-RESET-CNF-002** Expired/used/invalid token
    - **Expect** `400 Bad Request` with message `Invalid or expired reset token`. Audit `PASSWORD_RESET_FAILURE`.
- **TC-RESET-CNF-003** Weak password violates policy
    - **Expect** `400 Bad Request` with password policy message.

---

# 2) ADMIN USER CONTROLLER — `/admin/users` (Admin-only)

## 2.1 POST /admin/users — Create User
**Purpose:** Admin creates user with role (defaults to GRIDANALYST if your code supports defaulting).

### Positive
- **TC-USERS-CRT-001** Create with explicit role
    - **Request**
      ```http
      POST {{baseUrl}}/admin/users
      Authorization: Bearer {{accessToken}}
      Content-Type: application/json
  
      {
        "name": "John Analyst",
        "email": "john.analyst@grid.com",
        "phone": "9999912345",
        "tempPassword": "Strong@123",
        "role": "GRIDANALYST"
      }
      ```
    - **Expect** `201 Created`; response has `id`, `roles` includes `GRIDANALYST`. Audit `USER_CREATED`.

- **TC-USERS-CRT-002** Create without role (least-privilege default, if enabled)
    - **Expect** `201 Created`; `roles` defaults to `GRIDANALYST`.

### Negative
- **TC-USERS-CRT-003** Duplicate email
    - **Expect** `400 Bad Request` with duplicate message.
- **TC-USERS-CRT-004** Invalid role name (not in allow-list)
    - **Expect** `400 Bad Request`.
- **TC-USERS-CRT-005** Non-admin tries to create user
    - **Expect** `403 Forbidden`.

---

## 2.2 GET /admin/users — List Users (Paged)
**Purpose:** Admin lists users with pagination.

### Positive
- **TC-USERS-LST-001** List first page
    - **Request** `GET {{baseUrl}}/admin/users?page=0&size=10&sort=createdAt,desc`
    - **Expect** `200 OK` with Spring Page payload.

### Negative
- **TC-USERS-LST-002** Non-admin access
    - **Expect** `403 Forbidden`.

---

## 2.3 GET /admin/users/{id} — Get User
### Positive
- **TC-USERS-GET-001** Valid user id → `200 OK` with `UserResponse`.

### Negative
- **TC-USERS-GET-002** Non-existing id → `404/400` depending on handler.
- **TC-USERS-GET-003** Non-admin → `403 Forbidden`.

---

## 2.4 PUT /admin/users/{id} — Update User
**Purpose:** Modify name/email/phone/password/status/roles with auditing.

### Positive
- **TC-USERS-UPD-001** Change name/phone
    - **Expect** `200 OK`, audit `USER_UPDATED` with changedFields.
- **TC-USERS-UPD-002** Update roles to ["PLANNER","ESG"]
    - **Expect** `200 OK`, roles changed.

### Negative
- **TC-USERS-UPD-003** Change email to existing one → `400 Bad Request`.
- **TC-USERS-UPD-004** Set invalid role → `400 Bad Request`.
- **TC-USERS-UPD-005** Non-admin → `403 Forbidden`.

---

## 2.5 DELETE /admin/users/{id} — Delete User
### Positive
- **TC-USERS-DEL-001** Valid delete by Admin → `204 No Content`, audit `USER_DELETED`.

### Negative
- **TC-USERS-DEL-002** Non-admin → `403 Forbidden`.
- **TC-USERS-DEL-003** Non-existing id → `404/400`.

---

# 3) AUDIT LOG CONTROLLER — `/audit` (Admin-only)

## 3.1 GET /audit — Filtered Logs
**Purpose:** Investigate events by user/action/resource/date range.

### Positive
- **TC-AUDIT-GET-001** Filter by action=LOGIN_SUCCESS, resource=auth/login
    - **Request** `GET {{baseUrl}}/audit?action=LOGIN_SUCCESS&resource=auth/login`
    - **Expect** `200 OK` list of `AuditLogResponse` with `ipAddress`, `correlationId` populated where available.

- **TC-AUDIT-GET-002** Filter by userId and date range
    - **Request** `GET {{baseUrl}}/audit?userId=7&fromDate=2026-03-10T00:00:00Z&toDate=2026-03-10T23:59:59Z`
    - **Expect** `200 OK` correct subset.

### Negative
- **TC-AUDIT-GET-003** Non-admin → `403 Forbidden`.

---

## 3.2 GET /audit/export — Export CSV
**Purpose:** Download filtered audit logs as CSV.

### Positive
- **TC-AUDIT-CSV-001** Export all
    - **Request** `GET {{baseUrl}}/audit/export`
    - **Expect** `200 OK`, `Content-Type: text/csv`, `Content-Disposition: attachment; filename="audit_logs.csv"`.

- **TC-AUDIT-CSV-002** Export with filters
    - **Request** `GET {{baseUrl}}/audit/export?action=USER_CREATED&resource=User`
    - **Expect** CSV rows only for matching filters.

### Negative
- **TC-AUDIT-CSV-003** Non-admin → `403 Forbidden`.

---

# 4) Suggested Postman Tests (JS snippets)

## Assert JSON schema basics
```javascript
pm.test('JSON', () => pm.response.to.be.json);
```

## Save tokens
```javascript
const json = pm.response.json();
pm.environment.set('accessToken', json.accessToken);
pm.environment.set('refreshToken', json.refreshToken);
```

## CSV content-type
```javascript
pm.test('CSV content', () => pm.response.headers.get('Content-Type').includes('text/csv'));
```

## Lockout code check
```javascript
pm.test('Locked or Unauthorized', () => {
  pm.expect([401, 423]).to.include(pm.response.code);
});
```

---

# 5) Execution Order (Smoke Suite)
1. **Login (Admin)** → save `accessToken` (Admin)
2. **Create User** → save created `userId`
3. **List Users**
4. **Get User by ID**
5. **Update User**
6. **Delete User**
7. **Login (New User)**
8. **Force 5 failed logins** → verify lockout
9. **Refresh token** → verify rotation and old-token rejection
10. **Password Reset Request** → save `resetToken`
11. **Password Reset Confirm**
12. **Audit GET + CSV**

---

**End of Test Cases**
