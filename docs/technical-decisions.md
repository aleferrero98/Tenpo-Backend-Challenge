# Technical Decisions

## Arquitectura general

**Decisión:** La aplicación sigue una arquitectura MVC / Service-Repository.

**Motivo:** El alcance del challenge tiene poca lógica de negocio compleja y se enfoca más en manejo de errores, integración, persistencia, asincronía, rate limiting y documentación. Esta estructura permite separar responsabilidades de forma simple y mantener el código legible.

**Detalle:** Los DTOs se modelan principalmente con `record`, aprovechando características de Java 21.

## Cálculo con porcentaje dinámico

**Decisión:** Se utiliza `BigDecimal` para las operaciones numéricas.

**Motivo:** Aunque el challenge es simple, el contexto fintech requiere evitar errores de precisión y redondeo típicos de tipos floating point.

**Decisión:** El cálculo se expone mediante `POST` y recibe los argumentos en el request body.

**Motivo:** Resulta más claro y extensible que pasar los valores como query params o path variables.

**Decisión:** `PercentageProvider.getPercentage()` simula una llamada a un proveedor externo y utiliza retry.

**Motivo:** Ante la ausencia de un servicio externo real, se modela el comportamiento esperado con hasta 3 intentos y un backoff fijo de 500 ms. En un caso productivo, solo se mapearían como `PercentageProviderException` las fallas esperadas y reintentables del proveedor externo.

## Concurrencia y asincronía

**Decisión:** Se habilitan virtual threads para la aplicación.

**Motivo:** Permiten mantener un modelo imperativo y mejorar la escalabilidad frente a operaciones bloqueantes, sin adoptar programación reactiva para este alcance.

**Decisión:** El historial utiliza un executor dedicado basado en platform threads.

**Motivo:** Aunque la aplicación tiene virtual threads habilitados, el historial usa un `ThreadPoolTaskExecutor` propio para limitar la concurrencia de escrituras y proteger PostgreSQL. Esto evita competir directamente con otras tareas asíncronas y mantiene la persistencia del historial como un flujo best effort.

## Manejo de errores

**Decisión:** Las excepciones se centralizan en un `GlobalExceptionHandler`.

**Motivo:** Permite adaptar las respuestas con códigos HTTP y mensajes consistentes sin duplicar manejo de errores en los controllers.

## Persistencia y migraciones

**Decisión:** Se utiliza PostgreSQL como base de datos y `JSONB` para los campos flexibles del historial.

**Motivo:** `parameters` y `response_body` pueden variar según el endpoint, el body, los query params, los headers y el tipo de respuesta. `JSONB` permite almacenar esa información sin multiplicar columnas ni acoplar el esquema a cada endpoint.

**Decisión:** Se utiliza Flyway con migraciones versionadas y `spring.jpa.hibernate.ddl-auto=validate`.

**Motivo:** Flyway queda como responsable de crear y evolucionar la estructura de base de datos, mientras Hibernate solo valida que el esquema existente sea compatible con las entidades JPA.

## Historial de llamadas HTTP

**Decisión:** La escritura del historial se implementa con `ApiCallHistoryFilter`, eventos internos de Spring y un listener asíncrono.

**Motivo:** El filtro permite capturar transversalmente endpoints existentes y futuros. La publicación de eventos desacopla el filtro de la persistencia y evita que el flujo HTTP principal dependa directamente de JPA o PostgreSQL.

**Decisión:** Se usan `ContentCachingRequestWrapper` y `ContentCachingResponseWrapper`.

**Motivo:** Permiten leer el request body y el response body sin consumir definitivamente los streams originales ni alterar la respuesta recibida por el cliente.

**Decisión:** Los bodies persistidos se truncan cuando superan el límite definido.

**Motivo:** Evita almacenar payloads excesivamente grandes en PostgreSQL. El truncado solo afecta la información persistida, no la request procesada por el controller ni la response enviada al cliente.

**Decisión:** Se excluyen headers sensibles del historial.

**Motivo:** Headers como `Authorization`, cookies o API keys pueden contener credenciales o tokens. Se omiten completamente en lugar de guardarlos enmascarados para reducir riesgos de exposición.

**Decisión:** La consulta del historial también genera una entrada en el historial.

**Motivo:** Para el alcance actual y la poca cantidad de endpoints, se considera aceptable registrar también esta consulta. En una API más grande podría evaluarse excluirla para reducir ruido.

## Consulta paginada del historial

**Decisión:** La lectura del historial se expone mediante un endpoint paginado.

**Motivo:** Evita devolver todos los registros y permite consultar el historial de forma controlada.

**Detalle:** La paginación comienza en `page=0`, usa `size=20` por defecto y limita `size` a un máximo de `100`.

**Decisión:** El ordenamiento se valida con una whitelist.

**Motivo:** Solo se permite ordenar por campos seguros y mapeables contra la entidad: `httpMethod`, `endpoint`, `httpStatusCode` y `createdAt`. No se permite ordenar por columnas `JSONB` como `parameters` o `responseBody`.

## Rate limiting

**Decisión:** El rate limiting se implementa en memoria y por dirección IP.

**Motivo:** El challenge está orientado a una única instancia de la aplicación. Por eso, un `ConcurrentHashMap` administrado por `InMemoryRateLimitStore` es suficiente para mantener contadores independientes por IP sin incorporar Redis u otro almacenamiento externo.

**Decisión:** Se utiliza el algoritmo Fixed Window.

**Motivo:** Es simple de implementar y requiere pocos recursos. Cada IP tiene una ventana de 60 segundos iniciada desde su primera request dentro de la ventana, no sincronizada necesariamente con el segundo 0 de cada minuto.

**Decisión:** El filtro de rate limiting se ejecuta después del filtro de historial.

**Motivo:** De esta forma, las solicitudes rechazadas por rate limit quedan registradas en el historial con status `429` y su body de error. En un entorno productivo, normalmente sería conveniente ejecutar el rate limiting antes del historial para rechazar tráfico excedente lo antes posible y evitar trabajo innecesario.

**Decisión:** Se agrega un scheduler de limpieza.

**Motivo:** `RateLimitCleanupScheduler` elimina periódicamente de memoria las ventanas expiradas para evitar crecimiento innecesario del mapa.

## Documentación OpenAPI

**Decisión:** La documentación se mantiene en un archivo `openapi.yaml` manual.

**Motivo:** Evita llenar el código fuente con anotaciones y detalles exclusivos de documentación. La documentación se sirve con Swagger UI.

**Decisión:** Las URLs de Swagger/OpenAPI se excluyen de los filtros de historial y rate limiting.

**Motivo:** Las visitas a la documentación no deberían consumir cuota de rate limit ni generar ruido en el historial de llamadas de negocio.
