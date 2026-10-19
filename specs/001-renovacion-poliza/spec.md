# Feature Specification: Renovación de póliza

**Feature Branch**: `001-renovacion-poliza`

**Created**: 2026-10-19

**Status**: Draft

**Input**: User description: "Renovación de póliza: POST /api/polizas/{id}/renovacion crea una nueva póliza con vigencia de un año desde la fecha de fin de la original; la prima se ajusta +10% si hubo siniestros en los últimos 12 meses (regla del catálogo corporativo); solo se permite si la póliza está VIGENTE o VENCIDA hace 30 días o menos; la original queda marcada como RENOVADA (nuevo estado); botón 'Renovar' en el detalle del frontend; no cambiar contratos existentes."

## Clarifications

### Session 2026-10-19

- Q: ¿Desde qué fecha arranca la nueva vigencia? → A: La nueva póliza inicia el día siguiente a la `fecha_fin` de la original y termina exactamente un año después de esa `fecha_fin`, sin solapamiento ni huecos de cobertura.
- Q: Si se renueva una póliza VENCIDA (dentro de los 30 días), ¿la nueva póliza puede tener `fecha_inicio` en el pasado? → A: Sí. La renovación preserva la continuidad de cobertura; la regla RN-POL-03 ("fecha de inicio no anterior a hoy") aplica solo a la emisión de pólizas nuevas, no a renovaciones.
- Q: ¿Qué siniestros cuentan para el recargo? → A: Según el catálogo corporativo (`recargoSiniestralidad`), cuentan los siniestros en cualquier estado **excepto `RECHAZADO`**, con fecha de ocurrencia dentro de los últimos 12 meses contados desde la fecha en que se solicita la renovación.
- Q: ¿Sobre qué prima se aplica el recargo? → A: Sobre la prima anual de la póliza original (no se recalcula con la tasa base). Redondeo a 2 decimales, HALF_UP.
- Q: Una póliza VIGENTE renovada antes de su vencimiento pasa a RENOVADA, ¿sigue cubriendo? → A: Sí. Conserva la cobertura hasta su `fecha_fin`; los siniestros ocurridos dentro de su vigencia se siguen registrando sobre ella.
- Q: ¿Aplica alguna regla de suscripción adicional del catálogo? → A: Sí, para VIDA: `edadMaximaRenovacion = 70`. No se renueva si el cliente tiene más de 70 años a la fecha de inicio de la nueva vigencia.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Renovar una póliza elegible (Priority: P1)

Un operador de la aseguradora renueva la póliza de un cliente que está por vencer o venció hace pocos días. El sistema emite una póliza nueva para el mismo cliente, tipo y suma asegurada, con vigencia continua respecto de la anterior, aplica el recargo por siniestralidad cuando corresponde y marca la póliza original como RENOVADA.

**Why this priority**: Es el núcleo del negocio de la feature: sin la renovación vía API no hay nada que mostrar en la interfaz. Reduce la pérdida de clientes por vencimiento y evita re-emitir pólizas a mano.

**Independent Test**: Se prueba completamente con llamadas a `POST /api/polizas/{id}/renovacion` sobre los datos de demostración, verificando la póliza creada (`GET /api/polizas/{nuevoId}`) y el nuevo estado de la original (`GET /api/polizas/{id}`).

**Acceptance Scenarios**:

1. **Given** la póliza POL-HOG-0002 está VIGENTE, con prima anual 400,00 y sin siniestros, **When** el operador solicita su renovación, **Then** se crea una póliza nueva VIGENTE para el mismo cliente, tipo HOGAR y suma asegurada, con `fecha_inicio` = `fecha_fin` original + 1 día, `fecha_fin` = `fecha_fin` original + 1 año y prima anual 400,00; la respuesta es 201 con `Location` de la póliza nueva; y la original queda en estado RENOVADA.
2. **Given** la póliza POL-AUT-0001 está VIGENTE con prima 1.000,00 y tiene siniestros no rechazados en los últimos 12 meses, **When** se solicita su renovación, **Then** la póliza nueva tiene prima anual 1.100,00 (recargo de 10 %).
3. **Given** la póliza POL-VID-0003 está VENCIDA hace 10 días y su único siniestro ocurrió hace más de 12 meses, **When** se solicita su renovación, **Then** se crea la póliza nueva sin recargo (prima 600,00) y con `fecha_inicio` en el pasado (continuidad de cobertura).
4. **Given** la póliza POL-AUT-0004 está VENCIDA hace 90 días, **When** se solicita su renovación, **Then** la respuesta es 422 con código `PLAZO_RENOVACION_VENCIDO` y no se crea ninguna póliza.
5. **Given** la póliza POL-HOG-0005 está CANCELADA, **When** se solicita su renovación, **Then** la respuesta es 422 con código `POLIZA_NO_RENOVABLE`.
6. **Given** una póliza que ya está RENOVADA, **When** se solicita su renovación nuevamente, **Then** la respuesta es 422 con código `POLIZA_NO_RENOVABLE` (no se puede renovar dos veces).
7. **Given** un id de póliza inexistente, **When** se solicita su renovación, **Then** la respuesta es 404 con código `NO_ENCONTRADO`.

