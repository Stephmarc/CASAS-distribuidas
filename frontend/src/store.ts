import {signal, computed} from '@angular/core';
import {API, kc, http, ApiError, deposit, RESERVA_HORAS, fileToBase64, Tone} from './core';

export interface Route {name: string; parts: string[]; q: Record<string, string>}
export interface Step {label: string; state: 'done' | 'current' | 'todo' | 'fail'}
export interface OpView {
  o: any; prop: any; reserva: any; pago: any; contrato: any; visita: any; cliente: any;
  steps: Step[]; headline: string; hint: string; tone: Tone; cta: '' | 'pagar' | 'contrato';
  cancelled: boolean; done: boolean; staffNote: string;
}
export interface Toast {id: number; kind: 'ok' | 'err' | 'info'; text: string}

const COMPENSANDO = ['CANCELANDO_CONTRATO', 'REEMBOLSANDO_PAGO', 'LIBERANDO_PROPIEDAD', 'CANCELANDO_RESERVA'];
const ROUTES: Record<string, string> = {'': 'home', propiedades: 'catalog', propiedad: 'detail', 'mi-espacio': 'account', panel: 'panel'};

function loadFavs(): number[] {
  try {
    return JSON.parse(localStorage.getItem('casas.favs') || '[]');
  } catch {
    return [];
  }
}

class Store {
  // ---- sesión
  ready = signal(false);
  logged = signal(false);
  roles = signal<string[]>([]);
  userName = signal('');
  userEmail = signal('');
  userSub = signal('');
  clientId = signal(0);
  isAdmin = computed(() => this.roles().includes('ADMIN'));
  isStaff = computed(() => this.isAdmin() || this.roles().includes('AGENTE'));
  isClient = computed(() => this.logged() && !this.isStaff());
  roleLabel = computed(() => (this.isAdmin() ? 'Administrador' : this.isStaff() ? 'Agente' : 'Cliente'));

  // ---- navegación
  route = signal<Route>({name: 'home', parts: [], q: {}});

  // ---- datos
  props = signal<any[]>([]);
  propsLoaded = signal(false);
  propsOk = signal(true);
  orders = signal<any[]>([]);
  visits = signal<any[]>([]);
  reservas = signal<any[]>([]);
  pagos = signal<any[]>([]);
  contratos = signal<any[]>([]);
  avisos = signal<any[]>([]);
  agentes = signal<any[]>([]);
  clientes = signal<any[]>([]);
  eventos = signal<Record<string, number>>({});
  perfil = signal<any>(null);
  favs = signal<number[]>(loadFavs());

  // ---- interfaz
  busy = signal(false);
  toasts = signal<Toast[]>([]);
  authModal = signal<'' | 'ingreso' | 'registro'>('');
  authReason = signal('');
  reserveFlow = signal<any>(null);
  payFor = signal<OpView | null>(null);
  visitFor = signal<any>(null);
  contractFor = signal<OpView | null>(null);
  opDetail = signal<OpView | null>(null);
  propEditor = signal<any>(null);
  agentModal = signal(false);
  confirmBox = signal<{title: string; text: string; ok: string; danger?: boolean; run: () => Promise<void>} | null>(null);

  // ---- derivados
  propById = computed(() => new Map(this.props().map(p => [p.id, p])));
  clientById = computed(() => new Map(this.clientes().map(c => [c.id, c])));
  agentById = computed(() => new Map(this.agentes().map(a => [a.id, a])));
  myAgent = computed(() => {
    const mail = this.userEmail().toLowerCase();
    return this.agentes().find(a => (a.keycloakUserId && a.keycloakUserId === this.userSub()) || a.email?.toLowerCase() === mail) || null;
  });
  unread = computed(() => this.avisos().filter(n => !n.leida).length);
  ops = computed<OpView[]>(() => [...this.orders()].sort((a, b) => b.id - a.id).map(o => this.build(o)));

  reservasSinOrden = computed(() => this.reservas().filter(r => ['PENDIENTE', 'ACTIVA'].includes(r.estado) && !this.orders().some(o => o.reservaId === r.id)));
  pagosPorRevisar = computed(() => this.pagos().filter(p => p.estado === 'PENDIENTE' && p.comprobante));
  visitasPorConfirmar = computed(() => this.visits().filter(v => v.estado === 'SOLICITADA'));
  contratosPorEnviar = computed(() => this.contratos().filter(c => c.estado === 'GENERADO'));
  contratosPorActivar = computed(() => this.contratos().filter(c => c.estado === 'ACEPTADO'));
  sinPago = computed(() => this.ops().filter(x => x.o.estado === 'ESPERANDO_PAGO' && !(x.pago?.comprobante)));
  pendientes = computed(
    () => this.pagosPorRevisar().length + this.visitasPorConfirmar().length + this.contratosPorEnviar().length + this.contratosPorActivar().length);

