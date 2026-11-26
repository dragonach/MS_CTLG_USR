---
name: Skill Validaciones Java
description: Estándar para DTOs y validaciones Bean Validation (Jakarta/Javax)
invokable: true
globs: ["**/dto/**/*.java", "**/*Request.java", "**/*DTO.java"]
alwaysApply: false
---



Cuando crees o modifiques DTOs y validaciones:
- Utiliza anotaciones de Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Positive`) con mensajes descriptivos en español (`message = "..."`).
- Prefiere `record` de Java (si el proyecto usa Java 17+) o clases inmutables con Lombok (`@Builder`, `@Value` o `@Data`) según el estándar detectado en el proyecto.
Valida siempre buscar la capa DTO con file_glob_search
Si encuentras la capa DTO usa read_file para revisar el contenido del archivo
Si no existe usa create_new_file para crear el archivo
Utiliza siempre la herramienta run_terminal_command si es necesario
Usa el comando ls si es necesario
Si encuentras la capa y los archivos pero no cumplen con los estandares utiliza edit_existing_file para agregar las validaciones

