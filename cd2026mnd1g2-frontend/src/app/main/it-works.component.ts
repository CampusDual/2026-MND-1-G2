import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { RequestCategory, RequestService, RequestSubcategory, ServiceRequest } from '../core/request.service';

/**
 * Pantalla del pasajero para crear y consultar peticiones del camarote ficticio.
 */
@Component({
  selector: 'app-it-works',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './it-works.component.html',
  styleUrl: './it-works.component.scss'
})
export class ItWorksComponent {
  screen: 'home' | 'categories' | 'subcategories' | 'details' | 'confirmation' | 'requests' | 'request-detail' = 'home';
  requestType: 'INCIDENT' | 'SERVICE' = 'INCIDENT';
  categories: RequestCategory[] = [];
  subcategories: RequestSubcategory[] = [];
  selectedCategory: RequestCategory | null = null;
  selectedSubcategory: RequestSubcategory | null = null;
  selectedRequest: ServiceRequest | null = null;
  requests: ServiceRequest[] = [];
  quantity: number = 1;
  preferredDelivery: string = 'ASAP';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly requestService: RequestService
  ) {}

  start(type: 'INCIDENT' | 'SERVICE'): void {
    this.requestType = type;
    this.screen = 'categories';
    this.errorMessage = '';
    this.requestService.getCategories(type).subscribe({
      next: (categories: RequestCategory[]) => this.categories = categories,
      error: () => this.errorMessage = 'No se ha podido cargar el catálogo.'
    });
  }

  selectCategory(category: RequestCategory): void {
    this.selectedCategory = category;
    this.subcategories = category.subcategories;
    this.screen = 'subcategories';
  }

  selectSubcategory(subcategory: RequestSubcategory): void {
    this.selectedSubcategory = subcategory;
    if (this.requestType === 'SERVICE') {
      this.screen = 'details';
      return;
    }
    this.sendRequest();
  }

  sendRequest(): void {
    if (!this.selectedCategory || !this.selectedSubcategory) return;
    this.isLoading = true;
    this.requestService.create({
      categoryId: this.selectedCategory.categoryId,
      subcategoryId: this.selectedSubcategory.subcategoryId,
      quantity: this.requestType === 'SERVICE' ? this.quantity : null,
      preferredDelivery: this.requestType === 'SERVICE' ? this.preferredDelivery : null
    }).subscribe({
      next: () => { this.screen = 'confirmation'; this.isLoading = false; },
      error: () => { this.errorMessage = 'No se ha podido enviar la petición.'; this.isLoading = false; }
    });
  }

  changeQuantity(amount: number): void {
    this.quantity = Math.max(1, this.quantity + amount);
  }

  showRequests(): void {
    this.screen = 'requests';
    this.requestService.getRequests().subscribe({
      next: (requests: ServiceRequest[]) => this.requests = requests,
      error: () => this.errorMessage = 'No se han podido cargar tus solicitudes.'
    });
  }

  showRequest(request: ServiceRequest): void {
    this.selectedRequest = request;
    this.screen = 'request-detail';
    this.requestService.getRequest(request.serviceRequestId).subscribe({
      next: (detail: ServiceRequest) => this.selectedRequest = detail,
      error: () => this.errorMessage = 'No se ha podido cargar el detalle.'
    });
  }

  home(): void {
    this.screen = 'home';
    this.errorMessage = '';
    this.selectedCategory = null;
    this.selectedSubcategory = null;
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      ASSIGNED: 'Asignada',
      IN_PROGRESS: 'En proceso',
      RESOLVED: 'Resuelta'
    };
    return labels[status] ?? status;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}