# Feature Specification: Renovación de póliza

**Feature Branch**: `001-renovacion-poliza`

**Created**: 2026-10-20

**Status**: Draft

**Input**: User description: "Renovación de póliza. Un operador puede renovar una póliza que está VIGENTE o que está VENCIDA hace 30 días o menos. La renovación crea una póliza nueva del mismo cliente y tipo, con vigencia de un año a partir del fin de la póliza original. La prima se ajusta según la regla de siniestralidad del catálogo corporativo (si hubo siniestros en los últimos 12 meses). La póliza original queda marcada como renovada y no se puede renovar dos veces. Desde el detalle de la póliza en la web debe haber una acción "Renovar" que muestre la prima nueva antes de confirmar. No debe cambiar el comportamiento ni el formato de respuesta de los endpoints existentes. Fuera de alcance: cobro, envío de notificaciones y renovación masiva."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Renovar una póliza elegible (Priority: P1)

Un operador abre el detalle de una póliza que está vigente, o vencida hace 30 días o menos, y que todavía no fue renovada. Elige **Renovar**, ve la prima anual de la póliza nueva y la vigencia que va a crear, y confirma. El sistema crea una sola póliza nueva para el mismo cliente y el mismo tipo, y deja la original marcada como renovada.

**Why this priority**: Es el motivo de la función. Sin este recorrido el operador no puede continuar la cobertura de un cliente.

**Independent Test**: Con una póliza vigente no renovada, el operador completa la vista previa y la confirmación, y queda una póliza nueva vinculada a la original.

**Acceptance Scenarios**:

1. **Given** una póliza vigente que no fue renovada, **When** el operador elige **Renovar** en su detalle, **Then** ve la prima anual nueva y la vigencia nueva, y todavía no existe una póliza nueva ni la original queda marcada como renovada.
2. **Given** esa vista previa, **When** el operador confirma, **Then** se crea una póliza nueva vigente, del mismo cliente y tipo, con la misma suma asegurada, un número nuevo en el formato ya usado, prima igual a la mostrada y vigencia de un año que empieza el día de fin de la original. La original conserva su estado y queda marcada como renovada, con referencia a la póliza nueva.
3. **Given** una póliza vencida cuyo fin fue hoy o hace hasta 30 días, y que no fue renovada, **When** el operador confirma la renovación, **Then** el resultado es el mismo que en el escenario anterior: la vigencia nueva empieza en la fecha de fin de la original, aunque esa fecha ya haya pasado.
4. **Given** la vista previa abierta, **When** el operador no confirma, **Then** no se crea ninguna póliza y la original sigue sin marca de renovada.
5. **Given** una póliza ya renovada, **When** el operador abre su detalle, **Then** ve que fue renovada y cuál es la póliza nueva, y no puede renovarla otra vez.
6. **Given** la póliza nueva, **When** el operador abre su detalle, **Then** ve que continúa a la póliza original y que ella misma no está marcada como renovada.
7. **Given** las consultas y gestiones que ya existían, **When** se usan sobre las mismas pólizas de demostración sin ejecutar **Renovar**, **Then** entregan la misma información, con el mismo significado, que antes de esta función.

---

### User Story 2 - Ver el ajuste de prima por siniestralidad (Priority: P2)

Antes de confirmar, el operador entiende si la prima nueva es la prima de emisión habitual o si incluye el recargo porque la póliza original tuvo siniestros que cuentan en los últimos 12 meses.

**Why this priority**: La prima es lo que el operador debe aceptar. El recargo cambia el monto y tiene que ser explicable antes de confirmar.

**Independent Test**: Se comparan dos pólizas del mismo tipo y suma asegurada, una sin siniestros que cuenten y otra con al menos uno, y la vista previa muestra montos distintos según la regla.

**Acceptance Scenarios**:

1. **Given** una póliza elegible sin siniestros que cuenten en los últimos 12 meses, **When** el operador pide renovarla, **Then** la prima mostrada es la misma que correspondería al emitir hoy ese tipo y esa suma asegurada, incluido el mínimo de 100,00 USD.
2. **Given** una póliza elegible con al menos un siniestro que cuenta, **When** el operador pide renovarla, **Then** la prima mostrada es esa prima de emisión más un 10 %, redondeada a centavos del mismo modo que al emitir, y la vista previa indica que el recargo aplica.
3. **Given** varios siniestros que cuentan sobre la misma póliza, **When** el operador pide renovarla, **Then** el recargo sigue siendo un único 10 %, no un 10 % por cada siniestro.
4. **Given** la prima mostrada, **When** el operador confirma en ese momento, **Then** la póliza nueva guarda exactamente esa prima.

---

### User Story 3 - Rechazar renovaciones no permitidas (Priority: P2)

