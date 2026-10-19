-- Datos de demostración. TODOS los datos son ficticios.
-- Las fechas son relativas a CURRENT_DATE para que los estados sean coherentes
-- sin importar el día en que se ejecute el laboratorio.

INSERT INTO clientes (nombre_completo, documento_identidad, email, telefono, fecha_nacimiento, fecha_registro) VALUES
('Ana Lucía Rivas',     'V-10000001', 'ana.rivas@ejemplo.com',     '+58 212 555 0101', DATE '1985-03-14', DATE '2019-06-01'),
('Carlos Eduardo Mora', 'V-10000002', 'carlos.mora@ejemplo.com',   '+58 212 555 0102', DATE '1962-11-02', DATE '2015-01-20'),
('Beatriz Salazar',     'V-10000003', 'beatriz.salazar@ejemplo.com', '+58 241 555 0103', DATE '1994-07-28', DATE '2022-09-15');

-- 1: AUTO vigente (con siniestro reciente)
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin) VALUES
('POL-AUT-0001', 1, 'AUTO', 'VIGENTE', 25000.00, 1000.00,
 DATEADD('DAY', -200, CURRENT_DATE), DATEADD('DAY', 165, CURRENT_DATE));
-- 2: HOGAR vigente
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin) VALUES
('POL-HOG-0002', 1, 'HOGAR', 'VIGENTE', 80000.00, 400.00,
 DATEADD('DAY', -90, CURRENT_DATE), DATEADD('DAY', 275, CURRENT_DATE));
-- 3: VIDA vencida hace 10 días
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin) VALUES
('POL-VID-0003', 2, 'VIDA', 'VENCIDA', 50000.00, 600.00,
 DATEADD('DAY', -375, CURRENT_DATE), DATEADD('DAY', -10, CURRENT_DATE));
-- 4: AUTO vencida hace 90 días
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin) VALUES
('POL-AUT-0004', 2, 'AUTO', 'VENCIDA', 18000.00, 720.00,
 DATEADD('DAY', -455, CURRENT_DATE), DATEADD('DAY', -90, CURRENT_DATE));
-- 5: HOGAR cancelada
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin, motivo_cancelacion) VALUES
('POL-HOG-0005', 3, 'HOGAR', 'CANCELADA', 60000.00, 300.00,
 DATEADD('DAY', -120, CURRENT_DATE), DATEADD('DAY', 245, CURRENT_DATE), 'Venta del inmueble');
-- 6: VIDA vigente, vence en 20 días
INSERT INTO polizas (numero, cliente_id, tipo, estado, suma_asegurada, prima_anual, fecha_inicio, fecha_fin) VALUES
('POL-VID-0006', 3, 'VIDA', 'VIGENTE', 40000.00, 480.00,
 DATEADD('DAY', -345, CURRENT_DATE), DATEADD('DAY', 20, CURRENT_DATE));

INSERT INTO siniestros (poliza_id, fecha_ocurrencia, fecha_reporte, descripcion, monto_reclamado, estado) VALUES
(1, DATEADD('DAY', -60, CURRENT_DATE),  DATEADD('DAY', -58, CURRENT_DATE),  'Choque leve en estacionamiento, daño en parachoques', 1200.00, 'APROBADO'),
(1, DATEADD('DAY', -15, CURRENT_DATE),  DATEADD('DAY', -14, CURRENT_DATE),  'Rotura de parabrisas', 450.00, 'EN_REVISION'),
(3, DATEADD('DAY', -370, CURRENT_DATE), DATEADD('DAY', -368, CURRENT_DATE), 'Gastos médicos por hospitalización', 3500.00, 'APROBADO'),
(4, DATEADD('DAY', -200, CURRENT_DATE), DATEADD('DAY', -199, CURRENT_DATE), 'Robo de accesorios del vehículo', 900.00, 'RECHAZADO');
