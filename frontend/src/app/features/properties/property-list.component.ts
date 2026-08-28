import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PropertyService } from '@core/services/property.service';
import { Property, PropertyPage, PropertyStatus, PROPERTY_TYPES, PROPERTY_STATUSES } from '@core/models/property.model';
import { HttpErrorResponse } from '@angular/common/http';
import { PropertyFormComponent } from './property-form.component';
import { PropertyDetailComponent } from './property-detail.component';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PropertyFormComponent, PropertyDetailComponent],
  templateUrl: './property-list.component.html',
  styleUrl: './property-list.component.scss'
})
export class PropertyListComponent implements OnInit {
  private propertyService = inject(PropertyService);
  private router = inject(Router);

  properties = signal<Property[]>([]);
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
  selectedProperty = signal<Property | null>(null);
  editingProperty = signal<Property | null>(null);

  statuses = PROPERTY_STATUSES;
  propertyTypes = PROPERTY_TYPES;

  filteredProperties = computed(() => this.properties());

  ngOnInit(): void {
    this.loadProperties();
  }

  loadProperties(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.propertyService.getProperties(
      this.searchTerm() || undefined,
      this.statusFilter() || undefined,
      this.currentPage(),
      this.pageSize,
      'createdAt,desc'
    ).subscribe({
      next: (response: PropertyPage) => {
        this.properties.set(response.content);
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
          this.errorMessage.set(err.error?.message || 'Failed to load properties.');
        }
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadProperties();
  }

  onStatusFilterChange(): void {
    this.currentPage.set(0);
    this.loadProperties();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('');
    this.currentPage.set(0);
    this.loadProperties();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadProperties();
    }
  }

  openAddModal(): void {
    this.showAddModal.set(true);
  }

  closeAddModal(): void {
    this.showAddModal.set(false);
  }

  openEditModal(property: Property): void {
    this.editingProperty.set({ ...property });
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.editingProperty.set(null);
  }

  openDetailModal(property: Property): void {
    this.selectedProperty.set(property);
    this.showDetailModal.set(true);
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedProperty.set(null);
  }

  navigateToTenant(tenantId: number): void {
    this.closeDetailModal();
    this.router.navigate(['/tenants', tenantId]);
  }

  onPropertyCreated(): void {
    this.closeAddModal();
    this.loadProperties();
  }

  onPropertyUpdated(): void {
    this.closeEditModal();
    this.loadProperties();
  }

  onStatusChange(property: Property, newStatus: PropertyStatus): void {
    if (confirm(`Are you sure you want to ${newStatus === 'ACTIVE' ? 'activate' : 'deactivate'} this property?`)) {
      this.propertyService.updatePropertyStatus(property.id, { status: newStatus }).subscribe({
        next: () => {
          this.loadProperties();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage.set(err.error?.message || 'Failed to update property status.');
        }
      });
    }
  }

  getStatusClass(status: PropertyStatus): string {
    return status === 'ACTIVE' ? 'status-active' : 'status-inactive';
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
      month: 'short',
      day: 'numeric'
    });
  }

  getPropertyTypeLabel(type: string): string {
    const found = this.propertyTypes.find(t => t.value === type);
    return found ? found.label : type;
  }
}