El operador no puede renovar una póliza cancelada, una vencida hace más de 30 días, ni una que ya fue renovada. Si lo intenta, no se crea una póliza y recibe un motivo claro en español.

**Why this priority**: Evita coberturas indebidas y renovaciones duplicadas. Puede comprobarse sin depender del caso feliz más allá de tener pólizas en esos estados.

**Independent Test**: Se intenta renovar una póliza de cada caso no permitido y se verifica que no aparece una póliza nueva.

**Acceptance Scenarios**:

1. **Given** una póliza cancelada, **When** el operador intenta renovarla, **Then** no se crea una póliza nueva y el motivo indica que está cancelada.
2. **Given** una póliza vencida cuyo fin fue hace 31 días o más, **When** el operador intenta renovarla, **Then** no se crea una póliza nueva y el motivo indica que está fuera del plazo de 30 días.
3. **Given** una póliza ya renovada, **When** el operador intenta renovarla de nuevo, **Then** no se crea otra póliza y el motivo indica que ya fue renovada, aunque también esté vencida fuera de plazo.
4. **Given** una vista previa válida, **When** antes de confirmar la póliza deja de ser elegible o la prima que correspondería ya no es la mostrada, **Then** la confirmación no crea la póliza y el operador tiene que volver a ver la prima vigente.

---

### Edge Cases

- El día 30 después del fin de una póliza vencida todavía se puede renovar. El día 31, no.
- Una póliza vencida cuyo fin es hoy (hace 0 días) se puede renovar.
- Una póliza vigente se puede renovar aunque su fecha de fin sea futura, sea hoy o ya haya pasado. El plazo de 30 días se aplica solo al estado vencida. Esta función no cambia un estado vigente a vencida por las fechas.
- Si la original sigue vigente, su estado no pasa a otro por el hecho de renovarla. Sigue cubriendo hasta su propio fin. La nueva también queda vigente desde su creación, aunque su inicio sea futuro o pasado.
- El día de fin de la original queda incluido en las dos vigencias, porque la nueva empieza ese mismo día y el fin de la original es inclusivo.
- Un siniestro rechazado no dispara el recargo. Tampoco uno cuya ocurrencia es anterior al día que cae exactamente 12 meses antes de la renovación. Uno ocurrido ese día límite, o después, sí cuenta, si no está rechazado.
- Los siniestros siguen asociados a la póliza original. No se copian a la nueva.
- Confirmar dos veces la misma original, incluso por dos operadores a la vez, crea una sola póliza nueva.
- La póliza nueva puede renovarse más adelante, cuando ella misma cumpla las reglas. Esa renovación no vuelve a renovar la original.
- Cancelar, registrar siniestros o consultar lo ya existente sigue las reglas de hoy. Renovar no cobra, no avisa al cliente y no renueva otras pólizas del mismo cliente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema debe permitir renovar una póliza que está vigente y que aún no fue renovada.
- **FR-002**: El sistema debe permitir renovar una póliza vencida que aún no fue renovada cuando la fecha de renovación es la fecha de fin o cae dentro de los 30 días siguientes, inclusive.
- **FR-003**: El sistema debe rechazar la renovación de una póliza cancelada, de una vencida hace más de 30 días y de una ya renovada, sin crear una póliza nueva, e indicar el motivo en español. Si ya fue renovada, ese motivo prevalece sobre el de plazo o estado.
- **FR-004**: Desde el detalle de la póliza en la web, el operador debe poder iniciar **Renovar** cuando la póliza es elegible. Si no lo es, el detalle no debe completar una renovación y debe poder explicar el motivo.
- **FR-005**: Antes de confirmar, el operador debe ver la prima anual nueva, si incluye el recargo por siniestralidad, la suma asegurada, el cliente, el tipo y las fechas de inicio y fin de la póliza nueva. Hasta que confirme, no se crea la póliza ni se marca la original.
- **FR-006**: Al confirmar, el sistema debe crear una única póliza nueva, vigente, del mismo cliente y del mismo tipo, con la misma suma asegurada y con un número nuevo en el formato de numeración ya vigente.
- **FR-007**: La vigencia nueva debe durar un año: la fecha de inicio es la fecha de fin de la original y la fecha de fin es esa fecha de inicio más un año, con el mismo criterio de calendario que ya se usa al emitir. Esa fecha de inicio puede ser anterior a hoy cuando la original ya terminó.
- **FR-008**: La prima nueva, sin recargo, debe calcularse con la misma regla de emisión ya vigente para ese tipo y esa suma asegurada, incluido el mínimo de 100,00 USD y el redondeo a centavos.
- **FR-009**: Si la póliza original tiene al menos un siniestro no rechazado cuya fecha de ocurrencia está dentro de los últimos 12 meses (desde el día que cae exactamente 12 meses antes de la fecha de renovación, inclusive, hasta esa fecha), la prima nueva debe ser la prima del FR-008 más un 10 %, redondeada a centavos. El recargo se aplica una sola vez.
- **FR-010**: La prima guardada debe ser exactamente la que se mostró al confirmar. Si entre la vista previa y la confirmación la elegibilidad o la prima cambió, la confirmación no debe crear la póliza.
- **FR-011**: La póliza original debe conservar su estado y quedar marcada como renovada, vinculada a la póliza nueva, de modo que no pueda renovarse otra vez. La póliza nueva no nace marcada como renovada y su detalle identifica la póliza de origen.
- **FR-012**: Las consultas y gestiones ya disponibles (listar y ver pólizas, emitir, cancelar y registrar o consultar siniestros) deben conservar su comportamiento y la información que ya entregan: no se quita, no se renombra y no cambia el significado de ningún dato existente. La marca de renovación y el vínculo entre pólizas son información nueva de esta función.
- **FR-013**: Cada confirmación debe renovar solo la póliza elegida. La función no cobra la prima, no envía notificaciones y no ofrece renovación masiva.

