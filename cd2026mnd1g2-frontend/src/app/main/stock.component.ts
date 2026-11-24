import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { StockProduct, StockService, StockSummary } from '../core/stock.service';

/**
 * Pantalla de consulta readonly del stock disponible para solicitudes de servicio.
 */
@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stock.component.html',
  styleUrl: './stock.component.scss'
})
export class StockComponent implements OnInit {
  products: StockProduct[] = [];
  differentProducts = 0;
  isLoading = false;
  errorMessage = '';

  constructor(
    private readonly stockService: StockService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadStock();
  }

  loadStock(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.stockService.getStock().subscribe({
      next: (summary: StockSummary) => {
        this.products = summary.products;
        this.differentProducts = summary.differentProducts;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se ha podido cargar el stock.';
        this.isLoading = false;
      }
    });
  }

  backToWorkers(): void {
    this.router.navigate(['/workers']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
