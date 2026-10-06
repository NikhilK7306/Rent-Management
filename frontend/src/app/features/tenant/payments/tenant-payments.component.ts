import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-tenant-payments',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tenant-payments.component.html',
  styleUrl: './tenant-payments.component.scss'
})
export class TenantPaymentsComponent {
  private authService = inject(AuthService);

  getCurrentUser() {
    return this.authService.currentUser;
  }
}