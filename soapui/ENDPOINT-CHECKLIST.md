# SoapUI Endpoint Checklist

Use this checklist when testing the current API manually in SoapUI.

## Base Setup

- Base URL: `http://localhost:8085`
- Protected endpoints require `Authorization: Bearer <token>`
- `POST /auth/login` does not return the token in the body
- For local runs, read the token from the backend console
- For Docker runs, read the token from Docker logs

## Endpoint Order

Follow this order because some endpoints depend on earlier in-memory state.

1. `POST /auth/register`
Expected: `200 OK`
Expected body: `User registered: <username>`

Request body:
```json
{
  "username": "soapui-demo-user",
  "password": "Test1234!",
  "email": "soapui-demo-user@example.com"
}
```

2. `POST /auth/login`
Expected: `200 OK`
Expected body: `User logged in`
Note: copy JWT from console or Docker logs

Request body:
```json
{
  "username": "soapui-demo-user",
  "password": "Test1234!"
}
```

3. `GET /hotels?location=Barcelona`
Expected: `200 OK`
Expected body: JSON array of location objects
Use one returned `dest_id` in the next request

4. `POST /hotels`
Expected: `200 OK`
Expected body: JSON array of hotels

Request body:
```json
{
  "hobbiesAndInterests": "architecture, museums, local food, walking",
  "promptToOllama": "Recommend hotels for a curious traveler who likes history and walkable neighborhoods.",
  "destinationId": -372490,
  "checkInDate": "2026-06-15",
  "checkOutDate": "2026-06-18",
  "roomNumber": 1,
  "adultsNumber": 2
}
```

5. `POST /analysis/analyze`
Expected: `200 OK`
Expected body: JSON object with `analysis` and `hotels`
Note: requires Ollama to be reachable

Request body:
```json
{
  "userPrompt": "Plan a comfortable city break with good food and museums.",
  "userHobbies": "museums, architecture, cafes"
}
```

6. `POST /analysis`
Expected: `200 OK`
Expected body: `Analysis with id: <id>, saved`
Note: keep the saved `<id>` for the next requests

7. `GET /analysis/analyses`
Expected: `200 OK`
Expected body: HATEOAS collection of saved analyses for the logged-in user

8. `GET /analysis/{id}`
Expected: `200 OK`
Expected body: one saved analysis with a `hotels` link

9. `GET /analysis/{id}/hotels`
Expected: `200 OK`
Expected body: HATEOAS collection of hotel links for that analysis

10. `GET /analysis/{analysisId}/hotels/{hotelId}`
Expected: `200 OK`
Expected body: one hotel resource

11. `DELETE /analysis/{id}`
Expected: `200 OK`
Expected body: deleted analysis id

12. `DELETE /analysis/clearAnalysis`
Expected: `200 OK`
Expected body: `clear`
Note: clears only in-memory current analysis, not saved DB rows

13. `DELETE /analysis/clearHotels`
Expected: `200 OK`
Expected body: `Hotels service: Hotels cleared`
Note: clears only in-memory hotel list

14. `POST /auth/logout?token=<jwt>`
Expected: `200 OK`
Expected body: `User with username: <username>, logged out.`
Note: run this last because it blacklists the token

## Common Failure Modes

- `401` or `403`: missing or invalid JWT
- `POST /hotels` fails: wrong `destinationId` or missing auth header
- `POST /analysis/analyze` times out: Ollama not reachable
- `GET /analysis/{id}` returns errors: using in-memory `id: 0` instead of the saved analysis id from `POST /analysis`
