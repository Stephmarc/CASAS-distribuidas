import '@angular/compiler';
import {LOCALE_ID} from '@angular/core';
import {bootstrapApplication} from '@angular/platform-browser';
import {registerLocaleData} from '@angular/common';
import localeEs from '@angular/common/locales/es-PE';
import {App} from './app';

registerLocaleData(localeEs);

bootstrapApplication(App, {providers: [{provide: LOCALE_ID, useValue: 'es-PE'}]}).catch(console.error);
