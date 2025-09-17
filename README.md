# YouTrapBackend

This document describes the endpoints, how to connect to them, what to send and what you will receive, and the
refactoring rationale applied in this iteration.

Current date: 2025-09-17 20:55

## Authentication

All `/api/**` endpoints require authentication (see SecurityConfig). Two ways are supported:

1. OAuth2 login (e.g., Google) — after successful login, you will be redirected to the configured
   `app.oauth2.redirect-uri` with a `token` query parameter.
2. Subsequent API calls must include the JWT in the `Authorization` header as `Bearer <token>`.

Configuration:

- `jwt.secret` and `jwt.expiration-ms` are used to sign and validate tokens.
- CORS is configured to allow common local dev origins.

## Error Handling

Errors are handled centrally by `GlobalExceptionHandler` (@ControllerAdvice):

- `IllegalArgumentException` -> 400
- `UnauthorizedToYouTrackException` -> 401
- Any other exceptions -> 500

Response body for errors:

```
{
  "message": "..."
}
```

## Endpoints

Base URL: depends on deployment. For local dev, defaults to `http://localhost:8080`.

Remember to include: `Authorization: Bearer <token>` header for all endpoints under `/api/**`.

### 1) YouTrack: Get dependency graph for an issue

- Method: GET
- Path: `/api/youtrack/issues/{issueId}/graph`
- Path params:
    - `issueId` (string): e.g., `PRJ-123`
- Request headers:
    - `Authorization: Bearer <token>`
- Response: 200 OK
    - Body: JSON array of IssueDetailsResponse

IssueDetailsResponse schema:

```
[
  {
    "idReadable": "PRJ-123",
    "summary": "Issue title",
    "project": { "name": "Project Name" },
    "created": 1700000000000,
    "updated": 1700000100000,
    "url": "https://<youtrack>/issue/PRJ-123",
    "links": [
      {
        "direction": "OUTWARD",
        "linkType": { "name": "Depend" },
        "issues": [ { "idReadable": "PRJ-124" } ]
      }
    ],
    "state": "Open"
  }
]
```

### 2) YouTrack: Get issues for a project

- Method: GET
- Path: `/api/youtrack/projects/{projectName}/issues`
- Path params:
    - `projectName` (string): Project display name as in YouTrack
- Response: 200 OK
    - Body: JSON array of IssueDetailsResponse (same schema as above)

### 3) YouTrack: Get all projects

- Method: GET
- Path: `/api/youtrack/projects`
- Response: 200 OK
    - Body: JSON array of projects

Project schema:

```
[
  { "id": "0-0", "name": "Project Name", "shortName": "PRJ" }
]
```

### 4) User: Get current user info

- Method: GET
- Path: `/api/user/me`
- Response: 200 OK
    - Body: UserInfoResponse

UserInfoResponse schema:

```
{
  "email": "user@example.com",
  "name": "User Name",
  "picture": "https://...",
  "authorities": ["ROLE_USER"]
}
```

## Refactoring Summary (Why and What)

### Package-by-Feature vs. Package-by-Layer

- Rationale:
    - Modularity: Grouping by feature (youtrack, user) keeps everything needed for a feature together (controller,
      service, client, exceptions), easing change.
    - Cohesion: Classes that change together live together, reducing cross-package churn.
    - Scalability: As features grow, new subpackages can be added without exploding cross-cutting dependencies.
- Applied structure:
    - `com.jetbrains.youtrapbackend.youtrack` — YouTrackController, YouTrackService, YouTrackClient,
      UnauthorizedToYouTrackException
    - `com.jetbrains.youtrapbackend.user` — UserController
    - `com.jetbrains.youtrapbackend.common.config` — SecurityConfig
    - `com.jetbrains.youtrapbackend.common.security` — JwtAuthenticationFilter, JwtTokenProvider,
      OAuth2AuthenticationSuccessHandler, UserPrincipal
    - `com.jetbrains.youtrapbackend.common.exception` — GlobalExceptionHandler
    - `com.jetbrains.youtrapbackend.dto` — ErrorResponse
    - `com.jetbrains.youtrapbackend.dto.youtrack` — IssueDetailsResponse
    - `com.jetbrains.youtrapbackend.dto.user` — UserInfoResponse

### Centralized Exception Handling with @ControllerAdvice

- Purpose: Eliminate repetitive try/catch blocks in controllers and ensure consistent error responses.
- Benefits:
    - Clean controllers: Controllers focus on orchestration and delegating to services.
    - Consistency: One place defines HTTP codes and error payloads.
    - Maintainability: Add or modify mappings once; applies everywhere.
- Implemented in `GlobalExceptionHandler` which returns `ErrorResponse` for known and generic errors.

### DTO Organization

- Role of DTOs: Stable API contracts decoupled from internal/domain models; they shape exactly what the API returns.
- Why a dedicated package: Clear separation from controllers/services; discoverable by clients and maintainers; avoids
  circular dependencies between features.
- Changes:
    - Moved `ErrorResponse` to `com.jetbrains.youtrapbackend.dto`
    - Moved `IssueDetailsResponse` to `com.jetbrains.youtrapbackend.dto.youtrack`
    - Moved `UserInfoResponse` to `com.jetbrains.youtrapbackend.dto.user`

## Notes

- Package declarations were updated to reflect the feature-based structure. Physical file locations can remain; Kotlin
  compiles by package name, not folder layout, but IDEs may suggest moving files accordingly.
- No functional behavior was changed; only structure and exception handling were improved. All builds pass after
  refactor.
