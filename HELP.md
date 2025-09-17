# API Documentation

This backend exposes authenticated REST endpoints under the `/api` prefix. All `/api/**` routes require a valid JWT in
the `Authorization` header.

- Base URL: https://youtrapbackend.onrender.com
- CORS: Allowed origins — https://youtrapbackend.onrender.com, http://localhost:8080 and http://localhost:3000
- Auth header: `Authorization: Bearer <JWT>`

Authentication overview

- OAuth2 login: Start the OAuth2 flow at `/oauth2/authorization/{provider}` (e.g., `google`). After successful login,
  you will be redirected to the configured `app.oauth2.redirect-uri` with a `token` query parameter (the JWT). Use that
  JWT as a Bearer token for subsequent API calls.
- Direct JWT: If you already have a JWT from elsewhere, include it in the `Authorization` header.

Error format
On errors, the APIs return:
{
"message": "<description>"
}
with appropriate HTTP status (e.g., 400 Bad Request, 401 Unauthorized).

Endpoints

1) Get current user info

- Method: GET
- Path: /api/user/me
- Headers: Authorization: Bearer <JWT>
- Description: Returns authenticated user profile and roles derived from the JWT.
- cURL:
  curl -X GET \
  https://youtrapbackend.onrender.com/api/user/me \
  -H "Authorization: Bearer <JWT>"
- Example response:
  {
  "email": "jane.doe@example.com",
  "name": "Jane Doe",
  "picture": "https://example.com/jane.png",
  "authorities": [
  "ROLE_USER"
  ]
  }

2) List YouTrack projects

- Method: GET
- Path: /api/youtrack/projects
- Headers: Authorization: Bearer <JWT>
- Description: Returns all projects available from the configured YouTrack instance.
- cURL:
  curl -X GET \
  https://youtrapbackend.onrender.com/api/youtrack/projects \
  -H "Authorization: Bearer <JWT>"
- Example response:
  [
  {
  "id": "0-0",
  "name": "Backend",
  "shortName": "BE"
  },
  {
  "id": "0-1",
  "name": "Frontend",
  "shortName": "FE"
  }
  ]

3) List issues for a project

- Method: GET
- Path: /api/youtrack/projects/{projectName}/issues
- Path params:
    - projectName: The project short name or name as configured in YouTrack (e.g., BE)
- Headers: Authorization: Bearer <JWT>
- Description: Returns issues for the specified project.
- cURL:
  curl -X GET \
  https://youtrapbackend.onrender.com/api/youtrack/projects/BE/issues \
  -H "Authorization: Bearer <JWT>"
- Example response:
  [
  {
  "idReadable": "BE-4",
  "summary": "Implement login",
  "project": { "name": "Backend" },
  "created": 1736451200000,
  "updated": 1736537600000,
  "url": "https://youtrack.example.com/issue/BE-4"
  },
  {
  "idReadable": "BE-5",
  "summary": "Add JWT support",
  "project": { "name": "Backend" },
  "created": 1736537600000,
  "updated": 1736624000000,
  "url": "https://youtrack.example.com/issue/BE-5"
  }
  ]

4) Get full dependency graph for an issue

- Method: GET
- Path: /api/youtrack/issues/{issueId}/graph
- Path params:
    - issueId: The readable YouTrack issue ID (e.g., BE-4)
- Headers: Authorization: Bearer <JWT>
- Description: Traverses "Depend" links in YouTrack to return all related issues reachable from the starting issue. Each
  element can include link information showing dependency directions and connected issue IDs.
- cURL:
  curl -X GET \
  https://youtrapbackend.onrender.com/api/youtrack/issues/BE-4/graph \
  -H "Authorization: Bearer <JWT>"
- Example response:
  [
  {
  "idReadable": "BE-4",
  "summary": "Implement login",
  "project": { "name": "Backend" },
  "created": 1736451200000,
  "updated": 1736537600000,
  "url": "https://youtrack.example.com/issue/BE-4",
  "links": [
  {
  "direction": "OUTWARD",
  "linkType": { "name": "Depend" },
  "issues": [ { "idReadable": "BE-5" } ]
  }
  ]
  },
  {
  "idReadable": "BE-5",
  "summary": "Add JWT support",
  "project": { "name": "Backend" },
  "created": 1736537600000,
  "updated": 1736624000000,
  "url": "https://youtrack.example.com/issue/BE-5",
  "links": [
  {
  "direction": "INWARD",
  "linkType": { "name": "Depend" },
  "issues": [ { "idReadable": "BE-4" } ]
  }
  ]
  }
  ]

Notes

- All examples assume your backend is configured with valid YouTrack credentials via `application.properties` (e.g.,
  `youtrack.base-url` and `youtrack.api.token`).
- Timestamps are epoch milliseconds from YouTrack.
- If you receive 401 Unauthorized, ensure your Bearer token is valid and not expired; for YouTrack errors, the backend
  also returns 401 when the configured YouTrack token is invalid.

