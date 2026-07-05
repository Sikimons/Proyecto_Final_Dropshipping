# Proyecto Final Dropshipping

Repositorio principal del proyecto final de maestria para el caso de estudio `dropshipping`. Este repo concentra cuatro frentes de trabajo que cubren discovery, delivery, construccion del servicio y aseguramiento de calidad.

## Vision General

El proyecto esta organizado como un flujo de trabajo por etapas:

1. `discovery-agent` define el MVP, requisitos, hipotesis y artefactos iniciales.
2. `agile-delivery-team` transforma esos insumos en backlog, historias, arquitectura y plan de sprint.
3. `dropshipping-service` implementa el servicio principal en Spring Boot.
4. `quality-agent` verifica calidad tecnica, cobertura, seguridad y cumplimiento de criterios.

## Estructura Del Repositorio

```text
Proyecto Final/
|- README.md
|- discovery-agent/
|- agile-delivery-team/
|- dropshipping-service/
`- quality-agent/
```

## Componentes

### `discovery-agent`

Contiene los artefactos de descubrimiento del dominio `dropshipping`, incluyendo entrevistas, mapa de evidencia, hipotesis, canvas MVP, requisitos y reporte.

Punto de entrada recomendado:
- [discovery-agent/README.md](./discovery-agent/README.md)

### `agile-delivery-team`

Convierte los resultados de discovery en entregables de planificacion y arquitectura: epicas, backlog, historias, ADRs y sprint plan.

Puntos de interes:
- [agile-delivery-team/README.md](./agile-delivery-team/README.md)
- [agile-delivery-team/deliveries/dropshipping/outputs/](./agile-delivery-team/deliveries/dropshipping/outputs/)

### `dropshipping-service`

Implementacion tecnica del servicio de dropshipping en Java con Spring Boot. Incluye codigo fuente, pruebas, contratos y especificaciones funcionales.

Puntos de interes:
- [dropshipping-service/src/](./dropshipping-service/src/)
- [dropshipping-service/specs/001-gestionar-ordenes-proveedor/](./dropshipping-service/specs/001-gestionar-ordenes-proveedor/)
- [dropshipping-service/build.gradle](./dropshipping-service/build.gradle)

### `quality-agent`

Agente de verificacion y gobernanza que revisa pruebas, seguridad y trazabilidad de requisitos sobre un proyecto Spring Boot.

Punto de entrada recomendado:
- [quality-agent/README.md](./quality-agent/README.md)

## Flujo De Trabajo Sugerido

1. Revisar discovery en `discovery-agent`.
2. Validar backlog y arquitectura en `agile-delivery-team`.
3. Implementar o extender el servicio en `dropshipping-service`.
4. Ejecutar verificacion con `quality-agent`.

## Estado Del Repositorio

- Repositorio Git principal unificado.
- Las carpetas `agile-delivery-team`, `discovery-agent` y `dropshipping-service` ya no se manejan como repos separados.
- Rama principal: `main`.

## Notas

- El nombre historico del repo remoto usa `Proyecto_Final_dropsiping`, pero el contenido del proyecto corresponde a `dropshipping`.
- Algunos README internos tienen problemas de codificacion de caracteres; este README raiz sirve como indice limpio del proyecto.
