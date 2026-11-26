## Plan: Endurecer y ordenar la API

Reorganizar la API de usuarios y autenticación con límites claros entre HTTP, lógica de aplicación y persistencia. El usuario permite cambios de contrato; se puede evolucionar rutas y payloads. Mantener el alcance en las funciones existentes, sin añadir roles o refresh tokens sin requisito.

**Pasos**

**Fase 1: Contrato HTTP y DTOs**
1. Definir rutas coherentes para usuarios y autenticación; mover el saludo de `GET /users/` a una ruta raíz/health o eliminarlo.
2. Crear DTOs separados para alta, actualización, login y respuesta pública de usuario. No aceptar `id` ni serializar contraseñas desde la entidad.
3. Aplicar `@Valid` a los endpoints de escritura, corregir restricciones por tipo de dato y decidir semántica explícita para `PUT` completo frente a `PATCH` parcial.
4. Añadir respuestas HTTP consistentes: 201 con `Location`, 400 por validación, 404 por ausencia y 409 por username duplicado; centralizar errores con `@RestControllerAdvice`.

**Fase 2: Capas de aplicación y datos**
5. Mantener `UserController` enfocado en HTTP y mover autenticación/token fuera del controlador a un servicio de aplicación.
6. Mantener `UserService` como única capa de casos de uso; retirar `UserDAO`, que no tiene consumidores y duplica `UserRepository`, o justificar explícitamente un patrón de persistencia distinto antes de hacerlo.
7. Alinear `User`, `UserRepository` y servicios: restricciones de entidad/DB, `Optional` para búsqueda por username, errores de dominio y transacciones en operaciones de escritura/lectura.
8. Aislar H2 y `ddl-auto=create-drop` a desarrollo/pruebas; definir configuración externa y estrategia de migraciones antes de producción. Quitar Spring Data JDBC si no se incorpora un repositorio JDBC.

**Fase 3: Seguridad y organización**
9. Alinear paquetes y responsabilidades (`security.config` para filtros/configuración; `security.service` para autenticación y JWT), y evitar que el controlador dependa de `AuthenticationManager` o del servicio JWT directamente.
10. Mantener la API stateless, definir explícitamente 401/403 para token ausente, inválido o insuficiente, y conservar la codificación BCrypt al registrar usuarios.
11. Retirar el secreto JWT predeterminado para perfiles no locales; exigir `JWT_SECRET` seguro y documentar expiración y configuración.
12. Revisar dependencias y estructura: un único paquete `dto`, retirar carpetas raíz vacías (`dtos/`, `path/to/`) si están confirmadas como residuos, y actualizar `AGENTS.md` para Java/Maven y la estructura real.

**Fase 4: Verificación y documentación**
13. Añadir pruebas de validación, CRUD, username duplicado, contraseña almacenada con hash, respuesta sin contraseña, login 401/200 y rutas protegidas con token ausente, inválido, expirado y válido.
14. Actualizar Swagger con esquema Bearer, DTOs y códigos de respuesta reales; documentar endpoints, perfiles de configuración y variables de entorno.
15. Ejecutar `mvn clean verify` desde la raíz y revisar manualmente el contrato OpenAPI y las respuestas de error.

**Archivos relevantes**
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\controller\UserController.java` — rutas y contratos HTTP actuales.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\service\UserService.java` — casos de uso y acceso directo actual al repositorio.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\dao\UserDAO.java` — DAO duplicado sin referencias externas.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\repository\UserRepository.java` — interfaz JPA y búsqueda por username.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\model\User.java` — entidad y serialización actual.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\dto\UserDTO.java`, `AuthenticationRequest.java`, `AuthenticationResponse.java` — contratos actuales; corregir restricciones y separar alta/actualización/respuesta.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\security\config\SecurityConfig.java`, `JwtAuthenticationFilter.java`, `JwtService.java`, `AuthService.java` — configuración y autenticación existentes; alinear paquetes/roles.
- `H:\Cursos\Dev\SpringBoot\demo\src\main\resources\application.properties` — H2, esquema y secreto JWT.
- `H:\Cursos\Dev\SpringBoot\demo\src\test\java\com\example\demo\DemoApplicationTests.java` — pruebas de integración actuales para ampliar.
- `H:\Cursos\Dev\SpringBoot\demo\pom.xml`, `H:\Cursos\Dev\SpringBoot\demo\AGENTS.md`, `H:\Cursos\Dev\SpringBoot\demo\src\main\java\com\example\demo\config\SwaggerConfig.java` — dependencias, guía desactualizada y OpenAPI.

**Verificación**
1. Pruebas unitarias de servicio para reglas de alta/actualización, duplicados y codificación de contraseña.
2. Pruebas MockMvc de CRUD, validación y matriz de autenticación/autorización; confirmar que ninguna respuesta expone `password`.
3. `mvn clean verify` desde `H:\Cursos\Dev\SpringBoot\demo`.
4. Inspección de Swagger para confirmar rutas, DTOs, Bearer y códigos HTTP documentados.

**Decisiones**
- Cambios incompatibles de contrato están permitidos.
- Recomendación: no conservar una capa DAO vacía; Spring Data `UserRepository` ya cubre ese límite de persistencia.
- No se asume base de datos de producción, roles múltiples, refresh tokens ni paginación obligatoria; decidirlos si aparecen requisitos de despliegue o volumen.
- Estado verificado en esta revisión: `mvn clean test` pasa con 3 pruebas; esto no cubre aún validaciones ni casos de seguridad negativos completos.
