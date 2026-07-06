 Feature Specification: [US-01] Gestión de Órdenes Dropshipping por Proveedor

**Feature Branch**: `001-proveedor-gestion-ordenes`

**Created**: 2026-07-05

**Status**: Draft

**Input**: Como Proveedor, quiero ver mis órdenes asignadas y poder aceptarlas o rechazarlas con una
fecha estimada de despacho, para operar desde un solo canal sin depender del correo.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Ver órdenes asignadas (Priority: P1)

Como proveedor autenticado en el portal, quiero ver el listado de mis órdenes Dropshipping
asignadas con toda la información necesaria para procesarlas, de modo que no deba depender del
correo electrónico para conocer los pedidos que debo gestionar.

**Why this priority**: Es el punto de partida de todo el flujo; sin visibilidad de las órdenes,
las acciones de aceptar o rechazar son imposibles. Constituye el MVP mínimo del canal único.

**Independent Test**: El proveedor ingresa al portal, navega a "Mis Órdenes" y verifica que cada
orden muestra código de producto, descripción, cantidad, dirección completa, contacto del cliente,
fecha esperada de entrega y condiciones especiales.

**Acceptance Scenarios**:

1. **Given** que el sistema ha generado una o más órdenes Dropshipping asignadas al proveedor,
   **When** el proveedor accede al portal y navega a la sección de órdenes,
   **Then** ve el listado de órdenes con: código de producto, descripción, cantidad, dirección
   completa de entrega, nombre y contacto del cliente, fecha esperada de despacho y condiciones
   especiales.

2. **Given** que el proveedor no tiene órdenes asignadas,
   **When** accede a la sección de órdenes,
   **Then** ve un mensaje informativo indicando que no hay órdenes pendientes.

3. **Given** que existen órdenes en distintos estados (Pendiente, Aceptado, Rechazado),
   **When** el proveedor accede al listado,
   **Then** puede filtrar o identificar visualmente las órdenes según su estado actual.

---

### User Story 2 — Aceptar una orden con fecha estimada de despacho (Priority: P2)

Como proveedor, quiero aceptar una orden ingresando una fecha estimada de despacho, para que el
sistema registre mi compromiso y notifique al analista encargado sin necesidad de intercambio de
correos.

**Why this priority**: La aceptación es la acción de mayor frecuencia en el flujo operativo y
genera el primer punto de trazabilidad auditada.

**Independent Test**: El proveedor selecciona una orden en estado "Pendiente", elige "Aceptar",
ingresa una fecha estimada de despacho válida, confirma y verifica que el estado cambia a
"Aceptado", que se registra quién y cuándo, y que el analista recibe la notificación de la
actualización.

**Acceptance Scenarios**:

1. **Given** que el proveedor visualiza una orden en estado "Pendiente",
   **When** selecciona "Aceptar" e ingresa una fecha estimada de despacho futura válida y confirma,
   **Then** el pedido cambia a estado "Aceptado", se registra el identificador del proveedor
   (actor) y el timestamp de la acción, y el analista asignado recibe una notificación con el
   detalle de la aceptación y la fecha comprometida.

2. **Given** que el proveedor intenta aceptar una orden con una fecha estimada en el pasado o
   vacía,
   **When** confirma la acción,
   **Then** el sistema rechaza la operación y muestra un mensaje de validación claro sin modificar
   el estado de la orden.

3. **Given** que la orden ya fue aceptada o rechazada previamente,
   **When** el proveedor intenta volver a aceptarla,
   **Then** el sistema impide la acción e informa que la orden ya tiene un estado definitivo.

---

### User Story 3 — Rechazar una orden con motivo (Priority: P3)

Como proveedor, quiero rechazar una orden indicando el motivo, para que el equipo comercial sea
alertado de inmediato y pueda ofrecer una alternativa al cliente sin demoras.

**Why this priority**: El rechazo activa un proceso de contingencia crítico para la experiencia del
cliente; su trazabilidad y velocidad de notificación son requisitos de negocio.

**Independent Test**: El proveedor selecciona una orden en estado "Pendiente", elige "Rechazar",
ingresa el motivo obligatorio, confirma y verifica que el estado cambia a "Rechazado", que el
motivo queda registrado, y que el equipo comercial recibe una alerta inmediata.

**Acceptance Scenarios**:

1. **Given** que el proveedor visualiza una orden en estado "Pendiente",
   **When** selecciona "Rechazar", ingresa el motivo del rechazo y confirma,
   **Then** el pedido cambia a estado "Rechazado", se registra el motivo, el actor y el timestamp,
   y el equipo comercial recibe una alerta inmediata con el detalle de la orden rechazada para
   ofrecer alternativa al cliente.

2. **Given** que el proveedor intenta rechazar una orden sin ingresar el motivo,
   **When** intenta confirmar la acción,
   **Then** el sistema bloquea la operación y solicita el motivo obligatorio.

3. **Given** que la orden ya fue aceptada o rechazada previamente,
   **When** el proveedor intenta rechazarla,
   **Then** el sistema impide la acción e informa que la orden ya tiene un estado definitivo.

