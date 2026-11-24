import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { RequestService, WorkerDepartment, WorkerRequest } from '../core/request.service';

/**
 * Pantalla de gestion de solicitudes asignables a trabajadores.
 */
@Component({
  selector: 'app-workers',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './workers.component.html',
  styleUrl: './workers.component.scss'
})
export class WorkersComponent {
  department: WorkerDepartment | null = null;
  unassigned: WorkerRequest[] = [];
  assigned: WorkerRequest[] = [];
  resolved: WorkerRequest[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly requestService: RequestService
  ) {}

  enterDepartment(department: WorkerDepartment): void {
    this.department = department;
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
        this.errorMessage = typeof error.error?.message === 'string'
          ? error.error.message
          : 'La solicitud ya no esta disponible.';
      }
    });
  }

  start(request: WorkerRequest): void {
    if (!this.department) {
      return;
    }
    this.requestService.startWorkerRequest(request.serviceRequestId, this.department).subscribe({
      next: () => this.loadRequests(),
      error: () => this.errorMessage = 'No se ha podido iniciar la solicitud.'
    });
  }

  resolve(request: WorkerRequest): void {
    if (!this.department) {
      return;
    }
    this.requestService.resolveWorkerRequest(request.serviceRequestId, this.department).subscribe({
      next: () => this.loadRequests(),
      error: () => this.errorMessage = 'No se ha podido finalizar la solicitud.'
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
}
