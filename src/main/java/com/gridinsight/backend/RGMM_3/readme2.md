# RGMM – Batch CSV Ingestion Feature

This README explains how to use and test the **Renewable Generation Monitoring Module (RGMM)** batch ingestion feature for generation records.  
The feature allows **Asset Managers** and **Admins** to upload historical generation data in bulk via CSV.

---

## 1. Prerequisites
- Backend application running locally on **http://localhost:8081**
- Postman installed
- A user with role:
    - `ASSET_MANAGER` or
    - `ADMIN`
- Valid JWT access token from IAM login

---

## 2. CSV Schema
The CSV file must contain the following columns:

- `AssetID` (string, must exist in system)
- `Timestamp` (ISO 8601 format, e.g., `2026-03-10T10:00:00`)
- `GeneratedEnergyMWh` (numeric, ≥ 0)
- `AvailabilityPct` (numeric, between 0 and 100)

### Example CSV (`generation.csv`)
```csv
AssetID,Timestamp,GeneratedEnergyMWh,AvailabilityPct
SOLAR-001,2026-03-10T10:00:00,12.5,95
SOLAR-001,2026-03-10T11:00:00,11.8,92
WIND-101,2026-03-10T10:00:00,30.2,88
HYDRO-500,2026-03-10T10:00:00,200.0,99
BIO-300,2026-03-10T10:00:00,15.0,90


Login via IAM to get an access token:

Code
POST http://localhost:8081/api/auth/login
Body (JSON):
{
  "identifier": "asset.manager@example.com",
  "password": "ManagerPassword123!"
}


Expected Responses
Success
json
{
  "processed": 5,
  "success": 5,
  "failed": 0,
  "errors": []
}
Partial Failure
json
{
  "processed": 5,
  "success": 4,
  "failed": 1,
  "errors": [
    "Row 3: AvailabilityPct must be between 0 and 100"
  ]
}