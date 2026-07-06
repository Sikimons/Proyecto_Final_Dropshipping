# language: es
Característica: Ver órdenes asignadas al proveedor

  Antecedentes:
    Dado el proveedor 42 tiene órdenes asignadas en el sistema

  Escenario: El proveedor ve sus órdenes con todos los campos requeridos
    Cuando el proveedor consulta sus órdenes
    Entonces la respuesta tiene código HTTP 200
    Y la lista contiene al menos una orden con productCode y productDescription

  Escenario: El proveedor filtra órdenes por estado PENDING
    Cuando el proveedor filtra sus órdenes por estado "PENDING"
    Entonces la respuesta tiene código HTTP 200
    Y todas las órdenes en la respuesta tienen estado "PENDING"

  Escenario: El proveedor obtiene el detalle completo de una orden
    Cuando el proveedor solicita el detalle de la primera orden
    Entonces la respuesta tiene código HTTP 200
    Y el detalle incluye deliveryAddress con street, city, state y country
    Y el detalle incluye customerName y customerContact
    Y el detalle incluye expectedDeliveryDate
