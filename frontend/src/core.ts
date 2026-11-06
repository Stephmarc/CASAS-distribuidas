import Keycloak from 'keycloak-js';

declare global {
  interface Window {
    CASAS_CONFIG?: {api?: string; keycloak?: string};
  }
}

export const API = window.CASAS_CONFIG?.api || 'http://localhost:28080/api/v1';
export const KEYCLOAK_URL = window.CASAS_CONFIG?.keycloak || 'http://localhost:28090';
export const GRAFANA_URL = 'http://localhost:23000';

/** Datos de cobro mostrados al cliente (reemplazar por los reales de la empresa). */
export const COBRO = {
  empresa: 'CASAS S.A.C.',
  yape: '999 000 111',
  plin: '999 000 111',
  banco: 'BCP',
  cuenta: '191-0000000-0-00',
  cci: '002-191-000000000000-00',
};

export const money = (n: number | null | undefined) =>
  'S/ ' + new Intl.NumberFormat('es-PE', {maximumFractionDigits: 0}).format(Number(n || 0));

export type Tone = 'good' | 'warn' | 'bad' | 'info' | 'neutral';

const L = (m: Record<string, string>) => (k: string | null | undefined) => (k && m[k]) || (k || '').replaceAll('_', ' ').toLowerCase();

export const labelOrden = L({
  VALIDANDO_RESERVA: 'Validando reserva', BLOQUEANDO: 'Reservando inmueble', ESPERANDO_PAGO: 'Esperando pago',
  GENERANDO_CONTRATO: 'Preparando contrato', CONFIRMANDO_PROPIEDAD: 'Confirmando inmueble',
  CONVIRTIENDO_RESERVA: 'Cerrando reserva', CONFIRMADA: 'Confirmada', CANCELANDO_CONTRATO: 'Cancelando',
  REEMBOLSANDO_PAGO: 'Cancelando', LIBERANDO_PROPIEDAD: 'Cancelando', CANCELANDO_RESERVA: 'Cancelando', CANCELADA: 'Cancelada',
});
export const labelVisita = L({
  SOLICITADA: 'Por confirmar', CONFIRMADA: 'Confirmada', REPROGRAMADA: 'Reprogramada', REALIZADA: 'Realizada',
  CANCELADA: 'Cancelada', NO_ASISTIO: 'No asistió',
});
export const labelPago = L({
  CREADO: 'Creado', PENDIENTE: 'Pendiente', APROBADO: 'Aprobado', RECHAZADO: 'Rechazado', CANCELADO: 'Cancelado',
  REEMBOLSADO: 'Reembolsado',
});
export const labelContrato = L({
  BORRADOR: 'Borrador', GENERADO: 'En preparación', PENDIENTE_ACEPTACION: 'Por aceptar', ACEPTADO: 'Aceptado',
  VIGENTE: 'Vigente', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado',
});
export const labelProp = L({
  BORRADOR: 'Borrador', PUBLICADA: 'Publicada', DISPONIBLE: 'Disponible', RESERVADA: 'Reservada', EN_PROCESO: 'En proceso',
  VENDIDA: 'Vendida', ALQUILADA: 'Alquilada', INACTIVA: 'Inactiva',
});
export const labelMetodo = L({YAPE: 'Yape', PLIN: 'Plin', TRANSFERENCIA: 'Transferencia', DEPOSITO: 'Depósito'});

const T = (good: string[], warn: string[], bad: string[], info: string[] = []) => (k: string): Tone =>
  good.includes(k) ? 'good' : warn.includes(k) ? 'warn' : bad.includes(k) ? 'bad' : info.includes(k) ? 'info' : 'neutral';
export const toneOrden = T(['CONFIRMADA'], ['ESPERANDO_PAGO'], ['CANCELADA', 'CANCELANDO_CONTRATO', 'REEMBOLSANDO_PAGO', 'LIBERANDO_PROPIEDAD', 'CANCELANDO_RESERVA'], ['VALIDANDO_RESERVA', 'BLOQUEANDO', 'GENERANDO_CONTRATO', 'CONFIRMANDO_PROPIEDAD', 'CONVIRTIENDO_RESERVA']);
export const toneVisita = T(['CONFIRMADA', 'REALIZADA'], ['SOLICITADA', 'REPROGRAMADA'], ['CANCELADA', 'NO_ASISTIO']);
export const tonePago = T(['APROBADO'], ['PENDIENTE', 'CREADO'], ['RECHAZADO', 'CANCELADO', 'REEMBOLSADO']);
export const toneContrato = T(['ACEPTADO', 'VIGENTE', 'FINALIZADO'], ['GENERADO', 'PENDIENTE_ACEPTACION', 'BORRADOR'], ['CANCELADO']);
export const toneProp = T(['DISPONIBLE', 'PUBLICADA'], ['RESERVADA', 'EN_PROCESO', 'BORRADOR'], ['INACTIVA'], ['VENDIDA', 'ALQUILADA']);

