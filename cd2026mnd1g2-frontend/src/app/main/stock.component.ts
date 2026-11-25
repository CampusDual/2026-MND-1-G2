import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { RequestCategory, RequestService, RequestSubcategory } from '../core/request.service';
import {
  StockMovement,
  StockProduct,
  StockService,
  StockStatus,
  StockSummary,
  StockType
} from '../core/stock.service';

/**
 * Panel de inventario persistido para productos de Housekeeping.
 */
@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock.component.html',
  styleUrl: './stock.component.scss'
})
export class StockComponent implements OnInit, OnDestroy {
  products: StockProduct[] = [];
  summary: StockSummary | null = null;
  subcategories: RequestSubcategory[] = [];
  movements: StockMovement[] = [];
  selectedProduct: StockProduct | null = null;
  editingProduct: StockProduct | null = null;
  isLoading = false;
  isSaving = false;
  isRegisteringEntry = false;
  showEditor = false;
  searchTerm = '';
  typeFilter: StockType | 'ALL' = 'ALL';
  statusFilter: StockStatus | 'ALL' = 'ALL';
  attentionOnly = false;
  errorMessage = '';
  successMessage = '';
  subcategoryError = '';
  formName = '';
  formType: StockType = 'CONSUMABLE';
  formInitialUnits = 0;
  formMinimumStock = 0;
  formDescription = '';
  formSubcategoryId: number | null = null;
  entryQuantity = 1;
  private readonly refreshSubscription: Subscription;

  constructor(
    private readonly stockService: StockService,
    private readonly requestService: RequestService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.refreshSubscription = interval(15000).subscribe(() => {
      if (this.isLoading || this.isSaving || this.isRegisteringEntry) {
        return;
      }
      this.loadStock();
      if (this.selectedProduct) {
        this.loadMovements(this.selectedProduct.productId);
      }
    });
  }

  ngOnInit(): void {
    this.loadStock();
    this.loadServiceSubcategories();
  }

  ngOnDestroy(): void {
    this.refreshSubscription.unsubscribe();
  }

  get filteredProducts(): StockProduct[] {
    const search = this.searchTerm.trim().toLocaleLowerCase();
    return this.products.filter((product) => {
      const matchesName = !search || product.name.toLocaleLowerCase().includes(search);
      const matchesType = this.typeFilter === 'ALL' || product.stockType === this.typeFilter;
      const matchesStatus = this.statusFilter === 'ALL' || product.status === this.statusFilter;
      const matchesAttention = !this.attentionOnly || product.status !== 'CORRECT';
      return matchesName && matchesType && matchesStatus && matchesAttention;
    });
  }