---

### Edge Cases

- ¿Qué ocurre si el proveedor pierde conectividad justo después de confirmar la acción?
  El sistema debe garantizar idempotencia; una acción duplicada accidental no debe generar doble
  registro de estado.
- ¿Qué pasa si la notificación al analista o al equipo comercial falla?
  El cambio de estado DEBE persistir; la notificación fallida DEBE reintentarse de forma asíncrona
  y registrarse como evento de fallo para monitoreo.
- ¿Qué ocurre si la fecha estimada de despacho es igual a la fecha esperada de entrega del cliente?
  El sistema acepta la fecha pero puede mostrar una advertencia al proveedor indicando que el margen
  de tiempo es ajustado.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE mostrar al proveedor autenticado únicamente las órdenes Dropshipping
  asignadas a él.
- **FR-002**: Cada orden DEBE presentar: código de producto, descripción del producto, cantidad,
  dirección completa de entrega, nombre y contacto del cliente, fecha esperada de entrega y
  condiciones especiales.
- **FR-003**: El sistema DEBE permitir al proveedor aceptar una orden en estado "Pendiente"
  ingresando una fecha estimada de despacho futura obligatoria.
- **FR-004**: Al aceptar una orden, el sistema DEBE cambiar su estado a "Aceptado" y registrar el
  identificador del actor y el timestamp exacto de la acción.
- **FR-005**: Al aceptar una orden, el sistema DEBE notificar al analista asignado con los detalles
  de la aceptación y la fecha estimada de despacho comprometida.
- **FR-006**: El sistema DEBE permitir al proveedor rechazar una orden en estado "Pendiente"
  ingresando obligatoriamente el motivo del rechazo.
- **FR-007**: Al rechazar una orden, el sistema DEBE cambiar su estado a "Rechazado" y registrar
  el motivo, el actor y el timestamp exacto.
- **FR-008**: Al rechazar una orden, el sistema DEBE enviar una alerta inmediata al equipo comercial
  con el detalle de la orden rechazada.
- **FR-009**: El sistema DEBE impedir la transición de estado en órdenes que ya tienen un estado
  definitivo (Aceptado o Rechazado).
- **FR-010**: La fecha estimada de despacho DEBE ser una fecha futura; el sistema DEBE rechazar
  fechas pasadas o iguales a la fecha actual.

### Key Entities

- **DropshippingOrder**: Representa el pedido asignado al proveedor. Atributos clave: código de
  producto, descripción, cantidad, dirección de entrega, contacto del cliente, fecha esperada,
  condiciones especiales, estado (Pendiente / Aceptado / Rechazado).
- **OrderStatusEvent**: Registro inmutable de cada cambio de estado. Atributos: orden relacionada,
  estado anterior, estado nuevo, actor (proveedor), timestamp, motivo (requerido en rechazo) /
  fecha estimada de despacho (requerida en aceptación).
- **Provider**: Proveedor autenticado que opera en el portal. Atributos: identificador, nombre.
- **Notification**: Evento de comunicación saliente. Atributos: tipo (aceptación / rechazo),
  destinatario (analista / equipo comercial), contenido, timestamp, estado de entrega.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El proveedor puede ver, aceptar o rechazar una orden en menos de 2 minutos desde
  que accede al portal.
- **SC-002**: El 100 % de los cambios de estado quedan registrados con actor y timestamp; ningún
  cambio ocurre sin trazabilidad auditada.
- **SC-003**: Las notificaciones al analista y al equipo comercial son entregadas en menos de
  30 segundos tras la confirmación del proveedor.
- **SC-004**: El sistema no permite transiciones de estado inválidas en ningún escenario;
  la tasa de estados inconsistentes es 0 %.
- **SC-005**: Al menos el 90 % de los proveedores piloto completan la tarea de aceptar o rechazar
  una orden sin requerir asistencia externa durante las primeras dos semanas de operación.

---

## Assumptions

- La autenticación y autorización del proveedor al portal están resueltas por un sistema preexistente;
  esta feature no gestiona login ni roles desde cero.
- El "analista" y el "equipo comercial" son roles internos ya definidos en el sistema con canales
  de notificación configurados (correo interno, sistema de alertas u otro mecanismo existente).
- Una orden Dropshipping es creada por el sistema (o por el equipo comercial); esta feature solo
  gestiona la respuesta del proveedor, no la creación de la orden.
- El portal es una interfaz web accesible desde navegador de escritorio; el soporte móvil está fuera
  del alcance de esta versión.
- La fecha estimada de despacho ingresada por el proveedor es una fecha calendario (no fecha y hora
  exacta), con zona horaria configurada a nivel de sistema.
- El proveedor solo tiene visibilidad de sus propias órdenes; no puede ver órdenes de otros
  proveedores.
- La persistencia del cambio de estado es prioritaria sobre la entrega de la notificación; si la
  notificación falla, el estado ya persistido es la fuente de verdad.
