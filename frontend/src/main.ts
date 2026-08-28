import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withEnabledBlockingInitialNavigation } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/interceptors/auth.interceptor';
import { provideEnvironment } from './app/core/services/environment.service';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes, withEnabledBlockingInitialNavigation()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations(),
    provideEnvironment()
  ]
}).catch(err => console.error(err));