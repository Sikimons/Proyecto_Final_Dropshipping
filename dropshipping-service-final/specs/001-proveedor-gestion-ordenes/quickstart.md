# Quickstart — Validación End-to-End: [US-01] Gestión de Órdenes por Proveedor

**Feature**: `specs/001-proveedor-gestion-ordenes`
**Date**: 2026-07-05

Esta guía permite verificar que la feature funciona correctamente de extremo a extremo
sin conocer detalles de implementación.

---

## Prerrequisitos

| Herramienta | Versión mínima | Verificar con |
|-------------|----------------|---------------|
| Java | 21 | `java -version` |
| Gradle Wrapper | (incluido) | `./gradlew --version` |
| curl o Postman | Cualquiera | `curl --version` |

---

## 1. Levantar el servicio

```bash
# Desde la raíz del proyecto
./gradlew bootRun
```

El servicio levanta en `http://localhost:8080`.
La consola H2 está disponible en `http://localhost:8080/h2-console` (solo desarrollo).

**Verificar que el servicio responde**:
```bash
curl http://localhost:8080/api/v1/providers/1/orders
# Esperado: HTTP 200 con lista vacía o con órdenes de seed
```

---

## 2. Datos de prueba (seed inicial)

Al arrancar, Spring SQL Init ejecuta automáticamente `src/main/resources/db/schema.sql` (DDL)
seguido de `src/main/resources/db/data.sql` (DML seed). Las siguientes órdenes estarán
disponibles sin ninguna acción adicional, asignadas al proveedor con ID `42`:

| Order Code | Producto | Estado |
|------------|----------|--------|
| ORD-2026-TEST-01 | Zapatillas Running XR-500 | PENDING |
| ORD-2026-TEST-02 | Mochila Outdoor 45L | PENDING |
| ORD-2026-TEST-03 | Reloj Deportivo GPS | ACCEPTED |

---

## 3. Escenario BDD — Ver órdenes asignadas (US1-P1)

**Given** el proveedor 42 tiene órdenes asignadas,
**When** consulta su listado,
**Then** ve todas sus órdenes con datos completos.

```bash
# Listar todas las órdenes
curl -s http://localhost:8080/api/v1/providers/42/orders | python3 -m json.tool

# Filtrar solo PENDING
curl -s "http://localhost:8080/api/v1/providers/42/orders?status=PENDING" | python3 -m json.tool

# Ver detalle completo de una orden
curl -s http://localhost:8080/api/v1/providers/42/orders/1 | python3 -m json.tool
```

**Criterio de éxito**: La respuesta incluye `productCode`, `productDescription`, `quantity`,
`deliveryAddress` (calle, ciudad, estado, país), `customerName`, `customerContact`,
`expectedDeliveryDate` y `specialConditions`.

---

## 4. Escenario BDD — Aceptar orden (US2-P2)

**Given** la orden ORD-2026-TEST-01 está en estado PENDING,
**When** el proveedor la acepta con una fecha estimada futura,
**Then** el estado cambia a ACCEPTED y se registra actor + timestamp.

```bash
# Aceptar la orden 1 (ajustar orderId según el seed)
curl -s -X POST http://localhost:8080/api/v1/providers/42/orders/1/accept \
  -H "Content-Type: application/json" \
  -d '{"estimatedDispatchDate": "2026-07-10"}' | python3 -m json.tool
```

**Criterio de éxito**:
- `newStatus` = `ACCEPTED`
- `estimatedDispatchDate` = `"2026-07-10"`
- `actorId` = `"42"`
- `timestamp` presente (formato ISO 8601)
- `message` confirma notificación al analista

**Verificar estado actualizado**:
```bash
curl -s http://localhost:8080/api/v1/providers/42/orders/1 | python3 -m json.tool
# status debe ser ACCEPTED y statusHistory debe contener el evento
```

**Validar protección de doble acción**:
```bash
# Intentar aceptar de nuevo — debe retornar HTTP 409
curl -s -X POST http://localhost:8080/api/v1/providers/42/orders/1/accept \
  -H "Content-Type: application/json" \
  -d '{"estimatedDispatchDate": "2026-07-11"}'
# Esperado: {"status": 409, "error": "Conflict", ...}
```

---

## 5. Escenario BDD — Rechazar orden (US3-P3)

**Given** la orden ORD-2026-TEST-02 está en estado PENDING,
**When** el proveedor la rechaza con un motivo,
**Then** el estado cambia a REJECTED y el equipo comercial es alertado.

```bash
# Rechazar la orden 2 (ajustar orderId según el seed)
curl -s -X POST http://localhost:8080/api/v1/providers/42/orders/2/reject \
  -H "Content-Type: application/json" \
  -d '{"reason": "Sin stock del producto hasta agosto de 2026."}' | python3 -m json.tool
```

**Criterio de éxito**:
- `newStatus` = `REJECTED`
- `rejectionReason` = el motivo enviado
- `actorId` = `"42"`
- `timestamp` presente
- `message` confirma notificación al equipo comercial

**Validar que motivo vacío es rechazado**:
```bash
curl -s -X POST http://localhost:8080/api/v1/providers/42/orders/2/reject \
  -H "Content-Type: application/json" \
  -d '{"reason": ""}' 
# Esperado: HTTP 400 con mensaje de validación
```

---

## 6. Ejecutar suite de tests completa

```bash
# Todos los tests (unit + integration + functional BDD)
./gradlew test

# Solo tests unitarios
./gradlew test --tests "*.domain.*" --tests "*.application.*"

# Solo tests funcionales Cucumber
./gradlew test --tests "*.bdd.*"

# Generar reporte de cobertura JaCoCo
./gradlew test jacocoTestReport

# Verificar umbrales de cobertura (falla el build si no se cumplen)
./gradlew test jacocoTestCoverageVerification
```

**Reportes generados**:
- HTML: `build/reports/tests/test/index.html`
- JaCoCo HTML: `build/reports/jacoco/test/html/index.html`
- JaCoCo XML: `build/reports/jacoco/test/jacocoTestReport.xml`
- Cucumber HTML: `build/reports/cucumber/index.html`

---

## 7. Criterios de aceptación de la validación

| Escenario | Indicador | Resultado esperado |
|-----------|-----------|-------------------|
| Listar órdenes | HTTP status | 200 OK |
| Listar órdenes | Campos presentes | productCode, quantity, deliveryAddress, customerContact, expectedDeliveryDate |
| Aceptar orden válida | HTTP status | 200 OK |
| Aceptar orden ya procesada | HTTP status | 409 Conflict |
| Aceptar con fecha pasada | HTTP status | 400 Bad Request |
| Rechazar orden válida | HTTP status | 200 OK |
| Rechazar sin motivo | HTTP status | 400 Bad Request |
| Rechazar orden ya procesada | HTTP status | 409 Conflict |
| Tests totales | Resultado | 0 fallos |
| Cobertura por clase | JaCoCo | > 80 % |
| Cobertura global | JaCoCo | ≥ 80 % |

---

## Referencias

- Contrato OpenAPI: [`contracts/openapi.yml`](./contracts/openapi.yml)
- Modelo de datos: [`data-model.md`](./data-model.md)
- Decisiones de diseño: [`research.md`](./research.md)
- Especificación: [`spec.md`](./spec.md)