---

### User Story 2 - Botón "Renovar" en el detalle de la póliza (Priority: P2)

Desde la pantalla de detalle de una póliza, el operador ve un botón "Renovar" solo cuando la póliza es renovable. Al confirmar, la interfaz llama a la API, informa el resultado (número y prima de la póliza nueva) y lleva al detalle de la póliza nueva. Si la API rechaza la operación, muestra el mensaje de error.

**Why this priority**: Hace usable la feature para operadores sin conocimientos técnicos, pero depende de que la API (P1) exista.

**Independent Test**: Con el backend corriendo, abrir el detalle de POL-HOG-0002, pulsar "Renovar", confirmar y verificar que se navega a la póliza nueva y que la original aparece como "Renovada" en el listado.

**Acceptance Scenarios**:

1. **Given** el detalle de una póliza VIGENTE, **When** se carga la pantalla, **Then** se muestra el botón "Renovar".
2. **Given** el detalle de una póliza VENCIDA dentro del período de gracia, **When** se carga la pantalla, **Then** se muestra el botón "Renovar" junto al aviso de período de gracia.
3. **Given** el detalle de una póliza CANCELADA, RENOVADA o VENCIDA fuera del período de gracia, **When** se carga la pantalla, **Then** no se muestra el botón "Renovar".
4. **Given** el operador pulsa "Renovar", **When** cancela el diálogo de confirmación, **Then** no se realiza ninguna llamada a la API.
5. **Given** el operador confirma la renovación, **When** la API responde 201, **Then** se muestra un mensaje de éxito con el número y la prima de la póliza nueva y se navega a su detalle.
6. **Given** el operador confirma la renovación, **When** la API responde con error (422), **Then** se muestra el mensaje devuelto por la API y la pantalla permanece en el detalle actual.
7. **Given** el listado de pólizas, **When** se filtra por estado, **Then** la opción "RENOVADA" está disponible y las pólizas renovadas se muestran con la etiqueta "Renovada".

---

### User Story 3 - Continuidad de cobertura de la póliza renovada (Priority: P3)

Una póliza VIGENTE renovada antes de su vencimiento queda RENOVADA pero sigue cubriendo hasta su fecha de fin. El operador puede seguir registrando siniestros ocurridos dentro de su vigencia.

**Why this priority**: Evita una pérdida de cobertura que sería un problema legal y de servicio, pero solo afecta a renovaciones anticipadas, que son la minoría de los casos.

**Independent Test**: Renovar POL-AUT-0001 (VIGENTE) y registrar con `POST /api/siniestros` un siniestro con fecha de ocurrencia de hoy sobre la póliza original: debe aceptarse (201).

**Acceptance Scenarios**:

1. **Given** una póliza RENOVADA cuya `fecha_fin` aún no pasó, **When** se registra un siniestro con fecha de ocurrencia dentro de su vigencia, **Then** el siniestro se registra (201) sobre la póliza original.
2. **Given** una póliza RENOVADA, **When** se registra un siniestro con fecha de ocurrencia posterior a su `fecha_fin`, **Then** la respuesta es 422 con código `FUERA_DE_VIGENCIA`.
3. **Given** una póliza CANCELADA o VENCIDA, **When** se registra un siniestro, **Then** la respuesta sigue siendo 422 con código `POLIZA_NO_VIGENTE` (comportamiento actual sin cambios).

---

### Edge Cases

