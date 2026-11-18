import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface RequestSubcategory {
  subcategoryId: number;
  categoryId: number;
  name: string;
  description: string;
  icon: string;
  active: boolean;
}

export interface RequestCategory {
  categoryId: number;
  name: string;
  type: 'INCIDENT' | 'SERVICE';
  description: string;
  icon: string;
  active: boolean;
  subcategories: RequestSubcategory[];
}

export interface ServiceRequest {
  serviceRequestId: number;
  cabinNumber: number;
  category: RequestCategory;
  subcategory: RequestSubcategory;
  quantity: number | null;
  preferredDelivery: string | null;
  status: 'PENDING' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED';
  createdAt: string;
  updatedAt: string;
  statusHistory: { previousStatus: string | null; newStatus: string; changedAt: string }[];
}

export interface ServiceRequestCreate {
  categoryId: number;
  subcategoryId: number;
  quantity: number | null;
  preferredDelivery: string | null;
}

export type WorkerDepartment = 'HOUSEKEEPING' | 'MANTENIMIENTO';

export interface WorkerRequest {
  serviceRequestId: number;
  cabinNumber: number;
  itemName: string;
  quantity: number | null;
  status: 'PENDING' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED';
}

@Injectable({ providedIn: 'root' })
export class RequestService {
  constructor(private readonly http: HttpClient, private readonly authService: AuthService) {}

  getCategories(type: 'INCIDENT' | 'SERVICE'): Observable<RequestCategory[]> {
    return this.http.get<RequestCategory[]>(`/api/categories?type=${type}`, { headers: this.headers() });
  }

  create(request: ServiceRequestCreate): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>('/api/requests', request, { headers: this.headers() });
  }

  getRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>('/api/requests', { headers: this.headers() });
  }

  getRequest(id: number): Observable<ServiceRequest> {
    return this.http.get<ServiceRequest>(`/api/requests/${id}`, { headers: this.headers() });
  }

  getUnassignedWorkerRequests(department: WorkerDepartment): Observable<WorkerRequest[]> {
    return this.http.get<WorkerRequest[]>(
      `/api/worker-requests/unassigned?department=${department}`,
      { headers: this.headers() });
  }

  getAssignedWorkerRequests(department: WorkerDepartment): Observable<WorkerRequest[]> {
    return this.http.get<WorkerRequest[]>(
      `/api/worker-requests/assigned?department=${department}`,
      { headers: this.headers() });
  }

  getResolvedWorkerRequests(department: WorkerDepartment): Observable<WorkerRequest[]> {
    return this.http.get<WorkerRequest[]>(
      `/api/worker-requests/resolved?department=${department}`,
      { headers: this.headers() });
  }

  assignWorkerRequest(requestId: number, department: WorkerDepartment): Observable<WorkerRequest> {
    return this.http.post<WorkerRequest>(
      `/api/worker-requests/${requestId}/assign?department=${department}`,
      {},
      { headers: this.headers() });
  }

  startWorkerRequest(requestId: number, department: WorkerDepartment): Observable<WorkerRequest> {
    return this.http.post<WorkerRequest>(
      `/api/worker-requests/${requestId}/start?department=${department}`,
      {},
      { headers: this.headers() });
  }

  resolveWorkerRequest(requestId: number, department: WorkerDepartment): Observable<WorkerRequest> {
    return this.http.post<WorkerRequest>(
      `/api/worker-requests/${requestId}/resolve?department=${department}`,
      {},
      { headers: this.headers() });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: this.authService.getAuthHeader() });
  }
}
