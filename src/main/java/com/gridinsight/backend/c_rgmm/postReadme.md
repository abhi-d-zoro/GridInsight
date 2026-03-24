--------------Register Asset--------------
Endpoint

Code
POST /api/assets
Headers

Code
Content-Type: application/json
Authorization: Bearer <token>
Body

json
{
"type": "SOLAR",
"location": "Plant A",
"identifier": "SOLAR-001",
"capacity": 50.0,
"commissionDate": "2024-05-01"
}
Expected

Asset created with default status = OPERATIONAL

Duplicate prevention enforced by location + identifier

-----------------List Assets---------------
Endpoint

Code
GET /api/assets
Expected

Returns all registered assets with details.

----------------Get Asset by ID------------------
Endpoint

Code
GET /api/assets/{id}
Expected

Returns details of the specific asset.

--------------------Update Asset Status------------------
Endpoint

Code
PUT /api/assets/{id}/status?status=OFFLINE
Expected

Status updated if transition is valid.

Invalid transitions return error.

-------------------Flag Asset Under Maintenance----------------
Endpoint

Code
PUT /api/assets/{id}/maintenance
Headers

Code
Content-Type: application/json
Authorization: Bearer <token>
Body

json
{
"note": "Scheduled turbine inspection",
"startDate": "2026-03-15",
"endDate": "2026-03-20"
}
Expected

Asset status set to UnderMaintenance

Note and date range stored

-------------------Upload Generation Records (CSV)--------------
Endpoint

Code
POST /api/generation/upload
Headers

Code
Authorization: Bearer <token>
Content-Type: multipart/form-data
Form-data

Key: file → attach CSV file

CSV Example

Code
AssetID,Timestamp,GeneratedEnergyMWh,AvailabilityPct
SOLAR-001,2026-03-10T04:30:00,12.5,95.0
WIND-101,2026-03-10T04:30:00,30.2,88.0
HYDRO-500,2026-03-10T04:30:00,200.0,99.0
BIO-300,2026-03-10T04:30:00,15.0,90.0
Expected

json
{
"processed": 4,
"success": 4,
"failed": 0,
"errors": []
}
------------------Get Trends (JSON)----------------------
Endpoint

Code
GET /api/generation/trends?assetId=SOLAR-001&start=2026-03-10T00:00:00&end=2026-03-10T23:59:59
Expected

json
{
"assetId": "SOLAR-001",
"points": [
{
"timestamp": "2026-03-10T04:30:00",
"energy": 12.5,
"availability": 95.0
}
]
}
------------------Export Trends as CSV-----------------------
Endpoint

Code
GET /api/generation/trends/export?assetId=SOLAR-001&start=2026-03-10T00:00:00&end=2026-03-10T23:59:59&format=csv
Headers

Code
Authorization: Bearer <token>
Accept: text/plain
Expected

Downloadable CSV file:

Code
Timestamp,GeneratedEnergyMWh,AvailabilityPct
2026-03-10T04:30:00,12.50,95.00
-----------------Export Trends as PNG--------------------
Endpoint

Code
GET /api/generation/trends/export?assetId=SOLAR-001&start=2026-03-10T00:00:00&end=2026-03-10T23:59:59&format=png
Headers

Code
Authorization: Bearer <token>
Accept: image/png
Expected

Downloadable PNG chart:

X-axis: Timestamp

Y-axis: GeneratedEnergyMWh

Series 1: Energy values

Series 2: Availability overlay