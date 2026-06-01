## Backend Tests

Run from the project root:

```powershell
.\mvnw.cmd test
```

This runs:

- Spring context test
- Unit tests
- Internal backend Cucumber API acceptance test
- Delete-analysis acceptance scenario


## Frontend Cucumber Tests

Run from the frontend folder:

```powershell
cd frontend
npm.cmd run test:cucumber
```

These tests start the Vite app and use Playwright to test real browser behavior. Backend responses are mocked, so Docker and the Spring Boot server are not required.

On a fresh machine, install dependencies first:

```powershell
cd frontend
npm install
npx playwright install chromium
```

## Frontend Build Check

Run from the frontend folder:

```powershell
npm.cmd run build
```

This confirms the React app can be built for production.

## External Cucumber Test

This test calls a real running application and real configured external services. Start the app first, for example with Docker:

```powershell
docker compose up --build
```

Then in another terminal run:

```powershell
.\mvnw.cmd -Dtest=ExternalCucumberIT -Dexternal.baseUrl=http://localhost:8085 test
```

It can fail if Ollama, RapidAPI, Docker networking, or the outside database is unavailable.

## SoapUI Testing

SoapUI is used for manual API testing and presentation. It is good for showing that endpoints work against the running app.

Typical flow:

1. Start the app with Docker:

   ```powershell
   docker compose up --build
   ```

2. Open the SoapUI project.
3. Register or login.
4. Copy the JWT token from the login response.
5. Add the token to protected requests:

   ```text
   Authorization: Bearer YOUR_TOKEN_HERE
   ```

6. Run endpoint requests manually.

To stop Docker:

```powershell
docker compose down
```
