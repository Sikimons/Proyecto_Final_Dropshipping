---
description: "Task list for US-01 Gestión de Órdenes Dropshipping por Proveedor"
---

# Tasks: [US-01] Gestión de Órdenes Dropshipping por Proveedor

**Input**: Design documents from `specs/001-proveedor-gestion-ordenes/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/openapi.yml ✅

**Base package**: `org.ups.dropshippingservicefinal` → abbreviated below as `o.u.d`

**Corrections applied** (from /speckit-analyze):
- C1: Added domain entity unit tests (DropshippingOrder, OrderStatusEvent, Address)
- C2: Added ArchUnit dependency + CleanArchitectureTest task
- H1: GlobalExceptionHandler moved to Phase 2 Foundation (before story phases)
- H2: Added Checkstyle + SpotBugs to T001; added checkstyle.xml task
- H4: Added InvalidRejectionReasonException; updated RejectOrderService and GlobalExceptionHandler
- M2: JaCoCo XML report explicitly enabled in T003
- M3: Security scheme added to openapi.yml (separate file update)
- M5: CucumberSpringConfiguration task added
- L1: Fixed T007/T008 parallel markers (schema before data)
- L2: .gitignore update moved to Phase 1 Setup

**Tests**: Incluidos — la Constitución (Principio II) exige BDD en tres niveles. Test-first
obligatorio: cada test se escribe y está en ROJO antes de implementar.

## Format: `[ID] [P?] [Story?] Description — file path`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias incompletas)
- **[Story]**: US1/US2/US3 mapea a la historia de usuario en spec.md

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Configuración del proyecto — build tool, plugins, contrato, YAML, .gitignore.

- [X] T001 Update `build.gradle` adding: openapi-generator-gradle-plugin 7.x, springdoc-openapi-starter-webmvc-ui, cucumber-java + cucumber-spring 7.x, rest-assured 5.x, jacoco plugin, net.sourceforge.pmd.pmd-core, com.github.spotbugs plugin 6.x, checkstyle plugin (built-in Gradle), com.tngtech.archunit:archunit-junit5 1.x (testImplementation); keep existing spring-boot-starter-data-jpa, spring-boot-starter-webmvc, lombok, h2
- [X] T002 Configure `openapi-generator` Gradle task in `build.gradle`: inputSpec = `src/main/resources/openapi/openapi.yml`, generatorName = `spring`, library = `spring-boot`, delegatePattern = `true`, apiPackage = `o.u.d.adapter.in.web.generated`, modelPackage = `o.u.d.adapter.in.web.generated.model`, outputDir = `$buildDir/generated/openapi`; add generated sources dir to compileJava sourceSet
- [X] T003 [P] Configure JaCoCo in `build.gradle`: `jacocoTestReport` task with `reports { xml.required = true; html.required = true }`; `jacocoTestCoverageVerification` task with per-class line coverage minimum 0.80 and global coverage minimum 0.80; excludes: `**/generated/**`, `**/*Application*`, `**/*JpaEntity*`, `**/*JpaRepository*`, `**/*Config*`
- [X] T004 [P] Copy OpenAPI contract to `src/main/resources/openapi/openapi.yml` (source: `specs/001-proveedor-gestion-ordenes/contracts/openapi.yml`)
- [X] T005 Update `src/main/resources/application.yaml`: H2 datasource url `jdbc:h2:mem:dropshippingdb;DB_CLOSE_DELAY=-1;MODE=MySQL`, JPA `hibernate.ddl-auto: validate`, `show-sql: false`, `spring.sql.init.mode: always`, `spring.sql.init.schema-locations: classpath:db/schema.sql`, `spring.sql.init.data-locations: classpath:db/data.sql`, `spring.jpa.defer-datasource-initialization: true`, SpringDoc `api-docs.path: /api-docs`, `swagger-ui.path: /swagger-ui.html`
- [X] T006 [P] Add `build/generated/` and `.gradle/` to `.gitignore` — `/.gitignore` (so generated openapi-generator code is never committed; do this before any generation task runs)

**Checkpoint**: `./gradlew compileJava` succeeds; generated interfaces appear in `build/generated/openapi/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Esquema de BD, seed data, entidades de dominio, puertos, JPA entities, exception
handler global, ArchUnit y wiring. NINGUNA historia puede comenzar hasta completar esta fase.

**⚠️ CRÍTICO**: Respetar orden interno de dependencias antes de paralelizar.

- [X] T007 Create `src/main/resources/db/schema.sql` with DDL: `CREATE TABLE IF NOT EXISTS dropshipping_order` (id BIGINT AUTO_INCREMENT PK, order_code VARCHAR(50) UNIQUE NOT NULL, provider_id BIGINT NOT NULL, product_code VARCHAR(100) NOT NULL, product_description VARCHAR(500) NOT NULL, quantity INT NOT NULL CHECK(quantity > 0), street VARCHAR(255) NOT NULL, city VARCHAR(100) NOT NULL, state VARCHAR(100) NOT NULL, postal_code VARCHAR(20), country VARCHAR(100) NOT NULL, customer_name VARCHAR(200) NOT NULL, customer_contact VARCHAR(200) NOT NULL, expected_delivery_date DATE NOT NULL, special_conditions TEXT, status VARCHAR(20) NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL); `CREATE TABLE IF NOT EXISTS order_status_event` (id BIGINT AUTO_INCREMENT PK, order_id BIGINT NOT NULL FK → dropshipping_order.id, previous_status VARCHAR(20) NOT NULL, new_status VARCHAR(20) NOT NULL, actor_id VARCHAR(100) NOT NULL, timestamp TIMESTAMP NOT NULL, estimated_dispatch_date DATE, rejection_reason TEXT)
- [X] T008 Create `src/main/resources/db/data.sql` with seed DML (depends on T007 schema): INSERT 3 orders for provider_id=42 — ORD-2026-TEST-01 status=PENDING; ORD-2026-TEST-02 status=PENDING; ORD-2026-TEST-03 status=ACCEPTED with full address, customerName, customerContact, expectedDeliveryDate, specialConditions; INSERT 1 order_status_event for ORD-2026-TEST-03 (actor_id='42', previous_status='PENDING', new_status='ACCEPTED', estimated_dispatch_date='2026-07-08')
- [X] T009 [P] Create `OrderStatus` enum — `src/main/java/o/u/d/domain/model/OrderStatus.java` (PENDING, ACCEPTED, REJECTED; no framework imports)
- [X] T010 [P] Create `Address` value object — `src/main/java/o/u/d/domain/model/Address.java` (fields: street, city, state, postalCode, country; all-args constructor; getters; equals/hashCode; no Lombok, no JPA annotations)
- [X] T011 Create `DropshippingOrder` domain entity — `src/main/java/o/u/d/domain/model/DropshippingOrder.java` (all fields from data-model.md; constructor validates quantity > 0; method `canTransitionTo(OrderStatus newStatus)` returns true only PENDING→ACCEPTED or PENDING→REJECTED, false otherwise; no framework annotations)
- [X] T012 Create `OrderStatusEvent` domain entity — `src/main/java/o/u/d/domain/model/OrderStatusEvent.java` (immutable; all-args constructor validates: estimatedDispatchDate non-null when newStatus=ACCEPTED; rejectionReason non-blank when newStatus=REJECTED; no framework annotations)
- [X] T013 [P] Create domain exceptions — `src/main/java/o/u/d/domain/exception/`: `OrderNotFoundException.java`, `OrderAlreadyProcessedException.java`, `InvalidDispatchDateException.java`, `InvalidRejectionReasonException.java` (all extend RuntimeException with descriptive message constructor; InvalidRejectionReasonException used when rejection reason is null/blank)
- [X] T014 [P] Create input port interfaces — `src/main/java/o/u/d/application/port/in/`: `GetProviderOrdersUseCase.java`, `GetOrderDetailUseCase.java`, `AcceptOrderUseCase.java`, `RejectOrderUseCase.java` (exact signatures from data-model.md; no framework imports)
- [X] T015 [P] Create output port interfaces — `src/main/java/o/u/d/application/port/out/`: `LoadOrderPort.java`, `SaveOrderPort.java`, `SaveOrderStatusEventPort.java`, `SendNotificationPort.java` (exact signatures from data-model.md)
- [X] T016 [P] Create `DropshippingOrderJpaEntity` — `src/main/java/o/u/d/adapter/out/persistence/entity/DropshippingOrderJpaEntity.java` (@Entity @Table("dropshipping_order"); all columns matching schema.sql; @Enumerated(EnumType.STRING) for status; Lombok @Data @NoArgsConstructor @AllArgsConstructor @Builder)
- [X] T017 [P] Create `OrderStatusEventJpaEntity` — `src/main/java/o/u/d/adapter/out/persistence/entity/OrderStatusEventJpaEntity.java` (@Entity @Table("order_status_event"); @ManyToOne to DropshippingOrderJpaEntity via order_id; @Enumerated(EnumType.STRING) for status fields; Lombok @Data @NoArgsConstructor @AllArgsConstructor @Builder)
- [X] T018 [P] Create `DropshippingOrderJpaRepository` — `src/main/java/o/u/d/adapter/out/persistence/repository/DropshippingOrderJpaRepository.java` (extends JpaRepository<DropshippingOrderJpaEntity, Long>; methods: `findByProviderId(Long)`, `findByProviderIdAndStatus(Long, String)`)
- [X] T019 [P] Create `OrderStatusEventJpaRepository` — `src/main/java/o/u/d/adapter/out/persistence/repository/OrderStatusEventJpaRepository.java` (extends JpaRepository<OrderStatusEventJpaEntity, Long>; method: `findByOrderIdOrderByTimestampAsc(Long)`)
- [X] T020 Create `ApplicationConfig` — `src/main/java/o/u/d/infrastructure/config/ApplicationConfig.java` (@Configuration; @Bean for GetProviderOrdersService, GetOrderDetailService, AcceptOrderService, RejectOrderService, each injecting the appropriate output-port implementations)
- [X] T021 [P] Write unit test `DropshippingOrderTest` — `src/test/java/o/u/d/domain/model/DropshippingOrderTest.java` (methods: `given_validFields_when_construct_then_entityCreated`; `given_zeroQuantity_when_construct_then_throwsException`; `given_pendingOrder_when_canTransitionToAccepted_then_returnsTrue`; `given_pendingOrder_when_canTransitionToRejected_then_returnsTrue`; `given_acceptedOrder_when_canTransitionToAccepted_then_returnsFalse`; `given_rejectedOrder_when_canTransitionToAccepted_then_returnsFalse`) ← C1 fix
- [X] T022 [P] Write unit test `OrderStatusEventTest` — `src/test/java/o/u/d/domain/model/OrderStatusEventTest.java` (methods: `given_acceptedStatusWithDate_when_construct_then_eventCreated`; `given_acceptedStatusWithNullDate_when_construct_then_throwsException`; `given_rejectedStatusWithReason_when_construct_then_eventCreated`; `given_rejectedStatusWithBlankReason_when_construct_then_throwsException`) ← C1 fix
- [X] T023 [P] Write unit test `AddressTest` — `src/test/java/o/u/d/domain/model/AddressTest.java` (methods: `given_allFields_when_construct_then_addressCreated`; `given_twoAddressesWithSameFields_when_equals_then_areEqual`; verify equals/hashCode contract; postalCode nullable) ← C1 fix
- [X] T024 Create `GlobalExceptionHandler` — `src/main/java/o/u/d/adapter/in/web/GlobalExceptionHandler.java` (@RestControllerAdvice; @ExceptionHandler: OrderNotFoundException→404, OrderAlreadyProcessedException→409, InvalidDispatchDateException→400, InvalidRejectionReasonException→400, MethodArgumentNotValidException→400 with field errors summary, Exception→500; each returns `ErrorResponse` DTO with timestamp/status/error/message/path) ← H1 + H4 fix
- [X] T025 [P] Write unit test `GlobalExceptionHandlerTest` — `src/test/java/o/u/d/adapter/in/web/GlobalExceptionHandlerTest.java` (given_orderNotFoundException_then_404; given_orderAlreadyProcessedException_then_409; given_invalidDispatchDateException_then_400; given_invalidRejectionReasonException_then_400; given_genericException_then_500; each verifies ErrorResponse structure) ← H1 + H4 fix
- [X] T026 Write `CleanArchitectureTest` — `src/test/java/o/u/d/CleanArchitectureTest.java` (using ArchUnit @AnalyzeClasses; rules: `noClasses().that().resideInAPackage("..domain..")  .should().dependOnClassesThat().resideInAPackage("..adapter..")`, same for application→adapter, domain→application; verify no `jakarta.persistence.*` or `org.springframework.*` imports in domain or application packages) ← C2 fix
- [X] T027 Create `config/checkstyle/checkstyle.xml` — `config/checkstyle/checkstyle.xml` (Google Java Style Guide ruleset; configure Gradle `checkstyle { toolVersion = '10.x'; configFile = file("config/checkstyle/checkstyle.xml") }`; add `checkstyleMain` and `checkstyleTest` tasks to `check` lifecycle in build.gradle) ← H2 fix

**Checkpoint**: `./gradlew check` passes (compileJava + Checkstyle + ArchUnit + JaCoCo compile-time checks). H2 console shows 3 seed orders. GlobalExceptionHandler wired and tested before any story begins.

---

## Phase 3: User Story 1 — Ver órdenes asignadas (Priority: P1) 🎯 MVP

**Goal**: El proveedor puede ver su listado completo de órdenes con todos los campos requeridos
(código producto, descripción, cantidad, dirección completa, contacto cliente, fecha esperada,
condiciones especiales) y el detalle de cada orden.

**Independent Test**: `./gradlew test --tests "*.bdd.*"` ejecuta `ver_ordenes.feature` con 3 scenarios en verde; `GET /api/v1/providers/42/orders` retorna las órdenes seed con todos los campos del spec.md.

### Tests para User Story 1 ⚠️ escribir PRIMERO — confirmar ROJO antes de implementar

- [X] T028 [P] [US1] Write unit test `GetProviderOrdersServiceTest` — `src/test/java/o/u/d/application/service/GetProviderOrdersServiceTest.java` (given_proveedorConOrdenes_when_getOrders_then_retornaListaCompleta; given_proveedorSinOrdenes_when_getOrders_then_retornaListaVacia; given_filtroEstado_when_getOrders_then_retornaFiltrado; mock LoadOrderPort with Mockito)
- [X] T029 [P] [US1] Write unit test `GetOrderDetailServiceTest` — `src/test/java/o/u/d/application/service/GetOrderDetailServiceTest.java` (given_ordenExistente_when_getOrder_then_retornaDetalle; given_ordenInexistente_when_getOrder_then_lanzaOrderNotFoundException; given_ordenDeOtroProveedor_when_getOrder_then_lanzaOrderNotFoundException)
- [X] T030 [P] [US1] Write `@DataJpaTest` integration test `DropshippingOrderPersistenceAdapterTest` — `src/test/java/o/u/d/adapter/out/persistence/DropshippingOrderPersistenceAdapterTest.java` (annotated @Sql(scripts={"classpath:db/schema.sql","classpath:db/data.sql"}); verifies findByProviderId returns all, findByProviderIdAndStatus filters, loadOrder returns empty for unknown id)
- [X] T031 [P] [US1] Write Cucumber feature file `ver_ordenes.feature` — `src/test/resources/features/ver_ordenes.feature` (3 scenarios: "El proveedor ve sus órdenes con todos los campos", "Proveedor sin órdenes ve mensaje informativo", "Proveedor filtra órdenes por estado PENDING"; Given/When/Then in Spanish matching spec.md acceptance criteria)

### Implementation for User Story 1

- [X] T032 [US1] Implement `GetProviderOrdersService` — `src/main/java/o/u/d/application/service/GetProviderOrdersService.java` (implements GetProviderOrdersUseCase; delegates to LoadOrderPort.loadOrdersByProvider; no Spring annotations in class body; wired via ApplicationConfig)
- [X] T033 [US1] Implement `GetOrderDetailService` — `src/main/java/o/u/d/application/service/GetOrderDetailService.java` (implements GetOrderDetailUseCase; load order; validate order.providerId == providerId; throw OrderNotFoundException if not found or mismatched provider)
- [X] T034 [US1] Implement `OrderPersistenceMapper` — `src/main/java/o/u/d/adapter/out/persistence/mapper/OrderPersistenceMapper.java` (static methods: toDomain(DropshippingOrderJpaEntity)→DropshippingOrder including Address; toJpaEntity(DropshippingOrder)→DropshippingOrderJpaEntity; toEventDomain(OrderStatusEventJpaEntity)→OrderStatusEvent; toEventJpaEntity(OrderStatusEvent)→OrderStatusEventJpaEntity)
- [X] T035 [US1] Implement `DropshippingOrderPersistenceAdapter` — `src/main/java/o/u/d/adapter/out/persistence/DropshippingOrderPersistenceAdapter.java` (@Component; implements LoadOrderPort + SaveOrderPort; uses DropshippingOrderJpaRepository + OrderPersistenceMapper; loadOrdersByProvider delegates to findByProviderId or findByProviderIdAndStatus depending on null filter)
- [X] T036 [US1] Create `OrderWebMapper` — `src/main/java/o/u/d/adapter/in/web/mapper/OrderWebMapper.java` (@Component; toSummary(DropshippingOrder)→DropshippingOrderSummary DTO; toDetail(DropshippingOrder, List<OrderStatusEvent>)→DropshippingOrderDetail DTO; map all fields including statusHistory)
- [X] T037 [US1] Implement `ProviderOrderController` — `src/main/java/o/u/d/adapter/in/web/ProviderOrderController.java` (@RestController; implements generated API interface; inject GetProviderOrdersUseCase, GetOrderDetailUseCase, OrderWebMapper; implement getProviderOrders and getOrderDetail; exceptions propagate to GlobalExceptionHandler)
- [X] T038 [US1] Write Cucumber step definitions `VerOrdenesSteps` — `src/test/java/o/u/d/bdd/steps/VerOrdenesSteps.java` (REST-Assured steps for ver_ordenes.feature; assert HTTP 200 and response contains productCode, productDescription, quantity, deliveryAddress with all sub-fields, customerName, customerContact, expectedDeliveryDate)
- [X] T039 [US1] Create `CucumberSpringConfiguration` + `CucumberIntegrationTest` — `src/test/java/o/u/d/bdd/CucumberSpringConfiguration.java` (@CucumberContextConfiguration @SpringBootTest(webEnvironment=RANDOM_PORT); provides Spring context to all step definitions) and `CucumberIntegrationTest.java` (@Suite @SelectClasspathResource("features") @ConfigurationParameter(PLUGIN_PROPERTY_NAME, "html:build/reports/cucumber/index.html,json:build/reports/cucumber/report.json")) ← M5 fix

**Checkpoint**: `./gradlew test jacocoTestReport` — US1 verde; Swagger UI en `/swagger-ui.html`; JaCoCo report generado con XML. User Story 1 funcional independientemente.

---

## Phase 4: User Story 2 — Aceptar orden con fecha estimada (Priority: P2)

**Goal**: El proveedor acepta una orden PENDING con fecha estimada de despacho futura. Estado cambia a ACCEPTED con actor + timestamp en `order_status_event`. Analista notificado.

**Independent Test**: `POST /api/v1/providers/42/orders/1/accept` con `{"estimatedDispatchDate":"2099-01-01"}` → HTTP 200 + `newStatus: ACCEPTED`; repetida → HTTP 409; fecha pasada → HTTP 400.

### Tests para User Story 2 ⚠️ escribir PRIMERO — confirmar ROJO antes de implementar

- [X] T040 [P] [US2] Write unit test `AcceptOrderServiceTest` — `src/test/java/o/u/d/application/service/AcceptOrderServiceTest.java` (given_ordenPendiente_when_accept_then_cambiaAAccepted; given_ordenYaAceptada_when_accept_then_lanzaOrderAlreadyProcessedException; given_fechaEnPasado_when_accept_then_lanzaInvalidDispatchDateException; given_ordenPendiente_when_accept_then_llamaNotifyOrderAccepted; mock all ports)
- [X] T041 [P] [US2] Write `@SpringBootTest(webEnvironment=RANDOM_PORT)` integration test `AcceptOrderControllerTest` — `src/test/java/o/u/d/adapter/in/web/AcceptOrderControllerTest.java` using REST-Assured: given_ordenPendiente_when_postAccept_then_http200; given_fechaPasada_when_postAccept_then_http400; given_ordenYaProcesada_when_postAccept_then_http409 ← M6 fix: standardized to REST-Assured + RANDOM_PORT
- [X] T042 [P] [US2] Write Cucumber feature file `aceptar_orden.feature` — `src/test/resources/features/aceptar_orden.feature` (3 scenarios from spec.md: aceptación exitosa con fecha futura, fecha pasada rechazada → 400, orden ya procesada → 409; Given/When/Then in Spanish)

### Implementation for User Story 2

- [X] T043 [US2] Implement `AcceptOrderService` — `src/main/java/o/u/d/application/service/AcceptOrderService.java` (implements AcceptOrderUseCase; load order via LoadOrderPort; throw OrderNotFoundException if absent; throw OrderAlreadyProcessedException if !canTransitionTo(ACCEPTED); throw InvalidDispatchDateException if date is not after LocalDate.now(); build OrderStatusEvent; save updated order; save event; notify via SendNotificationPort.notifyOrderAccepted)
- [X] T044 [US2] Implement `OrderStatusEventPersistenceAdapter` — `src/main/java/o/u/d/adapter/out/persistence/OrderStatusEventPersistenceAdapter.java` (@Component; implements SaveOrderStatusEventPort; converts OrderStatusEvent to JpaEntity via OrderPersistenceMapper; saves and returns domain entity)
- [X] T045 [US2] Implement `InternalNotificationAdapter.notifyOrderAccepted` — `src/main/java/o/u/d/adapter/out/notification/InternalNotificationAdapter.java` (@Component; implements SendNotificationPort; notifyOrderAccepted logs at INFO level with orderCode, actorId, estimatedDispatchDate, recipient=ANALYST)
- [X] T046 [US2] Add `acceptOrder` to `ProviderOrderController` — `src/main/java/o/u/d/adapter/in/web/ProviderOrderController.java` (inject AcceptOrderUseCase; implement generated acceptOrder method; delegate to use case; map OrderStatusEvent to OrderActionResponse DTO; exceptions handled by GlobalExceptionHandler already in place)
- [X] T047 [US2] Write step definitions `AceptarOrdenSteps` — `src/test/java/o/u/d/bdd/steps/AceptarOrdenSteps.java` (REST-Assured steps for aceptar_orden.feature; assert HTTP 200 + newStatus=ACCEPTED; HTTP 400 for past date; HTTP 409 for already-processed)

**Checkpoint**: `./gradlew test` — US1 y US2 en verde; `order_status_event` persiste el evento de aceptación; H1 + H4 fixes confirm HTTP 409 and 400 are correctly returned by GlobalExceptionHandler.

---

## Phase 5: User Story 3 — Rechazar orden con motivo (Priority: P3)

**Goal**: El proveedor rechaza una orden PENDING con motivo obligatorio. Estado cambia a REJECTED con motivo + actor + timestamp. Equipo comercial alertado.

**Independent Test**: `POST /api/v1/providers/42/orders/2/reject` con `{"reason":"Sin stock."}` → HTTP 200 + `newStatus: REJECTED`; sin motivo → HTTP 400; orden ya procesada → HTTP 409.

### Tests para User Story 3 ⚠️ escribir PRIMERO — confirmar ROJO antes de implementar

- [X] T048 [P] [US3] Write unit test `RejectOrderServiceTest` — `src/test/java/o/u/d/application/service/RejectOrderServiceTest.java` (given_ordenPendiente_when_reject_then_cambiaARejected; given_ordenYaRechazada_when_reject_then_lanzaOrderAlreadyProcessedException; given_motivoVacio_when_reject_then_lanzaInvalidRejectionReasonException; given_ordenPendiente_when_reject_then_llamaNotifyOrderRejected; mock all ports) ← H4 fix
- [X] T049 [P] [US3] Write `@SpringBootTest(webEnvironment=RANDOM_PORT)` integration test `RejectOrderControllerTest` — `src/test/java/o/u/d/adapter/in/web/RejectOrderControllerTest.java` using REST-Assured: given_ordenPendiente_when_postReject_then_http200; given_motivoVacio_when_postReject_then_http400; given_ordenYaProcesada_when_postReject_then_http409
- [X] T050 [P] [US3] Write Cucumber feature file `rechazar_orden.feature` — `src/test/resources/features/rechazar_orden.feature` (3 scenarios: rechazo exitoso, motivo vacío → 400, orden ya procesada → 409; Given/When/Then in Spanish)

### Implementation for User Story 3

- [X] T051 [US3] Implement `RejectOrderService` — `src/main/java/o/u/d/application/service/RejectOrderService.java` (implements RejectOrderUseCase; load order; throw OrderNotFoundException if absent; throw OrderAlreadyProcessedException if !canTransitionTo(REJECTED); throw **InvalidRejectionReasonException** (not IllegalArgumentException) if reason is null or blank; build OrderStatusEvent with rejectionReason; save order + event; notify via SendNotificationPort.notifyOrderRejected) ← H4 fix
- [X] T052 [US3] Add `notifyOrderRejected` to `InternalNotificationAdapter` — `src/main/java/o/u/d/adapter/out/notification/InternalNotificationAdapter.java` (WARN level log with orderCode, actorId, rejectionReason, recipient=COMMERCIAL_TEAM; include marker "ALERT_COMMERCIAL_TEAM" for production log grep)
- [X] T053 [US3] Add `rejectOrder` to `ProviderOrderController` — `src/main/java/o/u/d/adapter/in/web/ProviderOrderController.java` (inject RejectOrderUseCase; implement generated rejectOrder method; delegate; map response to OrderActionResponse DTO)
- [X] T054 [US3] Write step definitions `RechazarOrdenSteps` — `src/test/java/o/u/d/bdd/steps/RechazarOrdenSteps.java` (REST-Assured steps for rechazar_orden.feature; assert HTTP 200 + newStatus=REJECTED + rejectionReason in body; HTTP 400 for blank reason; HTTP 409 for already-processed)

**Checkpoint**: `./gradlew test` — las 3 historias en verde; `InvalidRejectionReasonException` mapea a HTTP 400 vía GlobalExceptionHandler; ArchUnit test confirms no dependency violations.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Tests de mappers para cobertura JaCoCo y verificación final de umbrales.

- [X] T055 [P] Write unit test `OrderPersistenceMapperTest` — `src/test/java/o/u/d/adapter/out/persistence/mapper/OrderPersistenceMapperTest.java` (roundtrip toDomain(toJpaEntity(order)) == original; verify no field lost including embedded Address; OrderStatusEvent roundtrip with estimatedDispatchDate and rejectionReason)
- [X] T056 [P] Write unit test `OrderWebMapperTest` — `src/test/java/o/u/d/adapter/in/web/mapper/OrderWebMapperTest.java` (toSummary: verify orderCode, productCode, quantity, status, expectedDeliveryDate; toDetail: verify all fields including deliveryAddress sub-fields, customerContact, specialConditions, statusHistory list)
- [X] T057 Run full coverage gate — `./gradlew test jacocoTestReport jacocoTestCoverageVerification`; open `build/reports/jacoco/test/html/index.html`; if any class is below per-class > 80% threshold, add targeted tests for that class before closing; confirm XML report exists at `build/reports/jacoco/test/jacocoTestReport.xml`

---

## Dependencies & Execution Order

### Phase Dependencies

| Phase | Depends On | Blocks |
|-------|-----------|--------|
| Setup (Phase 1) | — | Foundation |
| Foundation (Phase 2) | Setup complete | All stories |
| US1 (Phase 3) | Foundation + T024 GlobalExceptionHandler ✅ | US2, US3 (controller base T037) |
| US2 (Phase 4) | Foundation + T037 (controller base) | — |
| US3 (Phase 5) | Foundation + T037 (controller base) | — |
| Polish (Phase 6) | All stories complete | — |

### User Story Dependencies

| Historia | Depende de | Notas |
|----------|-----------|-------|
| US1 (P1) | Foundation completa | Establece controller y persistence adapter base |
| US2 (P2) | Foundation + T037 (ProviderOrderController) | Agrega acceptOrder al mismo controller |
| US3 (P3) | Foundation + T037 (ProviderOrderController) | Agrega rejectOrder al mismo controller |
| US2 + US3 | Pueden ir en paralelo entre sí una vez T037 disponible | |

### Within Each Story

1. Tests escritos y en **ROJO** (test-first BDD — Principio II)
2. Output adapters antes de services
3. Services antes de controllers
4. Step definitions tras el endpoint funcional

---

## Parallel Opportunities

### Setup Phase

```
T001 (build.gradle — PRIMERO)
  ├── T002 (openapi-generator config)
  ├── T003 (JaCoCo config)
  ├── T004 (copy openapi.yml)
  └── T006 (.gitignore)
T005 (application.yaml — independiente)
```

### Foundation Phase

```
T007 (schema.sql — PRIMERO en BD)
  └── T008 (data.sql — depende de T007 contenido conocido; NO [P])
T009, T010 en paralelo (enum + value object)
  └── T011 (DropshippingOrder — depende T009 + T010)
      └── T012 (OrderStatusEvent — depende T011)
T013 (exceptions)  T014 (input ports)  T015 (output ports) — en paralelo
T016, T017 en paralelo (JPA entities)
T018, T019 en paralelo (JPA repos)
T020 (ApplicationConfig — depende de T014 + T015)
T021, T022, T023 en paralelo (domain entity tests — dependen de T011, T012, T010)
T024 (GlobalExceptionHandler — depende de T013)
T025 (GlobalExceptionHandlerTest — depende de T024)
T026 (CleanArchitectureTest — depende de T009-T020)
T027 (checkstyle.xml — independiente)
```

### US1 (tests en paralelo primero)

```
T028, T029, T030, T031 en paralelo (todos tests, todos en ROJO)
  └── T032, T033 en paralelo (services)
      └── T034 (OrderPersistenceMapper)
          └── T035 (DropshippingOrderPersistenceAdapter)
              └── T036, T037 en paralelo (OrderWebMapper + ProviderOrderController base)
                  └── T038, T039 en paralelo (steps + CucumberSpringConfig + runner)
```

### US2 y US3 (en paralelo entre sí)

```
US2: T040 T041 T042 → T043 → T044 T045 → T046 → T047
US3: T048 T049 T050 → T051 → T052 T053 → T054
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: Setup (T001–T006)
2. Phase 2: Foundation (T007–T027) — **CRÍTICO: incluye GlobalExceptionHandler**
3. Phase 3: User Story 1 (T028–T039)
4. **STOP y VALIDAR**: `./gradlew test jacocoTestReport`
5. Demo: `GET /api/v1/providers/42/orders` + Swagger UI + ArchUnit green

### Incremental Delivery

1. Setup + Foundation → base técnica + schema + seed + exception handler + ArchUnit listos
2. US1 → ver órdenes → demo/validación independiente
3. US2 → aceptar órdenes → demo/validación
4. US3 → rechazar órdenes → demo/validación completa
5. Polish → cobertura JaCoCo ≥ 80 %, cierre de feature

---

## Notes

- `[P]` = archivos distintos, sin dependencias incompletas entre tareas marcadas
- `[USx]` mapea directamente a la historia en `spec.md`
- **Test-first obligatorio**: ROJO → implementar → VERDE (Principio II)
- `GlobalExceptionHandler` está en **Foundation** (T024) — disponible para todos los tests de integración desde Phase 3 en adelante
- `InvalidRejectionReasonException` (T013) reemplaza `IllegalArgumentException` en RejectOrderService (T051) y es manejada por GlobalExceptionHandler (T024) → HTTP 400
- `db/schema.sql` (T007) es la fuente de verdad del esquema; `db/data.sql` (T008) depende de ella — T008 NO tiene [P]
- Código generado por openapi-generator vive en `build/generated/` — excluido de JaCoCo y de git (T006)
- JaCoCo XML habilitado explícitamente en T003 — requerido por constitution Principle V
- ArchUnit test (T026) falla el build automáticamente si alguna clase viola la regla de dependencias

## Task Count Summary

| Phase | Tasks | New vs Previous |
|-------|-------|-----------------|
| 1 — Setup | T001–T006 (6) | +1 (T006 .gitignore) |
| 2 — Foundation | T007–T027 (21) | +7 (entity tests C1, ArchUnit C2, GlobalExceptionHandler H1, checkstyle H2) |
| 3 — US1 | T028–T039 (12) | +1 (CucumberSpringConfiguration M5) |
| 4 — US2 | T040–T047 (8) | 0 |
| 5 — US3 | T048–T054 (7) | 0 |
| 6 — Polish | T055–T057 (3) | -2 (GlobalExceptionHandler moved, .gitignore moved) |
| **Total** | **57 tasks** | **+6 vs previous 51** |
