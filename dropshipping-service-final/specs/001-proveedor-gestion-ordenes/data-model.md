# Data Model: [US-01] Gestión de Órdenes Dropshipping por Proveedor

**Feature**: `specs/001-proveedor-gestion-ordenes`
**Date**: 2026-07-05

---

## Domain Layer Entities (Pure POJOs — no framework annotations)

### DropshippingOrder

Raíz de agregado. Representa una orden asignada a un proveedor.

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id` | `Long` | Sí | Identificador interno del sistema |
| `orderCode` | `String` | Sí | Código de negocio único (ej. `ORD-2026-0001`) |
| `providerId` | `Long` | Sí | Identificador del proveedor asignado |
| `productCode` | `String` | Sí | Código de catálogo del producto |
| `productDescription` | `String` | Sí | Descripción legible del producto |
| `quantity` | `Integer` | Sí | Cantidad de unidades a despachar (> 0) |
| `deliveryAddress` | `Address` | Sí | Dirección completa de entrega (value object) |
| `customerName` | `String` | Sí | Nombre completo del cliente |
| `customerContact` | `String` | Sí | Teléfono o email de contacto del cliente |
| `expectedDeliveryDate` | `LocalDate` | Sí | Fecha esperada de entrega acordada con el cliente |
| `specialConditions` | `String` | No | Condiciones especiales del despacho (nullable) |
| `status` | `OrderStatus` | Sí | Estado actual de la orden |
| `createdAt` | `LocalDateTime` | Sí | Timestamp de creación (UTC) |
| `updatedAt` | `LocalDateTime` | Sí | Timestamp de última actualización (UTC) |

**Business rules**:
- `quantity` DEBE ser mayor a 0.
- `status` solo puede transicionar de `PENDING` → `ACCEPTED` o `PENDING` → `REJECTED`.
  Cualquier otra transición lanza `OrderAlreadyProcessedException`.
- `orderCode` es único en el sistema.

---

### OrderStatus (Enum)

```
PENDING   → Estado inicial al crear la orden
ACCEPTED  → El proveedor confirmó con fecha estimada de despacho
REJECTED  → El proveedor rechazó con motivo
```

**Valid transitions**:
```
PENDING → ACCEPTED  (acción: accept)
PENDING → REJECTED  (acción: reject)
ACCEPTED → (terminal — no hay transición válida)
REJECTED → (terminal — no hay transición válida)
```

---

### Address (Value Object)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `street` | `String` | Sí | Calle y número |
| `city` | `String` | Sí | Ciudad |
| `state` | `String` | Sí | Departamento / Provincia |
| `postalCode` | `String` | No | Código postal |
| `country` | `String` | Sí | País |

---

### OrderStatusEvent (Immutable Audit Record)

Registro inmutable de cada cambio de estado. Jamás se modifica una vez creado.

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id` | `Long` | Sí | Identificador interno |
| `orderId` | `Long` | Sí | Referencia a `DropshippingOrder.id` |
| `previousStatus` | `OrderStatus` | Sí | Estado antes de la transición |
| `newStatus` | `OrderStatus` | Sí | Estado después de la transición |
| `actorId` | `String` | Sí | Identificador del proveedor que ejecutó la acción |
| `timestamp` | `LocalDateTime` | Sí | Momento exacto de la transición (UTC) |
| `estimatedDispatchDate` | `LocalDate` | Condicional | Obligatorio cuando `newStatus = ACCEPTED` |
| `rejectionReason` | `String` | Condicional | Obligatorio cuando `newStatus = REJECTED` |

**Business rules**:
- `estimatedDispatchDate` DEBE ser posterior a la fecha del `timestamp` del evento.
- `rejectionReason` no puede ser nulo ni vacío cuando `newStatus = REJECTED`.
- Una vez creado el evento, ningún campo puede ser modificado (append-only).

---

## Application Layer Ports

### Input Ports (Use Cases)

