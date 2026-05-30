# Docker Testing With SoapUI

This project can be tested through SoapUI while the backend runs in Docker and the database stays external.

## 1. Start the backend in Docker

From the project root:

```powershell
docker compose up --build
```

The backend should be available at:

- `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`

## 2. Why token handling is different

The current `POST /auth/login` endpoint returns only:

```text
User logged in
```

It does not return the JWT in the response body.

The token is printed by the backend to standard output during login, so when the app runs in Docker you must read it from the backend container logs.

## 3. Get the token from Docker logs

Keep the backend logs open:

```powershell
docker compose logs -f trip_advisor.server
```

Then run this request in SoapUI:

- `POST /auth/login`

Example body:

```json
{
  "username": "soapui-demo-user",
  "password": "Test1234!"
}
```

After login succeeds, look in the backend logs for a long JWT string like:

```text
eyJhbGciOiJIUzUxMiJ9...
```

Copy that token and paste it into SoapUI as:

```text
Authorization: Bearer <copied-jwt>
```

## 4. SoapUI flow with Docker

1. `POST /auth/register`
2. `POST /auth/login`
3. Read token from `docker compose logs -f trip_advisor.server`
4. Add `Authorization: Bearer <token>` to protected requests
5. Continue with the rest of the flow from [ENDPOINT-CHECKLIST.md](D:\Studijoms\Programavimas\Grupinis_playground\Grupinis_playground\soapui\ENDPOINT-CHECKLIST.md:1)

## 5. Notes about Ollama

When the backend runs in Docker, the compose setup uses the Docker Spring profile and points the backend to:

```text
http://host.docker.internal:11434
```

That means Ollama must be running on the host machine and reachable from Docker.

## 6. Stop Docker services

When you are done:

```powershell
docker compose down
```
