import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  @Input() isSidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  breadcrumb = [
    { label: 'Dashboard', path: '/dashboard' }
  ];
}