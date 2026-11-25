import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export type StockType = 'CONSUMABLE' | 'REUSABLE';
export type StockStatus = 'CORRECT' | 'REORDER_SOON' | 'REORDER';
export type StockMovementType = 'STOCK_ENTRY' | 'TO_LAUNDRY' | 'FROM_LAUNDRY' | 'DISPOSAL';
export type StockSourceState = 'WAREHOUSE' | 'LAUNDRY';

export interface StockProduct {
  productId: number;
  name: string;
  description: string | null;
  stockType: StockType;
  uds: number;
  warehouseUnits: number;
  laundryUnits: number;
  minimumStock: number;
  status: StockStatus;
  subcategoryId: number | null;
  subcategoryName: string | null;
  active: boolean;
}

export interface StockSummary {
  products: StockProduct[];
  differentProducts: number;
  correctProducts: number;
  reorderSoonProducts: number;
  reorderProducts: number;
}

export interface StockCreateRequest {
  name: string;
  stockType: StockType;
  initialUnits: number;
  minimumStock: number;
  description: string | null;
  subcategoryId: number | null;
  idempotencyKey: string;
}

export interface StockUpdateRequest {
  name: string;
  description: string | null;
  minimumStock: number;
}

export interface StockMovementCreateRequest {
  movementType: StockMovementType;
  quantity: number;
  sourceState: StockSourceState | null;
  reason: string | null;
  idempotencyKey: string;
  serviceRequestId: number | null;
}

export interface StockMovement {
  movementId: number;
  productId: number;
  productName: string;
  movementType: string;
  quantity: number;
  sourceState: StockSourceState | null;
  reason: string | null;
  createdAt: string;
  actorUsername: string;
  workerId: number | null;
  serviceRequestId: number | null;
  previousWarehouse: number;
  previousLaundry: number;
  previousTotal: number;
  nextWarehouse: number;
  nextLaundry: number;
  nextTotal: number;
}

@Injectable({ providedIn: 'root' })
export class StockService {
  constructor(private readonly http: HttpClient, private readonly authService: AuthService) {}

  getStock(): Observable<StockSummary> {
    return this.http.get<StockSummary>('/api/stock', { headers: this.headers() });
  }

  createProduct(request: StockCreateRequest): Observable<StockProduct> {
    return this.http.post<StockProduct>('/api/stock', request, { headers: this.headers() });
  }

  updateProduct(productId: number, request: StockUpdateRequest): Observable<StockProduct> {
    return this.http.put<StockProduct>(`/api/stock/${productId}`, request, { headers: this.headers() });
  }

  getMovements(productId: number): Observable<StockMovement[]> {
    return this.http.get<StockMovement[]>(`/api/stock/${productId}/movements`, {
      headers: this.headers()
    });
  }

  createMovement(productId: number, request: StockMovementCreateRequest): Observable<StockMovement> {
    return this.http.post<StockMovement>(`/api/stock/${productId}/movements`, request, {
      headers: this.headers()
    });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: this.authService.getAuthHeader() });
  }
}
