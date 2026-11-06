import {Component, computed} from '@angular/core';
import {DatePipe} from '@angular/common';
import {store, OpView} from './store';
import {Ic, Badge, Stepper, PropCard} from './ui';
import {money, labelOrden, toneOrden, labelVisita, toneVisita, imageOf} from './core';

const TIPOS: Record<string, [string, string]> = {
  ORDEN_CONFIRMADA: ['Operación confirmada', 'check'],
  ORDEN_CANCELADA: ['Operación cancelada', 'x'],
  PAGO_APROBADO: ['Pago aprobado', 'wallet'],
  PAGO_RECHAZADO: ['Pago rechazado', 'alert'],
  CONTRATO_GENERADO: ['Contrato generado', 'file'],
  RESERVA_CREADA: ['Reserva registrada', 'lock'],
};

@Component({
  selector: 'page-account',
  standalone: true,
  imports: [DatePipe, Ic, Badge, Stepper, PropCard],
  template: `
  <section class="page-head"><div class="container"><p class="eyebrow">Mi espacio</p><h1>Hola, {{ s.userName() }}.</h1><p class="muted">Aquí sigues cada paso de tus reservas, pagos y contratos.</p></div></section>
  <div class="container account">
    <nav class="tabs" aria-label="Secciones de mi espacio">
      @for (t of tabs(); track t.id) {<a [href]="'#/mi-espacio/' + t.id" [class.on]="tab() === t.id"><ic [n]="t.icon" [s]="16"/>{{ t.label }}@if (t.count) {<span class="count">{{ t.count }}</span>}</a>}
    </nav>

    @switch (tab()) {
      @case ('operaciones') {
        @for (r of s.reservasSinOrden(); track r.id) {<div class="callout warn"><div><b>Reserva #{{ r.id }} guardada, falta iniciar la operación</b><p>{{ title(r.propiedadId) }} · adelanto {{ money(r.montoReserva) }}. Continúa para poder enviar tu pago.</p></div><button class="btn primary" [disabled]="s.busy()" (click)="s.resumeReservation(r)">Continuar mi reserva</button></div>}
        @if (!s.ops().length && !s.reservasSinOrden().length) {<div class="empty-state"><ic n="key" [s]="34"/><h3>Todavía no tienes operaciones</h3><p>Cuando reserves un inmueble, aquí verás cada paso: pago, verificación y contrato.</p><a class="btn primary lg" href="#/propiedades">Explorar propiedades</a></div>}
        @for (op of s.ops(); track op.o.id) {
          <article class="op-card" [class.cancelled]="op.cancelled">
            <div class="op-top">
              <div class="thumb">@if (img(op)) {<img [src]="img(op)" alt="">} @else {<ic n="home" [s]="26"/>}</div>
              <div class="op-info">
                <p class="eyebrow">{{ op.o.tipo === 'COMPRA' ? 'Compra' : 'Alquiler' }} · Operación #{{ op.o.id }}</p>
                <h3><a [href]="'#/propiedad/' + op.o.propiedadId">{{ op.prop?.titulo || 'Inmueble #' + op.o.propiedadId + ' (ya no está publicado)' }}</a></h3>
                <p class="muted">{{ op.prop?.ciudad ? op.prop.ciudad + ' · ' : '' }}Adelanto {{ money(op.o.monto) }} de {{ money(op.o.total) }}</p>
              </div>
              <badge [tone]="tone(op.o.estado)">{{ label(op.o.estado) }}</badge>
            </div>
            <stepper [steps]="op.steps"/>
            <div class="callout" [class]="'callout ' + op.tone">
              <div><b>{{ op.headline }}</b><p>{{ op.hint }}</p>
                @if (op.o.estado === 'ESPERANDO_PAGO' && op.reserva) {<p class="timer"><ic n="clock" [s]="14"/> Tu reserva vence el {{ op.reserva.fechaExpiracion | date: 'd MMM, h:mm a' }} ({{ remaining(op) }})</p>}</div>
              <div class="row gap wrap">
                @if (op.cta === 'pagar') {<button class="btn primary" (click)="s.openPay(op)"><ic n="upload" [s]="16"/> {{ op.pago?.motivoRevision ? 'Enviar nueva captura' : 'Pagar adelanto' }}</button>}
                @if (op.cta === 'contrato') {<button class="btn primary" (click)="s.contractFor.set(op)"><ic n="file" [s]="16"/> Revisar contrato</button>}
                <button class="btn ghost" (click)="s.opDetail.set(op)">Ver detalle</button>
                @if (op.o.estado === 'ESPERANDO_PAGO') {<button class="btn text danger" (click)="s.cancelOrder(op)">Cancelar</button>}
              </div>
            </div>
          </article>}
      }
      @case ('visitas') {
        @if (!s.visits().length) {<div class="empty-state"><ic n="calendar" [s]="34"/><h3>No tienes visitas programadas</h3><p>Agenda una visita desde la ficha de cualquier inmueble disponible.</p><a class="btn primary" href="#/propiedades">Buscar inmuebles</a></div>}
        <div class="list">@for (v of visitsSorted(); track v.id) {
          <article class="row-card">
            <div class="date-box"><b>{{ v.fechaHora | date: 'd' }}</b><span>{{ v.fechaHora | date: 'MMM' }}</span></div>
            <div class="grow"><h3><a [href]="'#/propiedad/' + v.propiedadId">{{ title(v.propiedadId) }}</a></h3><p class="muted"><ic n="clock" [s]="14"/> {{ v.fechaHora | date: 'EEEE d MMMM, h:mm a' }}</p>@if (v.observaciones) {<p class="muted">{{ v.observaciones }}</p>}</div>
            <badge [tone]="vTone(v.estado)">{{ vLabel(v.estado) }}</badge>
            <div class="row gap">
              @if (v.estado === 'SOLICITADA' || v.estado === 'CONFIRMADA' || v.estado === 'REPROGRAMADA') {<button class="btn ghost sm" (click)="s.cancelVisit(v)">Cancelar</button>}
              @if (canReserve(v)) {<button class="btn primary sm" (click)="s.startReserve(s.propById().get(v.propiedadId))">Reservar</button>}
            </div>
          </article>}</div>
      }
      @case ('favoritos') {
        @if (!favProps().length) {<div class="empty-state"><ic n="heart" [s]="34"/><h3>Aún no guardas favoritos</h3><p>Toca el corazón de un inmueble para guardarlo aquí.</p><a class="btn primary" href="#/propiedades">Explorar</a></div>}
        <div class="grid-cards">@for (p of favProps(); track p.id) {<prop-card [p]="p"/>}</div>
      }
      @case ('avisos') {
        <div class="row between"><p class="muted">{{ s.unread() }} sin leer</p>@if (s.unread()) {<button class="btn ghost sm" (click)="s.readAll()">Marcar todo como leído</button>}</div>
        @if (!s.avisos().length) {<div class="empty-state"><ic n="bell" [s]="34"/><h3>No tienes notificaciones</h3><p>Te avisaremos aquí cuando tu pago o contrato cambie de estado.</p></div>}
        <div class="list">@for (n of avisosSorted(); track n.id) {
          <article class="row-card notice-row" [class.unread]="!n.leida" (click)="s.readNotice(n)">
            <span class="ico"><ic [n]="tipo(n.tipo)[1]" [s]="20"/></span>
            <div class="grow"><h3>{{ tipo(n.tipo)[0] }}</h3><p>{{ n.mensaje }}</p><p class="muted small">{{ n.createdAt | date: 'd MMM yyyy, h:mm a' }}@if (n.ordenId) { <span> · Operación #{{ n.ordenId }}</span> }</p></div>
            @if (!n.leida) {<span class="dot-new" title="Sin leer"></span>}
          </article>}</div>
      }
      @case ('perfil') {
        <div class="card profile">
          <span class="avatar xl">{{ ini() }}</span>
          <div class="grow"><h2>{{ s.perfil()?.nombres || s.userName() }}</h2><p class="muted">Cliente CASAS</p></div>
          <dl>
            <div><dt>Correo</dt><dd>{{ s.perfil()?.email || s.userEmail() }}</dd></div>
            <div><dt>DNI</dt><dd>{{ s.perfil()?.dni || 'No registrado' }}</dd></div>
            <div><dt>Celular</dt><dd>{{ s.perfil()?.telefono || 'No registrado' }}</dd></div>
            <div><dt>N.º de cliente</dt><dd>{{ s.clientId() || '—' }}</dd></div>
          </dl>
          <button class="btn ghost" (click)="s.logout()"><ic n="logout" [s]="16"/> Cerrar sesión</button>
        </div>
      }
    }
  </div>`,
})
export class AccountPage {
  s = store;
  money = money;
  label = labelOrden;
  tone = toneOrden;
  vLabel = labelVisita;
  vTone = toneVisita;
  tab = computed(() => store.route().parts[0] || 'operaciones');
  tabs = computed(() => [
    {id: 'operaciones', label: 'Mis operaciones', icon: 'key', count: store.ops().filter(o => !o.done && !o.cancelled).length},
    {id: 'visitas', label: 'Visitas', icon: 'calendar', count: store.visits().filter(v => ['SOLICITADA', 'CONFIRMADA', 'REPROGRAMADA'].includes(v.estado)).length},
    {id: 'favoritos', label: 'Favoritos', icon: 'heart', count: store.favs().length},
    {id: 'avisos', label: 'Notificaciones', icon: 'bell', count: store.unread()},
    {id: 'perfil', label: 'Mi perfil', icon: 'user', count: 0},
  ]);
  visitsSorted = computed(() => [...store.visits()].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)));
  avisosSorted = computed(() => [...store.avisos()].sort((a, b) => b.id - a.id));
  favProps = computed(() => store.props().filter(p => store.favs().includes(p.id)));
  ini = computed(() => (store.perfil()?.nombres || store.userName() || '?').split(' ').map((x: string) => x[0]).slice(0, 2).join('').toUpperCase());
  img = (op: OpView) => imageOf(op.prop);
  title = (id: number) => store.propById().get(id)?.titulo || 'Inmueble #' + id;
  tipo = (t: string) => TIPOS[t] || [(t || 'Aviso').replaceAll('_', ' ').toLowerCase().replace(/^./, c => c.toUpperCase()), 'bell'];
  canReserve(v: any) {
    const p = store.propById().get(v.propiedadId);
    return !!p && p.estado === 'DISPONIBLE' && ['CONFIRMADA', 'REALIZADA'].includes(v.estado);
  }
  remaining(op: OpView) {
    const ms = new Date(op.reserva?.fechaExpiracion).getTime() - Date.now();
    if (!(ms > 0)) return 'vencida';
    const h = Math.floor(ms / 3600000), m = Math.floor((ms % 3600000) / 60000);
    return 'quedan ' + (h ? h + ' h ' : '') + m + ' min';
  }
}
