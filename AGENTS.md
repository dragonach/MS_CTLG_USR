# Project Guide

- Use Java 23 as configured in `pom.xml`; run Maven from the repository root with `mvn`.
- Common checks: `mvn clean compile`, `mvn test`, and `mvn clean verify`.
- Application code is under `src/main/java/com/example/demo`, grouped into `controller`, `service`, `repository`, `model`, `dto`, `security`, `exception`, and `config` packages.
- Keep HTTP contracts in DTO records under `dto`; do not return JPA entities or expose password fields from controllers.
- `UserRepository` is the persistence boundary; keep use-case logic in services and avoid adding a duplicate DAO layer.
- H2 and `create-drop` are configured only for `dev` and `test`; production must provide a datasource and a base64 JWT secret through environment configuration.
- Add focused MockMvc/service tests for behavior changes and finish with the relevant Maven check.
