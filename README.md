# Demo API

Spring Boot 4 API for user registration, profile management, and JWT authentication. Requires Java 23.

## Run locally

The default `dev` profile uses an in-memory H2 database and a development-only JWT key. From the repository root:

```powershell
mvn spring-boot:run
```

For deployment, activate the `prod` profile explicitly and provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and a base64-encoded `JWT_SECRET` containing at least 256 bits of key material. The production profile validates the schema; database migrations must be applied by the deployment process.

## API

OpenAPI UI: `/swagger-ui/index.html`  
OpenAPI document: `/v3/api-docs`

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/users` | Public | Register; returns `201` and a `Location` header |
| `POST` | `/api/v1/auth/token` | Public | Authenticate and issue a one-hour JWT |
| `GET` | `/api/v1/users/me` | Bearer JWT | Read the authenticated user's profile |
| `PUT` | `/api/v1/users/me` | Bearer JWT | Replace the authenticated user's public profile |
| `DELETE` | `/api/v1/users/me` | Bearer JWT | Delete the authenticated user's account |

Validation errors return `400`, invalid credentials or tokens return `401`, inaccessible resources return `404`, and duplicate usernames return `409`. User responses never include password hashes.

## Checks

```powershell
mvn clean compile
mvn test
mvn clean verify
```