/** Adelanto de reserva: 1 % del precio en compra, un mes en alquiler. */
export const deposit = (p: any) => Math.round(p.tipoOperacion === 'ALQUILER' ? p.precio : p.precio * 0.01);
export const RESERVA_HORAS = 24;

export const initials = (n: string) =>
  (n || '?').split(/\s+/).filter(Boolean).slice(0, 2).map(x => x[0]?.toUpperCase()).join('') || '?';

export function imageOf(p: any): string {
  return p?.imagenPrincipalUrl?.startsWith('https://') ? p.imagenPrincipalUrl : '';
}

export const kc = new Keycloak({url: KEYCLOAK_URL, realm: 'casa-o-nada', clientId: 'casa-o-nada-web'});

export class ApiError extends Error {
  constructor(message: string, public status: number) {
    super(message);
  }
}

/** Llamada autenticada al gateway. */
export async function http<T = any>(path: string, method = 'GET', body?: any, key?: string): Promise<T> {
  const h: Record<string, string> = {'X-Trace-ID': crypto.randomUUID()};
  if (kc.authenticated) {
    try {
      await kc.updateToken(30);
    } catch {
      throw new ApiError('Tu sesión expiró. Vuelve a ingresar.', 401);
    }
    h['Authorization'] = 'Bearer ' + kc.token;
  }
  if (body !== undefined) h['Content-Type'] = 'application/json';
  if (key) h['X-Idempotency-Key'] = key;
  const ctl = new AbortController();
  const timer = setTimeout(() => ctl.abort(), 20000);
  try {
    const r = await fetch(API + path, {method, headers: h, body: body === undefined ? undefined : JSON.stringify(body), signal: ctl.signal});
    if (!r.ok) {
      let b: any = {};
      try {
        b = await r.json();
      } catch { /* sin cuerpo */ }
      throw new ApiError(
        r.status === 401 ? 'Tu sesión expiró. Vuelve a ingresar.'
          : r.status === 403 ? 'No tienes permiso para esta operación.'
          : b.message || b.detail || b.error || 'No se pudo completar la operación (HTTP ' + r.status + ').',
        r.status);
    }
    if (r.status === 204) return null as T;
    const text = await r.text();
    return (text ? JSON.parse(text) : null) as T;
  } catch (e: any) {
    if (e?.name === 'AbortError') throw new ApiError('El servidor tardó demasiado en responder. Actualiza e inténtalo de nuevo.', 0);
    if (e instanceof ApiError) throw e;
    throw new ApiError('No hay conexión con el servidor.', 0);
  } finally {
    clearTimeout(timer);
  }
}

/** Descarga un archivo protegido (captura de pago) como URL local. */
export async function blobUrl(path: string): Promise<string> {
  await kc.updateToken(30);
  const r = await fetch(API + path, {headers: {Authorization: 'Bearer ' + kc.token}});
  if (!r.ok) throw new ApiError('No se pudo cargar el archivo.', r.status);
  return URL.createObjectURL(await r.blob());
}

export function fileToBase64(file: File): Promise<string> {
  return new Promise((ok, fail) => {
    const r = new FileReader();
    r.onload = () => ok(String(r.result).split(',')[1] || '');
    r.onerror = () => fail(new Error('No se pudo leer el archivo.'));
    r.readAsDataURL(file);
  });
}

export function downloadCsv(name: string, rows: Record<string, any>[]) {
  if (!rows.length) return;
  const cols = Object.keys(rows[0]);
  const esc = (v: any) => '"' + String(v ?? '').replaceAll('"', '""') + '"';
  const csv = [cols.join(','), ...rows.map(r => cols.map(c => esc(r[c])).join(','))].join('\n');
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob(['﻿' + csv], {type: 'text/csv;charset=utf-8'}));
  a.download = name;
  a.click();
}

export const CIUDADES_FALLBACK = ['Lima', 'Arequipa', 'Cusco', 'Trujillo', 'Piura', 'Chiclayo', 'Huancayo', 'Juliaca', 'Tacna', 'Iquitos'];