  private seq = 1;
  private timer: any;

  // =============== arranque
  async init() {
    window.addEventListener('hashchange', () => this.readRoute());
    this.readRoute();
    this.loadProps();
    try {
      const ok = await kc.init({onLoad: 'check-sso', responseMode: 'query', pkceMethod: 'S256', checkLoginIframe: false});
      this.logged.set(ok);
      if (ok) {
        this.applyToken();
        await this.loadAccount();
        const r = this.route();
        if (r.name === 'home' && this.isStaff() && sessionStorage.getItem('casas.staffHome') !== '1') {
          sessionStorage.setItem('casas.staffHome', '1');
          location.hash = '#/panel';
        }
        this.readRoute();
      }
    } catch {
      this.toast('err', 'El inicio de sesión no está disponible. Comprueba que Keycloak esté encendido.');
    }
    this.ready.set(true);
    this.timer = setInterval(() => {
      if (document.hidden) return;
      if (this.logged()) this.loadAccount(true);
      this.loadProps(true);
    }, 8000);
  }

  private applyToken() {
    const t: any = kc.tokenParsed || {};
    this.roles.set(kc.realmAccess?.roles || []);
    this.userName.set(String(t['given_name'] || t['preferred_username'] || ''));
    this.userEmail.set(String(t['email'] || ''));
    this.userSub.set(String(t['sub'] || ''));
    this.clientId.set(Number(t['cliente_id'] || 0));
  }

  login(path = '') {
    sessionStorage.removeItem('casas.staffHome');
    kc.login({redirectUri: location.origin + '/#/' + path}).catch(() => this.toast('err', 'No se pudo abrir el inicio de sesión.'));
  }

  logout() {
    sessionStorage.removeItem('casas.staffHome');
    kc.logout({redirectUri: location.origin + '/'});
  }

  requireLogin(reason: string): boolean {
    if (this.logged()) return true;
    this.authReason.set(reason);
    this.authModal.set('ingreso');
    return false;
  }

