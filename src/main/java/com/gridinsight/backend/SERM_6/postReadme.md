
 US019 – Compute Monthly Sustainability Metrics
Compute metric

Code
POST http://localhost:8081/api/metrics/compute?period=2026-01
Headers: Authorization: Basic <base64(email:password)>

Response:

json
{
"metricId": 1,
"period": "2026-01",
"renewableSharePct": 45.0,
"emissionsAvoidedTons": 1200.0,
"generatedDate": "2026-03-16"
}

This simulates an ESG Analyst calculating sustainability metrics for a given month. It saves values like renewable energy share and emissions avoided into the database.
Test purpose: Verifies that analysts can trigger metric computation and that the system stores results correctly.


Get all metrics

Code
GET http://localhost:8081/api/metrics

----------------------------------------------------------------------------
 US020 – Generate ESG Report
Create report

Code
POST http://localhost:8081/api/reports
Content-Type: application/json

{
"reportingStandard": "GRI",
"period": "2026-01"
}
This creates a new ESG report for a given reporting standard (e.g., GRI) and period.
Test purpose: Ensures analysts can generate reports and the system saves them with correct metadata.



GRI (Global Reporting Initiative)  
The most widely used framework for sustainability reporting, covering environmental, social, and governance topics.

SASB (Sustainability Accounting Standards Board)  
Focuses on industry‑specific standards, especially for financial disclosure and investor relevance.

CDP (Carbon Disclosure Project)  
Specializes in climate change, water security, and deforestation reporting.

TCFD (Task Force on Climate‑related Financial Disclosures)  
Provides guidance on climate‑related risks and opportunities, often used by financial institutions.

IFRS S1 & S2 (International Sustainability Standards Board)  
New global baseline standards for sustainability and climate disclosures, increasingly adopted worldwide.

Integrated Reporting (<IR>) Framework  
Combines financial and non‑financial information to show how an organization creates value over time.

EU CSRD (Corporate Sustainability Reporting Directive)  
Mandatory for many companies in the EU, aligned with European sustainability taxonomy.


Get all reports
Lists all reports created.
Code
GET http://localhost:8081/api/reports

Export CSV
Exports a report into CSV format.
Test purpose: Validates that reports can be exported for external sharing or compliance submissions.

Code
GET http://localhost:8081/api/reports/1/export/csv
Export PDF (placeholder)

Exports a report into PDF format (currently placeholder).
Test purpose: Confirms the endpoint exists and responds, even if PDF generation is not yet fully implemented.
Code
GET http://localhost:8081/api/reports/1/export/pdf

-----------------------------------------------------------------------------
US021 – View ESG Dashboard
Get dashboard summary
This aggregates metrics into a dashboard summary (average renewable share, total emissions avoided, drill‑down metrics).
Test purpose: Validates that Admins can view high‑level KPIs and drill down into details.
Important: Only users with the ADMIN role (like admin@gridinsight.local) can access this. If an ESG Analyst tries, they’ll get 403 Forbidden.
Code
GET http://localhost:8081/api/dashboard?period=2026-01
Requires Admin role user.

Response:

json
{
"avgRenewableShare": 45.0,
"totalEmissionsAvoided": 1200.0,
"drillDownMetrics": [
{
"metricId": 1,
"period": "2026-01",
"renewableSharePct": 45.0,
"emissionsAvoidedTons": 1200.0,
"generatedDate": "2026-03-16"
}
]
}