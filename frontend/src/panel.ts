import {Component, computed, effect, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {store, OpView} from './store';
import {Ic, Badge} from './ui';
import {
  money, imageOf, initials, blobUrl, downloadCsv, GRAFANA_URL,
  labelOrden, toneOrden, labelVisita, toneVisita, labelPago, tonePago, labelContrato, toneContrato, labelProp, toneProp, labelMetodo,
} from './core';

interface NavItem {id: string; label: string; icon: string; count?: number}

@Component({
  selector: 'page-panel',
  standalone: true,
  imports: [DatePipe, FormsModule, Ic, Badge],
  template: `
  <div class="panel">
    <aside class="sidebar" [class.open]="menu()">
      <div class="side-user"><span class="avatar">{{ ini() }}</span><div><b>{{ s.userName() }}</b><small>{{ s.roleLabel() }}@if (s.myAgent(); as a) { · Mat. {{ a.matricula }} }</small></div></div>
      <nav aria-label="Panel">
        @for (n of nav(); track n.id) {<a [href]="'#/panel/' + n.id" [class.on]="sec() === n.id" (click)="menu.set(false)"><ic [n]="n.icon" [s]="18"/><span>{{ n.label }}</span>@if (n.count) {<em>{{ n.count }}</em>}</a>}
      </nav>
      <div class="side-foot"><a href="#/propiedades"><ic n="eye" [s]="16"/> Ver sitio público</a><button (click)="s.logout()"><ic n="logout" [s]="16"/> Cerrar sesión</button></div>
    </aside>

    <section class="panel-main">
      <button class="btn ghost sm menu-btn" (click)="menu.set(!menu())"><ic n="menu" [s]="16"/> Menú</button>

      @switch (sec()) {
      @case ('resumen') {
        <header class="ph"><div><p class="eyebrow">{{ s.isAdmin() ? 'Administración' : 'Panel del agente' }}</p><h1>{{ greeting() }}, {{ s.userName() }}</h1><p class="muted">{{ s.pendientes() ? 'Tienes ' + s.pendientes() + ' tareas que requieren tu atención.' : 'No tienes tareas pendientes. ¡Buen trabajo!' }}</p></div>
          <div class="row gap"><button class="btn ghost" (click)="s.loadAccount(); s.loadProps()"><ic n="refresh" [s]="16"/> Actualizar</button><button class="btn primary" (click)="newProp()"><ic n="plus" [s]="16"/> Publicar inmueble</button></div></header>

        <div class="task-grid">
          <a class="task" [class.hot]="s.pagosPorRevisar().length" href="#/panel/pagos"><ic n="wallet" [s]="22"/><b>{{ s.pagosPorRevisar().length }}</b><span>Pagos por revisar</span></a>
          <a class="task" [class.hot]="s.visitasPorConfirmar().length" href="#/panel/visitas"><ic n="calendar" [s]="22"/><b>{{ s.visitasPorConfirmar().length }}</b><span>Visitas por confirmar</span></a>
          <a class="task" [class.hot]="s.contratosPorEnviar().length" href="#/panel/contratos"><ic n="file" [s]="22"/><b>{{ s.contratosPorEnviar().length }}</b><span>Contratos por enviar</span></a>
          <a class="task" [class.hot]="s.contratosPorActivar().length" href="#/panel/contratos"><ic n="key" [s]="22"/><b>{{ s.contratosPorActivar().length }}</b><span>Contratos por activar</span></a>
          <a class="task" href="#/panel/operaciones"><ic n="clock" [s]="22"/><b>{{ s.sinPago().length }}</b><span>Reservas esperando pago</span></a>
        </div>

        <div class="kpis">
          @for (k of kpis(); track k.l) {<article class="kpi"><span class="kl">{{ k.l }}</span><b>{{ k.v }}</b><small>{{ k.n }}</small></article>}
        </div>

        <div class="two">
          <section class="card"><h2>Operaciones por estado</h2>
            @for (b of opBars(); track b.l) {<div class="bar"><span>{{ b.l }}</span><div class="track"><i [style.width.%]="b.pct" [class]="b.t"></i></div><b>{{ b.n }}</b></div>}
            @if (!s.ops().length) {<p class="muted">Aún no hay operaciones.</p>}</section>
          <section class="card"><h2>Inmuebles por estado</h2>
            @for (b of propBars(); track b.l) {<div class="bar"><span>{{ b.l }}</span><div class="track"><i [style.width.%]="b.pct" [class]="b.t"></i></div><b>{{ b.n }}</b></div>}</section>
        </div>

        <section class="card"><div class="row between"><h2>Actividad reciente</h2><a class="btn text" href="#/panel/operaciones">Ver todas <ic n="arrow" [s]="14"/></a></div>
          <div class="table-wrap"><table class="table"><thead><tr><th>Operación</th><th>Inmueble</th><th>Cliente</th><th>Estado</th></tr></thead><tbody>
            @for (x of s.ops().slice(0, 6); track x.o.id) {<tr (click)="s.opDetail.set(x)" class="click"><td>#{{ x.o.id }}</td><td>{{ x.prop?.titulo }}</td><td>{{ cname(x.o.clienteId) }}</td><td><badge [tone]="oTone(x.o.estado)">{{ oLabel(x.o.estado) }}</badge> <small class="muted">{{ x.staffNote }}</small></td></tr>}
            @empty {<tr><td colspan="4" class="muted center">Sin actividad todavía.</td></tr>}
          </tbody></table></div></section>
      }
      @case ('inmuebles') {
        <header class="ph"><div><p class="eyebrow">Catálogo</p><h1>{{ s.isAdmin() ? 'Todos los inmuebles' : 'Mis inmuebles' }}</h1><p class="muted">{{ propList().length }} inmuebles</p></div>
          <button class="btn primary" (click)="newProp()"><ic n="plus" [s]="16"/> Publicar inmueble</button></header>
        <div class="filterbar"><label class="search"><ic n="search" [s]="16"/><input [(ngModel)]="q" placeholder="Buscar por título, ciudad o dirección"></label>
          <select [(ngModel)]="fEstado"><option value="">Todos los estados</option>@for (e of estados; track e) {<option [value]="e">{{ pLabel(e) }}</option>}</select>
          @if (!s.isAdmin() && s.myAgent()) {<label class="check"><input type="checkbox" [(ngModel)]="soloMios"> Solo los míos</label>}</div>
        <div class="table-wrap"><table class="table"><thead><tr><th>Inmueble</th><th>Operación</th><th>Precio</th><th>Estado</th><th>Agente</th><th></th></tr></thead><tbody>
          @for (p of propList(); track p.id) {<tr>
            <td><div class="cell-prop"><span class="thumb sm">@if (img(p)) {<img [src]="img(p)" alt="">} @else {<ic n="home" [s]="18"/>}</span><div><b><a [href]="'#/propiedad/' + p.id">{{ p.titulo }}</a></b><small class="muted">{{ p.ciudad }} · {{ p.habitaciones }} dorm. · {{ p.areaM2 }} m²</small></div></div></td>
            <td>{{ p.tipoOperacion === 'COMPRA' ? 'Venta' : 'Alquiler' }}</td><td>{{ money(p.precio) }}</td>
            <td>@if (['DISPONIBLE', 'INACTIVA', 'BORRADOR', 'PUBLICADA'].includes(p.estado)) {<select class="inline" [ngModel]="p.estado" (ngModelChange)="s.setPropState(p, $event)"><option value="DISPONIBLE">Disponible</option><option value="INACTIVA">Inactiva</option><option value="BORRADOR">Borrador</option></select>} @else {<badge [tone]="pTone(p.estado)">{{ pLabel(p.estado) }}</badge>}</td>
            <td>{{ agentName(p.agenteId) }}</td>
            <td class="actions"><button class="icon-btn" title="Editar" (click)="s.propEditor.set(p)"><ic n="edit" [s]="16"/></button><button class="icon-btn danger" title="Eliminar" (click)="s.deleteProp(p)"><ic n="trash" [s]="16"/></button></td></tr>}
          @empty {<tr><td colspan="6" class="muted center">No hay inmuebles con esos filtros.</td></tr>}
        </tbody></table></div>
      }
      @case ('operaciones') {
        <header class="ph"><div><p class="eyebrow">Seguimiento</p><h1>Operaciones</h1><p class="muted">Cada reserva con su pago y contrato.</p></div>
          @if (s.isAdmin()) {<button class="btn ghost" (click)="exportOps()"><ic n="download" [s]="16"/> Exportar CSV</button>}</header>
        <div class="filterbar"><label class="search"><ic n="search" [s]="16"/><input [(ngModel)]="q" placeholder="Buscar por inmueble, cliente o número"></label>
          <div class="seg"><button [class.on]="fOp === ''" (click)="fOp = ''">Todas</button><button [class.on]="fOp === 'curso'" (click)="fOp = 'curso'">En curso</button><button [class.on]="fOp === 'ok'" (click)="fOp = 'ok'">Completadas</button><button [class.on]="fOp === 'bad'" (click)="fOp = 'bad'">Canceladas</button></div></div>
        <div class="table-wrap"><table class="table"><thead><tr><th>N.º</th><th>Inmueble</th><th>Cliente</th><th>Adelanto</th><th>Estado</th><th>Fecha</th><th></th></tr></thead><tbody>
          @for (x of opList(); track x.o.id) {<tr class="click" (click)="s.opDetail.set(x)">
            <td>#{{ x.o.id }}</td><td><b>{{ x.prop?.titulo }}</b><small class="muted">{{ x.o.tipo === 'COMPRA' ? 'Compra' : 'Alquiler' }}</small></td><td>{{ cname(x.o.clienteId) }}</td><td>{{ money(x.o.monto) }}</td>
            <td><badge [tone]="oTone(x.o.estado)">{{ oLabel(x.o.estado) }}</badge><small class="muted">{{ x.staffNote }}</small></td><td>{{ x.o.createdAt | date: 'd MMM, h:mm a' }}</td>
            <td class="actions"><button class="icon-btn" title="Ver detalle"><ic n="eye" [s]="16"/></button></td></tr>}
          @empty {<tr><td colspan="7" class="muted center">No hay operaciones con esos filtros.</td></tr>}
        </tbody></table></div>
      }
      @case ('visitas') {
        <header class="ph"><div><p class="eyebrow">Agenda</p><h1>Visitas</h1><p class="muted">Confirma, reprograma y registra las visitas a los inmuebles.</p></div></header>
        <div class="seg wide"><button [class.on]="fVis === 'pend'" (click)="fVis = 'pend'">Por confirmar ({{ s.visitasPorConfirmar().length }})</button><button [class.on]="fVis === 'prox'" (click)="fVis = 'prox'">Próximas</button><button [class.on]="fVis === 'hist'" (click)="fVis = 'hist'">Historial</button></div>
        <div class="list">@for (v of visList(); track v.id) {
          <article class="row-card">
            <div class="date-box"><b>{{ v.fechaHora | date: 'd' }}</b><span>{{ v.fechaHora | date: 'MMM' }}</span></div>
            <div class="grow"><h3>{{ propTitle(v.propiedadId) }}</h3><p class="muted"><ic n="clock" [s]="14"/> {{ v.fechaHora | date: 'EEEE d MMMM, h:mm a' }}</p><p class="muted"><ic n="user" [s]="14"/> {{ cname(v.clienteId) }}@if (cphone(v.clienteId)) { · {{ cphone(v.clienteId) }} }</p>@if (v.observaciones) {<p class="muted small">{{ v.observaciones }}</p>}</div>
            <badge [tone]="vTone(v.estado)">{{ vLabel(v.estado) }}</badge>
            <div class="row gap wrap">
              @if (v.estado === 'SOLICITADA') {<button class="btn primary sm" (click)="s.visitState(v, 'CONFIRMADA')">Confirmar</button>}
              @if (v.estado === 'CONFIRMADA' || v.estado === 'REPROGRAMADA') {<button class="btn primary sm" (click)="s.visitState(v, 'REALIZADA')">Marcar realizada</button><button class="btn ghost sm" (click)="s.visitState(v, 'NO_ASISTIO')">No asistió</button>}
              @if (['SOLICITADA', 'CONFIRMADA', 'REPROGRAMADA'].includes(v.estado)) {<button class="btn ghost sm" (click)="resch.set(resch() === v.id ? 0 : v.id)">Reprogramar</button><button class="btn text danger sm" (click)="s.visitState(v, 'CANCELADA')">Cancelar</button>}
            </div>
            @if (resch() === v.id) {<div class="inline-form"><input type="datetime-local" [(ngModel)]="newDate"><button class="btn primary sm" (click)="doResch(v)">Guardar nueva fecha</button></div>}
          </article>}
          @empty {<div class="empty-state"><ic n="calendar" [s]="30"/><h3>No hay visitas en esta lista</h3></div>}</div>
      }
      @case ('pagos') {
        <header class="ph"><div><p class="eyebrow">Verificación</p><h1>Pagos</h1><p class="muted">Revisa cada captura. Al aprobar, la operación avanza al contrato.</p></div>
          @if (s.isAdmin()) {<button class="btn ghost" (click)="exportPagos()"><ic n="download" [s]="16"/> Exportar CSV</button>}</header>
        <div class="seg wide"><button [class.on]="fPago === 'rev'" (click)="fPago = 'rev'">Por revisar ({{ s.pagosPorRevisar().length }})</button><button [class.on]="fPago === 'esp'" (click)="fPago = 'esp'">Esperando captura</button><button [class.on]="fPago === 'hist'" (click)="fPago = 'hist'">Historial</button></div>
        <div class="list">@for (p of pagoList(); track p.id) {
          <article class="pay-card">
            <div class="pay-head"><div><p class="eyebrow">Pago #{{ p.id }} · Operación #{{ p.ordenId }}</p><h3>{{ money(p.monto) }} <span class="muted">· {{ p.metodo ? mLabel(p.metodo) : 'Sin método' }}</span></h3>
              <p class="muted">{{ propOfPago(p) }} · {{ cname(p.clienteId ?? opOf(p)?.o.clienteId) }}</p></div>
              <badge [tone]="pTone2(p)">{{ p.estado === 'PENDIENTE' && p.comprobante ? 'En revisión' : pgLabel(p.estado) }}</badge></div>
            <dl class="pay-data"><div><dt>N.º de operación</dt><dd>{{ p.nroOperacion || '—' }}</dd></div><div><dt>Enviado</dt><dd>{{ p.updatedAt | date: 'd MMM, h:mm a' }}</dd></div>@if (p.motivoRevision) {<div><dt>Observación</dt><dd>{{ p.motivoRevision }}</dd></div>}</dl>
            @if (p.comprobante) {<button class="btn ghost sm" (click)="toggleProof(p)"><ic n="image" [s]="16"/> {{ proofs()[p.id] ? 'Ocultar captura' : 'Ver captura' }}</button>@if (proofs()[p.id]) {<a [href]="proofs()[p.id]" target="_blank" rel="noopener"><img class="proof" [src]="proofs()[p.id]" alt="Captura del pago"></a>}}
            @else if (p.estado === 'PENDIENTE') {<p class="muted"><ic n="clock" [s]="14"/> El cliente aún no subió la captura.</p>}
            @if (p.estado === 'PENDIENTE' && p.comprobante) {
              @if (rev()?.id === p.id) {<div class="reason"><label class="field"><span>{{ rev()?.accion === 'OBSERVAR' ? 'Qué debe corregir el cliente' : 'Motivo del rechazo (se cancelará la operación)' }}</span><input [(ngModel)]="reason" placeholder="Ej.: la captura no muestra el número de operación" autofocus></label>
                <div class="row gap"><button class="btn" [class.primary]="rev()?.accion === 'OBSERVAR'" [class.danger]="rev()?.accion === 'RECHAZAR'" [disabled]="s.busy()" (click)="sendReview(p)">Confirmar</button><button class="btn ghost" (click)="rev.set(null)">Volver</button></div></div>}
              @else {<div class="row gap wrap"><button class="btn primary" [disabled]="s.busy()" (click)="approve(p)"><ic n="check" [s]="16"/> Aprobar pago</button><button class="btn ghost" (click)="ask(p, 'OBSERVAR')">Pedir otra captura</button><button class="btn text danger" (click)="ask(p, 'RECHAZAR')">Rechazar</button></div>}}
          </article>}
          @empty {<div class="empty-state"><ic n="wallet" [s]="30"/><h3>{{ fPago === 'rev' ? 'No hay pagos por revisar' : 'No hay pagos en esta lista' }}</h3></div>}</div>
      }
      @case ('contratos') {
        <header class="ph"><div><p class="eyebrow">Cierre</p><h1>Contratos</h1><p class="muted">Envíalos al cliente, espera su aceptación y actívalos.</p></div></header>
        <div class="table-wrap"><table class="table"><thead><tr><th>Contrato</th><th>Inmueble</th><th>Cliente</th><th>Valor</th><th>Estado</th><th></th></tr></thead><tbody>
          @for (c of contratosSorted(); track c.id) {<tr>
            <td>#{{ c.id }}<small class="muted">{{ c.tipo === 'COMPRA' ? 'Compra' : 'Alquiler' }}</small></td><td>{{ propTitle(c.propiedadId) }}</td><td>{{ cname(c.clienteId) }}</td><td>{{ money(c.monto) }}</td>
            <td><badge [tone]="cTone(c.estado)">{{ cLabel(c.estado) }}</badge></td>
            <td class="actions">
              @if (c.estado === 'GENERADO') {<button class="btn primary sm" (click)="s.contractState(c, 'PENDIENTE_ACEPTACION', 'Contrato enviado al cliente.')">Enviar al cliente</button>}
              @if (c.estado === 'PENDIENTE_ACEPTACION') {<small class="muted">Esperando al cliente</small>}
              @if (c.estado === 'ACEPTADO') {<button class="btn primary sm" (click)="s.contractState(c, 'VIGENTE', 'Contrato activado.')">Activar</button>}
              @if (c.estado === 'VIGENTE') {<button class="btn ghost sm" (click)="s.contractState(c, 'FINALIZADO', 'Contrato finalizado.')">Finalizar</button>}
            </td></tr>}
          @empty {<tr><td colspan="6" class="muted center">Todavía no hay contratos.</td></tr>}
        </tbody></table></div>
      }
      @case ('clientes') {
        <header class="ph"><div><p class="eyebrow">Personas</p><h1>Clientes</h1><p class="muted">{{ s.clientes().length }} clientes registrados</p></div>
          @if (s.isAdmin()) {<button class="btn ghost" (click)="exportClientes()"><ic n="download" [s]="16"/> Exportar CSV</button>}</header>
        <div class="filterbar"><label class="search"><ic n="search" [s]="16"/><input [(ngModel)]="q" placeholder="Buscar por nombre, DNI o correo"></label></div>
        <div class="table-wrap"><table class="table"><thead><tr><th>Cliente</th><th>DNI</th><th>Contacto</th><th>Operaciones</th><th>Acceso</th></tr></thead><tbody>
          @for (c of clientList(); track c.id) {<tr>
            <td><div class="cell-prop"><span class="avatar">{{ ini2(c.nombres) }}</span><div><b>{{ c.nombres }}</b><small class="muted">Cliente #{{ c.id }}</small></div></div></td><td>{{ c.dni || '—' }}</td><td>{{ c.email }}<small class="muted">{{ c.telefono || '' }}</small></td>
            <td>{{ opsOf(c.id) }}</td>
            <td>@if (s.isAdmin()) {<button class="btn sm" [class.ghost]="c.activo" [class.primary]="!c.activo" (click)="toggleClient(c)">{{ c.activo ? 'Desactivar' : 'Activar' }}</button>} @else {<badge [tone]="c.activo ? 'good' : 'bad'">{{ c.activo ? 'Activo' : 'Inactivo' }}</badge>}</td></tr>}
          @empty {<tr><td colspan="5" class="muted center">No hay clientes con ese criterio.</td></tr>}
        </tbody></table></div>
      }
      @case ('agentes') {
        <header class="ph"><div><p class="eyebrow">Equipo</p><h1>Agentes</h1><p class="muted">Crea accesos para tu equipo y controla quién puede ingresar.</p></div>
          <button class="btn primary" (click)="s.agentModal.set(true)"><ic n="plus" [s]="16"/> Nuevo agente</button></header>
        <div class="table-wrap"><table class="table"><thead><tr><th>Agente</th><th>Matrícula</th><th>Contacto</th><th>Inmuebles</th><th>Acceso</th></tr></thead><tbody>
          @for (a of s.agentes(); track a.id) {<tr>
            <td><div class="cell-prop"><span class="avatar">{{ ini2(a.nombres + ' ' + a.apellidos) }}</span><div><b>{{ a.nombres }} {{ a.apellidos }}</b><small class="muted">{{ a.keycloakUserId ? 'Con acceso al panel' : 'Sin usuario de ingreso' }}</small></div></div></td>
            <td>{{ a.matricula }}</td><td>{{ a.email }}<small class="muted">{{ a.telefono || '' }}</small></td><td>{{ propsOfAgent(a.id) }}</td>
            <td><div class="row gap"><badge [tone]="a.estado === 'ACTIVO' ? 'good' : 'bad'">{{ a.estado === 'ACTIVO' ? 'Activo' : 'Inactivo' }}</badge><button class="btn ghost sm" (click)="s.setAgentState(a, a.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO')">{{ a.estado === 'ACTIVO' ? 'Desactivar' : 'Activar' }}</button></div></td></tr>}
          @empty {<tr><td colspan="5" class="muted center">Aún no hay agentes.</td></tr>}
        </tbody></table></div>
      }
      @case ('sistema') {
        <header class="ph"><div><p class="eyebrow">Salud del sistema</p><h1>Sistema y monitoreo</h1><p class="muted">Estado de los eventos entre servicios y accesos a herramientas.</p></div><button class="btn ghost" (click)="s.loadEventos()"><ic n="refresh" [s]="16"/> Actualizar</button></header>
        <div class="kpis">@for (e of eventos(); track e[0]) {<article class="kpi"><span class="kl">{{ eventLabel(e[0]) }}</span><b>{{ e[1] }}</b></article>}</div>
        <section class="card"><h2>Herramientas</h2><div class="tools">
          <a class="tool" [href]="grafana" target="_blank" rel="noopener"><ic n="chart" [s]="20"/><b>Grafana</b><span>Métricas y logs</span></a>
          <a class="tool" href="http://localhost:28761" target="_blank" rel="noopener"><ic n="building" [s]="20"/><b>Eureka</b><span>Servicios registrados</span></a>
          <a class="tool" href="http://localhost:28090" target="_blank" rel="noopener"><ic n="lock" [s]="20"/><b>Keycloak</b><span>Usuarios y roles</span></a>
          <a class="tool" href="http://localhost:28085" target="_blank" rel="noopener"><ic n="inbox" [s]="20"/><b>Kafka UI</b><span>Eventos de la saga</span></a>
        </div></section>
      }
      }
    </section>
  </div>`,
})
export class PanelPage {
  s = store;
  money = money;
  grafana = GRAFANA_URL;
  menu = signal(false);
  q = '';
  fEstado = '';
  soloMios = true;
  fOp = '';
  fVis = 'pend';
  fPago = 'rev';
  resch = signal(0);
  newDate = '';
  rev = signal<{id: number; accion: 'OBSERVAR' | 'RECHAZAR'} | null>(null);
  reason = '';
  proofs = signal<Record<number, string>>({});
  estados = ['DISPONIBLE', 'RESERVADA', 'EN_PROCESO', 'VENDIDA', 'ALQUILADA', 'INACTIVA', 'BORRADOR'];
  oLabel = labelOrden; oTone = toneOrden; vLabel = labelVisita; vTone = toneVisita; pgLabel = labelPago; cLabel = labelContrato; cTone = toneContrato; pLabel = labelProp; pTone = toneProp; mLabel = labelMetodo;

  sec = computed(() => {
    const p = store.route().parts[0] || 'resumen';
    return this.nav().some(n => n.id === p) ? p : 'resumen';
  });
  nav = computed<NavItem[]>(() => {
    const c = store;
    const base: NavItem[] = [
      {id: 'resumen', label: 'Resumen', icon: 'chart', count: c.pendientes()},
      {id: 'inmuebles', label: c.isAdmin() ? 'Inmuebles' : 'Mis inmuebles', icon: 'building'},
      {id: 'operaciones', label: 'Operaciones', icon: 'key'},
      {id: 'visitas', label: 'Visitas', icon: 'calendar', count: c.visitasPorConfirmar().length},
      {id: 'pagos', label: 'Pagos', icon: 'wallet', count: c.pagosPorRevisar().length},
      {id: 'contratos', label: 'Contratos', icon: 'file', count: c.contratosPorEnviar().length + c.contratosPorActivar().length},
      {id: 'clientes', label: 'Clientes', icon: 'users'},
    ];
    if (c.isAdmin()) base.push({id: 'agentes', label: 'Agentes', icon: 'badge'}, {id: 'sistema', label: 'Sistema', icon: 'shield'});
    return base;
  });
  ini = computed(() => (store.userName() || '?').slice(0, 2).toUpperCase());
  constructor() {
    effect(() => {
      if (this.sec() === 'sistema') store.loadEventos();
    });
  }
  greeting = () => {
    const h = new Date().getHours();
    return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
  };

  // ---- helpers
  img = imageOf;
  ini2 = initials;
  cname(id: number) {
    return store.clientById().get(id)?.nombres || (id ? 'Cliente #' + id : '—');
  }
  cphone(id: number) {
    return store.clientById().get(id)?.telefono || '';
  }
  agentName(id: number) {
    const a = store.agentById().get(id);
    return a ? a.nombres + ' ' + a.apellidos : id ? 'Agente #' + id : 'Sin asignar';
  }
  propTitle(id: number) {
    return store.propById().get(id)?.titulo || 'Inmueble #' + id;
  }
  opOf(p: any): OpView | undefined {
    return store.ops().find(x => x.o.id === p.ordenId);
  }
  propOfPago(p: any) {
    return this.opOf(p)?.prop?.titulo || 'Operación #' + p.ordenId;
  }
  pTone2(p: any) {
    return p.estado === 'PENDIENTE' && p.comprobante ? 'info' : tonePago(p.estado);
  }
  opsOf(id: number) {
    return store.orders().filter(o => o.clienteId === id).length;
  }
  propsOfAgent(id: number) {
    return store.props().filter(p => p.agenteId === id).length;
  }
  eventLabel(k: string) {
    return ({published: 'Eventos publicados', processed: 'Eventos procesados', pending: 'Pendientes', failed: 'Con error'} as any)[k] || k;
  }
  eventos = computed(() => Object.entries(store.eventos()));

  newProp() {
    store.propEditor.set({});
  }

  // ---- listas
  propList = computed(() => {
    const q = this.q.trim().toLowerCase();
    const mine = !store.isAdmin() && this.soloMios ? store.myAgent()?.id : null;
    return store.props().filter(p => (!this.fEstado || p.estado === this.fEstado) && (!mine || p.agenteId === mine) && (!q || (p.titulo + ' ' + p.ciudad + ' ' + p.direccion).toLowerCase().includes(q)));
  });
  opList = computed(() => {
    const q = this.q.trim().toLowerCase();
    return store.ops().filter(x => {
      if (this.fOp === 'curso' && (x.cancelled || x.done)) return false;
      if (this.fOp === 'ok' && !x.done) return false;
      if (this.fOp === 'bad' && !x.cancelled) return false;
      return !q || ((x.prop?.titulo || '') + ' ' + this.cname(x.o.clienteId) + ' #' + x.o.id).toLowerCase().includes(q);
    });
  });
  visList = computed(() => {
    const now = Date.now();
    const vs = [...store.visits()];
    if (this.fVis === 'pend') return vs.filter(v => v.estado === 'SOLICITADA').sort((a, b) => a.fechaHora.localeCompare(b.fechaHora));
    if (this.fVis === 'prox') return vs.filter(v => ['CONFIRMADA', 'REPROGRAMADA'].includes(v.estado) && new Date(v.fechaHora).getTime() >= now - 3600000).sort((a, b) => a.fechaHora.localeCompare(b.fechaHora));
    return vs.filter(v => !['SOLICITADA'].includes(v.estado) && !(['CONFIRMADA', 'REPROGRAMADA'].includes(v.estado) && new Date(v.fechaHora).getTime() >= now - 3600000)).sort((a, b) => b.fechaHora.localeCompare(a.fechaHora));
  });
  pagoList = computed(() => {
    const ps = [...store.pagos()].sort((a, b) => b.id - a.id);
    if (this.fPago === 'rev') return ps.filter(p => p.estado === 'PENDIENTE' && p.comprobante);
    if (this.fPago === 'esp') return ps.filter(p => p.estado === 'PENDIENTE' && !p.comprobante);
    return ps.filter(p => p.estado !== 'PENDIENTE');
  });
  contratosSorted = computed(() => [...store.contratos()].sort((a, b) => b.id - a.id));
  clientList = computed(() => {
    const q = this.q.trim().toLowerCase();
    return store.clientes().filter(c => !q || ((c.nombres || '') + ' ' + (c.dni || '') + ' ' + (c.email || '')).toLowerCase().includes(q));
  });

  // ---- indicadores
  kpis = computed(() => {
    const ps = store.props(), os = store.ops(), pagos = store.pagos();
    const ingresos = pagos.filter(p => p.estado === 'APROBADO').reduce((t, p) => t + Number(p.monto), 0);
    const valor = os.filter(x => x.done || x.o.estado === 'CONFIRMADA').reduce((t, x) => t + Number(x.o.total || 0), 0);
    const base = [
      {l: 'Inmuebles disponibles', v: ps.filter(p => p.estado === 'DISPONIBLE').length, n: 'de ' + ps.length + ' publicados'},
      {l: 'Operaciones en curso', v: os.filter(x => !x.done && !x.cancelled).length, n: os.filter(x => x.done).length + ' completadas'},
      {l: 'Adelantos verificados', v: money(ingresos), n: pagos.filter(p => p.estado === 'APROBADO').length + ' pagos aprobados'},
      {l: 'Valor de operaciones', v: money(valor), n: 'confirmadas y cerradas'},
      {l: 'Clientes', v: store.clientes().length, n: store.clientes().filter(c => c.activo).length + ' activos'},
    ];
    if (store.isAdmin()) base.push({l: 'Agentes', v: store.agentes().length, n: store.agentes().filter(a => a.estado === 'ACTIVO').length + ' activos'});
    return base;
  });
  opBars = computed(() => {
    const os = store.ops();
    const rows = [
      {l: 'Esperando pago', n: os.filter(x => x.o.estado === 'ESPERANDO_PAGO' && !x.pago?.comprobante).length, t: 'warn'},
      {l: 'Pago en revisión', n: os.filter(x => x.o.estado === 'ESPERANDO_PAGO' && x.pago?.comprobante).length, t: 'info'},
      {l: 'Contrato en curso', n: os.filter(x => (x.o.estado === 'CONFIRMADA' && !x.done) || ['GENERANDO_CONTRATO', 'CONFIRMANDO_PROPIEDAD', 'CONVIRTIENDO_RESERVA'].includes(x.o.estado)).length, t: 'info'},
      {l: 'Completadas', n: os.filter(x => x.done).length, t: 'good'},
      {l: 'Canceladas', n: os.filter(x => x.cancelled).length, t: 'bad'},
    ];
    const max = Math.max(1, ...rows.map(r => r.n));
    return rows.map(r => ({...r, pct: (r.n / max) * 100}));
  });
  propBars = computed(() => {
    const ps = store.props();
    const rows = [
      {l: 'Disponibles', n: ps.filter(p => p.estado === 'DISPONIBLE').length, t: 'good'},
      {l: 'Reservados / en proceso', n: ps.filter(p => ['RESERVADA', 'EN_PROCESO'].includes(p.estado)).length, t: 'warn'},
      {l: 'Vendidos / alquilados', n: ps.filter(p => ['VENDIDA', 'ALQUILADA'].includes(p.estado)).length, t: 'info'},
      {l: 'Inactivos / borrador', n: ps.filter(p => ['INACTIVA', 'BORRADOR', 'PUBLICADA'].includes(p.estado)).length, t: 'bad'},
    ];
    const max = Math.max(1, ...rows.map(r => r.n));
    return rows.map(r => ({...r, pct: (r.n / max) * 100}));
  });

  // ---- acciones
  async doResch(v: any) {
    if (await store.rescheduleVisit(v, this.newDate)) this.resch.set(0);
  }
  async toggleProof(p: any) {
    const cur = {...this.proofs()};
    if (cur[p.id]) {
      delete cur[p.id];
      this.proofs.set(cur);
      return;
    }
    try {
      cur[p.id] = await blobUrl('/pagos/' + p.id + '/comprobante');
      this.proofs.set(cur);
    } catch (e: any) {
      store.toast('err', e.message);
    }
  }
  approve(p: any) {
    store.confirm('¿Aprobar este pago?', 'Verificaste que la captura muestra ' + money(p.monto) + ' y el número de operación ' + p.nroOperacion + '. La operación avanzará hacia el contrato.', 'Sí, aprobar pago', async () => {
      await store.reviewPago(p, 'APROBAR');
    });
  }
  ask(p: any, accion: 'OBSERVAR' | 'RECHAZAR') {
    this.reason = '';
    this.rev.set({id: p.id, accion});
  }
  async sendReview(p: any) {
    const r = this.rev();
    if (!r) return;
    if (!this.reason.trim()) {
      store.toast('err', 'Escribe el motivo para el cliente.');
      return;
    }
    if (await store.reviewPago(p, r.accion, this.reason)) this.rev.set(null);
  }
  toggleClient(c: any) {
    store.confirm(c.activo ? '¿Desactivar este cliente?' : '¿Activar este cliente?', c.activo ? 'No podrá iniciar sesión hasta que lo actives de nuevo.' : 'Podrá volver a iniciar sesión.', c.activo ? 'Desactivar' : 'Activar', async () => {
      await store.setClientActive(c, !c.activo);
    }, c.activo);
  }
  exportOps() {
    downloadCsv('operaciones-casas.csv', store.ops().map(x => ({numero: x.o.id, inmueble: x.prop?.titulo, cliente: this.cname(x.o.clienteId), tipo: x.o.tipo, adelanto: x.o.monto, total: x.o.total, estado: labelOrden(x.o.estado), creada: x.o.createdAt})));
  }
  exportPagos() {
    downloadCsv('pagos-casas.csv', store.pagos().map(p => ({pago: p.id, operacion: p.ordenId, monto: p.monto, metodo: p.metodo || '', nro_operacion: p.nroOperacion || '', estado: labelPago(p.estado), observacion: p.motivoRevision || '', actualizado: p.updatedAt})));
  }
  exportClientes() {
    downloadCsv('clientes-casas.csv', store.clientes().map(c => ({id: c.id, nombres: c.nombres, dni: c.dni || '', email: c.email, telefono: c.telefono || '', activo: c.activo ? 'si' : 'no'})));
  }
}
