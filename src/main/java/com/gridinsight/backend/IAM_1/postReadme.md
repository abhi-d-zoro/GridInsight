GRIDINSIGHT – POSTMAN TESTING GUIDE (TEXT FORMAT)
This document explains how to test all user stories in the GridInsight IAM & RBAC module using Postman.
No code formatting is used, so copying/pasting is easy for all team members.
===============================================
PREREQUISITES


Backend running at:
http://localhost:8081


Import the Postman Collection:
GridInsight_Postman_Collection.json


Create a Postman Environment with:
baseUrl = http://localhost:8081
adminEmail = admin@gridinsight.local
adminPassword = Admin@12345
accessToken = (leave empty)
refreshToken = (leave empty)


Select the environment before running requests.


===============================================
USER STORY 1 — Admin Can CRUD Users

STEP 1 — Login as Admin
POST {{baseUrl}}/api/auth/login
Body:
{
"identifier": "{{adminEmail}}",
"password": "{{adminPassword}}"
}
Postman automatically stores accessToken and refreshToken into environment.
Expected: 200 OK and expiresIn = 600.

STEP 2 — Create User
POST {{baseUrl}}/api/admin/users
Headers: Authorization: Bearer {{accessToken}}
Body:
{
"name": "Planner One",
"email": "planner1@gridinsight.local",
"phone": "9876543210",
"role": "PLANNER",
"tempPassword": "Planner@12345"
}
Expected: 201 Created.
STEP 3 — List Users
GET {{baseUrl}}/api/admin/users
Authorization: Bearer {{accessToken}}
Expected: 200 OK.
STEP 4 — Get User by ID
GET {{baseUrl}}/api/admin/users/{{userId}}
STEP 5 — Update User
PUT {{baseUrl}}/api/admin/users/{{userId}}
Body:
{
"phone": "8888888888"
}
Expected: 200 OK.
STEP 6 — Delete User
DELETE {{baseUrl}}/api/admin/users/{{userId}}
Expected: 204 No Content.
===============================================
USER STORY 2 — Authentication & Security
Covers: Password policy, lockout, JWT issuance, refresh rotation.
STEP 1 — Successful Login
POST /api/auth/login
Use correct password.
Expected: 200 OK, tokens generated.
STEP 2 — Wrong Password Lockout
Execute login with wrong password 5 times:
Attempts 1 to 4 = 401 Invalid credentials
Attempt 5 = 400 Account locked. Try again later.
STEP 3 — Request Password Reset Token
POST /api/auth/password/forgot
Body:
{
"identifier": "planner1@gridinsight.local"
}
Expected: 200 OK.
STEP 4 — Apply Password Reset
POST /api/auth/password/reset
Body:
{
"token": "<VALID_RESET_TOKEN>",
"newPassword": "NewPass@123"
}
If weak password, expect:
400 Password must be at least 8 chars and include upper, lower, number, and special character.
===============================================
USER STORY 3 — JWT Token Logic (10-Min Access, Idle Timeout, Refresh Rotation)
STEP 1 — Verify Access Token = 10 Minutes
Login response should contain:
"expiresIn": 600
STEP 2 — Refresh Token (Valid Case)
POST /api/auth/refresh
Body:
{
"refreshToken": "{{refreshToken}}"
}
Expected: new access token + new refresh token.
Old refresh token now invalid — refreshing with it should return 401 Invalid refresh token.
STEP 3 — Idle Timeout (30 Minutes)
If refresh token is unused for 30 minutes:
Refreshing after 30 minutes should return:
401 Session idle timeout. Please login again.
Fast test: temporarily set REFRESH_IDLE_TIMEOUT_MINUTES = 1, wait 70 seconds, then refresh.
===============================================
USER STORY 4 — Audit Logs (Filtering + CSV Export)
STEP 1 — Get All Audit Logs
GET {{baseUrl}}/api/audit
Authorization: Bearer {{accessToken}}
Expected: 200 OK with logs such as:
LOGIN_SUCCESS, LOGIN_FAILURE, USER_CREATED, USER_UPDATED, USER_DELETED, PASSWORD_RESET
STEP 2 — Filter by User ID
GET /api/audit?userId=9
STEP 3 — Filter by Action
GET /api/audit?action=LOGIN_SUCCESS
STEP 4 — Filter by Date Range
GET /api/audit?fromDate=2026-03-10T00:00:00Z&toDate=2026-03-10T23:59:59Z
STEP 5 — Export CSV
GET /api/audit/export?action=USER_CREATED
Headers:
Accept: text/csv
Authorization: Bearer {{accessToken}}
Expected: File download (audit_logs.csv)
===============================================
5-MINUTE QUICK DEMO FLOW (THE MANAGER DEMO)

-Login as admin
-Create a user
-List users
-Update user
-Delete user
-Refresh token
-Idle timeout test
-View audit logs
-Export CSV
-Show access token expiresIn = 600

===============================================
YOU'RE DONE!