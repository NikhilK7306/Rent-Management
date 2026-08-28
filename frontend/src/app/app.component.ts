import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastContainerComponent } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastContainerComponent],
  template: `
    <router-outlet />
    <app-toast-container />
  `,
  styles: []
})
export class AppComponent {
  title = 'Rent Management System';
}