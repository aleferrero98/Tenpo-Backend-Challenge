# Tenpo Backend Challenge

## Descripción

API REST desarrollada con Java 21 y Spring Boot para resolver el challenge backend de Tenpo.

El servicio permite realizar un cálculo a partir de dos valores numéricos y aplicar sobre la suma un porcentaje dinámico obtenido desde un proveedor externo. Además, registra de forma asíncrona el historial de llamadas HTTP realizadas a la API y permite consultarlo de manera paginada y ordenable.

La aplicación incluye:

- Cálculo con porcentaje dinámico provisto por un servicio externo.
- Reintentos ante fallos del proveedor de porcentaje.
- Historial asíncrono de llamadas HTTP a la API.
- Consulta paginada y ordenable del historial.
- Rate limiting de 3 requests por minuto por dirección IP.
- Manejo global de errores.
- Persistencia en PostgreSQL.
- Migraciones de base de datos con Flyway.

## Funcionalidades principales

La API expone dos funcionalidades principales:

- Realizar un cálculo sobre dos números y aplicar un porcentaje dinámico a la suma.
- Consultar el historial de llamadas realizadas a la API, incluyendo datos como método HTTP, endpoint invocado, parámetros, código HTTP de respuesta, respuesta o error retornado, y fecha y hora de la llamada.

Todos los endpoints están sujetos a rate limiting por dirección IP.

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- Spring Retry
- Spring Async Events
- Spring Scheduling
- PostgreSQL
- Flyway
- Jackson
- Lombok
- Docker
- Docker Compose

## Requisitos previos

Para ejecutar la aplicación localmente con Docker Compose se requiere:

- Docker
- Docker Compose

## Ejecución con Docker Compose

1. Clonar el repositorio:

```bash
git clone https://github.com/aleferrero98/Tenpo-Backend-Challenge.git
cd Tenpo-Backend-Challenge
```

2. Crear el archivo de variables de entorno:

```bash
cp .env.example .env
```

3. Levantar la API y PostgreSQL:

```bash
docker compose up --build
```

La aplicación quedará disponible en:

```text
http://localhost:8282
```

Para detener los contenedores:

```bash
docker compose down
```

Para detener los contenedores y eliminar los datos persistidos en PostgreSQL:

```bash
docker compose down -v
```

## Ejecución de tests

Los tests unitarios pueden ejecutarse con Maven Wrapper:

```bash
./mvnw test
```

La suite de tests no requiere levantar Docker Compose ni una base PostgreSQL local.

## Variables de entorno

Las variables utilizadas por Docker Compose se definen en el archivo `.env`.

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_NAME` | Nombre de la base de datos PostgreSQL | `tenpo_challenge` |
| `DB_USER` | Usuario de PostgreSQL | `tenpo` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `12345` |

Estas variables son utilizadas para inicializar el contenedor de PostgreSQL y configurar la conexión de la aplicación.

## Consumo de la API

### Realizar un cálculo

Calcula la suma de `num1` y `num2`, obtiene un porcentaje dinámico desde el proveedor externo y aplica ese porcentaje sobre la suma.

```http
POST /api/v1/percentage/calculate HTTP/1.1
Host: localhost:8282
Content-Type: application/json
```

Ejemplo con `curl`:

```bash
curl -X POST http://localhost:8282/api/v1/percentage/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1": 2, "num2": 7}'
```

Request body:

```json
{
  "num1": 2,
  "num2": 7
}
```

Response `200 OK`:

```json
{
  "result": 9.9
}
```

### Consultar el historial

Permite consultar el historial de llamadas registradas por la API.

```http
GET /api/v1/call-history?page=0&size=6&sort=createdAt,asc HTTP/1.1
Host: localhost:8282
```

Ejemplo con `curl`:

```bash
curl "http://localhost:8282/api/v1/call-history?page=0&size=6&sort=createdAt,asc"
```

Parámetros soportados:

| Parámetro | Descripción | Valor por defecto |
|---|---|---|
| `page` | Número de página, comenzando en `0` | `0` |
| `size` | Cantidad de elementos por página. El máximo permitido es `100` | `20` |
| `sort` | Campo y dirección de ordenamiento en formato `campo,direccion` | `createdAt,asc` |

Campos permitidos para ordenamiento:

- `httpMethod`
- `endpoint`
- `httpStatusCode`
- `createdAt`

Response `200 OK`:

```json
{
  "data": [
    {
      "http_method": "POST",
      "endpoint": "/api/v1/percentage/calculate",
      "parameters": {
        "headers": {
          "host": "localhost:8282",
          "content-type": "application/json"
        },
        "body": {
          "num1": 2,
          "num2": 7
        }
      },
      "http_status_code": 200,
      "response": {
        "result": 9.9
      },
      "created_at": "2026-07-29T16:05:27Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 6,
    "total_elements": 1,
    "total_pages": 1
  }
}
```

### Rate limiting

Cada dirección IP puede realizar hasta 3 requests por minuto.

Al superar el límite, la API responde con `429 Too Many Requests` e incluye el header `Retry-After` indicando cuántos segundos esperar antes de realizar una nueva solicitud.

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 27
Content-Type: application/json
```

```json
{
  "message": "Rate limit exceeded. Maximum 3 requests per minute."
}
```

## Documentación de la API

La especificación OpenAPI se mantiene en:

```text
src/main/resources/static/openapi.yaml
```

Con la aplicación en ejecución, la especificación puede consultarse en:

```text
http://localhost:8282/openapi.yaml
```

Swagger UI está disponible en:

```text
http://localhost:8282/swagger-ui.html
```

## Imagen de Docker Hub

La imagen pública puede descargarse mediante:

```bash
docker pull <dockerhub-user>/backend-challenge:latest
```

Para ejecutarla:

```bash
docker run --rm \
  -p 8282:8282 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/tenpo_challenge \
  -e SPRING_DATASOURCE_USERNAME=tenpo \
  -e SPRING_DATASOURCE_PASSWORD=12345 \
  <dockerhub-user>/backend-challenge:latest
```

## Decisiones técnicas

Las decisiones de arquitectura, persistencia, asincronía, rate limiting, manejo de errores y manejo de fechas están documentadas en:

[Decisiones técnicas](docs/technical-decisions.md)