### Key Entities *(include if feature involves data)*

- **Póliza original**: La que el operador renueva. Conserva cliente, tipo, suma asegurada, vigencia, estado y siniestros. Gana la marca de renovada y la referencia a la póliza nueva.
- **Póliza nueva**: La cobertura que continúa a la original. Mismo cliente, tipo y suma asegurada; número, prima y vigencia propios; estado vigente; referencia a la póliza de origen; sin marca de renovada y sin los siniestros de la original.
- **Siniestro**: Hecho ya registrado sobre la original. Solo aporta al recargo su estado y su fecha de ocurrencia. No se modifica ni se traslada por la renovación.
- **Vista previa de renovación**: El resumen que el operador acepta o abandona: prima, recargo, suma asegurada, cliente, tipo y vigencia nueva. No es una póliza hasta la confirmación.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un operador que ya tiene abierto el detalle de una póliza elegible completa la vista previa y la confirmación en menos de 2 minutos.
- **SC-002**: En la prueba de aceptación, el 100 % de las pólizas vigentes no renovadas y de las vencidas dentro de los 30 días quedan renovadas con una sola póliza nueva, el mismo cliente, el mismo tipo y la vigencia de un año definida en esta especificación.
- **SC-003**: En la prueba de aceptación, el 100 % de los intentos sobre pólizas canceladas, vencidas hace más de 30 días o ya renovadas terminan sin póliza nueva y con un motivo comprensible para el operador.
- **SC-004**: En un conjunto que incluya pólizas sin siniestros que cuenten, con un siniestro que cuenta, con varios, con solo rechazados y con siniestros fuera de los 12 meses, la prima mostrada y la prima guardada coinciden con la regla de esta especificación en el 100 % de los casos.
- **SC-005**: Después de incorporar la función, las consultas y gestiones que ya existían, ejercidas sobre el mismo conjunto de pólizas de demostración y sin usar **Renovar**, entregan la misma información que antes.

## Assumptions

- El actor es el operador que hoy consulta el detalle de una póliza. No se agrega un permiso distinto para renovar.
- La acción **Renovar** vive en el detalle. No se exige en el listado.
- La renovación no pide datos nuevos: no se cambia la suma asegurada ni se eligen coberturas. El operador solo revisa y confirma.
- "Un año" y "12 meses" de vigencia se resuelven como en la emisión: la fecha de fin es la fecha de inicio más un año calendario.
- La nueva empieza el día de fin de la original, como indica el catálogo corporativo ("desde la fecha de fin"). Ese día pertenece a las dos vigencias.
- El recargo es el del catálogo corporativo: 10 % si hubo al menos un siniestro no rechazado con ocurrencia dentro de los últimos 12 meses, contados sobre la póliza original. La ventana incluye el día que cae exactamente 12 meses antes. Los siniestros de otras pólizas del cliente no cuentan.
- El mínimo de 100,00 USD se aplica a la prima base. El 10 % se calcula después, sobre esa base ya ajustada al mínimo.
- Una póliza vigente cuyo fin ya pasó sigue siendo renovable mientras su estado siga siendo vigente. El proceso que pasa pólizas a vencidas queda fuera de esta función.
- La póliza nueva nace vigente aunque su inicio no sea hoy. Esta función no calcula el estado a partir de las fechas.
- No se aplican en esta función la edad máxima de renovación de vida, el examen médico ni los mínimos de suma asegurada del catálogo que hoy tampoco se aplican al emitir.
- Quedan fuera de alcance el cobro de la prima, el envío de notificaciones al cliente y la renovación de varias pólizas en una sola acción.
- Los clientes, pólizas y siniestros que se usen para probar esta función son ficticios.
