# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build all modules (Maven multi-module)
./install.sh                      # builds jars + docker compose build & up

# Build individual module
cd api && mvn clean install -DskipTests
cd worker && mvn clean install -DskipTests
cd shared && mvn clean install -DskipTests

# Run tests
mvn test                          # all modules from root
cd worker && mvn test             # single module

# Stop containers
./stop.sh
```

## Architecture

Multi-module Spring Boot 3.1.3 (Java 21) project with event-driven architecture for Kenya HMIS case surveillance.

**Three modules:**
- **api** — REST gateway. Receives events at `PUT /api/event/sync`, validates, publishes to Kafka. Runs on port 8088. Secured with OAuth2/JWT (Keycloak).
- **worker** — Kafka consumer. Listens on `events` topic, maps DTOs to JPA entities, persists to PostgreSQL. No HTTP endpoints.
- **shared** — Common DTOs, custom validators, constants, utilities. Used by both api and worker.

**Infrastructure:** PostgreSQL + Kafka (KRaft mode, Bitnami 3.4.1) + Redis 7 (rate limiting). Orchestrated via `deploy/docker-compose.yml`.

**Request flow:**
```
EMR → PUT /api/event/sync (JWT auth) → Kafka "events" topic → Worker @KafkaListener → PostgreSQL
```

## Key Patterns

**Event type dispatch:** `EventService.processEvent()` uses a switch on `eventType` string to route to the correct handler. Each event type has a DTO (in shared), a JPA entity and repository (in worker), and a MapStruct mapper.

**Supported event types:** `new_case`, `linked_case`, `at_risk_pbfw`, `prep_linked_at_risk_pbfw`, `prep_uptake`, `mortality`, `eligible_for_vl`, `unsuppressed_viral_load`, `hei_without_pcr`, `hei_without_final_outcome`, `hei_at_6_to_8_weeks`, `hei_at_24_weeks`, `roll_call`

**Adding a new event type requires:**
1. DTO in `shared/src/.../dto/` (use Java record pattern, see `MortalityDto.java`)
2. Custom validators in `shared/src/.../validator/` if needed
3. Flyway migration in `worker/src/main/resources/db/migration/` (next version number)
4. JPA entity in `worker/src/.../model/`
5. Repository in `worker/src/.../repository/`
6. MapStruct mapper update in `worker/src/.../mapper/EventMapper.java`
7. Switch case in `EventService.processEvent()`
8. Add event type string to `ValidEventTypeValidator`

**DTO wrapping:** All events use `EventBase<T>` wrapper → batched in `EventList<EventBase<?>>`. Kafka messages use `EventBaseMessage`.

**Validation:** Jakarta JSR-380 annotations on DTOs + custom validators (`@ValidEventType`, `@ValidDate`, `@ValidGender`, `@ValidPrepStatus`, etc.). Global exception handler via `@ControllerAdvice` in `RequestExceptionHandler`.

**Upsert pattern:** Worker deduplicates by `patientPk + mflCode`, updating existing records rather than inserting duplicates.

**Date threshold:** Events before `2025-06-01` (`PROGRAM_START_THRESHOLD` in `GlobalConstants`) are filtered out.

**Rate limiting:** Redis checksum-based deduplication, controlled by `RATE_LIMITING_ENABLED` env var.

## Database Migrations

Flyway migrations live in `worker/src/main/resources/db/migration/`. Current latest: `V2.2__add_mortality_table.sql`. Follow the `V{major}.{minor}__description.sql` naming convention.

## Code Generation

- **Lombok** (`@Data`, `@NoArgsConstructor`, etc.) for boilerplate reduction
- **MapStruct** (`@Mapper(componentModel = "spring")`) for DTO↔Entity mapping
- Both configured as annotation processors in maven-compiler-plugin

## Environment Variables

Key variables (see `deploy/.env`): `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `KEYCLOAK_ISSUER_URL`, `KEYCLOAK_JWT_CERT_URL`, `CS_REDIS_HOST`, `CS_REDIS_PORT`, `RATE_LIMITING_ENABLED`, `RATE_LIMITING_TTL`.
