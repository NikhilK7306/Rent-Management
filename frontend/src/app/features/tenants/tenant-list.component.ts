import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TenantService } from '@core/services/tenant.service';
import { PropertyService } from '@core/services/property.service';
import { Tenant, TenantPage, TenantStatus, TENANT_STATUSES } from '@core/models/tenant.model';
import { Property, PropertyPage } from '@core/models/property.model';
import { HttpErrorResponse } from '@angular/common/http';
import { TenantFormComponent } from './tenant-form.component';
import { TenantDetailComponent } from './tenant-detail.component';
import { ToastService } from '@core/services/notification/toast.service';

@Component({
  selector: 'app-tenant-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TenantFormComponent, TenantDetailComponent],
  templateUrl: './tenant-list.component.html',
  styleUrl: './tenant-list.component.scss'
})
export class TenantListComponent implements OnInit {
  private tenantService = inject(TenantService);
  private propertyService = inject(PropertyService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  tenants = signal<Tenant[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');
  searchTerm = signal('');
  statusFilter = signal<string>('');
  currentPage = signal(0);
  pageSize = 10;
  totalElements = signal(0);
  totalPages = signal(0);

  showAddModal = signal(false);
  showEditModal = signal(false);
  showDetailModal = signal(false);
  selectedTenant = signal<Tenant | null>(null);
  editingTenant = signal<Tenant | null>(null);

  statuses = TENANT_STATUSES;
  assignableProperties = signal<Property[]>([]);

  filteredTenants = computed(() => this.tenants());

  ngOnInit(): void {
    this.loadTenants();
  }

  loadTenants(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.tenantService.getTenants(
      this.searchTerm() || undefined,
      this.statusFilter() || undefined,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (response: TenantPage) => {
        this.tenants.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to backend. Please check if the server is running.');
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to load tenants.');
        }
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadTenants();
  }

  onStatusFilterChange(): void {
    this.currentPage.set(0);
    this.loadTenants();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('');
    this.currentPage.set(0);
    this.loadTenants();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadTenants();
    }
  }

  openAddModal(): void {
    this.showAddModal.set(true);
  }

  closeAddModal(): void {
    this.showAddModal.set(false);
  }

  openEditModal(tenant: Tenant): void {
    this.editingTenant.set({ ...tenant });
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.editingTenant.set(null);
  }

  openDetailModal(tenant: Tenant): void {
    this.selectedTenant.set(tenant);
    this.showDetailModal.set(true);
    this.loadAssignableProperties();
  }

  loadAssignableProperties(): void {
    this.propertyService.getProperties(undefined, 'ACTIVE', 0, 100).subscribe({
      next: (response: PropertyPage) => {
        // Filter out properties that already have an active tenant
        this.assignableProperties.set(response.content.filter(p => !p.tenant));
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load assignable properties:', err);
      }
    });
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedTenant.set(null);
  }

  navigateToProperty(propertyId: number): void {
    this.closeDetailModal();
    this.router.navigate(['/properties', propertyId]);
  }

  onTenantCreated(): void {
    this.closeAddModal();
    this.loadTenants();
    this.toastService.success('Tenant Created', 'Tenant has been created successfully');
  }

  onTenantUpdated(): void {
    this.closeEditModal();
    this.loadTenants();
    this.toastService.success('Tenant Updated', 'Tenant has been updated successfully');
  }

  onStatusChange(tenant: Tenant, newStatus: TenantStatus): void {
    if (confirm(`Are you sure you want to ${newStatus === 'ACTIVE' ? 'activate' : 'deactivate'} this tenant?`)) {
      this.tenantService.updateTenantStatus(tenant.id, { status: newStatus }).subscribe({
        next: () => {
          this.loadTenants();
          this.toastService.success('Status Updated', `Tenant status changed to ${newStatus}`);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage.set(err.error?.message || 'Failed to update tenant status.');
          this.toastService.error('Status Update Failed', err.error?.message || 'Failed to update tenant status. Please try again.');
        }
      });
    }
  }

  getStatusClass(status: TenantStatus): string {
    return status === 'ACTIVE' ? 'status-active' : 'status-inactive';
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getPropertyDisplay(property: Tenant['property']): string {
    if (!property) {
      return 'Not Assigned';
    }
    return `${property.propertyCode} — ${property.propertyName}`;
  }

  isPropertyAssigned(property: Tenant['property']): boolean {
    return property !== null;
  }
}