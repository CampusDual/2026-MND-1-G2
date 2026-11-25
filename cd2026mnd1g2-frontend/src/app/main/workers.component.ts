import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { RequestService, WorkerDepartment, WorkerRequest } from '../core/request.service';
import {
  StockMovementType,
  StockProduct,
  StockService,
  StockSourceState,
  StockSummary
} from '../core/stock.service';

/**
 * Pantalla de gestión de tareas y operaciones de inventario de Housekeeping.
 */
@Component({
  selector: 'app-workers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './workers.component.html',
  styleUrl: './workers.component.scss'
})
export class WorkersComponent {
  department: WorkerDepartment | null = null;
  unassigned: WorkerRequest[] = [];
  assigned: WorkerRequest[] = [];
  resolved: WorkerRequest[] = [];
  reusableProducts: StockProduct[] = [];
  inventoryOperations: Partial<Record<number, StockMovementType | ''>> = {};
  inventoryProductIds: Record<number, number | null> = {};
  inventoryQuantities: Record<number, number> = {};
  inventorySources: Partial<Record<number, StockSourceState>> = {};
  inventoryReasons: Record<number, string> = {};
  inventorySaving: Record<number, boolean> = {};
  inventorySuccess: Record<number, string> = {};
  isLoading = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly requestService: RequestService,
    private readonly stockService: StockService
  ) {}

  enterDepartment(department: WorkerDepartment): void {
    this.department = department;
    if (department === 'HOUSEKEEPING') {
      this.loadReusableProducts();
    } else {
      this.reusableProducts = [];
    }
    this.loadRequests();
  }

  loadRequests(): void {
    if (!this.department) {
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    forkJoin({
      unassigned: this.requestService.getUnassignedWorkerRequests(this.department),
      assigned: this.requestService.getAssignedWorkerRequests(this.department),
      resolved: this.requestService.getResolvedWorkerRequests(this.department)
    }).subscribe({
      next: (lists) => {
        this.unassigned = lists.unassigned;
        this.assigned = lists.assigned;
        this.resolved = lists.resolved;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se han podido cargar las solicitudes del departamento.';
        this.isLoading = false;
      }
    });
  }

  assign(request: WorkerRequest): void {
    if (!this.department) {
      return;
    }
    this.requestService.assignWorkerRequest(request.serviceRequestId, this.department).subscribe({
      next: () => this.loadRequests(),
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'La solicitud ya no está disponible.');
      }
    });
  }

  start(request: WorkerRequest): void {
    if (!this.department) {
      return;
    }
    this.requestService.startWorkerRequest(request.serviceRequestId, this.department).subscribe({
      next: () => this.loadRequests(),
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'No se ha podido iniciar la solicitud.');
      }
    });
  }

  resolve(request: WorkerRequest): void {
    if (!this.department) {
      return;
    }
    this.requestService.resolveWorkerRequest(request.serviceRequestId, this.department).subscribe({
      next: () => this.loadRequests(),
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'No se ha podido finalizar la solicitud.');
      }
    });
  }

  selectInventoryOperation(requestId: number, operation: string): void {
    if (operation === '' || operation === 'TO_LAUNDRY'
      || operation === 'FROM_LAUNDRY' || operation === 'DISPOSAL') {
      this.inventoryOperations[requestId] = operation;
    }
    this.inventorySuccess[requestId] = '';
  }

  selectInventoryProduct(requestId: number, productId: number | null): void {
    this.inventoryProductIds[requestId] = productId;
  }

  selectInventorySource(requestId: number, source: string): void {
    if (source === 'WAREHOUSE' || source === 'LAUNDRY') {
      this.inventorySources[requestId] = source;
    }
  }

  recordInventoryOperation(request: WorkerRequest): void {
    const requestId = request.serviceRequestId;
    if (this.inventorySaving[requestId]) {
      return;
    }
    const operation = this.inventoryOperations[requestId];
    const productId = this.inventoryProductIds[requestId];
    const quantity = this.inventoryQuantities[requestId];
    if (!operation || !productId || typeof quantity !== 'number'
      || !Number.isInteger(quantity) || quantity <= 0) {
      this.errorMessage = 'Selecciona un producto e introduce una cantidad entera positiva.';
      return;
    }
    const isDisposal = operation === 'DISPOSAL';
    const reason = this.inventoryReasons[requestId] || '';
    if (isDisposal && !reason) {
      this.errorMessage = 'Selecciona el motivo del desecho.';
      return;
    }

    this.errorMessage = '';
    this.inventorySaving[requestId] = true;
    this.stockService.createMovement(productId, {
      movementType: operation,
      quantity,
      sourceState: isDisposal ? this.inventorySources[requestId] ?? 'WAREHOUSE' : null,
      reason: isDisposal ? reason : null,
      idempotencyKey: crypto.randomUUID(),
      serviceRequestId: requestId
    }).subscribe({
      next: () => {
        this.inventorySaving[requestId] = false;
        this.inventorySuccess[requestId] = 'Movimiento de inventario registrado.';
        this.inventoryQuantities[requestId] = 1;
        this.loadReusableProducts();
      },
      error: (error: HttpErrorResponse) => {
        this.inventorySaving[requestId] = false;
        this.errorMessage = this.readError(error, 'No se ha podido registrar el movimiento.');
      }
    });
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      ASSIGNED: 'Asignada',
      IN_PROGRESS: 'En curso',
      RESOLVED: 'Finalizada'
    };
    return labels[status] ?? status;
  }

  backToDepartments(): void {
    this.department = null;
    this.unassigned = [];
    this.assigned = [];
    this.resolved = [];
    this.reusableProducts = [];
  }

  backToCabin(): void {
    this.router.navigate(['/it-works']);
  }

  goToStock(): void {
    this.router.navigate(['/stock']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private loadReusableProducts(): void {
    this.stockService.getStock().subscribe({
      next: (summary: StockSummary) => {
        this.reusableProducts = summary.products.filter((product) => product.stockType === 'REUSABLE');
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'No se han podido cargar los productos reutilizables.');
      }
    });
  }

  private readError(error: HttpErrorResponse, fallback: string): string {
    return typeof error.error?.message === 'string' ? error.error.message : fallback;
  }
}
