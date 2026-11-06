import {Component, computed, effect, signal} from '@angular/core';
import {DecimalPipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {store} from './store';
import {Ic, PropCard, Badge} from './ui';
import {money, deposit, imageOf, labelProp, toneProp, http, RESERVA_HORAS} from './core';

const available = (p: any) => p.estado === 'DISPONIBLE';

// ============================================================ INICIO
@Component({
  selector: 'page-home',
  standalone: true,
  imports: [FormsModule, Ic, PropCard],
  template: `
  <section class="hero">
    <div class="container hero-grid">
      <div class="hero-copy">
        <p class="eyebrow">Compra y alquiler de inmuebles en el Perú</p>
        <h1>Encuentra el lugar donde quieres <em>empezar de nuevo</em>.</h1>
        <p class="lead">Explora, visita, reserva con un adelanto y firma tu contrato. Todo en un solo lugar y con un agente que te acompaña en cada paso.</p>
        <form class="searchbar" (ngSubmit)="search()">
          <div class="seg" role="tablist" aria-label="Tipo de operación">
            <button type="button" role="tab" [class.on]="op === 'COMPRA'" (click)="op = 'COMPRA'">Comprar</button>
            <button type="button" role="tab" [class.on]="op === 'ALQUILER'" (click)="op = 'ALQUILER'">Alquilar</button>
          </div>
          <label class="sb-field"><span>Ubicación</span><input name="q" [(ngModel)]="q" placeholder="Distrito, ciudad o palabra clave" autocomplete="off"></label>
          <label class="sb-field"><span>Presupuesto máximo</span>
            <select name="max" [(ngModel)]="max"><option value="">Sin límite</option>@for (m of budgets(); track m) {<option [value]="m">Hasta {{ fmt(m) }}</option>}</select></label>
          <button class="btn primary lg" type="submit"><ic n="search" [s]="18"/> Buscar</button>
        </form>
        <ul class="trust"><li><ic n="shield" [s]="16"/> Pagos verificados por un agente</li><li><ic n="calendar" [s]="16"/> Visitas sin compromiso</li><li><ic n="file" [s]="16"/> Contrato digital</li></ul>
      </div>
      <div class="hero-visual" aria-hidden="true">
        @for (p of heroProps(); track p.id; let i = $index) {<a class="hv" [class]="'hv hv' + i" [href]="'#/propiedad/' + p.id"><img [src]="img(p)" alt=""><span>{{ money(p.precio) }}</span></a>}
        @if (!heroProps().length) {<div class="hv hv0 empty"><ic n="home" [s]="64"/></div>}
      </div>
    </div>
  </section>

  <section class="statband"><div class="container">
    <div><b>{{ stats().n }}</b><span>inmuebles disponibles</span></div>
    <div><b>{{ stats().cities }}</b><span>ciudades</span></div>
    <div><b>{{ stats().sale }}</b><span>en venta</span></div>
    <div><b>{{ stats().rent }}</b><span>en alquiler</span></div>
  </div></section>

  <section class="section container">
    <div class="section-head"><div><p class="eyebrow">Selección CASAS</p><h2>Propiedades destacadas</h2></div><a class="btn ghost" href="#/propiedades">Ver todas <ic n="arrow" [s]="16"/></a></div>
    @if (!s.propsLoaded()) {<div class="grid-cards">@for (i of [1,2,3]; track i) {<div class="skeleton card-sk"></div>}</div>}
    @else if (!s.propsOk()) {<div class="empty-state"><ic n="alert" [s]="30"/><h3>No pudimos cargar el catálogo</h3><p>Comprueba que los servicios estén encendidos e inténtalo otra vez.</p><button class="btn primary" (click)="s.loadProps()">Reintentar</button></div>}
    @else {<div class="grid-cards">@for (p of featured(); track p.id) {<prop-card [p]="p"/>} @empty {<div class="empty-state"><ic n="home" [s]="30"/><h3>Aún no hay inmuebles publicados</h3><p>Vuelve pronto.</p></div>}</div>}
  </section>

  <section class="section alt"><div class="container">
    <div class="section-head center"><div><p class="eyebrow">Así funciona</p><h2>De la primera visita a tus llaves, en cinco pasos</h2></div></div>
    <ol class="how">
      <li><span class="n">1</span><ic n="search" [s]="26"/><h3>Explora</h3><p>Filtra por ciudad, precio y tipo. Guarda tus favoritos.</p></li>
      <li><span class="n">2</span><ic n="calendar" [s]="26"/><h3>Visita (opcional)</h3><p>Agenda una visita con el agente antes de decidir.</p></li>
      <li><span class="n">3</span><ic n="lock" [s]="26"/><h3>Reserva</h3><p>Aparta el inmueble {{ hours }} horas con un adelanto pequeño.</p></li>
      <li><span class="n">4</span><ic n="wallet" [s]="26"/><h3>Paga y verifica</h3><p>Yape, Plin o transferencia. Sube la captura y un agente la valida.</p></li>
      <li><span class="n">5</span><ic n="file" [s]="26"/><h3>Firma tu contrato</h3><p>Revisa y acepta el contrato desde tu espacio. Listo.</p></li>
    </ol>
  </div></section>

  @if (cities().length) {<section class="section container">
    <div class="section-head"><div><p class="eyebrow">Por ciudad</p><h2>Explora dónde vivir</h2></div></div>
    <div class="chips">@for (c of cities(); track c[0]) {<a class="chip lg" [href]="'#/propiedades?q=' + c[0]">{{ c[0] }} <b>{{ c[1] }}</b></a>}</div>
  </section>}

  <section class="section container">
    <div class="cta-band">
      <div><h2>¿Vendes o alquilas inmuebles?</h2><p>Los agentes de CASAS publican, atienden visitas y validan pagos desde su propio panel.</p></div>
      @if (s.isStaff()) {<a class="btn light lg" href="#/panel">Ir a mi panel</a>} @else {<button class="btn light lg" (click)="s.login('panel')">Ingreso para agentes</button>}
    </div>
  </section>`,
})
export class HomePage {
  s = store;
  money = money;
  hours = RESERVA_HORAS;
  op = 'COMPRA';
  q = '';
  max = '';
  avail = computed(() => store.props().filter(available));
  featured = computed(() => [...this.avail()].sort((a, b) => Number(!!imageOf(b)) - Number(!!imageOf(a)) || b.id - a.id).slice(0, 6));
  heroProps = computed(() => this.avail().filter(p => imageOf(p)).slice(0, 3));
  stats = computed(() => {
    const a = this.avail();
    return {n: a.length, cities: new Set(a.map(p => p.ciudad)).size, sale: a.filter(p => p.tipoOperacion === 'COMPRA').length, rent: a.filter(p => p.tipoOperacion === 'ALQUILER').length};
  });
  cities = computed(() => {
    const m = new Map<string, number>();
    this.avail().forEach(p => m.set(p.ciudad, (m.get(p.ciudad) || 0) + 1));
    return [...m.entries()].sort((a, b) => b[1] - a[1]).slice(0, 10);
  });
  budgets = computed(() => (this.op === 'ALQUILER' ? [1500, 2500, 4000, 6000] : [150000, 300000, 500000, 800000]));
  fmt = money;
  img = imageOf;
  search() {
    const p = new URLSearchParams({op: this.op});
    if (this.q.trim()) p.set('q', this.q.trim());
    if (this.max) p.set('max', this.max);
    location.hash = '#/propiedades?' + p.toString();
  }
}

// ============================================================ CATÁLOGO
interface Filters {op: string; q: string; max: string; beds: string; sort: string; avail: boolean}

@Component({
  selector: 'page-catalog',
  standalone: true,
  imports: [FormsModule, Ic, PropCard],
  template: `
  <section class="page-head"><div class="container"><p class="eyebrow">Catálogo</p><h1>{{ title() }}</h1><p class="muted">{{ results().length }} {{ results().length === 1 ? 'inmueble encontrado' : 'inmuebles encontrados' }}</p></div></section>
  <div class="container catalog">
    <aside class="filters" [class.open]="open()">
      <div class="filters-head"><h2><ic n="sliders" [s]="18"/> Filtros</h2><button class="btn ghost sm" (click)="clear()">Limpiar</button></div>
      <label class="field"><span>Buscar</span><input [ngModel]="f().q" (ngModelChange)="set('q', $event)" placeholder="Ciudad, distrito o palabra" autocomplete="off"></label>
      <div class="field"><span>Operación</span>
        <div class="seg block"><button type="button" [class.on]="f().op === ''" (click)="set('op', '')">Todas</button><button type="button" [class.on]="f().op === 'COMPRA'" (click)="set('op', 'COMPRA')">Comprar</button><button type="button" [class.on]="f().op === 'ALQUILER'" (click)="set('op', 'ALQUILER')">Alquilar</button></div></div>
      <label class="field"><span>Precio máximo</span><select [ngModel]="f().max" (ngModelChange)="set('max', $event)"><option value="">Sin límite</option>@for (m of budgets(); track m) {<option [value]="m">Hasta {{ fmt(m) }}</option>}</select></label>
      <label class="field"><span>Dormitorios</span><select [ngModel]="f().beds" (ngModelChange)="set('beds', $event)"><option value="">Cualquiera</option><option value="1">1 o más</option><option value="2">2 o más</option><option value="3">3 o más</option><option value="4">4 o más</option></select></label>
      <label class="check"><input type="checkbox" [ngModel]="f().avail" (ngModelChange)="set('avail', $event)"> Solo disponibles</label>
      <button class="btn primary block mobile-only" (click)="open.set(false)">Ver {{ results().length }} resultados</button>
    </aside>
    <section class="results">
      <div class="toolbar">
        <button class="btn ghost sm mobile-only" (click)="open.set(true)"><ic n="filter" [s]="16"/> Filtros</button>
        <span class="muted">Mostrando {{ visible().length }} de {{ results().length }}</span>
        <label class="inline-select"><span>Ordenar por</span><select [ngModel]="f().sort" (ngModelChange)="set('sort', $event)"><option value="recent">Más recientes</option><option value="asc">Precio: menor a mayor</option><option value="desc">Precio: mayor a menor</option><option value="area">Mayor superficie</option></select></label>
      </div>
      @if (!s.propsLoaded()) {<div class="grid-cards">@for (i of [1,2,3,4]; track i) {<div class="skeleton card-sk"></div>}</div>}
      @else if (!s.propsOk()) {<div class="empty-state"><ic n="alert" [s]="30"/><h3>No pudimos cargar el catálogo</h3><p>Los servicios no responden. Inténtalo de nuevo en unos segundos.</p><button class="btn primary" (click)="s.loadProps()">Reintentar</button></div>}
      @else if (!results().length) {<div class="empty-state"><ic n="search" [s]="30"/><h3>No encontramos inmuebles con esos filtros</h3><p>Prueba ampliando el precio o quitando algún filtro.</p><button class="btn primary" (click)="clear()">Quitar filtros</button></div>}
      @else {<div class="grid-cards">@for (p of visible(); track p.id) {<prop-card [p]="p"/>}</div>
        @if (visible().length < results().length) {<div class="more"><button class="btn ghost lg" (click)="shown.set(shown() + 12)">Mostrar más inmuebles</button></div>}}
    </section>
  </div>`,
})
export class CatalogPage {
  s = store;
  fmt = money;
  open = signal(false);
  shown = signal(12);
  f = signal<Filters>({op: '', q: '', max: '', beds: '', sort: 'recent', avail: true});
  constructor() {
    effect(() => {
      const r = store.route();
      if (r.name !== 'catalog') return;
      this.f.set({op: r.q['op'] || '', q: r.q['q'] || '', max: r.q['max'] || '', beds: r.q['beds'] || '', sort: r.q['sort'] || 'recent', avail: r.q['all'] !== '1'});
      this.shown.set(12);
    });
  }
  set(k: keyof Filters, v: any) {
    this.f.update(x => ({...x, [k]: v}));
    this.shown.set(12);
  }
  clear() {
    this.f.set({op: '', q: '', max: '', beds: '', sort: 'recent', avail: true});
    location.hash = '#/propiedades';
  }
  title = computed(() => (this.f().op === 'COMPRA' ? 'Inmuebles en venta' : this.f().op === 'ALQUILER' ? 'Inmuebles en alquiler' : 'Todas las propiedades'));
  budgets = computed(() => (this.f().op === 'ALQUILER' ? [1500, 2500, 4000, 6000] : this.f().op === 'COMPRA' ? [150000, 300000, 500000, 800000, 1200000] : [2500, 6000, 150000, 300000, 500000, 800000]));
  results = computed(() => {
    const f = this.f(), q = f.q.trim().toLowerCase();
    let r = store.props().filter(p => !['BORRADOR', 'INACTIVA'].includes(p.estado));
    if (f.avail) r = r.filter(available);
    if (f.op) r = r.filter(p => p.tipoOperacion === f.op);
    if (q) r = r.filter(p => (p.titulo + ' ' + p.ciudad + ' ' + p.direccion).toLowerCase().includes(q));
    if (f.max) r = r.filter(p => p.precio <= Number(f.max));
    if (f.beds) r = r.filter(p => p.habitaciones >= Number(f.beds));
    return [...r].sort((a, b) => (f.sort === 'asc' ? a.precio - b.precio : f.sort === 'desc' ? b.precio - a.precio : f.sort === 'area' ? b.areaM2 - a.areaM2 : b.id - a.id));
  });
  visible = computed(() => this.results().slice(0, this.shown()));
}

// ============================================================ DETALLE
@Component({
  selector: 'page-detail',
  standalone: true,
  imports: [FormsModule, DecimalPipe, Ic, PropCard, Badge],
  template: `
  @if (!s.propsLoaded()) {<div class="container section"><div class="skeleton" style="height:420px"></div></div>}
  @else if (!p()) {<div class="container section"><div class="empty-state"><ic n="search" [s]="30"/><h3>No encontramos este inmueble</h3><p>Puede que ya no esté publicado.</p><a class="btn primary" href="#/propiedades">Ver el catálogo</a></div></div>}
  @else {
  <div class="container detail">
    <nav class="crumbs" aria-label="Ruta"><a href="#/">Inicio</a><span>/</span><a href="#/propiedades">Propiedades</a><span>/</span><b>{{ p().titulo }}</b></nav>
    <header class="detail-head">
      <div>
        <div class="row gap"><span class="tag inline" [class.rent]="p().tipoOperacion === 'ALQUILER'">{{ p().tipoOperacion === 'COMPRA' ? 'En venta' : 'En alquiler' }}</span><badge [tone]="tone(p().estado)">{{ label(p().estado) }}</badge></div>
        <h1>{{ p().titulo }}</h1>
        <p class="loc"><ic n="pin" [s]="16"/> {{ p().direccion }}, {{ p().ciudad }}</p>
      </div>
      <div class="row gap"><button class="btn ghost" (click)="s.toggleFav(p().id)"><ic n="heart" [s]="16"/> {{ s.isFav(p().id) ? 'Guardado' : 'Guardar' }}</button><button class="btn ghost" (click)="share()"><ic n="external" [s]="16"/> Compartir</button></div>
    </header>

    <div class="detail-grid">
      <div class="detail-main">
        <figure class="gallery">@if (img()) {<img [src]="img()" [alt]="p().titulo" (error)="broken.set(true)">} @else {<div class="noimg big"><ic n="home" [s]="72"/><span>Sin fotografía</span></div>}</figure>
        <ul class="keyfacts">
          <li><ic n="bed" [s]="22"/><b>{{ p().habitaciones }}</b><span>Dormitorios</span></li>
          <li><ic n="bath" [s]="22"/><b>{{ p().banos }}</b><span>Baños</span></li>
          <li><ic n="area" [s]="22"/><b>{{ p().areaM2 }} m²</b><span>Superficie</span></li>
          <li><ic n="tag" [s]="22"/><b>{{ money(ppm2()) }}</b><span>Por m²</span></li>
        </ul>
        <section class="card"><h2>Descripción</h2><p class="prose">{{ p().descripcion }}</p></section>
        <section class="card"><h2>Ubicación</h2><p class="row gap"><ic n="pin" [s]="18"/> {{ p().direccion }}, {{ p().ciudad }}, Perú</p>
          @if (geo(); as g) {
            @if (g.disponible) {<ul class="geo"><li><span>Ciudad</span><b>{{ g.ciudad }}</b></li><li><span>Región</span><b>{{ g.region }}</b></li><li><span>País</span><b>{{ g.pais }}</b></li><li><span>Coordenadas de referencia</span><b>{{ g.latitud | number: '1.4-4' }}, {{ g.longitud | number: '1.4-4' }}</b></li></ul>
              <p class="fineprint"><ic n="info" [s]="14"/> Referencia urbana obtenida en tiempo real de {{ g.proveedor }}; no es el domicilio exacto del inmueble.</p>
              <a class="btn ghost" target="_blank" rel="noopener" [href]="osmUrl(g)"><ic n="external" [s]="16"/> Ver la ciudad en el mapa</a>}
            @else {<p class="muted small">{{ g.mensaje }}</p><a class="btn ghost" target="_blank" rel="noopener" [href]="mapUrl()"><ic n="external" [s]="16"/> Ver en el mapa</a>}
          } @else {<a class="btn ghost" target="_blank" rel="noopener" [href]="mapUrl()"><ic n="external" [s]="16"/> Ver en el mapa</a>}</section>
        <section class="card agent">
          <h2>Tu agente</h2>
          @if (agent(); as a) {<div class="agent-row"><span class="avatar lg">{{ ini(a) }}</span><div><b>{{ a.nombres }} {{ a.apellidos }}</b><p class="muted">Agente inmobiliario · Mat. {{ a.matricula }}</p>
              <div class="row gap wrap"><span class="muted"><ic n="phone" [s]="14"/> {{ a.telefono || 'Sin teléfono' }}</span><span class="muted"><ic n="mail" [s]="14"/> {{ a.email }}</span></div></div></div>}
          @else {<div class="agent-row"><span class="avatar lg"><ic n="user" [s]="22"/></span><div><b>Equipo CASAS</b><p class="muted">{{ s.logged() ? 'Este inmueble aún no tiene un agente asignado.' : 'Inicia sesión para ver los datos de contacto de tu agente.' }}</p></div></div>}
        </section>
      </div>

      <aside class="booking">
        <div class="card sticky">
          <div class="bprice">{{ money(p().precio) }}@if (p().tipoOperacion === 'ALQUILER') {<small> / mes</small>}</div>
          @if (p().estado === 'DISPONIBLE') {
            <div class="deposit"><ic n="lock" [s]="16"/><div>Aparta este inmueble con un adelanto de <b>{{ money(dep()) }}</b> <span class="muted">({{ p().tipoOperacion === 'ALQUILER' ? 'un mes de renta' : '1 % del precio' }})</span></div></div>
            <button class="btn primary lg block" [disabled]="s.busy()" (click)="s.startReserve(p())">{{ s.isStaff() ? 'Reservar para un cliente' : 'Reservar ahora' }}</button>
            @if (!s.isStaff()) {<button class="btn ghost lg block" (click)="visit()"><ic n="calendar" [s]="18"/> Agendar una visita</button>}
            <ol class="mini-steps"><li>Reservas por {{ hours }} horas.</li><li>Pagas el adelanto con Yape, Plin o transferencia.</li><li>Un agente verifica tu pago.</li><li>Aceptas tu contrato.</li></ol>
          } @else {
            <div class="notice warn"><ic n="info" [s]="18"/><div><b>Este inmueble ya no está disponible</b><p>Estado: {{ label(p().estado) }}. Mira otras opciones parecidas.</p></div></div>
            <a class="btn primary block" href="#/propiedades">Ver más propiedades</a>
          }
          <p class="fineprint"><ic n="shield" [s]="14"/> Tu adelanto se confirma solo cuando un agente verifica el comprobante.</p>
        </div>
      </aside>
    </div>

    @if (similar().length) {<section class="section"><div class="section-head"><h2>Inmuebles similares</h2></div><div class="grid-cards">@for (x of similar(); track x.id) {<prop-card [p]="x"/>}</div></section>}
  </div>}`,
})
export class DetailPage {
  s = store;
  money = money;
  label = labelProp;
  tone = toneProp;
  hours = RESERVA_HORAS;
  broken = signal(false);
  agentData = signal<any>(null);
  geo = signal<any>(null);
  p = computed(() => store.propById().get(Number(store.route().parts[0])));
  img = computed(() => (this.broken() ? '' : imageOf(this.p())));
  dep = computed(() => deposit(this.p()));
  ppm2 = computed(() => (this.p() ? this.p().precio / Math.max(1, this.p().areaM2) : 0));
  agent = computed(() => this.agentData() || (this.p() ? store.agentById().get(this.p().agenteId) : null) || null);
  similar = computed(() => {
    const p = this.p();
    return p ? store.props().filter(x => x.id !== p.id && x.estado === 'DISPONIBLE' && x.tipoOperacion === p.tipoOperacion).sort((a, b) => Number(b.ciudad === p.ciudad) - Number(a.ciudad === p.ciudad) || Math.abs(a.precio - p.precio) - Math.abs(b.precio - p.precio)).slice(0, 3) : [];
  });
  constructor() {
    effect(() => {
      const p = this.p();
      this.broken.set(false);
      this.agentData.set(null);
      this.geo.set(null);
      if (p?.ciudad) http('/propiedades/ubicaciones?ciudad=' + encodeURIComponent(p.ciudad)).then(g => this.geo.set(g)).catch(() => {});
      if (p?.agenteId && store.logged()) http('/agentes/' + p.agenteId).then(a => this.agentData.set(a)).catch(() => {});
    });
  }
  ini(a: any) {
    return ((a.nombres?.[0] || '') + (a.apellidos?.[0] || '')).toUpperCase();
  }
  osmUrl(g: any) {
    return 'https://www.openstreetmap.org/?mlat=' + g.latitud + '&mlon=' + g.longitud + '#map=12/' + g.latitud + '/' + g.longitud;
  }
  mapUrl() {
    const p = this.p();
    return 'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(p.direccion + ', ' + p.ciudad + ', Perú');
  }
  visit() {
    if (store.requireLogin('Para agendar una visita necesitas una cuenta.')) store.visitFor.set(this.p());
  }
  async share() {
    const url = location.href;
    try {
      if (navigator.share) await navigator.share({title: this.p().titulo, url});
      else {
        await navigator.clipboard.writeText(url);
        store.toast('ok', 'Enlace copiado al portapapeles.');
      }
    } catch { /* cancelado */ }
  }
}
