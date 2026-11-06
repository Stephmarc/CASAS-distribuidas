import {Component, input, computed, signal} from '@angular/core';
import {store, Step} from './store';
import {money, deposit, imageOf, labelProp, toneProp, Tone} from './core';

const ICONS: Record<string, string> = {
  home: 'M3 11l9-8 9 8M5 10v10h14V10M10 20v-6h4v6',
  search: 'M11 19a8 8 0 100-16 8 8 0 000 16zM21 21l-4.3-4.3',
  bed: 'M3 19v-9a2 2 0 012-2h14a2 2 0 012 2v9M3 15h18M3 19v1M21 19v1M7 8V6h4v2',
  bath: 'M4 12h16v3a4 4 0 01-4 4H8a4 4 0 01-4-4v-3zM6 12V6a2 2 0 014 0M7 19l-1 2M17 19l1 2',
  area: 'M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5',
  pin: 'M12 21s7-6.2 7-11a7 7 0 10-14 0c0 4.8 7 11 7 11zM12 12a2.5 2.5 0 100-5 2.5 2.5 0 000 5z',
  heart: 'M12 20s-7-4.6-9-9.2C1.6 7 4 4 7 4c2 0 3.5 1 5 3 1.5-2 3-3 5-3 3 0 5.4 3 4 6.8C19 15.4 12 20 12 20z',
  calendar: 'M4 6h16v14H4zM4 10h16M8 3v4M16 3v4',
  user: 'M12 12a4 4 0 100-8 4 4 0 000 8zM4 21a8 8 0 0116 0',
  users: 'M9 11a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2 20a7 7 0 0114 0M17 4.5a3.5 3.5 0 010 6.5M18 14a6 6 0 013.5 6',
  check: 'M5 12.5l4.5 4.5L19 7.5',
  x: 'M6 6l12 12M18 6L6 18',
  bell: 'M6 17v-6a6 6 0 1112 0v6l2 2H4zM10 21h4',
  file: 'M7 3h7l5 5v13H7zM14 3v5h5M10 13h6M10 17h6',
  wallet: 'M3 7h18v12H3zM3 11h18M16 15h2',
  shield: 'M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6zM9 12l2 2 4-4',
  chart: 'M4 20V4M4 20h16M8 16v-5M12 16V8M16 16v-8',
  building: 'M5 21V4h9v17M14 9h5v12M8 8h3M8 12h3M8 16h3M3 21h18',
  inbox: 'M3 13l3-8h12l3 8v6H3zM3 13h5l1 3h6l1-3h5',
  logout: 'M10 4H4v16h6M16 8l4 4-4 4M20 12H9',
  plus: 'M12 5v14M5 12h14',
  edit: 'M4 20h4L19 9l-4-4L4 16zM13 7l4 4',
  trash: 'M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6',
  eye: 'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12zM12 15a3 3 0 100-6 3 3 0 000 6z',
  phone: 'M5 4h4l2 5-2.5 1.5a11 11 0 005 5L15 13l5 2v4a2 2 0 01-2 2A16 16 0 013 6a2 2 0 012-2z',
  mail: 'M3 5h18v14H3zM3 7l9 7 9-7',
  clock: 'M12 21a9 9 0 100-18 9 9 0 000 18zM12 7v5l3 2',
  tag: 'M3 12V4h8l10 10-8 8zM7.5 8.5h.01',
  key: 'M14 10a4 4 0 11-3 6.5L4 20v-3l2-2h2v-2l2-2a4 4 0 014-1zM16 8h.01',
  upload: 'M12 16V4M7 9l5-5 5 5M4 20h16',
  image: 'M3 4h18v16H3zM3 16l5-5 4 4 3-3 6 6M8.5 9h.01',
  arrow: 'M5 12h14M13 6l6 6-6 6',
  back: 'M19 12H5M11 6l-6 6 6 6',
  filter: 'M3 5h18l-7 8v6l-4 2v-8z',
  menu: 'M4 7h16M4 12h16M4 17h16',
  external: 'M14 4h6v6M20 4l-9 9M18 14v6H4V6h6',
  print: 'M7 9V3h10v6M6 17H3v-7h18v7h-3M7 14h10v7H7z',
  refresh: 'M20 11a8 8 0 00-14-4M4 4v4h4M4 13a8 8 0 0014 4M20 20v-4h-4',
  info: 'M12 21a9 9 0 100-18 9 9 0 000 18zM12 11v6M12 7.5h.01',
  alert: 'M12 3l10 18H2zM12 10v5M12 18h.01',
  download: 'M12 4v12M7 11l5 5 5-5M4 20h16',
  dots: 'M5 12h.01M12 12h.01M19 12h.01',
  gift: 'M4 11h16v9H4zM3 8h18v3H3zM12 8v12M12 8c-3 0-5-1-5-3s3-2 5 3c2-5 5-5 5-3s-2 3-5 3',
  star: 'M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z',
  sliders: 'M4 6h10M18 6h2M4 12h4M12 12h8M4 18h12M20 18h0M14 4v4M8 10v4M16 16v4',
  lock: 'M6 11h12v10H6zM8 11V8a4 4 0 118 0v3',
  badge: 'M12 3l2.4 2 3.1-.2.9 3 2.6 1.8-1 2.9 1 2.9-2.6 1.8-.9 3-3.1-.2L12 21l-2.4-2-3.1.2-.9-3L3 14.4l1-2.9-1-2.9L5.6 6.8l.9-3 3.1.2zM9 12l2 2 4-4',
};

