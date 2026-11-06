import {Component, computed, signal} from '@angular/core';
import {store} from './store';
import {Ic} from './ui';
import {HomePage, CatalogPage, DetailPage} from './pages-public';
import {AccountPage} from './pages-client';
import {PanelPage} from './panel';
import {Modals} from './modals';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Ic, HomePage, CatalogPage, DetailPage, AccountPage, PanelPage, Modals],
  template: `
  <a class="skip" href="#contenido">Saltar al contenido</a>
  <header class="site-header" [class.slim]="s.route().name === 'panel'">
    <div class="container hbar">
      <a class="brand" href="#/" aria-label="CASAS, inicio"><span class="logo"><ic n="home" [s]="22"/></span><span class="bname">CASAS<small>Casa o nada</small></span></a>
      @if (s.route().name !== 'panel') {
        <nav class="main-nav" [class.open]="menu()" aria-label="Principal">
          <a href="#/propiedades?op=COMPRA" (click)="menu.set(false)">Comprar</a>
          <a href="#/propiedades?op=ALQUILER" (click)="menu.set(false)">Alquilar</a>
          <a href="#/propiedades" (click)="menu.set(false)">Todas las propiedades</a>
          @if (s.isStaff()) {<a class="staff" href="#/panel" (click)="menu.set(false)">Mi panel</a>}
        </nav>
      }
      <div class="actions">
        @if (s.logged()) {
          @if (s.isClient()) {<a class="icon-link" href="#/mi-espacio/avisos" aria-label="Notificaciones"><ic n="bell" [s]="20"/>@if (s.unread()) {<span class="pip">{{ s.unread() }}</span>}</a>
            <a class="icon-link" href="#/mi-espacio/favoritos" aria-label="Favoritos"><ic n="heart" [s]="20"/></a>}
          <a class="userpill" [href]="s.isStaff() ? '#/panel' : '#/mi-espacio'"><span class="avatar">{{ ini() }}</span><span class="un"><b>{{ s.userName() }}</b><small>{{ s.roleLabel() }}</small></span></a>
          <button class="btn ghost sm hide-sm" (click)="s.logout()"><ic n="logout" [s]="16"/> Salir</button>
        } @else {
          <button class="btn ghost hide-sm" (click)="s.authModal.set('ingreso'); s.authReason.set('')">Ingresar</button>
          <button class="btn primary" (click)="openReg()">Crear cuenta</button>
        }
        @if (s.route().name !== 'panel') {<button class="menu-toggle" (click)="menu.set(!menu())" aria-label="Menú"><ic [n]="menu() ? 'x' : 'menu'" [s]="22"/></button>}
      </div>
    </div>
  </header>

  <main id="contenido">
    @switch (s.route().name) {
      @case ('catalog') {<page-catalog/>}
      @case ('detail') {<page-detail/>}
      @case ('account') {<page-account/>}
      @case ('panel') {<page-panel/>}
      @default {<page-home/>}
    }
  </main>

  @if (s.route().name !== 'panel') {
  <footer class="site-footer"><div class="container foot">
    <div><a class="brand light" href="#/"><span class="logo"><ic n="home" [s]="22"/></span><span class="bname">CASAS<small>Casa o nada</small></span></a><p>Compra y alquiler de inmuebles con agentes verificados, pagos revisados y contratos digitales.</p></div>
    <div><h4>Explorar</h4><a href="#/propiedades?op=COMPRA">Comprar</a><a href="#/propiedades?op=ALQUILER">Alquilar</a><a href="#/propiedades">Todas las propiedades</a></div>
    <div><h4>Mi cuenta</h4><a href="#/mi-espacio">Mi espacio</a><a href="#/mi-espacio/visitas">Mis visitas</a><a href="#/mi-espacio/favoritos">Favoritos</a></div>
    <div><h4>Contacto</h4><span>Lima, Perú</span><span>hola&#64;casas.example</span><span>Lun a sáb · 9:00 a 18:00</span></div>
  </div><div class="container legal"><span>© 2026 CASAS · Proyecto académico del Grupo 9</span><span>Los pagos y contratos de este entorno son de demostración.</span></div></footer>}

  <app-modals/>

  <div class="toasts" aria-live="polite">@for (t of s.toasts(); track t.id) {<div class="toast" [class]="'toast ' + t.kind" role="status"><ic [n]="t.kind === 'ok' ? 'check' : t.kind === 'err' ? 'alert' : 'info'" [s]="18"/><span>{{ t.text }}</span><button (click)="s.dismiss(t.id)" aria-label="Cerrar aviso"><ic n="x" [s]="14"/></button></div>}</div>
  @if (s.busy()) {<div class="busybar" aria-hidden="true"></div>}`,
})
export class App {
  s = store;
  menu = signal(false);
  ini = computed(() => (store.userName() || '?').slice(0, 2).toUpperCase());
  constructor() {
    store.init();
  }
  openReg() {
    store.authModal.set('registro');
  }
}
