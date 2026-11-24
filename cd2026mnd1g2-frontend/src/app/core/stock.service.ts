import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface StockProduct {
  name: string;
  uds: number;
}

export interface StockSummary {
  products: StockProduct[];
  differentProducts: number;
}

@Injectable({ providedIn: 'root' })
export class StockService {
  constructor(private readonly http: HttpClient, private readonly authService: AuthService) {}

  getStock(): Observable<StockSummary> {
    return this.http.get<StockSummary>('/api/stock', {
      headers: new HttpHeaders({ Authorization: this.authService.getAuthHeader() })
    });
  }
}
