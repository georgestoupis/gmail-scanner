# Gmail Scanner

A Spring Boot application that scans your Gmail inbox for order confirmation emails, parses them, and returns spending analytics broken down by category and year.

## Features

- Authenticates with your Google account via OAuth2 (read-only Gmail access)
- Scans emails from 9 supported vendors across 4 categories:
  - **Food:** Efood, Wolt, Box
  - **Games:** Steam, Kinguin, Riot Games, PlayStation
  - **Shopping:** Skroutz
  - **Travel:** Uber
- Returns total spend, per-vendor breakdown, average monthly spend, and a month-by-month breakdown for any given year

## Requirements

- Java 25
- Maven
- A Google Cloud project with the Gmail API enabled and OAuth2 credentials configured

## Setup

### 1. Google Cloud credentials

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project (or use an existing one).
3. Enable the **Gmail API**.
4. Create an **OAuth 2.0 Client ID** (application type: Web application).
5. Add `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI.
6. Note your **Client ID** and **Client Secret**.

### 2. Environment variables

Set the following before running the application:

```
GOOGLE_CLIENT_ID=<your-client-id>
GOOGLE_CLIENT_SECRET=<your-client-secret>
```

### 3. Build and run

```bash
mvn clean package
java -jar target/gmail-scanner-1.0-SNAPSHOT.jar
```

Then open `http://localhost:8080` in your browser and sign in with your Google account.

## API

All endpoints require an authenticated session (redirect to Google login happens automatically).

| Method | Path | Description |
| ------ | ---- | ----------- |
| `GET` | `/api/me` | Returns the authenticated user's email address |
| `GET` | `/scan/{group}/{year}` | Returns spending analytics for a category and year |

**Valid group values:** `food`, `games`, `shopping`, `travel`

### Example response — `GET /scan/food/2024`

```json
{
  "group": "food",
  "period": "2024",
  "sources": [
    { "source": "EFOOD", "orders": 47, "sum": "312.50" },
    { "source": "WOLT",  "orders": 12, "sum": "89.20"  }
  ],
  "totalSum": "401.70",
  "avgMonth": "33.47",
  "monthlyTotals": {
    "1": "28.90",
    "2": "41.30",
    "3": "35.00"
  },
  "msg": "OK."
}
```

## Deployment

The app is deployed to **Google Cloud Run** — Google's managed container runtime. CI/CD is handled by **GitHub Actions**, which builds the Docker image, pushes it to **Google Artifact Registry**, and deploys to Cloud Run on every push to `main`.

### How it works

1. A push to `main` triggers the GitHub Actions workflow (`.github/workflows/deploy.yml`)
2. The workflow builds the Docker image defined in `Dockerfile` (multi-stage: Maven build → JRE runtime)
3. The image is pushed to **Google Artifact Registry**
4. The workflow deploys the new image to **Google Cloud Run**

### First-time setup

A GCP service account with the following roles is required, with its key stored as a `GCP_SA_KEY` GitHub Actions secret:
- `roles/artifactregistry.writer`
- `roles/run.admin`
- `roles/iam.serviceAccountUser`

The following env vars must be set on the Cloud Run service:

```
GOOGLE_CLIENT_ID=<your-client-id>
GOOGLE_CLIENT_SECRET=<your-client-secret>
```

You must also add the Cloud Run service URL as an authorized redirect URI in your Google Cloud OAuth client:

```
https://<your-cloud-run-url>/login/oauth2/code/google
```

## Running Tests

```bash
mvn test
```

Tests cover all vendor parsers, the result mapper, and shared parsing utilities.

## Project Structure

```
src/main/java/com/gmail/scanner/
├── App.java                          # Spring Boot entry point
├── web/Endpoint.java                 # REST controller
├── service/
│   ├── OrderService.java             # Core Gmail scanning logic
│   ├── model/Source.java             # Vendor enum (9 vendors)
│   ├── parser/                       # Vendor-specific email parsers
│   └── queries/                      # Gmail search query builders
├── security/                         # OAuth2 config and token provider
├── google/GoogleServiceProvider.java # Gmail API client factory
└── mapper/ScanResultMapper.java      # Aggregates orders into results
```

## Notes

- This is a tool for personal use. Google's `gmail.readonly` scope requires a security audit to leave testing mode. 
- The app can only be used by accounts you manually whitelist in Google Cloud Console. See the `Audience` section of your app.
- Spending history is only as complete as your inbox. If you've deleted emails, they cannot and will not be scanned.
