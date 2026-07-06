# language: es
Característica: Aceptar orden de dropshipping

  Antecedentes:
    Dado el proveedor 42 tiene órdenes asignadas en el sistema

  Escenario: El proveedor acepta una orden pendiente con fecha futura válida
    Dado la orden con código "ORD-2026-TEST-01" está en estado PENDING
    Cuando el proveedor acepta la orden con fecha estimada "2099-01-01"
    Entonces la respuesta tiene código HTTP 200
    Y el campo newStatus de la respuesta es "ACCEPTED"
    Y el campo estimatedDispatchDate de la respuesta es "2099-01-01"

  Escenario: El proveedor intenta aceptar con una fecha en el pasado
    Dado la orden con código "ORD-2026-TEST-01" está en estado PENDING
    Cuando el proveedor acepta la orden con fecha estimada "2020-01-01"
    Entonces la respuesta tiene código HTTP 400

  Escenario: El proveedor intenta aceptar una orden ya procesada
    Dado la orden con código "ORD-2026-TEST-03" está en estado ACCEPTED
    Cuando el proveedor acepta la orden con fecha estimada "2099-01-01"
    Entonces la respuesta tiene código HTTP 409