@Component({
  selector: 'ic',
  standalone: true,
  template: `<svg [attr.width]="s()" [attr.height]="s()" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="d()"/></svg>`,
  styles: [':host{display:inline-flex;vertical-align:middle;flex-shrink:0}'],
})
export class Ic {
  n = input.required<string>();
  s = input(18);
  d = computed(() => ICONS[this.n()] || ICONS['info']);
}

@Component({
  selector: 'badge',
  standalone: true,
  template: `<span class="badge" [class]="'badge ' + tone()"><ng-content/></span>`,
  styles: [':host{display:inline-flex;align-self:flex-start}'],
})
export class Badge {
  tone = input<Tone>('neutral');
}

@Component({
  selector: 'stepper',
  standalone: true,
  imports: [Ic],
  template: `<ol class="stepper">@for (st of steps(); track st.label; let i = $index) {
    <li [class]="'step ' + st.state"><span class="dot">@if (st.state === 'done') {<ic n="check" [s]="14"/>} @else if (st.state === 'fail') {<ic n="x" [s]="14"/>} @else { <b>{{ i + 1 }}</b> }</span><span class="lbl">{{ st.label }}</span></li>
  }</ol>`,
})
export class Stepper {
  steps = input.required<Step[]>();
}

@Component({
  selector: 'prop-card',
  standalone: true,
  imports: [Ic, Badge],
  template: `
  <article class="pcard" [class.off]="p().estado !== 'DISPONIBLE'">
    <a class="pimg" [href]="'#/propiedad/' + p().id" [attr.aria-label]="'Ver ' + p().titulo">
      @if (img()) {<img [src]="img()" [alt]="p().titulo" loading="lazy" (error)="broken.set(true)">} @else {<div class="noimg"><ic n="home" [s]="42"/></div>}
      <span class="tag" [class.rent]="p().tipoOperacion === 'ALQUILER'">{{ p().tipoOperacion === 'COMPRA' ? 'En venta' : 'En alquiler' }}</span>
      @if (p().estado !== 'DISPONIBLE') {<span class="ribbon">{{ label(p().estado) }}</span>}
    </a>
    <button class="fav" [class.on]="s.isFav(p().id)" (click)="s.toggleFav(p().id)" [attr.aria-label]="s.isFav(p().id) ? 'Quitar de favoritos' : 'Guardar en favoritos'"><ic n="heart" [s]="18"/></button>
    <div class="pbody">
      <div class="price">{{ money(p().precio) }}@if (p().tipoOperacion === 'ALQUILER') {<small> / mes</small>}</div>
      <h3><a [href]="'#/propiedad/' + p().id">{{ p().titulo }}</a></h3>
      <p class="loc"><ic n="pin" [s]="14"/>{{ p().ciudad }} · {{ p().direccion }}</p>
      <ul class="specs"><li><ic n="bed" [s]="15"/>{{ p().habitaciones }} dorm.</li><li><ic n="bath" [s]="15"/>{{ p().banos }} baños</li><li><ic n="area" [s]="15"/>{{ p().areaM2 }} m²</li></ul>
    </div>
  </article>`,
})
export class PropCard {
  p = input.required<any>();
  s = store;
  money = money;
  broken = signal(false);
  label = labelProp;
  img = computed(() => (this.broken() ? '' : imageOf(this.p())));
}

export {deposit, toneProp};
