import { Component, inject, input, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Tenant, TenantPropertyRequest, TenantStatus, TENANT_STATUSES } from '@core/models/tenant.model';
import { Property } from '@core/models/property.model';
import { TenantService } from '@core/services/tenant.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-tenant-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tenant-detail.component.html',
  styleUrl: './tenant-detail.component.scss'
})
export class TenantDetailComponent {
  private tenantService = inject(TenantService);
  private toastService = inject(ToastService);

  tenant = input.required<Tenant | null>();
  assignableProperties = input<Property[]>([]);
  close = output<void>();

  isAssigning = signal(false);
  isChanging = signal(false);
  isUnassigning = signal(false);
  isUpdatingStatus = signal(false);
  errorMessage = signal('');
  showAssignModal = signal(false);
  showChangeModal = signal(false);
  showUnassignConfirm = signal(false);
  showStatusConfirm = signal(false);

  // Selected property IDs using signals (simpler and more reliable than form control for select)
  selectedAssignPropertyId = signal<number>(0);
  selectedChangePropertyId = signal<number>(0);

  // Status change tracking
  pendingStatusChange = signal<TenantStatus | null>(null);

  // Computed property to check if tenant is active
  isTenantActive = computed(() => this.tenant()?.status === 'ACTIVE');

  // Computed property to check if currently assigned property is active
  isAssignedPropertyActive = computed(() => {
    const prop = this.tenant()?.property;
    return prop !== null; // Property is active if assigned (backend ensures this)
  });

  // Computed property to check if assignment is allowed
  canAssignProperty = computed(() => this.isTenantActive());

  // Computed property to check if there are assignable properties
  hasAssignableProperties = computed(() => this.assignableProperties().length > 0);

  // Computed property to check if there are other assignable properties (excluding current)
  hasOtherAssignableProperties = computed(() => {
    const currentPropId = this.tenant()?.property?.id;
    return this.assignableProperties().filter(p => p.id !== currentPropId).length > 0;
  });

  openAssignModal(): void {
    if (!this.canAssignProperty()) return;
    this.selectedAssignPropertyId.set(0);
    this.showAssignModal.set(true);
  }

  closeAssignModal(): void {
    this.showAssignModal.set(false);
    this.selectedAssignPropertyId.set(0);
  }

  openChangeModal(): void {
    if (!this.canAssignProperty()) return;
    this.selectedChangePropertyId.set(0);
    this.showChangeModal.set(true);
  }

  closeChangeModal(): void {
    this.showChangeModal.set(false);
    this.selectedChangePropertyId.set(0);
  }

  confirmAssign(): void {
    const t = this.tenant();
    if (!t) return;

    const propertyId = this.selectedAssignPropertyId();
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
        this.toastService.success('Property Assigned', `Property has been assigned to ${t.fullName}`);
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isAssigning.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to assign property.');
        this.toastService.error('Assignment Failed', err.error?.message || 'Failed to assign property. Please try again.');
      }
    });
  }

  confirmChange(): void {
    const t = this.tenant();
    if (!t) return;

    const propertyId = this.selectedChangePropertyId();
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
        this.toastService.success('Property Changed', `Property has been changed for ${t.fullName}`);
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isChanging.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to change property.');
        this.toastService.error('Change Failed', err.error?.message || 'Failed to change property. Please try again.');
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
    const t = this.tenant();
    if (!t) return;

    this.isUnassigning.set(true);
    this.errorMessage.set('');

    this.tenantService.unassignProperty(t.id).subscribe({
      next: () => {
        this.isUnassigning.set(false);
        this.closeUnassignConfirm();
        this.toastService.success('Property Unassigned', `Property has been unassigned from ${t.fullName}`);
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isUnassigning.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to unassign property.');
        this.toastService.error('Unassign Failed', err.error?.message || 'Failed to unassign property. Please try again.');
      }
    });
  }

  openStatusConfirm(newStatus: TenantStatus): void {
    this.showStatusConfirm.set(true);
    this.pendingStatusChange.set(newStatus);
  }

  closeStatusConfirm(): void {
    this.showStatusConfirm.set(false);
    this.pendingStatusChange.set(null);
  }

  confirmStatusChange(): void {
    const t = this.tenant();
    if (!t) return;

    const newStatus = this.pendingStatusChange();
    if (!newStatus || newStatus === t.status) {
      this.closeStatusConfirm();
      return;
    }

    this.isUpdatingStatus.set(true);
    this.errorMessage.set('');

    this.tenantService.updateTenantStatus(t.id, { status: newStatus }).subscribe({
      next: () => {
        this.isUpdatingStatus.set(false);
        this.closeStatusConfirm();
        this.toastService.success('Status Updated', `Tenant status changed to ${newStatus}`);
        this.close.emit(); // Reload parent
      },
      error: (err: HttpErrorResponse) => {
        this.isUpdatingStatus.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to update tenant status.');
        this.toastService.error('Status Update Failed', err.error?.message || 'Failed to update tenant status. Please try again.');
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  getStatusClass(status: TenantStatus): string {
    return status === 'ACTIVE' ? 'status-active' : 'status-inactive';
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getPropertyDisplay(property: Tenant['property']): string {
    if (!property) {
      return 'Not Assigned';
    }
    return `${property.propertyCode} — ${property.propertyName} (${property.propertyType})`;
  }

  isPropertyAssigned(property: Tenant['property']): boolean {
    return property !== null;
  }

  statuses = TENANT_STATUSES;
}