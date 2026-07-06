# Research: [US-01] Gestión de Órdenes Dropshipping por Proveedor

**Feature**: `specs/001-proveedor-gestion-ordenes`
**Date**: 2026-07-05

---

## Decisión 1 — Generación de código desde OpenAPI

**Decision**: `openapi-generator-gradle-plugin` 7.x con generador `spring` (modo delegate pattern).

**Rationale**: El plugin se integra en el ciclo de build de Gradle y genera interfaces Java anotadas
con Spring MVC que el `ProviderOrderController` implementa. El modo delegate evita que el código
generado acumule lógica de negocio: el controlador delega directamente a los use cases a través de
un delegado. El código generado se ubica en `build/generated/` y se excluye de JaCoCo.

**Alternatives considered**:
- `springdoc-openapi-maven-plugin` (code-first): Descartado porque va en sentido contrario al
  principio IV (API First); genera la spec desde el código, no el código desde la spec.
- `openapi-generator-maven-plugin`: Equivalente funcional, descartado por coherencia con Gradle
  (build tool elegido).
- Escritura manual de controladores: Prohibido por constitución (Principio IV).

---

## Decisión 2 — Estrategia de notificación

**Decision**: Spring `ApplicationEventPublisher` (eventos de dominio síncronos en fase 1; fácil
migración a async con `@Async` o broker en producción).

**Rationale**: El proyecto es académico y no tiene broker de mensajes configurado. Los eventos de
Spring desacoplan el dominio de la infraestructura de notificación cumpliendo el Principio I
(Clean Architecture): el use case publica un evento; el adaptador de notificación lo escucha.
Si la entrega de la notificación falla, el estado de la orden ya está persistido (la transacción
de la orden confirma antes de que el evento se publique). Para producción, se puede cambiar el
listener por uno `@Async` o delegar a Kafka/SQS sin tocar la capa de aplicación.

**Alternatives considered**:
- `SendNotificationPort` (interfaz de salida + adaptador): Compatible con Clean Architecture pero
  introduce un puerto extra para un mecanismo que Spring ya provee de forma desacoplada mediante
  eventos. Se usa `ApplicationEventPublisher` internamente dentro del adaptador de notificación,
  no en los use cases directamente (así los use cases no dependen del mecanismo de evento).
  Finalmente se opta por tener `SendNotificationPort` como puerto de salida con el adaptador
  implementándolo via eventos: mantiene el dominio totalmente libre de dependencias de framework.
- Notificación síncrona directa (llamada HTTP o email): Acoplamiento directo; cualquier fallo en
  el servicio externo afecta la transacción principal. Descartado.
- RabbitMQ / Kafka: Correctos para producción pero sobredimensionados para el piloto académico.

---

## Decisión 3 — Framework de tests BDD funcionales

**Decision**: Cucumber 7.x (escenarios `.feature` en español) + REST-Assured 5.x (cliente HTTP
para ejercer la API en contexto `@SpringBootTest`).

**Rationale**: Cucumber permite escribir escenarios Given/When/Then directamente desde los
criterios de aceptación del `spec.md`, manteniendo trazabilidad 1-a-1 (Principio II). REST-Assured
proporciona una DSL fluida para hacer aserciones sobre respuestas HTTP sin acoplarse a Mockito.

**Alternatives considered**:
- Spring MockMvc solo: Adecuado para tests de controlador en aislamiento, pero no ejecuta el
  stack completo (base de datos real, listeners de eventos). Se usa para tests de integración de
  controlador (@WebMvcTest), no para los funcionales.
- Karate DSL: Alternativa BDD + REST en un solo framework; descartado por menor adopción en el
  ecosistema Spring académico y por requerir configuración adicional.
- TestRestTemplate (sin Cucumber): Pierde la narrativa BDD given/when/then en el código de test.

---

## Decisión 4 — Mapeo entre capas (sin MapStruct)

**Decision**: Mappers escritos a mano como clases de utilidad estáticas o Spring `@Component`
según la capa.

**Rationale**: YAGNI — MapStruct añade procesamiento de anotaciones y configuración extra que no
se justifica para el número de entidades de esta feature (< 5 clases por capa). Los mappers a
mano son simples, directamente testeables y sin magia de generación de código. Se revisa en el
próximo feature; si hay > 8 entidades mapeadas, se introduce MapStruct.

**Alternatives considered**:
- MapStruct: Excelente para proyectos grandes; descartado por YAGNI.
- ModelMapper: Demasiado dinámico (reflexión), dificulta la detección de errores de mapeo en
  compile time.

---

## Decisión 5 — Validación de fecha estimada de despacho

**Decision**: Validación en el use case `AcceptOrderService` (no en el controlador ni en la
entidad JPA).

**Rationale**: La regla "la fecha estimada DEBE ser futura" es una regla de negocio (Principio I:
reglas en capas internas). El controlador solo valida formato de entrada (Bean Validation /
`@Valid`); el use case lanza `InvalidDispatchDateException` si la fecha no cumple la restricción
de negocio. La entidad de dominio garantiza consistencia interna en su constructor.

**Alternatives considered**:
- Validación solo con `@Future` en el DTO: Solo controla el formato/constraint de Jakarta Bean
  Validation; no expresa la intención de negocio y acopla la regla a la capa de presentación.
- Validación en la entidad JPA: Viola Clean Architecture (regla de negocio en capa de
  infraestructura).

---

## Resolución de NEEDS CLARIFICATION

No quedaron marcadores `[NEEDS CLARIFICATION]` en `spec.md`. Los siguientes supuestos quedan
registrados como decisiones de diseño confirmadas:

| Supuesto en spec.md | Decisión de implementación |
|---------------------|---------------------------|
| Autenticación existente | El `providerId` se pasa como path param; la autenticación real se dejará como stub configurable (fuera del scope de US-01). |
| Canal de notificación existente | `InternalNotificationAdapter` hace log estructurado + publica evento. En producción se conecta a email/Slack sin cambiar contratos. |
| Fecha estimada es fecha calendario | `LocalDate` en el modelo; sin zona horaria en el dominio. |
| Portal es web de escritorio | Solo se expone API REST; el frontend es responsabilidad de otra feature. |
