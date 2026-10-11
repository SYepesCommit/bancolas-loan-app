import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';

import { CustomerService } from '@features/customer/services/customer.service';

import { Customer } from '@features/customer/models/customer.model';
import { LoanApplicationRequest, LoanApplicationResponse } from '@features/loan/models/loan.model';
import { DashboardHeaderComponent } from '@features/loan/components/dashboard-header/dashboard-header';
import { LoanFormComponent } from '@features/loan/components/loan-form/loan-form';
import { LoanTableComponent } from '@features/loan/components/loan-table/loan-table';
import { MetricsSummaryComponent } from '@features/loan/components/metrics-summary/metrics-summary';
import { LoanService } from '@features/loan/services/loan.service';


@Component({
  selector: 'app-loan-dashboard',
  standalone: true,
  imports: [
    DashboardHeaderComponent,
    MetricsSummaryComponent,
    LoanFormComponent,
    LoanTableComponent,
],
  templateUrl: './loan-dashboard.component.html',
  styleUrl: './loan-dashboard.component.scss'
})
export class LoanDashboardComponent implements OnInit {
  private readonly loanService = inject(LoanService);
  private readonly customerService = inject(CustomerService);

  @ViewChild(LoanFormComponent) loanFormComponent!: LoanFormComponent;

  loans = signal<LoanApplicationResponse[]>([]);
  customers = signal<Customer[]>([]);
  loading = signal<boolean>(false);
  lastResult = signal<LoanApplicationResponse | null>(null);
  errorMessage = signal<string | null>(null);

  approvedCount = computed(() => this.loans().filter(l => l.status === 'APPROVED').length);
  rejectedCount = computed(() => this.loans().filter(l => l.status === 'REJECTED').length);
  totalVolume = computed(() => this.loans()
    .filter(l => l.status === 'APPROVED')
    .reduce((acc, curr) => acc + curr.amount, 0));

  ngOnInit(): void {
    this.loadHistory();
    this.loadCustomers();
  }

  loadHistory(): void {
    this.loanService.getRecentLoans().subscribe({
      next: (data) => this.loans.set(data),
      error: (err) => console.error('Error al cargar préstamos:', err)
    });
  }

  loadCustomers(): void {
    this.customerService.getCustomers().subscribe({
      next: (data) => this.customers.set(data),
      error: (err) => console.error('Error al cargar clientes:', err)
    });
  }

  handleLoanSubmission(payload: LoanApplicationRequest): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.lastResult.set(null);

    this.loanService.applyForLoan(payload).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.lastResult.set(response);
        
        if (this.loanFormComponent) {
          this.loanFormComponent.resetForm();
        }

        this.loadHistory();
        this.loadCustomers();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set('Ocurrió un error al procesar la solicitud.');
      }
    });
  }
}