  // =============== rutas
  readRoute() {
    const raw = location.hash.replace(/^#\/?/, '');
    const [path, query = ''] = raw.split('?');
    const parts = path.split('/').filter(Boolean);
    const q: Record<string, string> = {};
    new URLSearchParams(query).forEach((v, k) => (q[k] = v));
    let name = ROUTES[parts[0] || ''] || 'home';
    const rest = parts.slice(1);
    if (this.ready() || this.logged()) {
      if (name === 'panel' && !this.isStaff()) {
        name = this.logged() ? 'account' : 'home';
      } else if (name === 'account' && !this.logged()) {
        name = 'home';
      } else if (name === 'account' && this.isStaff()) {
        name = 'panel';
      }
    }
    const prev = this.route();
    this.route.set({name, parts: rest, q});
    if (prev.name !== name || prev.parts[0] !== rest[0]) window.scrollTo({top: 0});
  }

  go(path: string) {
    location.hash = '#/' + path;
  }

  // =============== datos
  async loadProps(silent = false) {
    try {
      this.props.set(await http('/propiedades'));
      this.propsOk.set(true);
    } catch {
      if (!silent) this.propsOk.set(false);
    } finally {
      this.propsLoaded.set(true);
    }
  }

  async loadAccount(silent = false) {
    if (!this.logged()) return;
    try {
      const [o, v, p, c, n, r] = await Promise.all(
        ['/ordenes', '/visitas', '/pagos', '/contratos', '/notificaciones', '/reservas'].map(x => http<any[]>(x)));
      this.orders.set(o);
      this.visits.set(v);
      this.pagos.set(p);
      this.contratos.set(c);
      this.avisos.set(n);
      this.reservas.set(r);
      const [ag, cl] = await Promise.all([
        this.isStaff() ? http<any[]>('/agentes').catch(() => []) : Promise.resolve([]),
        this.isStaff() ? http<any[]>('/clientes').catch(() => []) : Promise.resolve([]),
      ]);
      if (this.isStaff()) {
        this.agentes.set(ag);
        this.clientes.set(cl);
      } else if (this.clientId() && !this.perfil()) {
        this.perfil.set(await http('/clientes/' + this.clientId()).catch(() => null));
      }
      // refresca los paneles abiertos
      const od = this.opDetail();
      if (od) this.opDetail.set(this.ops().find(x => x.o.id === od.o.id) || null);
    } catch (e: any) {
      if (!silent) this.toast('err', e.message);
    }
  }

  async loadEventos() {
    try {
      this.eventos.set(await http('/operaciones/eventos'));
    } catch (e: any) {
      this.toast('err', e.message);
    }
  }

  // =============== utilidades de interfaz
  toast(kind: Toast['kind'], text: string) {
    const id = this.seq++;
    this.toasts.update(t => [...t, {id, kind, text}]);
    setTimeout(() => this.dismiss(id), kind === 'err' ? 8000 : 5000);
  }

  dismiss(id: number) {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }

  /** Ejecuta una acción evitando doble envío y muestra el resultado. */
  async act(fn: () => Promise<any>, ok?: string): Promise<boolean> {
    if (this.busy()) return false;
    this.busy.set(true);
    try {
      await fn();
      if (ok) this.toast('ok', ok);
      return true;
    } catch (e: any) {
      this.toast('err', e instanceof ApiError || e instanceof Error ? e.message : 'Servicio no disponible.');
      return false;
    } finally {
      this.busy.set(false);
    }
  }

  confirm(title: string, text: string, ok: string, run: () => Promise<void>, danger = false) {
    this.confirmBox.set({title, text, ok, danger, run});
  }

  isFav(id: number) {
    return this.favs().includes(id);
  }

  toggleFav(id: number) {
    this.favs.update(f => (f.includes(id) ? f.filter(x => x !== id) : [...f, id]));
    try {
      localStorage.setItem('casas.favs', JSON.stringify(this.favs()));
    } catch { /* almacenamiento no disponible */ }
  }

  // =============== operaciones (vista unificada)
  private build(o: any): OpView {
    const prop = this.propById().get(o.propiedadId);
    const reserva = this.reservas().find(r => r.id === o.reservaId);
    const pago = this.pagos().filter(p => p.ordenId === o.id).sort((a, b) => b.id - a.id)[0];
    const contrato = this.contratos().find(c => c.ordenId === o.id);
    const visita = this.visits().filter(v => v.propiedadId === o.propiedadId && v.clienteId === o.clienteId).sort((a, b) => b.id - a.id)[0];
    const cliente = this.clientById().get(o.clienteId);
    const est: string = o.estado;
    const labels = ['Reserva', 'Pago', 'Verificación', 'Contrato', 'Listo'];
    let cur = 0, fail = false, headline = '', hint = '', tone: Tone = 'info', cta: OpView['cta'] = '', staffNote = '';
    if (est === 'CANCELADA' || COMPENSANDO.includes(est)) {
      fail = true;
      cur = pago?.comprobante ? 2 : pago ? 1 : 0;
      headline = est === 'CANCELADA' ? 'Operación cancelada' : 'Cancelando la operación';
      hint = o.motivo || 'La reserva se canceló y el inmueble volvió a estar disponible.';
      tone = 'bad';
      staffNote = 'Cancelada';
    } else if (est === 'VALIDANDO_RESERVA' || est === 'BLOQUEANDO') {
      headline = 'Reservando el inmueble';
      hint = 'Esto toma solo unos segundos.';
      staffNote = 'Procesando reserva';
    } else if (est === 'ESPERANDO_PAGO') {
      if (pago?.estado === 'PENDIENTE' && pago.comprobante) {
        cur = 2;
        headline = 'Un agente está revisando tu pago';
        hint = 'Verificamos la captura y te avisamos aquí. Normalmente toma menos de un día.';
        staffNote = 'Pago por revisar';
      } else {
        cur = 1;
        tone = 'warn';
        cta = 'pagar';
        headline = pago?.motivoRevision ? 'Tu pago fue observado' : 'Falta enviar tu comprobante de pago';
        hint = pago?.motivoRevision
          ? 'Motivo: ' + pago.motivoRevision + '. Envía una nueva captura para continuar.'
          : 'Paga el adelanto con Yape, Plin o transferencia y sube la captura de la operación.';
        staffNote = pago?.motivoRevision ? 'Pago observado, esperando nueva captura' : 'Esperando que el cliente pague';
      }
    } else if (['GENERANDO_CONTRATO', 'CONFIRMANDO_PROPIEDAD', 'CONVIRTIENDO_RESERVA'].includes(est)) {
      cur = 3;
      headline = 'Pago aprobado';
      hint = 'Estamos preparando tu contrato.';
      staffNote = 'Generando contrato';
    } else if (est === 'CONFIRMADA') {
      const ce = contrato?.estado;
      if (!contrato || ce === 'GENERADO' || ce === 'BORRADOR') {
        cur = 3;
        headline = 'Pago aprobado: contrato en preparación';
        hint = 'Tu agente te enviará el contrato para que lo revises y aceptes.';
        staffNote = 'Enviar contrato al cliente';
      } else if (ce === 'PENDIENTE_ACEPTACION') {
        cur = 3;
        tone = 'warn';
        cta = 'contrato';
        headline = 'Tu contrato está listo';
        hint = 'Revisa las condiciones y acéptalas para continuar.';
        staffNote = 'Esperando aceptación del cliente';
      } else if (ce === 'ACEPTADO') {
        cur = 4;
        tone = 'good';
        headline = 'Contrato aceptado';
        hint = 'Tu agente coordinará la entrega y activará el contrato.';
        staffNote = 'Activar contrato';
      } else {
        cur = 5;
        tone = 'good';
        headline = prop?.tipoOperacion === 'ALQUILER' ? '¡Alquiler concretado!' : '¡Compra concretada!';
        hint = 'La operación está completa. Gracias por confiar en CASAS.';
        staffNote = ce === 'FINALIZADO' ? 'Finalizada' : 'Contrato vigente';
      }
    }
    const steps: Step[] = labels.map((label, i) => ({
      label,
      state: cur === 5 ? 'done' : i < cur ? 'done' : i === cur ? (fail ? 'fail' : 'current') : 'todo',
    }));
    return {o, prop, reserva, pago, contrato, visita, cliente, steps, headline, hint, tone, cta, cancelled: fail, done: cur === 5, staffNote};
  }

  // =============== cliente: registro y DNI
  async lookupDni(dni: string): Promise<{nombres: string; apellidos: string}> {
    if (!/^\d{8}$/.test(dni || '')) throw new Error('El DNI debe tener 8 dígitos.');
    const r = await fetch(API + '/clientes/dni/' + dni);
    const j: any = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(j.message || 'No se pudo consultar el DNI. Ingresa tus datos manualmente.');
    return {nombres: j.nombres, apellidos: j.apellidos};
  }

  async registrar(c: any, password: string): Promise<any> {
    const r = await fetch(API + '/clientes/registro', {
      method: 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({dni: c.dni, nombres: c.nombres, apellidos: c.apellidos, email: c.email, telefono: c.telefono || null, password}),
    });
    const j: any = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(j.message || 'No se pudo registrar al cliente.');
    return j;
  }

  // =============== visitas
  async scheduleVisit(p: any, fecha: string, nota: string, clienteId?: number) {
    return this.act(async () => {
      if (!p.agenteId) throw new Error('Este inmueble aún no tiene un agente asignado para coordinar visitas.');
      if (!fecha || new Date(fecha).getTime() < Date.now() + 3600000) throw new Error('Elige una fecha y hora futuras (con al menos una hora de anticipación).');
      await http('/visitas', 'POST', {
        clienteId: clienteId || undefined, propiedadId: p.id, agenteId: p.agenteId,
        fechaHora: new Date(fecha).toISOString(), observaciones: nota || 'Solicitud desde CASAS Web', estado: 'SOLICITADA',
      });
      this.visitFor.set(null);
      await this.loadAccount();
    }, 'Visita solicitada. Tu agente la confirmará pronto.');
  }

  // =============== reserva
  startReserve(p: any) {
    if (!this.requireLogin('Para reservar un inmueble necesitas una cuenta.')) return;
    if (p.estado !== 'DISPONIBLE') {
      this.toast('info', 'Este inmueble ya no está disponible.');
      return;
    }
    this.reserveFlow.set({p, step: this.isStaff() ? 'cliente' : 'visita', clienteId: null, tempPass: '', conVisita: null, fecha: '', nota: '', acepta: false, result: null});
  }

  rf(patch: any) {
    this.reserveFlow.update(f => (f ? {...f, ...patch} : f));
  }

  async confirmReserve() {
    const f = this.reserveFlow();
    if (!f) return;
    if (!f.acepta) {
      this.toast('err', 'Acepta las condiciones de la reserva para continuar.');
      return;
    }
    if (f.conVisita && (!f.fecha || new Date(f.fecha).getTime() < Date.now() + 3600000)) {
      this.toast('err', 'Elige una fecha y hora futuras para la visita.');
      return;
    }
    if (f.conVisita && !f.p.agenteId) {
      this.toast('err', 'Este inmueble aún no tiene agente asignado para visitas.');
      return;
    }
    let result: any;
    const ok = await this.act(async () => {
      const cid = this.isStaff() ? f.clienteId : undefined;
      if (f.conVisita)
        await http('/visitas', 'POST', {clienteId: cid, propiedadId: f.p.id, agenteId: f.p.agenteId, fechaHora: new Date(f.fecha).toISOString(), observaciones: f.nota || 'Visita previa a la reserva', estado: 'SOLICITADA'});
      const venceEn = new Date(Date.now() + RESERVA_HORAS * 3600000);
      const reserva = await http('/reservas', 'POST', {clienteId: cid, propiedadId: f.p.id, fechaExpiracion: venceEn.toISOString(), montoReserva: deposit(f.p), estado: 'PENDIENTE'});
      let orden: any = null;
      try {
        orden = await http('/ordenes', 'POST', {reservaId: reserva.id, moneda: 'PEN'}, 'reserva-' + reserva.id);
      } catch (e: any) {
        throw new Error('Tu reserva #' + reserva.id + ' quedó guardada, pero la operación no pudo iniciarse. Actualiza e inténtalo de nuevo. (' + e.message + ')');
      }
      result = {reserva, orden, venceEn};
    });
    if (ok) {
      this.rf({step: 'ok', result});
      this.loadProps();
      await this.loadAccount();
    }
  }

  async resumeReservation(r: any) {
    const ok = await this.act(() => http('/ordenes', 'POST', {reservaId: r.id, moneda: 'PEN'}, 'reserva-' + r.id), 'Tu reserva continúa. Ya puedes enviar tu pago.');
    if (ok) await this.loadAccount();
  }

  // =============== pago con captura
  openPay(op: OpView) {
    this.payFor.set(op);
  }

  async submitPay(op: OpView, form: {metodo: string; nro: string; file: File | null}) {
    if (!form.file) {
      this.toast('err', 'Adjunta la captura de tu pago.');
      return;
    }
    const ok = await this.act(async () => {
      let pago = op.pago && op.pago.estado === 'PENDIENTE' ? op.pago : null;
      if (!pago)
        pago = await http('/pagos', 'POST', {ordenId: op.o.id, monto: op.o.monto, moneda: 'PEN', descripcion: 'Adelanto inmobiliario', payerEmail: this.userEmail() || 'cliente@casa.local', paymentMethodId: 'sandbox', installments: 1}, 'web-pago-' + op.o.id);
      const datos = await fileToBase64(form.file!);
      await http('/pagos/' + pago.id + '/comprobante', 'POST', {metodo: form.metodo, nroOperacion: form.nro, tipo: form.file!.type, datos});
    }, 'Comprobante enviado. Un agente lo revisará en breve.');
    if (ok) {
      this.payFor.set(null);
      await this.loadAccount();
    }
  }

  async acceptContract(c: any) {
    const ok = await this.act(() => http('/contratos/' + c.id + '/aceptar', 'POST'), 'Contrato aceptado. ¡Felicitaciones!');
    if (ok) {
      this.contractFor.set(null);
      await this.loadAccount();
    }
  }

  cancelVisit(v: any) {
    this.confirm('¿Cancelar la visita?', 'Podrás agendar otra cuando quieras.', 'Cancelar visita', async () => {
      await this.act(() => http('/visitas/' + v.id + '/cancelar', 'POST'), 'Visita cancelada.');
      await this.loadAccount();
    }, true);
  }

  cancelOrder(op: OpView) {
    this.confirm('¿Cancelar la operación?', 'Se liberará el inmueble y se anulará la reserva. Esta acción no se puede deshacer.', 'Sí, cancelar operación', async () => {
      await this.act(() => http('/ordenes/' + op.o.id + '/cancelar', 'POST'), 'Operación cancelada.');
      await this.loadAccount();
      this.loadProps();
    }, true);
  }

  async readNotice(n: any) {
    if (n.leida) return;
    this.avisos.update(a => a.map(x => (x.id === n.id ? {...x, leida: true} : x)));
    try {
      await http('/notificaciones/' + n.id + '/leer', 'POST');
    } catch { /* se reintenta en la próxima carga */ }
  }

  async readAll() {
    for (const n of this.avisos().filter(x => !x.leida)) await this.readNotice(n);
  }

  // =============== personal de CASAS
  async reviewPago(p: any, accion: 'APROBAR' | 'OBSERVAR' | 'RECHAZAR', motivo = '') {
    const msg = {APROBAR: 'Pago aprobado. La operación avanza hacia el contrato.', OBSERVAR: 'Pago observado. El cliente deberá enviar otra captura.', RECHAZAR: 'Pago rechazado y operación cancelada.'}[accion];
    const ok = await this.act(() => http('/pagos/' + p.id + '/revisar', 'POST', {accion, motivo}), msg);
    if (ok) await this.loadAccount();
    return ok;
  }

  async visitState(v: any, estado: string) {
    const ok = await this.act(() => http('/visitas/' + v.id + '/estado', 'PATCH', {estado}), 'Visita actualizada.');
    if (ok) await this.loadAccount();
    return ok;
  }

  async rescheduleVisit(v: any, fecha: string) {
    if (!fecha || new Date(fecha).getTime() < Date.now()) {
      this.toast('err', 'Elige una fecha futura.');
      return false;
    }
    const ok = await this.act(() => http('/visitas/' + v.id, 'PUT', {clienteId: v.clienteId, propiedadId: v.propiedadId, agenteId: v.agenteId, fechaHora: new Date(fecha).toISOString(), observaciones: v.observaciones || 'Reprogramada por el agente', estado: 'REPROGRAMADA'}), 'Visita reprogramada.');
    if (ok) await this.loadAccount();
    return ok;
  }

  async contractState(c: any, estado: string, msg: string) {
    const ok = await this.act(() => http('/contratos/' + c.id + '/estado', 'PATCH', {estado}), msg);
    if (ok) await this.loadAccount();
    return ok;
  }

  async saveProp(draft: any, id: number | null) {
    const url = String(draft.imagenPrincipalUrl || '').trim();
    if (url && (!/^https:\/\/.+/i.test(url) || url.length > 500)) {
      this.toast('err', 'La imagen debe ser una URL HTTPS válida (máx. 500 caracteres).');
      return false;
    }
    const body = {...draft, imagenPrincipalUrl: url || null, agenteId: draft.agenteId || null, precio: Number(draft.precio), habitaciones: Number(draft.habitaciones), banos: Number(draft.banos), areaM2: Number(draft.areaM2)};
    const ok = await this.act(() => (id ? http('/propiedades/' + id, 'PUT', body) : http('/propiedades', 'POST', body)), id ? 'Inmueble actualizado.' : 'Inmueble publicado.');
    if (ok) {
      this.propEditor.set(null);
      await this.loadProps();
    }
    return ok;
  }

  async setPropState(p: any, estado: string) {
    const ok = await this.act(() => http('/propiedades/' + p.id + '/estado', 'PATCH', {estado}), 'Estado actualizado.');
    if (ok) await this.loadProps();
  }

  deleteProp(p: any) {
    this.confirm('¿Eliminar el inmueble?', '"' + p.titulo + '" dejará de mostrarse. Si tiene operaciones en curso, el sistema no lo permitirá.', 'Eliminar', async () => {
      await this.act(() => http('/propiedades/' + p.id, 'DELETE'), 'Inmueble eliminado.');
      await this.loadProps();
    }, true);
  }

  async createAgent(a: any) {
    const ok = await this.act(() => http('/agentes/acceso', 'POST', a), 'Agente creado. Ya puede ingresar con su correo y contraseña.');
    if (ok) await this.loadAccount();
    return ok;
  }

  async setAgentState(a: any, estado: string) {
    const ok = await this.act(() => http('/agentes/' + a.id + '/estado', 'PATCH', {estado}), estado === 'ACTIVO' ? 'Agente activado.' : 'Acceso del agente desactivado.');
    if (ok) await this.loadAccount();
  }

  async setClientActive(c: any, activo: boolean) {
    const ok = await this.act(() => http('/clientes/' + c.id + '/activo', 'PATCH', {activo}), activo ? 'Cliente activado.' : 'Acceso del cliente desactivado.');
    if (ok) await this.loadAccount();
  }
}

export const store = new Store();
