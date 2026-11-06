import {Component, computed, effect, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {store, OpView} from './store';
import {Ic, Badge, Stepper} from './ui';
import {
  money, deposit, imageOf, kc, COBRO, RESERVA_HORAS, blobUrl,
  labelOrden, toneOrden, labelPago, labelContrato, labelMetodo, CIUDADES_FALLBACK,
} from './core';

const HORAS = ['09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00'];
const pad = (n: number) => String(n).padStart(2, '0');
const todayIso = () => {
  const d = new Date();
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
};
const genPass = () => 'Casa' + Math.floor(1000 + Math.random() * 9000) + '!';

@Component({
  selector: 'app-modals',
  standalone: true,
  imports: [DatePipe, FormsModule, Ic, Badge, Stepper],
  template: `
  <!-- ========== ACCESO / REGISTRO -->
  @if (s.authModal(); as mode) {
  <div class="overlay" (click)="s.authModal.set('')"><div class="modal" role="dialog" aria-modal="true" aria-label="Acceso a CASAS" (click)="$event.stopPropagation()">
    <button class="close" (click)="s.authModal.set('')" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    @if (mode === 'ingreso') {
      <div class="modal-icon"><ic n="user" [s]="26"/></div>
      <h2>Ingresa a tu cuenta</h2>
      <p class="muted">{{ s.authReason() || 'Accede para reservar, agendar visitas y seguir tus operaciones.' }}</p>
      <button class="btn primary lg block" (click)="s.login('mi-espacio')">Iniciar sesión</button>
      <div class="divider"><span>¿Eres nuevo en CASAS?</span></div>
      <button class="btn ghost lg block" (click)="openReg()">Crear mi cuenta</button>
    } @else {
      <h2>Crea tu cuenta</h2>
      <p class="muted">Necesitamos unos datos para tu contrato. Con tu DNI completamos tu nombre automáticamente.</p>
      <form (ngSubmit)="register()" #rf="ngForm" class="form">
        <label class="field"><span>DNI</span><div class="input-btn"><input name="dni" required pattern="[0-9]{8}" maxlength="8" inputmode="numeric" [(ngModel)]="reg.dni" placeholder="8 dígitos"><button type="button" class="btn ghost sm" [disabled]="looking()" (click)="dni(reg)">{{ looking() ? 'Buscando…' : 'Buscar' }}</button></div></label>
        <div class="two-col"><label class="field"><span>Nombres</span><input name="nom" required [(ngModel)]="reg.nombres"></label><label class="field"><span>Apellidos</span><input name="ape" required [(ngModel)]="reg.apellidos"></label></div>
        <label class="field"><span>Correo electrónico</span><input name="mail" type="email" required [(ngModel)]="reg.email" autocomplete="email"></label>
        <label class="field"><span>Celular</span><input name="tel" required pattern="[0-9 ]{9,13}" inputmode="tel" [(ngModel)]="reg.telefono" placeholder="999 999 999"></label>
        <div class="two-col"><label class="field"><span>Contraseña</span><input name="p1" type="password" required minlength="8" [(ngModel)]="reg.password" autocomplete="new-password" placeholder="Mínimo 8 caracteres"></label><label class="field"><span>Repite la contraseña</span><input name="p2" type="password" required [(ngModel)]="reg.password2" autocomplete="new-password"></label></div>
        <button class="btn primary lg block" type="submit" [disabled]="s.busy() || rf.invalid">{{ s.busy() ? 'Creando cuenta…' : 'Crear cuenta' }}</button>
        <p class="fineprint center">¿Ya tienes cuenta? <button type="button" class="link" (click)="s.login('mi-espacio')">Inicia sesión</button></p>
      </form>
    }
  </div></div>}

  <!-- ========== RESERVA -->
  @if (s.reserveFlow(); as f) {
  <div class="overlay" (click)="closeFlow()"><div class="modal wide printable" role="dialog" aria-modal="true" aria-label="Reservar inmueble" (click)="$event.stopPropagation()">
    <button class="close" (click)="closeFlow()" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <div class="flow-prop"><div class="thumb">@if (img(f.p)) {<img [src]="img(f.p)" alt="">} @else {<ic n="home" [s]="24"/>}</div><div><p class="eyebrow">{{ f.p.tipoOperacion === 'COMPRA' ? 'En venta' : 'En alquiler' }}</p><b>{{ f.p.titulo }}</b><p class="muted">{{ f.p.ciudad }} · {{ money(f.p.precio) }}</p></div></div>

    @if (f.step === 'cliente') {
      <h2>¿Para quién es la reserva?</h2>
      <p class="muted">Elige un cliente registrado o crea su cuenta con su DNI. Recibirá una clave temporal.</p>
      <label class="field"><span>Cliente registrado</span><select [ngModel]="f.clienteId" (ngModelChange)="s.rf({clienteId: $event})"><option [ngValue]="null">— Registrar un cliente nuevo —</option>@for (c of s.clientes(); track c.id) {<option [ngValue]="c.id">{{ c.nombres }} · {{ c.dni ? 'DNI ' + c.dni : c.email }}</option>}</select></label>
      @if (!f.clienteId) {
        <form (ngSubmit)="regClient()" #nf="ngForm" class="form">
          <label class="field"><span>DNI</span><div class="input-btn"><input name="ndni" required pattern="[0-9]{8}" maxlength="8" inputmode="numeric" [(ngModel)]="nc.dni"><button type="button" class="btn ghost sm" [disabled]="looking()" (click)="dni(nc)">{{ looking() ? 'Buscando…' : 'Buscar' }}</button></div></label>
          <div class="two-col"><label class="field"><span>Nombres</span><input name="nn" required [(ngModel)]="nc.nombres"></label><label class="field"><span>Apellidos</span><input name="na" required [(ngModel)]="nc.apellidos"></label></div>
          <div class="two-col"><label class="field"><span>Correo</span><input name="ne" type="email" required [(ngModel)]="nc.email"></label><label class="field"><span>Celular</span><input name="nt" required pattern="[0-9 ]{9,13}" [(ngModel)]="nc.telefono"></label></div>
          <button class="btn primary lg block" type="submit" [disabled]="s.busy() || nf.invalid">Registrar cliente y continuar</button>
        </form>
      } @else {<button class="btn primary lg block" (click)="s.rf({step: 'visita'})">Continuar</button>}
    }

    @if (f.step === 'visita') {
      @if (f.tempPass) {<div class="notice good"><ic n="check" [s]="18"/><div><b>Cliente registrado</b><p>Entrégale esta clave temporal para que ingrese: <code>{{ f.tempPass }}</code></p></div></div>}
      <h2>{{ s.isStaff() ? '¿El cliente quiere visitar el inmueble antes?' : '¿Quieres visitar el inmueble antes de reservar?' }}</h2>
      <p class="muted">Una visita no te compromete a nada. Si prefieres, puedes continuar directo con la reserva.</p>
      <div class="choices">
        <button type="button" class="choice" [class.on]="f.conVisita === true" (click)="s.rf({conVisita: true})"><ic n="calendar" [s]="22"/><b>Sí, quiero visitarlo</b><span>Coordinamos fecha y hora con el agente.</span></button>
        <button type="button" class="choice" [class.on]="f.conVisita === false" (click)="s.rf({conVisita: false})"><ic n="key" [s]="22"/><b>No, continuar con la reserva</b><span>Voy directo a apartar el inmueble.</span></button>
      </div>
      @if (f.conVisita) {
        <div class="two-col"><label class="field"><span>Fecha</span><input type="date" [min]="tomorrow()" [ngModel]="date()" (ngModelChange)="setDate($event, f)"></label>
          <label class="field"><span>Hora</span><select [ngModel]="hour()" (ngModelChange)="setHour($event, f)">@for (h of horas; track h) {<option [value]="h">{{ h }}</option>}</select></label></div>
        <label class="field"><span>Comentario para el agente (opcional)</span><input [ngModel]="f.nota" (ngModelChange)="s.rf({nota: $event})" maxlength="200" placeholder="Ej.: iré con mi familia"></label>
      }
      <div class="row gap"><button class="btn primary lg grow" [disabled]="f.conVisita === null || (f.conVisita && !f.fecha)" (click)="s.rf({step: 'resumen'})">Continuar</button></div>
    }

    @if (f.step === 'resumen') {
      <h2>Confirma tu reserva</h2>
      <dl class="summary">
        <div><dt>Adelanto de reserva</dt><dd><b>{{ money(dep(f.p)) }}</b></dd></div>
        <div><dt>Plazo para pagar</dt><dd>{{ hours }} horas desde ahora</dd></div>
        <div><dt>Visita</dt><dd>@if (f.conVisita) { <span>{{ f.fecha | date: 'EEEE d MMMM, h:mm a' }}</span> } @else { <span>Sin visita previa</span> }</dd></div>
        @if (s.isStaff()) {<div><dt>Cliente</dt><dd>{{ clienteNombre(f.clienteId) }}</dd></div>}
      </dl>
      <ul class="checklist"><li><ic n="check" [s]="16"/> El inmueble queda apartado para ti durante {{ hours }} horas.</li><li><ic n="check" [s]="16"/> Pagas el adelanto con Yape, Plin o transferencia y subes la captura.</li><li><ic n="check" [s]="16"/> Un agente verifica tu pago y se genera tu contrato.</li><li><ic n="check" [s]="16"/> Si no pagas a tiempo, la reserva se cancela sin costo.</li></ul>
      <label class="check"><input type="checkbox" [ngModel]="f.acepta" (ngModelChange)="s.rf({acepta: $event})"> Entiendo las condiciones de la reserva.</label>
      <div class="row gap"><button class="btn ghost lg" (click)="s.rf({step: 'visita'})">Atrás</button><button class="btn primary lg grow" [disabled]="s.busy() || !f.acepta" (click)="s.confirmReserve()">{{ s.busy() ? 'Reservando…' : 'Confirmar reserva' }}</button></div>
    }

    @if (f.step === 'ok') {
      <div class="success"><span class="big-check"><ic n="check" [s]="34"/></span><h2>¡Reserva confirmada!</h2><p class="muted">Apartamos <b>{{ f.p.titulo }}</b> {{ s.isStaff() ? 'para el cliente' : 'para ti' }}. {{ s.isStaff() ? 'El cliente debe enviar' : 'Ahora envía' }} el comprobante del adelanto.</p></div>
      <dl class="summary">
        <div><dt>N.º de reserva</dt><dd>#{{ f.result.reserva.id }}</dd></div>
        <div><dt>Adelanto a pagar</dt><dd><b>{{ money(dep(f.p)) }}</b></dd></div>
        <div><dt>Vence el</dt><dd>{{ f.result.venceEn | date: "EEEE d MMMM, h:mm a" }}</dd></div>
        @if (f.conVisita) {<div><dt>Visita solicitada</dt><dd>{{ f.fecha | date: 'EEEE d MMMM, h:mm a' }}</dd></div>}
      </dl>
      <div class="notice info"><ic n="info" [s]="18"/><div><b>Cómo pagar</b><p>Yape o Plin al <b>{{ cobro.yape }}</b>, o transferencia BCP <b>{{ cobro.cuenta }}</b> a nombre de {{ cobro.empresa }}. Luego sube la captura en tu espacio.</p></div></div>
      <div class="row gap wrap no-print">
        @if (!s.isStaff()) {<button class="btn primary lg grow" (click)="payNow(f)"><ic n="upload" [s]="18"/> Pagar adelanto ahora</button>}
        <button class="btn ghost lg" (click)="print()"><ic n="print" [s]="18"/> Imprimir</button>
        <button class="btn ghost lg" (click)="goAfter(f)">{{ s.isStaff() ? 'Ver operaciones' : 'Ir a mi espacio' }}</button>
      </div>
    }
  </div></div>}

  <!-- ========== PAGO CON CAPTURA -->
  @if (s.payFor(); as op) {
  <div class="overlay" (click)="s.payFor.set(null)"><div class="modal wide" role="dialog" aria-modal="true" aria-label="Pagar adelanto" (click)="$event.stopPropagation()">
    <button class="close" (click)="s.payFor.set(null)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <p class="eyebrow">Operación #{{ op.o.id }}</p>
    <h2>Paga tu adelanto</h2>
    <p class="amount">{{ money(op.o.monto) }}</p>
    @if (op.pago?.motivoRevision) {<div class="notice warn"><ic n="alert" [s]="18"/><div><b>Tu pago anterior fue observado</b><p>{{ op.pago.motivoRevision }}</p></div></div>}
    <p class="step-title"><span>1</span> Elige cómo pagas</p>
    <div class="methods">@for (m of metodos; track m.id) {<button type="button" class="method" [class.on]="pay.metodo === m.id" (click)="pay.metodo = m.id"><ic [n]="m.icon" [s]="20"/>{{ m.label }}</button>}</div>
    <div class="paybox">
      @if (pay.metodo === 'YAPE' || pay.metodo === 'PLIN') {<p>Envía <b>{{ money(op.o.monto) }}</b> por {{ pay.metodo === 'YAPE' ? 'Yape' : 'Plin' }} a:</p><div class="copy"><b>{{ pay.metodo === 'YAPE' ? cobro.yape : cobro.plin }}</b><button class="btn ghost sm" (click)="copy(pay.metodo === 'YAPE' ? cobro.yape : cobro.plin)">Copiar</button></div><p class="muted">A nombre de {{ cobro.empresa }}</p>}
      @else {<p>{{ pay.metodo === 'DEPOSITO' ? 'Deposita' : 'Transfiere' }} <b>{{ money(op.o.monto) }}</b> a la cuenta {{ cobro.banco }}:</p><div class="copy"><b>{{ cobro.cuenta }}</b><button class="btn ghost sm" (click)="copy(cobro.cuenta)">Copiar</button></div><p class="muted">CCI {{ cobro.cci }} · {{ cobro.empresa }}</p>}
    </div>
    <p class="step-title"><span>2</span> Sube la captura y el número de operación</p>
    <label class="field"><span>N.º de operación</span><input [(ngModel)]="pay.nro" maxlength="40" placeholder="Lo ves en tu comprobante" autocomplete="off"></label>
    <label class="drop" [class.has]="pay.preview"><input type="file" accept="image/png,image/jpeg,image/webp" (change)="onFile($event)">
      @if (pay.preview) {<img [src]="pay.preview" alt="Vista previa de la captura"><span class="muted">Toca para cambiar la imagen</span>} @else {<ic n="upload" [s]="26"/><b>Sube la captura de tu pago</b><span class="muted">PNG, JPG o WEBP · máx. 3 MB</span>}</label>
    <p class="fineprint"><ic n="shield" [s]="14"/> Un agente verificará la captura. Tu reserva sigue activa mientras tanto.</p>
    <div class="row gap"><button class="btn ghost lg" (click)="s.payFor.set(null)">Cancelar</button><button class="btn primary lg grow" [disabled]="s.busy() || !pay.file || !validNro()" (click)="sendPay(op)">{{ s.busy() ? 'Enviando…' : 'Enviar comprobante' }}</button></div>
  </div></div>}

  <!-- ========== AGENDAR VISITA -->
  @if (s.visitFor(); as p) {
  <div class="overlay" (click)="s.visitFor.set(null)"><div class="modal" role="dialog" aria-modal="true" aria-label="Agendar visita" (click)="$event.stopPropagation()">
    <button class="close" (click)="s.visitFor.set(null)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <div class="modal-icon"><ic n="calendar" [s]="26"/></div>
    <h2>Agendar una visita</h2><p class="muted">{{ p.titulo }} · {{ p.ciudad }}</p>
    <div class="two-col"><label class="field"><span>Fecha</span><input type="date" [min]="tomorrow()" [(ngModel)]="vDate"></label>
      <label class="field"><span>Hora</span><select [(ngModel)]="vHour">@for (h of horas; track h) {<option [value]="h">{{ h }}</option>}</select></label></div>
    <label class="field"><span>Comentario para el agente (opcional)</span><input [(ngModel)]="vNote" maxlength="200" placeholder="Ej.: iré con mi familia"></label>
    <p class="fineprint"><ic n="info" [s]="14"/> El agente confirmará tu visita. Verás la respuesta en Mi espacio.</p>
    <button class="btn primary lg block" [disabled]="s.busy() || !vDate" (click)="sendVisit(p)">{{ s.busy() ? 'Enviando…' : 'Solicitar visita' }}</button>
  </div></div>}

  <!-- ========== CONTRATO -->
  @if (s.contractFor(); as op) {
  <div class="overlay" (click)="s.contractFor.set(null)"><div class="modal wide printable" role="dialog" aria-modal="true" aria-label="Contrato" (click)="$event.stopPropagation()">
    <button class="close no-print" (click)="s.contractFor.set(null)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <article class="contract">
      <p class="eyebrow">CASAS · Contrato N.º {{ op.contrato?.id }}</p>
      <h2>Contrato de {{ op.o.tipo === 'COMPRA' ? 'compraventa' : 'alquiler' }} de inmueble</h2>
      <p>Celebrado entre <b>{{ cobro.empresa }}</b>, en calidad de intermediaria, y <b>{{ s.perfil()?.nombres || s.userName() }}</b>{{ s.perfil()?.dni ? ' (DNI ' + s.perfil().dni + ')' : '' }}, en adelante EL CLIENTE.</p>
      <h3>1. Inmueble</h3><p>{{ op.prop?.titulo }}, ubicado en {{ op.prop?.direccion }}, {{ op.prop?.ciudad }}. Superficie {{ op.prop?.areaM2 }} m², {{ op.prop?.habitaciones }} dormitorios y {{ op.prop?.banos }} baños.</p>
      <h3>2. Precio</h3><p>{{ op.o.tipo === 'COMPRA' ? 'El precio de venta es' : 'La renta mensual es' }} <b>{{ money(op.o.total) }}</b>. EL CLIENTE entregó un adelanto de <b>{{ money(op.o.monto) }}</b>, verificado por CASAS.</p>
      <h3>3. Entrega y formalización</h3><p>Un agente de CASAS coordinará con EL CLIENTE la entrega del inmueble y la formalización de la operación según la ley vigente.</p>
      <h3>4. Vigencia</h3><p>Este contrato entra en vigor con la aceptación de EL CLIENTE y su activación por CASAS.</p>
      <p class="fineprint">Documento generado el {{ op.contrato?.createdAt | date: 'd MMMM yyyy' }}. Versión de demostración: no equivale a una escritura pública ni a una firma digital certificada.</p>
    </article>
    @if (op.contrato?.estado === 'PENDIENTE_ACEPTACION') {
      <label class="check no-print"><input type="checkbox" [(ngModel)]="accept"> He leído el contrato y acepto sus condiciones.</label>
      <div class="row gap no-print"><button class="btn ghost lg" (click)="print()"><ic n="print" [s]="18"/> Imprimir</button><button class="btn primary lg grow" [disabled]="s.busy() || !accept" (click)="s.acceptContract(op.contrato)">Aceptar contrato</button></div>
    } @else {<div class="row gap no-print"><badge tone="good">{{ cLabel(op.contrato?.estado) }}</badge><button class="btn ghost" (click)="print()"><ic n="print" [s]="16"/> Imprimir</button></div>}
  </div></div>}

  <!-- ========== DETALLE DE OPERACIÓN -->
  @if (s.opDetail(); as op) {
  <div class="overlay right" (click)="s.opDetail.set(null)"><aside class="drawer printable" role="dialog" aria-modal="true" aria-label="Detalle de la operación" (click)="$event.stopPropagation()">
    <button class="close no-print" (click)="s.opDetail.set(null)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <p class="eyebrow">{{ op.o.tipo === 'COMPRA' ? 'Compra' : 'Alquiler' }} · Operación #{{ op.o.id }}</p>
    <h2>{{ op.prop?.titulo || 'Inmueble #' + op.o.propiedadId }}</h2>
    <div class="row gap"><badge [tone]="oTone(op.o.estado)">{{ oLabel(op.o.estado) }}</badge>@if (s.isStaff()) {<span class="muted">{{ op.staffNote }}</span>}</div>
    <stepper [steps]="op.steps"/>
    <div class="callout" [class]="'callout ' + op.tone"><div><b>{{ op.headline }}</b><p>{{ op.hint }}</p></div></div>
    <h3>Resumen</h3>
    <dl class="summary">
      @if (s.isStaff()) {<div><dt>Cliente</dt><dd>{{ s.clientById().get(op.o.clienteId)?.nombres || '#' + op.o.clienteId }}</dd></div>@if (s.clientById().get(op.o.clienteId); as c) {<div><dt>Contacto</dt><dd>{{ c.email }} {{ c.telefono ? '· ' + c.telefono : '' }}</dd></div>}}
      <div><dt>Precio {{ op.o.tipo === 'COMPRA' ? 'de venta' : 'mensual' }}</dt><dd>{{ money(op.o.total) }}</dd></div>
      <div><dt>Adelanto</dt><dd>{{ money(op.o.monto) }}</dd></div>
      @if (op.reserva) {<div><dt>Reserva</dt><dd>#{{ op.reserva.id }} · {{ op.reserva.estado === 'CONVERTIDA' ? 'convertida' : 'vence ' + (op.reserva.fechaExpiracion | date: 'd MMM, h:mm a') }}</dd></div>}
      @if (op.visita) {<div><dt>Visita</dt><dd>{{ op.visita.fechaHora | date: 'd MMM, h:mm a' }}</dd></div>}
      @if (op.pago) {<div><dt>Pago</dt><dd>{{ pgLabel(op.pago.estado) }}{{ op.pago.metodo ? ' · ' + mLabel(op.pago.metodo) + ' · N.º ' + op.pago.nroOperacion : '' }}</dd></div>}
      @if (op.contrato) {<div><dt>Contrato</dt><dd>#{{ op.contrato.id }} · {{ cLabel(op.contrato.estado) }}</dd></div>}
    </dl>
    <h3>Historial</h3>
    <ol class="timeline">
      @if (op.visita) {<li><b>Visita {{ op.visita.estado === 'REALIZADA' ? 'realizada' : 'solicitada' }}</b><span>{{ op.visita.fechaHora | date: 'd MMM yyyy, h:mm a' }}</span></li>}
      <li><b>Reserva creada</b><span>{{ op.o.createdAt | date: 'd MMM yyyy, h:mm a' }}</span></li>
      @if (op.pago?.metodo) {<li><b>Comprobante enviado ({{ mLabel(op.pago.metodo) }})</b><span>{{ op.pago.updatedAt | date: 'd MMM yyyy, h:mm a' }}</span></li>}
      @if (op.pago?.estado === 'APROBADO') {<li><b>Pago aprobado por un agente</b><span>{{ op.pago.updatedAt | date: 'd MMM yyyy, h:mm a' }}</span></li>}
      @if (op.contrato) {<li><b>Contrato {{ cLabel(op.contrato.estado).toLowerCase() }}</b><span>{{ op.contrato.updatedAt | date: 'd MMM yyyy, h:mm a' }}</span></li>}
      @if (op.cancelled) {<li class="bad"><b>Operación cancelada</b><span>{{ op.o.updatedAt | date: 'd MMM yyyy, h:mm a' }}</span></li>}
    </ol>
    <div class="row gap wrap no-print">
      @if (!s.isStaff() && op.cta === 'pagar') {<button class="btn primary" (click)="s.opDetail.set(null); s.openPay(op)">Pagar adelanto</button>}
      @if (!s.isStaff() && op.cta === 'contrato') {<button class="btn primary" (click)="s.opDetail.set(null); s.contractFor.set(op)">Revisar contrato</button>}
      @if (!s.isStaff() && op.contrato && op.contrato.estado !== 'GENERADO') {<button class="btn ghost" (click)="s.opDetail.set(null); s.contractFor.set(op)">Ver contrato</button>}
      @if (s.isStaff() && op.pago?.comprobante && op.pago.estado === 'PENDIENTE') {<a class="btn primary" href="#/panel/pagos" (click)="s.opDetail.set(null)">Revisar pago</a>}
      @if (s.isStaff() && op.contrato) {<a class="btn ghost" href="#/panel/contratos" (click)="s.opDetail.set(null)">Ir a contratos</a>}
      @if (s.isAdmin() && !op.o.estado.includes('CANCEL') && op.o.estado !== 'CONFIRMADA') {<button class="btn text danger" (click)="s.cancelOrder(op)">Cancelar operación</button>}
      <button class="btn ghost" (click)="print()"><ic n="print" [s]="16"/> Imprimir</button>
    </div>
  </aside></div>}

  <!-- ========== EDITOR DE INMUEBLE -->
  @if (s.propEditor(); as ed) {
  <div class="overlay" (click)="s.propEditor.set(null)"><div class="modal wide" role="dialog" aria-modal="true" aria-label="Inmueble" (click)="$event.stopPropagation()">
    <button class="close" (click)="s.propEditor.set(null)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <h2>{{ ed.id ? 'Editar inmueble' : 'Publicar inmueble' }}</h2>
    <form class="form" #pf="ngForm" (ngSubmit)="pf.valid && saveProp(ed)">
      <label class="field"><span>Título</span><input name="t" required maxlength="160" [(ngModel)]="draft.titulo" placeholder="Ej.: Departamento con balcón en Miraflores"></label>
      <label class="field"><span>Descripción</span><textarea name="d" required rows="4" maxlength="2000" [(ngModel)]="draft.descripcion"></textarea></label>
      <div class="two-col"><label class="field"><span>Ciudad</span><input name="c" required maxlength="120" list="ciudades" [(ngModel)]="draft.ciudad"><datalist id="ciudades">@for (c of ciudades; track c) {<option [value]="c"></option>}</datalist></label>
        <label class="field"><span>Dirección</span><input name="di" required maxlength="220" [(ngModel)]="draft.direccion"></label></div>
      <div class="two-col"><label class="field"><span>Operación</span><select name="o" [(ngModel)]="draft.tipoOperacion"><option value="COMPRA">Venta</option><option value="ALQUILER">Alquiler (precio mensual)</option></select></label>
        <label class="field"><span>Precio (S/)</span><input name="p" type="number" required min="1" [(ngModel)]="draft.precio"></label></div>
      <div class="three-col"><label class="field"><span>Dormitorios</span><input name="h" type="number" required min="0" [(ngModel)]="draft.habitaciones"></label>
        <label class="field"><span>Baños</span><input name="b" type="number" required min="0" [(ngModel)]="draft.banos"></label>
        <label class="field"><span>Área (m²)</span><input name="a" type="number" required min="1" [(ngModel)]="draft.areaM2"></label></div>
      <div class="two-col"><label class="field"><span>Estado</span><select name="e" [(ngModel)]="draft.estado"><option value="DISPONIBLE">Disponible</option><option value="BORRADOR">Borrador</option><option value="INACTIVA">Inactiva</option>@if (!['DISPONIBLE','BORRADOR','INACTIVA'].includes(draft.estado)) {<option [value]="draft.estado">{{ draft.estado }}</option>}</select></label>
        <label class="field"><span>Agente a cargo</span><select name="ag" [(ngModel)]="draft.agenteId"><option [ngValue]="null">Sin asignar</option>@for (a of s.agentes(); track a.id) {<option [ngValue]="a.id">{{ a.nombres }} {{ a.apellidos }}</option>}</select></label></div>
      <label class="field"><span>Foto principal (URL https)</span><input name="im" type="url" maxlength="500" [(ngModel)]="draft.imagenPrincipalUrl" placeholder="https://…"></label>
      @if (draft.imagenPrincipalUrl && draft.imagenPrincipalUrl.startsWith('https://')) {<img class="img-prev" [src]="draft.imagenPrincipalUrl" alt="Vista previa">}
      <div class="row gap"><button class="btn ghost lg" type="button" (click)="s.propEditor.set(null)">Cancelar</button><button class="btn primary lg grow" type="submit" [disabled]="s.busy() || pf.invalid">{{ ed.id ? 'Guardar cambios' : 'Publicar inmueble' }}</button></div>
    </form>
  </div></div>}

  <!-- ========== NUEVO AGENTE -->
  @if (s.agentModal()) {
  <div class="overlay" (click)="s.agentModal.set(false)"><div class="modal" role="dialog" aria-modal="true" aria-label="Nuevo agente" (click)="$event.stopPropagation()">
    <button class="close" (click)="s.agentModal.set(false)" aria-label="Cerrar"><ic n="x" [s]="18"/></button>
    <h2>Nuevo agente</h2><p class="muted">Se crea su usuario de ingreso con rol de agente. Podrá publicar inmuebles, atender visitas y revisar pagos.</p>
    <form class="form" #af="ngForm" (ngSubmit)="af.valid && saveAgent()">
      <div class="two-col"><label class="field"><span>Nombres</span><input name="n" required [(ngModel)]="ag.nombres"></label><label class="field"><span>Apellidos</span><input name="a" required [(ngModel)]="ag.apellidos"></label></div>
      <label class="field"><span>Correo (será su usuario)</span><input name="e" type="email" required [(ngModel)]="ag.email"></label>
      <div class="two-col"><label class="field"><span>Celular</span><input name="t" required pattern="[0-9 ]{9,13}" [(ngModel)]="ag.telefono"></label><label class="field"><span>N.º de matrícula</span><input name="m" required [(ngModel)]="ag.matricula"></label></div>
      <label class="field"><span>Contraseña inicial</span><div class="input-btn"><input name="p" type="text" required minlength="8" [(ngModel)]="ag.password"><button type="button" class="btn ghost sm" (click)="ag.password = pass()">Generar</button></div></label>
      <button class="btn primary lg block" type="submit" [disabled]="s.busy() || af.invalid">{{ s.busy() ? 'Creando…' : 'Crear agente' }}</button>
    </form>
  </div></div>}

  <!-- ========== CONFIRMACIÓN -->
  @if (s.confirmBox(); as c) {
  <div class="overlay" (click)="s.confirmBox.set(null)"><div class="modal small" role="alertdialog" aria-modal="true" (click)="$event.stopPropagation()">
    <div class="modal-icon" [class.danger]="c.danger"><ic [n]="c.danger ? 'alert' : 'info'" [s]="26"/></div>
    <h2>{{ c.title }}</h2><p class="muted">{{ c.text }}</p>
    <div class="row gap"><button class="btn ghost lg grow" (click)="s.confirmBox.set(null)">Volver</button><button class="btn lg grow" [class.danger]="c.danger" [class.primary]="!c.danger" [disabled]="s.busy()" (click)="runConfirm()">{{ c.ok }}</button></div>
  </div></div>}`,
})
export class Modals {
  s = store;
  money = money;
  cobro = COBRO;
  hours = RESERVA_HORAS;
  horas = HORAS;
  ciudades = [...CIUDADES_FALLBACK];
  oLabel = labelOrden; oTone = toneOrden; pgLabel = labelPago; cLabel = labelContrato; mLabel = labelMetodo;
  metodos = [{id: 'YAPE', label: 'Yape', icon: 'phone'}, {id: 'PLIN', label: 'Plin', icon: 'phone'}, {id: 'TRANSFERENCIA', label: 'Transferencia', icon: 'building'}, {id: 'DEPOSITO', label: 'Depósito', icon: 'wallet'}];

  looking = signal(false);
  reg: any = this.emptyReg();
  nc: any = {dni: '', nombres: '', apellidos: '', email: '', telefono: ''};
  pay: any = {metodo: 'YAPE', nro: '', file: null, preview: ''};
  vDate = '';
  vHour = '10:00';
  vNote = '';
  accept = false;
  draft: any = {};
  ag: any = {nombres: '', apellidos: '', email: '', telefono: '', matricula: '', password: ''};

  constructor() {
    effect(() => {
      if (store.payFor()) this.pay = {metodo: 'YAPE', nro: '', file: null, preview: ''};
    });
    effect(() => {
      if (store.visitFor()) {
        this.vDate = '';
        this.vHour = '10:00';
        this.vNote = '';
      }
    });
    effect(() => {
      if (store.contractFor()) this.accept = false;
    });
    effect(() => {
      const e = store.propEditor();
      if (!e) return;
      const mine = store.myAgent()?.id ?? null;
      this.draft = e.id
        ? {titulo: e.titulo, descripcion: e.descripcion, ciudad: e.ciudad, direccion: e.direccion, precio: e.precio, habitaciones: e.habitaciones, banos: e.banos, areaM2: e.areaM2, tipoOperacion: e.tipoOperacion, estado: e.estado, agenteId: e.agenteId ?? null, imagenPrincipalUrl: e.imagenPrincipalUrl || ''}
        : {titulo: '', descripcion: '', ciudad: '', direccion: '', precio: null, habitaciones: 2, banos: 1, areaM2: null, tipoOperacion: 'COMPRA', estado: 'DISPONIBLE', agenteId: store.isAdmin() ? null : mine, imagenPrincipalUrl: ''};
      const cs = new Set([...CIUDADES_FALLBACK, ...store.props().map(p => p.ciudad)]);
      this.ciudades = [...cs];
    });
    effect(() => {
      if (store.agentModal()) this.ag = {nombres: '', apellidos: '', email: '', telefono: '', matricula: '', password: this.pass()};
    });
  }

  emptyReg() {
    return {dni: '', nombres: '', apellidos: '', email: '', telefono: '', password: '', password2: ''};
  }
  pass = genPass;
  img = imageOf;
  dep = deposit;
  tomorrow = () => {
    const d = new Date(Date.now() + 86400000);
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
  };
  date = () => (store.reserveFlow()?.fecha || '').slice(0, 10);
  hour = () => (store.reserveFlow()?.fecha || '').slice(11, 16) || '10:00';
  setDate(d: string, f: any) {
    store.rf({fecha: d ? d + 'T' + (f.fecha?.slice(11, 16) || '10:00') : ''});
  }
  setHour(h: string, f: any) {
    const d = (f.fecha || '').slice(0, 10) || this.tomorrow();
    store.rf({fecha: d + 'T' + h});
  }
  clienteNombre(id: number | null) {
    return (id && store.clientById().get(id)?.nombres) || 'Cliente #' + id;
  }

  // ---- acceso
  openReg() {
    this.reg = this.emptyReg();
    store.authModal.set('registro');
  }
  async dni(target: any) {
    this.looking.set(true);
    try {
      const r = await store.lookupDni(target.dni);
      target.nombres = r.nombres;
      target.apellidos = r.apellidos;
      store.toast('ok', 'Datos encontrados con el DNI.');
    } catch (e: any) {
      store.toast('info', /no configurada|no se pudo consultar/i.test(e.message) ? 'La búsqueda automática de DNI no está disponible por ahora. Completa tus datos manualmente.' : e.message);
    } finally {
      this.looking.set(false);
    }
  }
  async register() {
    const g = this.reg;
    if (g.password !== g.password2) {
      store.toast('err', 'Las contraseñas no coinciden.');
      return;
    }
    const ok = await store.act(async () => {
      await store.registrar(g, g.password);
      store.authModal.set('');
      store.toast('ok', 'Cuenta creada. Ingresa con tu correo y contraseña.');
      await kc.login({loginHint: g.email, redirectUri: location.origin + '/#/mi-espacio'});
    });
    return ok;
  }

  // ---- reserva
  closeFlow() {
    const f = store.reserveFlow();
    store.reserveFlow.set(null);
    if (f?.step === 'ok') store.loadAccount();
  }
  async regClient() {
    const pass = genPass();
    const nc = this.nc;
    let id = 0;
    const ok = await store.act(async () => {
      id = (await store.registrar(nc, pass)).id;
    });
    if (ok) {
      store.rf({clienteId: id, tempPass: pass, step: 'visita'});
      this.nc = {dni: '', nombres: '', apellidos: '', email: '', telefono: ''};
      store.loadAccount();
    }
  }
  payNow(f: any) {
    const id = f.result.orden?.id;
    this.closeFlow();
    setTimeout(() => {
      const op = store.ops().find(x => x.o.id === id);
      if (op) store.openPay(op);
      else store.go('mi-espacio');
    }, 600);
  }
  goAfter(f: any) {
    this.closeFlow();
    store.go(store.isStaff() ? 'panel/operaciones' : 'mi-espacio');
  }
  print() {
    window.print();
  }

  // ---- pago
  validNro() {
    return /^[A-Za-z0-9-]{4,40}$/.test((this.pay.nro || '').trim());
  }
  onFile(ev: any) {
    const file: File | undefined = ev.target.files?.[0];
    if (!file) return;
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type)) {
      store.toast('err', 'La captura debe ser PNG, JPG o WEBP.');
      ev.target.value = '';
      return;
    }
    if (file.size > 3 * 1024 * 1024) {
      store.toast('err', 'La captura pesa más de 3 MB.');
      ev.target.value = '';
      return;
    }
    this.pay.file = file;
    this.pay.preview = URL.createObjectURL(file);
  }
  sendPay(op: OpView) {
    store.submitPay(op, {metodo: this.pay.metodo, nro: this.pay.nro.trim(), file: this.pay.file});
  }
  async copy(text: string) {
    try {
      await navigator.clipboard.writeText(text);
      store.toast('ok', 'Copiado: ' + text);
    } catch {
      store.toast('info', text);
    }
  }
  async sendVisit(p: any) {
    if (await store.scheduleVisit(p, this.vDate + 'T' + this.vHour, this.vNote)) store.go('mi-espacio/visitas');
  }

  // ---- personal
  saveProp(ed: any) {
    store.saveProp(this.draft, ed.id || null);
  }
  async saveAgent() {
    const ok = await store.createAgent(this.ag);
    if (ok) store.agentModal.set(false);
  }
  async runConfirm() {
    const c = store.confirmBox();
    if (!c) return;
    store.confirmBox.set(null);
    await c.run();
  }
}