- Póliza VENCIDA exactamente hace 30 días: **es renovable** (el límite es inclusive, igual que el período de gracia RN-POL-06).
- Póliza VENCIDA hace 31 días: no renovable (`PLAZO_RENOVACION_VENCIDO`).
- Siniestro ocurrido exactamente hace 12 meses: **no** cuenta (la ventana es "posterior a hoy − 12 meses", RN-SIN-06).
- Póliza con siniestros recientes todos `RECHAZADO`: no lleva recargo.
- Póliza con varios siniestros recientes: el recargo es único (10 %), no acumulativo.
- Póliza VIDA cuyo cliente supera los 70 años a la nueva `fecha_inicio`: 422 con código `EDAD_MAXIMA_RENOVACION`.
- Dos solicitudes de renovación simultáneas sobre la misma póliza: solo una debe crear póliza nueva; la otra recibe 422 `POLIZA_NO_RENOVABLE`.
- Renovación de una póliza que a su vez es renovación de otra: permitida si cumple las reglas; cada póliza referencia solo a su origen inmediato.
- Id no numérico en la ruta (`/api/polizas/abc/renovacion`): 400 `SOLICITUD_INVALIDA` (comportamiento actual del manejador global).
- Clientes de la API existentes que no conocen el estado RENOVADA: lo reciben como un valor de texto más; ningún campo existente cambia.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST exponer `POST /api/polizas/{id}/renovacion` (sin cuerpo) que renueva la póliza indicada y responde 201 con el detalle de la póliza nueva y el header `Location: /api/polizas/{nuevoId}`.
- **FR-002**: El sistema MUST permitir la renovación solo si la póliza está VIGENTE, o VENCIDA con `fecha_fin` a 30 días o menos de la fecha actual (inclusive). En otro caso MUST responder 422: `POLIZA_NO_RENOVABLE` (CANCELADA o RENOVADA) o `PLAZO_RENOVACION_VENCIDO` (VENCIDA hace más de 30 días).
- **FR-003**: La póliza nueva MUST tener el mismo cliente, tipo y suma asegurada que la original, estado VIGENTE, `fecha_inicio` = `fecha_fin` original + 1 día y `fecha_fin` = `fecha_fin` original + 1 año.
- **FR-004**: La prima anual de la póliza nueva MUST ser igual a la prima anual de la original, incrementada un 10 % si la póliza original tuvo al menos un siniestro no `RECHAZADO` con fecha de ocurrencia en los últimos 12 meses; redondeo a 2 decimales HALF_UP.
- **FR-005**: El sistema MUST marcar la póliza original con el nuevo estado RENOVADA en la misma transacción en que crea la póliza nueva (o se hacen ambas cosas o ninguna).
- **FR-006**: La póliza nueva MUST registrar una referencia a la póliza de origen, expuesta en la respuesta como campo nuevo `polizaOrigenId` (nulo para pólizas no renovadas).
- **FR-007**: Para pólizas VIDA, el sistema MUST rechazar la renovación con 422 `EDAD_MAXIMA_RENOVACION` si el cliente tiene más de 70 años a la `fecha_inicio` de la nueva vigencia.
- **FR-008**: El número de la póliza nueva MUST seguir el formato existente `POL-<TIP>-<nnnn>` (RN-POL-05).
- **FR-009**: Los contratos existentes MUST permanecer sin cambios: ningún endpoint, parámetro, campo o código de estado actual se renombra ni elimina. Solo se agregan el endpoint de FR-001, el valor RENOVADA del estado y el campo `polizaOrigenId`.
- **FR-010**: `GET /api/polizas` MUST aceptar `estado=RENOVADA` como filtro válido.
- **FR-011**: La interfaz MUST mostrar el botón "Renovar" en el detalle solo para pólizas VIGENTE o VENCIDA en período de gracia, pedir confirmación, y tras el éxito informar número y prima de la póliza nueva y navegar a su detalle; ante error MUST mostrar el mensaje de la API.
- **FR-012**: La interfaz MUST mostrar el estado RENOVADA con la etiqueta "Renovada" en listados y detalle, e incluirlo en el filtro de estado.
- **FR-013**: El registro de siniestros MUST aceptar pólizas RENOVADA cuando la fecha de ocurrencia está dentro de su vigencia; el resto de las reglas de registro de siniestros (RN-SIN-01 a RN-SIN-04) no cambia para los demás estados.
- **FR-014**: Las reglas nuevas MUST documentarse en `docs/REGLAS_NEGOCIO.md` con identificadores RN, y el nuevo estado y columna en `docs/DATABASE.md`.

### Key Entities *(include if feature involves data)*

- **Póliza**: se agrega el estado RENOVADA (estado final, como CANCELADA, pero con cobertura vigente hasta su `fecha_fin`) y una referencia opcional a la póliza de origen de la que es renovación.
- **Siniestro**: sin cambios de estructura; se usa para determinar la siniestralidad de los últimos 12 meses (excluyendo RECHAZADO).
- **Cliente**: sin cambios; su fecha de nacimiento se usa para la regla de edad máxima de renovación en VIDA.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un operador renueva una póliza elegible desde el detalle en menos de 30 segundos y con no más de 2 acciones (pulsar "Renovar" y confirmar).
- **SC-002**: El 100 % de las renovaciones de pólizas con siniestros no rechazados en los últimos 12 meses aplica el recargo del 10 %, y el 0 % de las demás lo aplica (verificable con los 6 casos de los datos de demostración).
- **SC-003**: El 0 % de las solicitudes sobre pólizas no elegibles (CANCELADA, RENOVADA, VENCIDA hace más de 30 días) produce una póliza nueva.
- **SC-004**: La totalidad de los tests existentes previos a la feature sigue pasando sin modificaciones (cero regresiones de contrato).
- **SC-005**: Ninguna renovación anticipada deja al cliente sin cobertura: entre la `fecha_fin` original y la `fecha_inicio` nueva hay exactamente 1 día de diferencia en el 100 % de los casos.

## Assumptions

- Los usuarios son operadores internos de la aseguradora; la autenticación y autorización están fuera del alcance (igual que en el resto de la aplicación).
- El cobro de la prima de renovación y la emisión de documentos al cliente están fuera del alcance.
- Las reglas de plazo (30 días), recargo (10 %, ventana de 12 meses, excluye RECHAZADO) y edad máxima VIDA (70) provienen del catálogo corporativo (MCP `catalogo-coberturas`, versión 2025.1) y se implementan como constantes; la aplicación no consulta el catálogo en tiempo de ejecución.
- La transición automática VIGENTE → VENCIDA sigue a cargo del proceso nocturno externo; esta feature no la modifica.
- Las pólizas RENOVADA no pueden cancelarse (la cancelación sigue permitida solo para VIGENTE).
