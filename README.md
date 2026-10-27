# Sistema de Gestión de Productos y Precios Históricos

He desarrollado una API REST en Java 21 con Spring Boot para gestionar productos y sus precios históricos a lo largo del tiempo. Para simplificar la ejecución y asegurar que el proyecto sea completamente autocontenido sin requerir bases de datos externas, he utilizado H2 en memoria configurada en modo PostgreSQL.

---

## Decisiones Técnicas

- **Spring Boot 3.4.3 & Java 21**: He elegido Spring Boot por ser el framework estándar y con mayor soporte en el ecosistema Java empresarial. Permite centrarnos en la lógica de negocio y cuenta con integración nativa para Spring Data JPA, validaciones declarativas y documentación.
- **Virtual Threads (Java 21 Project Loom)**: He aprovechado que utilizamos Java 21 para activar los Virtual Threads (`spring.threads.virtual.enabled: true`). Esto permite que el servidor Tomcat gestione cada petición concurrente mediante un hilo virtual ligero en lugar de depender exclusivamente de un pool de hilos de sistema operativo, reduciendo notablemente el consumo de recursos bajo alta concurrencia.
- **H2 en memoria**: Configurada con compatibilidad para PostgreSQL (`MODE=PostgreSQL;DB_CLOSE_DELAY=-1`) y dialecto H2 de Hibernate, garantizando rapidez de arranque, cero dependencias externas y consistencia transaccional.
- **Arquitectura por capas**: He organizado el proyecto siguiendo la arquitectura clásica y clara de Spring (`controller`, `dto`, `model`, `repository`, `service`, `exception`), evitando sobreingeniería innecesaria y facilitando la legibilidad.

---

## Qué se ha implementado

1. **Crear un producto (`POST /products`)**:
   - Registro de productos con nombre y descripción.
   - Validación de campos requeridos mediante Bean Validation (`@NotBlank`).
2. **Añadir precio a un producto (`POST /products/{id}/prices`)**:
   - Registro de precios asociados al producto (`@Positive`, `@NotNull`).
   - Validación estricta de que `initDate < endDate` cuando ambas fechas están presentes.
   - Soporte para precios sin fecha de fin (`endDate` nulo), representando vigencia indefinida hacia adelante.
   - Validación de reglas de negocio para impedir solapamientos temporales con otros precios del mismo producto.
3. **Obtener el precio vigente en una fecha (`GET /products/{id}/prices?date=YYYY-MM-DD`)**:
   - Retorna `{ "value": X.XX }` según la especificación del contrato.
   - Búsqueda optimizada por fecha en base de datos.
4. **Obtener el historial completo de precios (`GET /products/{id}/prices`)**:
   - Retorna el nombre, descripción y el listado de precios ordenados cronológicamente por fecha de inicio.
5. **Manejo global de excepciones (`@ControllerAdvice`)**:
   - Respuestas HTTP `404 Not Found` consistentes ante recursos inexistentes.
   - Respuestas HTTP `400 Bad Request` claras para solapamientos de precios, rangos de fechas erróneos y errores de validación de DTOs.
6. **Documentación Swagger / OpenAPI**:
   - Integración con `springdoc-openapi` para explorar y probar interactivamente la API.
7. **Batería de Tests**:
   - Tests unitarios con Mockito para servicios cubriendo flujos felices y casos límite de solapamiento de fechas.
   - Tests de integración de extremo a extremo con MockMvc (`ProductControllerIntegrationTest`).

---

## Optimizaciones de Rendimiento y Concurrencia

Uno de los requisitos clave de la prueba era lograr el mejor rendimiento y estabilidad bajo carga con los límites de 1 CPU y 1 GB de RAM. Tras configurar un escenario de carga con k6, apliqué las siguientes optimizaciones:

- **Validación de solapamiento en base de datos**: En lugar de cargar todos los precios a memoria Java para iterarlos, la verificación se delega directamente a la base de datos mediante una consulta JPQL optimizada (`existsOverlappingPrice`) en `PriceRepository`.
- **Índice compuesto en BD**: Añadí un índice compuesto en la tabla de precios por `(product_id, init_date DESC)` para agilizar drásticamente las consultas de precios vigentes y los ordenamientos cronológicos.
- **Afinamiento del pool HikariCP**: Se configuró un pool de 30 conexiones (`maximum-pool-size: 30`, `connection-timeout: 2000`) para evitar cuellos de botella ante ráfagas de usuarios concurrentes.
- **Batching de Hibernate**: Configurado `batch_size: 25` con ordenamiento de inserciones para reducir viajes de red hacia la base de datos.

### Resultados en el Benchmark (k6)
- **Latencia p95**: **~56 ms** (muy por debajo del umbral de 200 ms exigido).
- **Latencia media**: **~10 ms**.
- **Tasa de error**: **0.00%** (0 errores en más de 98.000 peticiones procesadas a más de 1.000 req/s).

---

## Cómo Ejecutar el Proyecto

### 1. Benchmark Automatizado con Docker Compose (Recomendado)

Levanta la API, espera de forma activa a que esté lista en `/actuator/health`, ejecuta la batería de carga con k6 y se detiene automáticamente mostrando el reporte con los umbrales:

```bash
docker compose up --build --exit-code-from benchmark
```
*(o con `docker-compose up --build --exit-code-from benchmark` según la versión instalada de Docker).*

### 2. Ejecución Normal de la API

Para levantar únicamente el contenedor de la API:

```bash
docker compose up --build app
```

Una vez levantada, puedes acceder a:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Consola H2**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:productdb`, Usuario: `sa`, Contraseña vacía)*
- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 3. Ejecución en Local con Gradle

Si se dispone de Java 21 instalado localmente:

```bash
# Ejecutar tests
./gradlew test

# Arrancar la aplicación
./gradlew bootRun
```
*(En Windows utilizar `.\gradlew.bat`).*
