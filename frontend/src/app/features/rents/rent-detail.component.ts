import { Component, inject, input, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Rent, RentStatus } from '@features/rents/rent.model';
import { Property } from '@core/models/property.model';
import { TenantPropertyRequest } from '@core/models/tenant.model';
import { TenantService } from '@core/services/tenant.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-rent-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rent-detail.component.html',
  styleUrl: './rent-detail.component.scss'
})
export class RentDetailComponent {
  rent = input.required<any>();
  isOpen = input.required<boolean>();
  assignableProperties = input<Property[]>([]);
  close = output<void>();

  private fb = inject(FormBuilder);
  private tenantService = inject(TenantService);

  isAssigning = signal(false);
  isChanging = signal(false);
  isUnassigning = signal(false);
  isUpdatingStatus = signal(false);
  errorMessage = signal('');
  showAssignModal = signal(false);
  showChangeModal = signal(false);
  showUnassignConfirm = signal(false);
  showStatusConfirm = signal(false);

  assignForm = this.fb.nonNullable.group({
    propertyId: [0, Validators.required]
  });

  // Computed property for tenant
  tenant = computed(() => this.rent()?.tenant);

  // Computed property to check if tenant is active
  isTenantActive = computed(() => this.rent()?.status === 'ACTIVE');

  // Computed property to check if currently assigned property is active
  isAssignedPropertyActive = computed(() => {
    const prop = this.rent()?.property;
    return prop !== null; // Property is active if assigned (backend ensures this)
  });

  // Computed property to check if assignment is allowed
  canAssignProperty = computed(() => this.isTenantActive());

  // Computed property to check if there are assignable properties
  hasAssignableProperties = computed(() => this.assignableProperties().length > 0);

  // Computed property to check if there are other assignable properties (excluding current)
  hasOtherAssignableProperties = computed(() => {
    const currentPropId = this.rent()?.property?.id;
    return this.assignableProperties().filter(p => p.id !== currentPropId).length > 0;
  });

  openAssignModal(): void {
    if (!this.canAssignProperty()) return;
    this.showAssignModal.set(true);
  }

  closeAssignModal(): void {
    this.showAssignModal.set(false);
    this.assignForm.reset({ propertyId: 0 });
  }

  openChangeModal(): void {
    if (!this.canAssignProperty()) return;
    this.showChangeModal.set(true);
  }

  closeChangeModal(): void {
    this.showChangeModal.set(false);
    this.assignForm.reset({ propertyId: 0 });
  }

  confirmAssign(): void {
    if (this.assignForm.invalid) return;

    const t = this.rent();
    if (!t) return;

    const propertyId = this.assignForm.getRawValue().propertyId;
    if (propertyId === 0) {
      this.errorMessage.set('Please select a property.');
      return;
    }

    this.isAssigning.set(true);
    this.errorMessage.set('');

    const request: TenantPropertyRequest = { propertyId };

    this.tenantService.assignProperty(t.id, request).subscribe({
      next: () => {
        this.isAssigning.set(false);
        this.closeAssignModal();
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isAssigning.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to assign property.');
      }
    });
  }

  confirmChange(): void {
    if (this.assignForm.invalid) return;

    const t = this.rent();
    if (!t) return;

    const propertyId = this.assignForm.getRawValue().propertyId;
    if (propertyId === 0) {
      this.errorMessage.set('Please select a property.');
      return;
    }

    this.isChanging.set(true);
    this.errorMessage.set('');

    const request: TenantPropertyRequest = { propertyId };

    this.tenantService.assignProperty(t.id, request).subscribe({
      next: () => {
        this.isChanging.set(false);
        this.closeChangeModal();
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isChanging.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to change property.');
      }
    });
  }

  openUnassignConfirm(): void {
    this.showUnassignConfirm.set(true);
  }

  closeUnassignConfirm(): void {
    this.showUnassignConfirm.set(false);
  }

  confirmUnassign(): void {
    const t = this.rent();
    if (!t) return;

    this.isUnassigning.set(true);
    this.errorMessage.set('');

    this.tenantService.unassignProperty(t.id).subscribe({
      next: () => {
        this.isUnassigning.set(false);
        this.closeUnassignConfirm();
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isUnassigning.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to unassign property.');
      }
    });
  }

  openStatusConfirm(newStatus: 'ACTIVE' | 'INACTIVE'): void {
    this.showStatusConfirm.set(true);
    this.assignForm.patchValue({ propertyId: newStatus === 'ACTIVE' ? 1 : 0 }); // Hack to pass status
  }

  closeStatusConfirm(): void {
    this.showStatusConfirm.set(false);
  }

  confirmStatusChange(): void {
    const t = this.rent();
    if (!t) return;

    const newStatus: 'ACTIVE' | 'INACTIVE' = this.assignForm.getRawValue().propertyId === 1 ? 'ACTIVE' : 'INACTIVE';
    if (newStatus === t.status) {
      this.closeStatusConfirm();
      return;
    }

    this.isUpdatingStatus.set(true);
    this.errorMessage.set('');

    // This would need a tenant service call to update status
    // For now, just close the modal
    this.isUpdatingStatus.set(false);
    this.closeStatusConfirm();
    this.close.emit(); // Reload parent
  }

  onClose(): void {
    this.close.emit();
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1];
  }

  getPropertyDisplay(property: any): string {
    if (!property) {
      return 'Not Assigned';
    }
    return `${property.propertyCode} — ${property.propertyName} (${property.propertyType})`;
  }

  isPropertyAssigned(property: any): boolean {
    return property !== null;
  }
}