```java
// GetProviderOrdersUseCase
List<DropshippingOrder> getOrders(Long providerId, OrderStatus statusFilter);

// GetOrderDetailUseCase
DropshippingOrder getOrder(Long providerId, Long orderId);

// AcceptOrderUseCase
OrderStatusEvent accept(Long providerId, Long orderId, LocalDate estimatedDispatchDate);

// RejectOrderUseCase
OrderStatusEvent reject(Long providerId, Long orderId, String reason);
```

### Output Ports (Infrastructure Contracts)

```java
// LoadOrderPort
Optional<DropshippingOrder> loadOrder(Long orderId);
List<DropshippingOrder> loadOrdersByProvider(Long providerId, OrderStatus statusFilter);

// SaveOrderPort
DropshippingOrder saveOrder(DropshippingOrder order);

// SaveOrderStatusEventPort
OrderStatusEvent saveEvent(OrderStatusEvent event);

// SendNotificationPort
void notifyOrderAccepted(OrderStatusEvent event);
void notifyOrderRejected(OrderStatusEvent event);
```

---

## Infrastructure Layer — JPA Entities

### DropshippingOrderJpaEntity

Tabla: `dropshipping_order`

| Columna | Tipo SQL | Constraint |
|---------|----------|------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `order_code` | `VARCHAR(50)` | UNIQUE, NOT NULL |
| `provider_id` | `BIGINT` | NOT NULL |
| `product_code` | `VARCHAR(100)` | NOT NULL |
| `product_description` | `VARCHAR(500)` | NOT NULL |
| `quantity` | `INTEGER` | NOT NULL, CHECK > 0 |
| `street` | `VARCHAR(255)` | NOT NULL |
| `city` | `VARCHAR(100)` | NOT NULL |
| `state` | `VARCHAR(100)` | NOT NULL |
| `postal_code` | `VARCHAR(20)` | NULLABLE |
| `country` | `VARCHAR(100)` | NOT NULL |
| `customer_name` | `VARCHAR(200)` | NOT NULL |
| `customer_contact` | `VARCHAR(200)` | NOT NULL |
| `expected_delivery_date` | `DATE` | NOT NULL |
| `special_conditions` | `TEXT` | NULLABLE |
| `status` | `VARCHAR(20)` | NOT NULL (enum string) |
| `created_at` | `TIMESTAMP` | NOT NULL |
| `updated_at` | `TIMESTAMP` | NOT NULL |

---

### OrderStatusEventJpaEntity

Tabla: `order_status_event`

| Columna | Tipo SQL | Constraint |
|---------|----------|------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT |
| `order_id` | `BIGINT` | FK → `dropshipping_order.id`, NOT NULL |
| `previous_status` | `VARCHAR(20)` | NOT NULL |
| `new_status` | `VARCHAR(20)` | NOT NULL |
| `actor_id` | `VARCHAR(100)` | NOT NULL |
| `timestamp` | `TIMESTAMP` | NOT NULL |
| `estimated_dispatch_date` | `DATE` | NULLABLE |
| `rejection_reason` | `TEXT` | NULLABLE |

---

## State Machine Diagram

```
         ┌─────────────────────────────────────────────────────┐
         │                   DropshippingOrder                 │
         │                                                     │
         │      [CREATE]                                       │
         │         │                                           │
         │         ▼                                           │
         │    ┌─────────┐                                      │
         │    │ PENDING │                                      │
         │    └────┬────┘                                      │
         │         │                                           │
         │    ┌────┴────────────────────┐                      │
         │    │                        │                      │
         │    ▼                        ▼                      │
         │ ┌──────────┐          ┌──────────┐                 │
         │ │ ACCEPTED │          │ REJECTED │                 │
         │ │ (terminal)│         │ (terminal)│                │
         │ └──────────┘          └──────────┘                 │
         │                                                     │
         │  ACCEPTED: registra actor, timestamp,               │
         │            estimatedDispatchDate                    │
         │  REJECTED: registra actor, timestamp,               │
         │            rejectionReason                          │
         └─────────────────────────────────────────────────────┘
```

---

## Relationships Summary

```
DropshippingOrder 1 ──── * OrderStatusEvent
     │
     └── embeds Address (valor, sin tabla propia)
```
