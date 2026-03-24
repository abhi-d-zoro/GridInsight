# RGMM API Testing Guide (via Postman)

This guide explains how to test the Renewable Generation Monitoring Module (RGMM) endpoints using **Postman**.  
RGMM is secured with JWT authentication, so you must first log in via the IAM module to obtain an access token.

---

## 1. Prerequisites
- Backend application running locally on **http://localhost:8081**
- Postman installed
- At least one user in the database with role:
    - `ASSET_MANAGER` (for asset operations), or
    - `ADMIN` (for full access)

---

## 2. Authenticate via IAM
1. In Postman, create a new request:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/api/auth/login`
    - **Body (JSON)**:
      ```json
      {
        "identifier": "asset.manager@example.com",
        "password": "ManagerPassword123!"
      }
      ```
2. Send the request.  
   You will receive a response containing an `accessToken` and `refreshToken`.

Example response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "refreshToken": "some-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 600
}

## Copy the accessToken and include it in the Authorization header for all RGMM requests:
Authorization: Bearer <your-access-token>

Create Asset
POST http://localhost:8081/api/assets
Headers:
  Authorization: Bearer <token>
  Content-Type: application/json

Body:
{
  "type": "SOLAR",
  "location": "Chennai",
  "identifier": "SOLAR-001",
  "capacity": 50.5,
  "commissionDate": "2026-03-11"
}

list assets
GET http://localhost:8081/api/assets
Headers:
  Authorization: Bearer <token>

Get Asset by ID
GET http://localhost:8081/api/assets/1
Headers:
  Authorization: Bearer <token>


Update Asset Status
PUT http://localhost:8081/api/assets/1/status?status=UNDER_MAINTENANCE
Headers:
  Authorization: Bearer <token>
  
  
Delete Asset
DELETE http://localhost:8081/api/assets/1
Headers:
  Authorization: Bearer <token>
