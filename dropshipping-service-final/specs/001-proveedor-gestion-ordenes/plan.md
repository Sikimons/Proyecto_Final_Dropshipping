# Implementation Plan: [US-01] Gestión de Órdenes Dropshipping por Proveedor

**Branch**: `001-proveedor-gestion-ordenes` | **Date**: 2026-07-05
**Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-proveedor-gestion-ordenes/spec.md`

---

## Summary

El proveedor necesita un canal web único para ver sus órdenes Dropshipping asignadas y responder a
ellas (aceptar con fecha estimada de despacho o rechazar con motivo). El sistema debe registrar
cada transición de estado con actor + timestamp y disparar notificaciones inmediatas al analista
(aceptación) o al equipo comercial (rechazo).

Enfoque técnico: API REST generada desde contrato OpenAPI (API First), implementada con
Spring Boot 4.1.0 / Java 21 siguiendo Clean Architecture estricta (4 capas). Las notificaciones
usan Spring Application Events para desacoplar el dominio de la infraestructura. JaCoCo con
umbrales > 80 % por clase y ≥ 80 % global. Tests BDD en tres niveles (JUnit 5 unitario,
Spring Boot Test integración, Cucumber + REST-Assured funcional).

---

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**:
- Spring Boot 4.1.0 (Spring MVC, Spring Data JPA)
- Lombok 1.18.x
- H2 (runtime dev/test) — reemplazable por PostgreSQL en producción sin tocar capas internas
- openapi-generator-gradle-plugin 7.x (generación de interfaces de controlador desde contrato)
- SpringDoc OpenAPI 3 (sirve Swagger UI y expone el contrato en runtime)
- Cucumber 7.x + REST-Assured 5.x (tests funcionales BDD)
- JaCoCo (Gradle plugin, umbrales de cobertura)
- Mockito 5 (incluido en spring-boot-starter-test)
- AssertJ (incluido en spring-boot-starter-test)

**Storage**: H2 (desarrollo/pruebas, en memoria); esquema definido en `src/main/resources/db/schema.sql`
(Spring SQL Init); datos precargados en `src/main/resources/db/data.sql`; reemplazable por
PostgreSQL en producción sin tocar capas internas (solo `application.yaml` + driver).

**Testing**:
- Unit: JUnit 5 + Mockito (uso cases aislados)
- Integration: Spring Boot Test (@SpringBootTest, @DataJpaTest)
- Functional: Cucumber 7 + REST-Assured (escenarios BDD end-to-end)
- Coverage gate: JaCoCo (per-class > 80 %, global ≥ 80 %)

**Target Platform**: Servidor Linux / JVM (REST API web service)

**Project Type**: web-service (REST API)

**Performance Goals**: P95 < 500 ms para operaciones de orden; notificaciones entregadas < 30 s

**Constraints**:
- La persistencia del estado de la orden NUNCA puede fallar silenciosamente por un fallo de
  notificación.
- No hay framework annotations (JPA, Spring) en las capas de Domain ni Application.
- El código generado por openapi-generator NO se modifica manualmente.
- El esquema de BD se define explícitamente en `db/schema.sql`; JPA `ddl-auto` se configura
  en `validate` (no `create`/`create-drop`) para que Hibernate valide contra el schema declarado.
- `spring.sql.init.mode=always` + `spring.jpa.defer-datasource-initialization=true` garantizan
  que `schema.sql` y `data.sql` se ejecuten antes de que Hibernate valide el modelo.

**Scale/Scope**: Proyecto académico / piloto — decenas de proveedores, cientos de órdenes

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Estado | Evidencia / Acción |
|-----------|--------|--------------------|
| **I. Clean Architecture** | ✅ PASS | Paquetes domain → application → adapter → infrastructure. Regla de dependencia hacia adentro. Sin anotaciones JPA/Spring en domain/application. |
| **II. BDD Testing** | ✅ PASS | Tests unitarios (JUnit 5 + Mockito), integración (Spring Boot Test), funcionales (Cucumber + REST-Assured). Naming: `given_*_when_*_then_*`. |
| **III. SOLID / YAGNI / DRY** | ✅ PASS | Un use case por clase. Ports como interfaces. Sin código especulativo. |
| **IV. API First + OpenAPI** | ✅ PASS | `openapi.yml` creado antes de toda implementación. Stubs generados via openapi-generator. Controlador delega a use case. |
| **V. JaCoCo Coverage** | ✅ PASS | Plugin JaCoCo configurado en build.gradle con `violationRules` que fallan el build si per-class < 80 % o global < 80 %. Código generado excluido. |

*Re-check post Phase 1*: Todos los gates se mantienen. La capa de adaptadores (web + persistencia +
notificación) no introduce dependencias hacia afuera del contrato de puertos definidos.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-proveedor-gestion-ordenes/
├── plan.md              # Este archivo
├── research.md          # Decisiones de diseño y alternativas evaluadas
├── data-model.md        # Entidades del dominio y transiciones de estado
├── quickstart.md        # Guía de validación end-to-end
├── contracts/
│   └── openapi.yml      # Contrato OpenAPI 3.0 (fuente de verdad de la API)
├── checklists/
│   └── requirements.md  # Checklist de calidad de especificación
└── tasks.md             # Generado por /speckit-tasks
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/org/ups/dropshippingservicefinal/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── DropshippingOrder.java       # Entidad raíz de dominio (POJO puro)
│   │   │   │   ├── OrderStatus.java             # Enum: PENDING, ACCEPTED, REJECTED
│   │   │   │   ├── OrderStatusEvent.java        # Value object de auditoría
│   │   │   │   └── Address.java                 # Value object de dirección
│   │   │   └── exception/
│   │   │       ├── OrderNotFoundException.java
│   │   │       ├── OrderAlreadyProcessedException.java
│   │   │       └── InvalidDispatchDateException.java
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── GetProviderOrdersUseCase.java
│   │   │   │   │   ├── GetOrderDetailUseCase.java
│   │   │   │   │   ├── AcceptOrderUseCase.java
│   │   │   │   │   └── RejectOrderUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── LoadOrderPort.java
│   │   │   │       ├── SaveOrderPort.java
│   │   │   │       ├── SaveOrderStatusEventPort.java
│   │   │   │       └── SendNotificationPort.java
│   │   │   └── service/
│   │   │       ├── GetProviderOrdersService.java
│   │   │       ├── GetOrderDetailService.java
│   │   │       ├── AcceptOrderService.java
│   │   │       └── RejectOrderService.java
│   │   ├── adapter/
│   │   │   ├── in/
│   │   │   │   └── web/
│   │   │   │       ├── ProviderOrderController.java   # Implementa interfaz generada
│   │   │   │       └── mapper/
│   │   │   │           └── OrderWebMapper.java
│   │   │   └── out/
│   │   │       ├── persistence/
│   │   │       │   ├── DropshippingOrderPersistenceAdapter.java
│   │   │       │   ├── OrderStatusEventPersistenceAdapter.java
│   │   │       │   ├── entity/
│   │   │       │   │   ├── DropshippingOrderJpaEntity.java
│   │   │       │   │   └── OrderStatusEventJpaEntity.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── OrderPersistenceMapper.java
│   │   │       │   └── repository/
│   │   │       │       ├── DropshippingOrderJpaRepository.java
│   │   │       │       └── OrderStatusEventJpaRepository.java
│   │   │       └── notification/
│   │   │           └── InternalNotificationAdapter.java
│   │   └── infrastructure/
│   │       └── config/
│   │           └── ApplicationConfig.java       # Wiring de beans (no en capas internas)
│   └── resources/
│       ├── application.yaml                     # Datasource, JPA, SpringDoc, sql.init config
│       ├── openapi/
│       │   └── openapi.yml                      # Contrato (copia desde specs/.../contracts/)
│       └── db/
│           ├── schema.sql                       # DDL: CREATE TABLE dropshipping_order,
│           │                                    #       CREATE TABLE order_status_event
│           └── data.sql                         # DML seed: proveedor_id=42, 3 órdenes de
│                                                #  prueba (2 PENDING, 1 ACCEPTED + evento)
└── test/
    ├── java/org/ups/dropshippingservicefinal/
    │   ├── domain/
    │   │   └── model/                           # Unit tests: entidades y value objects
    │   ├── application/
    │   │   └── service/                         # Unit tests: use cases con mocks
    │   ├── adapter/
    │   │   ├── in/web/                          # Integration tests: controllers
    │   │   └── out/persistence/                 # Integration tests: @DataJpaTest
    │   └── bdd/
    │       ├── steps/                           # Cucumber step definitions
    │       └── CucumberIntegrationTest.java     # Runner
    └── resources/
        └── features/
            ├── ver_ordenes.feature
            ├── aceptar_orden.feature
            └── rechazar_orden.feature
```

**Structure Decision**: Proyecto single-module Spring Boot con Clean Architecture expresada en
paquetes. Los adaptadores de entrada (web) y salida (persistence, notification) son plugins;
se pueden reemplazar sin tocar domain ni application.

---

## Complexity Tracking

> No hay violaciones a justificar. Todos los principios constitucionales se cumplen sin
> excepciones en el diseño propuesto.