  loadStock(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.stockService.getStock().subscribe({
      next: (summary: StockSummary) => {
        this.summary = summary;
        this.products = summary.products;
        if (this.selectedProduct) {
          this.selectedProduct = this.products.find(
            (product) => product.productId === this.selectedProduct?.productId
          ) ?? null;
        }
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'No se ha podido cargar el stock.');
        this.isLoading = false;
      }
    });
  }

  startCreate(): void {
    this.editingProduct = null;
    this.formName = '';
    this.formType = 'CONSUMABLE';
    this.formInitialUnits = 0;
    this.formMinimumStock = 0;
    this.formDescription = '';
    this.formSubcategoryId = null;
    this.showEditor = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  editProduct(product: StockProduct): void {
    this.editingProduct = product;
    this.formName = product.name;
    this.formType = product.stockType;
    this.formInitialUnits = 0;
    this.formMinimumStock = product.minimumStock;
    this.formDescription = product.description ?? '';
    this.formSubcategoryId = product.subcategoryId;
    this.showEditor = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  saveProduct(): void {
    if (!this.formName.trim() || this.formMinimumStock < 0
      || (!this.editingProduct && this.formInitialUnits < 0)) {
      this.errorMessage = 'Revisa el nombre y las cantidades del producto.';
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const description = this.formDescription.trim() || null;
    if (this.editingProduct) {
      this.stockService.updateProduct(this.editingProduct.productId, {
        name: this.formName.trim(),
        description,
        minimumStock: this.formMinimumStock
      }).subscribe({
        next: () => this.finishProductSave('Producto actualizado correctamente.'),
        error: (error: HttpErrorResponse) => this.failSave(error)
      });
      return;
    }
    this.stockService.createProduct({
      name: this.formName.trim(),
      stockType: this.formType,
      initialUnits: this.formInitialUnits,
      minimumStock: this.formMinimumStock,
      description,
      subcategoryId: this.formSubcategoryId,
      idempotencyKey: crypto.randomUUID()
    }).subscribe({
      next: () => this.finishProductSave('Producto creado correctamente.'),
      error: (error: HttpErrorResponse) => this.failSave(error)
    });
  }

  closeEditor(): void {
    this.showEditor = false;
    this.editingProduct = null;
  }

  openDetails(product: StockProduct): void {
    this.selectedProduct = product;
    this.entryQuantity = 1;
    this.loadMovements(product.productId);
  }

  closeDetails(): void {
    this.selectedProduct = null;
    this.movements = [];
  }

  registerEntry(): void {
    if (!this.selectedProduct || !Number.isInteger(this.entryQuantity) || this.entryQuantity <= 0) {
      this.errorMessage = 'La cantidad recibida debe ser un número entero positivo.';
      return;
    }
    const productId = this.selectedProduct.productId;
    this.isRegisteringEntry = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.stockService.createMovement(productId, {
      movementType: 'STOCK_ENTRY',
      quantity: this.entryQuantity,
      sourceState: null,
      reason: null,
      idempotencyKey: crypto.randomUUID(),
      serviceRequestId: null
    }).subscribe({
      next: () => {
        this.isRegisteringEntry = false;
        this.successMessage = 'Entrada de stock registrada correctamente.';
        this.loadStock();
        this.loadMovements(productId);
      },
      error: (error: HttpErrorResponse) => {
        this.isRegisteringEntry = false;
        this.errorMessage = this.readError(error, 'No se ha podido registrar la entrada.');
      }
    });
  }

  statusLabel(status: StockStatus): string {
    const labels: Record<StockStatus, string> = {
      CORRECT: 'Correcto',
      REORDER_SOON: 'Reponer pronto',
      REORDER: 'Reponer'
    };
    return labels[status];
  }

  typeLabel(type: StockType): string {
    return type === 'REUSABLE' ? 'Reutilizable' : 'Consumible';
  }

  movementLabel(type: string): string {
    const labels: Record<string, string> = {
      STOCK_ENTRY: 'Entrada de stock',
      CONSUMPTION: 'Consumo / entrega',
      TO_LAUNDRY: 'Envío a lavandería',
      FROM_LAUNDRY: 'Devolución de lavandería',
      DISPOSAL: 'Desecho'
    };
    return labels[type] ?? type;
  }

  backToWorkers(): void {
    this.router.navigate(['/workers']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private loadServiceSubcategories(): void {
    this.requestService.getCategories('SERVICE').subscribe({
      next: (categories: RequestCategory[]) => {
        this.subcategories = categories.flatMap((category) => category.subcategories);
      },
      error: () => {
        this.subcategoryError = 'No se han podido cargar las subcategorías de servicio.';
      }
    });
  }

  private loadMovements(productId: number): void {
    this.stockService.getMovements(productId).subscribe({
      next: (movements: StockMovement[]) => this.movements = movements,
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error, 'No se ha podido cargar el historial.');
      }
    });
  }

  private finishProductSave(message: string): void {
    this.isSaving = false;
    this.showEditor = false;
    this.editingProduct = null;
    this.successMessage = message;
    this.loadStock();
  }

  private failSave(error: HttpErrorResponse): void {
    this.isSaving = false;
    this.errorMessage = this.readError(error, 'No se ha podido guardar el producto.');
  }

  private readError(error: HttpErrorResponse, fallback: string): string {
    return typeof error.error?.message === 'string' ? error.error.message : fallback;
  }
}
