# Reglas de negocio

Reglas vigentes implementadas en la aplicación. Las reglas corporativas de suscripción y
renovación (catálogo oficial) se consultan en el servidor MCP `catalogo-coberturas`; si hay
diferencia entre este documento y el catálogo, **manda el catálogo** y se debe actualizar este documento.

## 1. Pólizas

### 1.1 Estados

```
             emisión
                │
                ▼
           ┌─────────┐   cancelación    ┌───────────┐
           │ VIGENTE │ ───────────────▶ │ CANCELADA │
           └─────────┘                  └───────────┘
                │ proceso nocturno
                │ (fecha_fin < hoy)
                ▼
           ┌─────────┐
           │ VENCIDA │
           └─────────┘
```

| Estado | Significado |
|--------|-------------|
| `VIGENTE` | La póliza cubre siniestros. |
| `VENCIDA` | Pasó su `fecha_fin`. No cubre siniestros nuevos. |
| `CANCELADA` | Terminada anticipadamente a pedido del cliente o de la aseguradora. Estado final. |

**RN-POL-01.** Una póliza nueva se emite siempre en estado `VIGENTE`.
**RN-POL-02.** La vigencia es de exactamente un año: `fecha_fin = fecha_inicio + 1 año`.
**RN-POL-03.** `fecha_inicio` no puede ser anterior a hoy.
**RN-POL-04.** Solo se pueden cancelar pólizas `VIGENTE`, indicando un motivo obligatorio (máx. 255 caracteres).
**RN-POL-05.** El número de póliza tiene el formato `POL-<3 primeras letras del tipo>-<correlativo de 4 dígitos>`.

### 1.2 Período de gracia

**RN-POL-06.** Una póliza `VENCIDA` está en *período de gracia* durante los **30 días** posteriores a su `fecha_fin`
(inclusive). La API lo informa en el campo `enPeriodoDeGracia` y el frontend muestra un aviso.
El período de gracia coincide con el plazo máximo de renovación del catálogo corporativo.

### 1.3 Cálculo de prima

**RN-POL-07.** Prima anual = `suma_asegurada × tasa base del tipo`, redondeada a 2 decimales (HALF_UP).

| Tipo | Tasa base |
|------|-----------|
| AUTO | 4,00 % |
| HOGAR | 0,50 % |
| VIDA | 1,20 % |

**RN-POL-08.** La prima anual mínima es **100,00 USD**.
**RN-POL-09.** La suma asegurada mínima para emitir es 1.000,00 USD (los mínimos por tipo del catálogo corporativo
todavía no se aplican en la emisión).

Ejemplo: AUTO con suma asegurada 20.000 → prima 800,00. HOGAR con 5.000 → 25,00 → se aplica mínimo 100,00.

## 2. Siniestros

| Estado | Significado |
|--------|-------------|
| `REPORTADO` | Recién registrado. Estado inicial. |
| `EN_REVISION` | Un analista lo está evaluando. |
| `APROBADO` | Se pagará. |
| `RECHAZADO` | No procede. |

**RN-SIN-01.** Solo se registran siniestros sobre pólizas `VIGENTE`.
**RN-SIN-02.** La `fecha_ocurrencia` debe estar dentro de la vigencia de la póliza y no puede ser futura.
**RN-SIN-03.** `monto_reclamado` debe ser mayor a 0 y no puede superar la `suma_asegurada` de la póliza.
**RN-SIN-04.** Todo siniestro nuevo se crea en estado `REPORTADO` con `fecha_reporte = hoy`.
**RN-SIN-05.** Las transiciones de estado de siniestros las hace el sistema de análisis (fuera de este repo).

### 2.1 Siniestralidad

**RN-SIN-06.** Se considera que una póliza "tuvo siniestros en los últimos 12 meses" si existe al menos un siniestro con
`fecha_ocurrencia` posterior a `hoy − 12 meses`. Disponible como `SiniestroService.contarUltimos12Meses(polizaId)`.
El catálogo corporativo precisa qué estados de siniestro cuentan para recargos.

## 3. Pendiente / no implementado

- Renovación de pólizas (ver catálogo corporativo, regla `renovacion`).
- Validación de edad para pólizas VIDA.
- Transiciones de estado de siniestros.
