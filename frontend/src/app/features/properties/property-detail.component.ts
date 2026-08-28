import { Component, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Property, PropertyStatus } from '@core/models/property.model';

@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property-detail.component.html',
  styleUrl: './property-detail.component.scss'
})
export class PropertyDetailComponent {
  property = input.required<Property>();
  isOpen = input.required<boolean>();
  close = output<void>();

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
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getPropertyTypeLabel(type: string): string {
    const types: Record<string, string> = {
      'HOUSE': 'House',
      'APARTMENT': 'Apartment',
      'ROOM': 'Room',
      'SHOP': 'Shop',
      'OFFICE': 'Office',
      'OTHER': 'Other'
    };
    return types[type] || type;
  }
}