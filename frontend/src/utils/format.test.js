import { describe, expect, it } from 'vitest';
import { formatEstado, formatFecha, formatMoneda } from './format.js';

describe('formatFecha', () => {
  it('convierte yyyy-MM-dd a dd/MM/yyyy', () => {
    expect(formatFecha('2025-03-07')).toBe('07/03/2025');
  });

  it('devuelve guion largo para valores vacíos o inválidos', () => {
    expect(formatFecha(null)).toBe('—');
    expect(formatFecha('')).toBe('—');
    expect(formatFecha('07/03/2025')).toBe('—');
  });
});

describe('formatMoneda', () => {
  it('formatea montos con dos decimales', () => {
    const texto = formatMoneda(1234.5);
    expect(texto).toMatch(/1\.234,50/);
  });

  it('acepta strings numéricos', () => {
    expect(formatMoneda('800')).toMatch(/800,00/);
  });

  it('devuelve guion largo para valores nulos o no numéricos', () => {
    expect(formatMoneda(null)).toBe('—');
    expect(formatMoneda('abc')).toBe('—');
  });
});

describe('formatEstado', () => {
  it('traduce estados conocidos', () => {
    expect(formatEstado('EN_REVISION')).toBe('En revisión');
  });

  it('devuelve el valor original si no lo conoce', () => {
    expect(formatEstado('OTRO')).toBe('OTRO');
  });
});
