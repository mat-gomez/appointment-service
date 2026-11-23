<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-54%2F100-yellow) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey) ![Build](https://img.shields.io/badge/Build-tests%20fallan-red) ![Duplicación](https://img.shields.io/badge/Duplicaci%C3%B3n-6.6%25-orange) ![%POO](https://img.shields.io/badge/%25POO-73%25-yellow) ![Sueltas](https://img.shields.io/badge/Sueltas-0-green) ![D1](https://img.shields.io/badge/D1-16-red) ![D2](https://img.shields.io/badge/D2-55-orange) ![D4](https://img.shields.io/badge/D4-40-orange) ![D5](https://img.shields.io/badge/D5-100-green) ![D6](https://img.shields.io/badge/D6-40-orange) ![D7](https://img.shields.io/badge/D7-100-green)

**Calidad (heurístico, 7 dimensiones):** índice **54/100** · build **tests fallan** · aprobar **no (5/11)** · destacar **no**
Detalle completo en [`docs/EVALUACION.md`](docs/EVALUACION.md).
<!-- calidad:fin -->

# appointment-service


# 📅 SERVICIO DE GESTIÓN Y ORQUESTACIÓN DE TURNOS

> **Proyecto Integrador 2026 — Sistema Distribuido de Gestión de Turnos**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache%2FState-red?style=for-the-badge&logo=redis)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Events-black?style=for-the-badge&logo=apachekafka)
![Build Status](https://img.shields.io/badge/Build-SUCCESS-brightgreen?style=for-the-badge)

---

## 🚀 1. Descripción General

El **`appointment-service`** es el microservicio backend encargado de orquestar el ciclo de vida de las reservas de turnos bajo **Arquitectura Hexagonal** (puertos y adaptadores) en **Java 21 y Spring Boot 4.1.1**.

Coordina la creación y confirmación de reservas temporales (*holds*), la captura de datos de contacto del paciente, la consulta del estado e historial propio del usuario, la cancelación o abandono de reservas, y la recuperación automática de procesos vencidos o inconclusos frente a la cátedra.

---

## 🏗️ 2. Arquitectura y Contratos entre Servicios

El módulo `ar.edu.um.appointment.turnos` separa estrictamente el dominio, la aplicación y la infraestructura:

* **Dominio (`domain/model`):** DTOs y eventos de negocio (`ReserveAppointmentRequestDTO`, `ReservationResponseDTO`, `ReservationDetailDTO`, `UserReservationSummaryDTO`, `SubmitPhoneRequestDTO`, `AdditionalInformationSubmittedEvent`, `OccupancyDTO`, `HoldResultDTO`, `ExternalAppointmentDTO`).
* **Aplicación (`application/ports/in`, `application/ports/out`, `application/usecases`):** Casos de uso (`InitiateReservationUseCase`, `SubmitPhoneUseCase`, `QueryAppointmentsUseCase`, `CancelReservationUseCase`) y puertos de salida (`OccupancyPort`, `ReservationHoldPort`, `CancelAppointmentPort`, `AppointmentReconciliationPort`, `ReservationPersistencePort`).
* **Infraestructura (`infrastructure/web`, `infrastructure/client`, `infrastructure/persistence`, `infrastructure/scheduler`):** `AppointmentController`, `CatedraAppointmentsClient`, `ReservationPersistenceAdapter` / `ReservationRepository` y `ExpiredHoldRecoveryScheduler`.

```text
Cliente / App KMP
       │
       ▼
[AppointmentController] ──► [Casos de Uso (ports/in)]
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
[ReservationPersistencePort] [Hold / Confirm / Cancel] [AppointmentReconciliationPort]
        │                        │                        │
        ▼                        ▼                        ▼
  PostgreSQL (Flyway)    API Cátedra (172.17.201.1:8090)  Kafka (:9094) & Redis (:6379)

```

### Endpoints REST expuestos (`/api/appointments`)

| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| `POST` | `/api/appointments/reserve` | Inicia el proceso de reserva, crea el *hold* en la cátedra y persiste el estado pendiente del usuario. | `201 Created` (`ReservationResponseDTO`) |
| `POST` | `/api/appointments/{reservationProcessId}/phone` | Envía el teléfono del paciente para continuar el flujo de reserva. | `202 Accepted` (`AdditionalInformationSubmittedEvent`) |
| `GET` | `/api/appointments/my` | Devuelve el listado de reservas y procesos propios del usuario autenticado. | `200 OK` (`List<UserReservationSummaryDTO>`) |
| `GET` | `/api/appointments/{reservationProcessId}` | Consulta el estado y detalle de un proceso propio (`403` si no pertenece, `404` si no existe). | `200 OK` (`ReservationDetailDTO`) |
| `POST` | `/api/appointments/{reservationProcessId}/cancel` | Cancela una reserva confirmada propia y propaga la cancelación a la cátedra. | `200 OK` (`ReservationDetailDTO`) |
| `POST` | `/api/appointments/{reservationProcessId}/abandon` | Abandona un proceso pendiente propio (`HOLD_CREATED`, `WAITING_FOR_PHONE`, `PHONE_REJECTED`) sin invocar la cancelación de turno confirmado. | `200 OK` (`ReservationDetailDTO`) |

### Contratos externos (`catalog-service`, Cátedra, Kafka y Redis)

* **`catalog-service`:** Referenciado mediante `CATALOG_SERVICE_URL` para consulta de categorías, profesionales y disponibilidad del catálogo.
* **API REST de la Cátedra (`CATEDRA_API_URL`, por defecto `http://172.17.201.1:8090`):**
* `GET /api/appointment-occupancies` — consulta de ocupación por profesional y rango de fechas.
* `POST /api/appointment-holds` y `POST /api/appointment-holds/{holdId}/confirm` — creación y confirmación de *holds*.
* `DELETE /api/appointments/{id}/cancel` — cancelación de turnos confirmados propagando el JWT del usuario (o el token técnico como respaldo).
* `GET /api/appointments` — consulta de turnos externos utilizada por el *scheduler* de reconciliación.


* **Kafka (`KAFKA_SERVERS` / `CATEDRA_KAFKA_SERVERS`, por defecto `172.17.201.1:9094`):** Consumo y publicación de eventos del flujo de turnos (`CATEDRA_APPOINTMENT_ACTIONS_TOPIC`, `CATEDRA_APPOINTMENT_PHONE_TOPIC`) bajo `KAFKA_GROUP_ID` / `CATEDRA_KAFKA_GROUP`.
* **Redis (`REDIS_HOST` / `CATEDRA_REDIS_HOST`, `172.17.201.1:6379`):** Configuración de caché/estado transitorio mediante `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME` y `REDIS_PASSWORD`.

---

## 🔄 3. Máquina de Estados del Proceso de Reserva

```text
[HOLD_CREATED] ──► [WAITING_FOR_PHONE] ──► [CONFIRMED] ──► [CANCELLED]
       │                    │                   ▲
       │                    ├──► [PHONE_REJECTED] ┘
       │                    └──► [EXPIRED] (expiresAt vencido o scheduler)
       └──► [CANCELLED] (abandono explícito en estado pendiente)
       └──► [INVALID] (reconciliación / estado inválido)

```

* **Procesos pendientes abandonables (`CU-10`):** `HOLD_CREATED`, `WAITING_FOR_PHONE` y `PHONE_REJECTED`.
* **Cancelación de reserva confirmada (`CU-08`):** Aplica sobre `CONFIRMED`; si ya está en `CANCELLED`, responde de forma idempotente sin volver a llamar a la cátedra.

---

## 🗄️ 4. Modelo de Datos (PostgreSQL + Flyway)

Definido en `src/main/resources/db/migration/V1__init_schema.sql`:

* **`app_user`:** Usuarios del sistema (`id`, `login`, `password_hash`, `first_name`, `last_name`, `email`, `image_url`, `lang_key`, `activated`, `created_at`, `updated_at`).
* **`appointment_reservation`:** Entidad principal de procesos y reservas (`id`, `reservation_process_id` único, `hold_id`, `external_patient_id`, `professional_id`, `professional_first_name`, `professional_last_name`, `appointment_date`, `start_time`, `end_time`, `status`, `expires_at`, `request_event_id`, `reservation_id`, `patient_phone`, `cancellation_reason`, `created_at`, `confirmed_at`, `cancelled_at`).
* **`processed_kafka_event`:** Tabla de deduplicación de eventos Kafka (`event_id` PK, `processed_at`).

---

## 🛡️ 5. Idempotencia, Seguridad y Recuperación

* **Aislamiento y validación de estados:** Se verifica autenticación (`401`), titularidad del recurso (`403`), existencia (`404`) y validez de la transición (`409`).
* **Idempotencia:** Cancelar o abandonar un proceso ya `CANCELLED` es idempotente; los eventos de Kafka se registran en `processed_kafka_event` por `event_id`.
* **Recuperación y reconciliación (`ExpiredHoldRecoveryScheduler`):** Se ejecuta al iniciar (`ApplicationReadyEvent`) y periódicamente (`@Scheduled(fixedDelayString = "${app.recovery.fixed-delay-ms:60000}")`), cerrando como `EXPIRED` los procesos cuyo `expiresAt` venció y reconciliando estados inciertos contra `GET /api/appointments` de la cátedra.

---

## ⚙️ 6. Variables de Entorno

| Variable de Entorno | Descripción | Valor por defecto |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | Conexión a PostgreSQL | `localhost` / `5434` / `appointment_db` (`5432` en compose) |
| `DB_USER` / `DB_PASSWORD` | Credenciales de PostgreSQL | `appointment_user` / `appointment_pass` |
| `CATALOG_SERVICE_URL` | URL base de `catalog-service` | `http://localhost:8081` |
| `CATEDRA_API_URL` | URL base de la API REST de la cátedra | `http://172.17.201.1:8090` |
| `CATEDRA_JWT_TOKEN` | Token JWT técnico para llamadas hacia la cátedra | *(configurar en `.env`)* |
| `CATEDRA_KAFKA_SERVERS` / `KAFKA_SERVERS` | Brokers Kafka de la cátedra | `172.17.201.1:9094` |
| `CATEDRA_KAFKA_GROUP` / `KAFKA_GROUP_ID` | Consumer group de Kafka | `alumnos-proyecto-turnos` |
| `CATEDRA_REDIS_HOST` / `REDIS_HOST` | Host de Redis | `172.17.201.1` |
| `CATEDRA_REDIS_PORT` / `REDIS_PORT` | Puerto de Redis | `6379` |
| `CATEDRA_REDIS_USER` / `CATEDRA_REDIS_PASSWORD` | Credenciales de Redis | *(vacío por defecto)* |

---

## 🐳 7. Ejecución Local y con Docker Compose

### Preparar entorno

```bash
cp .env.example .env

```

### Pruebas y empaquetado con Maven

```bash
./mvnw clean verify

```

### Levantar el entorno completo con Docker Compose

```bash
docker compose up --build

```