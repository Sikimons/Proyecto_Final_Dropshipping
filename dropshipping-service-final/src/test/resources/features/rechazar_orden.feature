# language: es
Característica: Rechazar orden de dropshipping

  Antecedentes:
    Dado el proveedor 42 tiene órdenes asignadas en el sistema

  Escenario: El proveedor rechaza una orden pendiente con motivo válido
    Dado la orden pendiente para rechazo está disponible
    Cuando el proveedor rechaza la orden con motivo "Sin stock del producto hasta agosto de 2026."
    Entonces la respuesta tiene código HTTP 200
    Y el campo newStatus de la respuesta de rechazo es "REJECTED"
    Y el campo rejectionReason contiene el motivo enviado

  Escenario: El proveedor intenta rechazar con motivo vacío
    Dado la orden pendiente para rechazo está disponible
    Cuando el proveedor rechaza la orden con motivo ""
    Entonces la respuesta tiene código HTTP 400

  Escenario: El proveedor intenta rechazar una orden ya procesada
    Dado la orden con código "ORD-2026-TEST-03" está en estado ACCEPTED
    Cuando el proveedor rechaza la orden con motivo "Sin stock del producto"
    Entonces la respuesta tiene código HTTP 409
