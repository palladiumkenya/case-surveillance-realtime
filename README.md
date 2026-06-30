# Kenya HMIS Case Surveillance Realtime API

A Java 17 / Spring Boot 3.1 multi-module Maven project (`org.kenyahmis`) that ingests HIV case
surveillance events from EMR systems. Events arrive over a REST API, flow through Kafka, and are
persisted to PostgreSQL.

## Modules

- **shared** - DTOs, validators, constants (`GlobalConstants`), and shared utilities. Used by `api` and `worker`.
- **api** - REST service exposing `PUT /api/event/sync`. Validates event batches, applies Redis based
  deduplication, and publishes to Kafka. Secured with Keycloak OAuth2 JWT.
- **worker** - Kafka consumers that validate, map (MapStruct), and upsert events plus clients into PostgreSQL.
  Uses Flyway for schema migrations.
- **admin** - Spring Boot Admin server for monitoring the api and worker instances.

## Data Flow

```
EMR system -> PUT /api/event/sync (JWT) -> Redis dedup check -> Kafka topics
Kafka topics -> worker consumers -> validate -> upsert to PostgreSQL
```

The API routes events to topics by type:

| Topic | Partitions | Event types |
|-------|-----------|-------------|
| `linkage_events` | 1 | `new_case`, `linked_case` |
| `prep_events` | 2 | `at_risk_pbfw`, `prep_uptake`, `prep_linked_at_risk_pbfw` |
| `hei_events` | 2 | `hei_without_pcr`, `hei_without_final_outcome`, `hei_at_6_to_8_weeks`, `hei_at_24_weeks` |
| `events` | 2 | all other persisted types |
| `reporting_manifest` | 1 | per request site manifest |

Each topic has a dedicated consumer in the worker. All consumers delegate to the same
processing logic, so the `events` consumer still handles any messages left on the legacy topic.

## Build

```bash
# Build everything (from repo root)
mvn clean install

# Build a single module (shared must be installed first)
mvn -pl shared clean install
mvn -pl api clean package
mvn -pl worker clean package
```

## Test

```bash
mvn test                                        # all modules
mvn -pl worker test                             # worker only
mvn -pl worker -Dtest=EventServiceTest test     # single class
```

## Run with Docker Compose

```bash
cd deploy
docker compose up --build
```

## Configuration

Settings are supplied through environment variables (see `deploy/.env` and the module
`application.properties` files). Key variables:

- `KAFKA_BOOTSTRAP_SERVERS`, `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `KEYCLOAK_ISSUER_URL`, `KEYCLOAK_JWT_CERT_URL`
- `CS_REDIS_HOST`, `CS_REDIS_PORT`, `RATE_LIMITING_ENABLED`, `RATE_LIMITING_TTL`
- `SPRING_DOC_SERVER_URL`

### Event thresholds

The worker skips events whose `createdAt` falls before a configurable start date (ISO `yyyy-MM-dd`).
These are bound by `EventThresholdProperties` and overridable per environment:

- `GLOBAL_START_THRESHOLD`
- `PREP_START_THRESHOLD`
- `VL_START_THRESHOLD`
- `HEI_START_THRESHOLD`

## Infrastructure

- **Kafka** (Bitnami 3.4.1, KRaft mode)
- **PostgreSQL** (worker database, Flyway managed)
- **Redis** (API request deduplication)
- **Keycloak** (OAuth2 / JWT auth; EMR vendor read from the `emr` JWT claim)

## Flyway Migrations

Located in `worker/src/main/resources/db/migration/`, named `V{major}.{minor}__{description}.sql`.
