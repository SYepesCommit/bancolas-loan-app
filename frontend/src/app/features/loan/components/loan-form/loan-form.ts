import { Component, inject, input, output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Customer } from '@features/customer/models/customer.model';
import { LoanApplicationResponse, LoanApplicationRequest } from '@features/loan/models/loan.model';

@Component({
  selector: 'app-loan-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './loan-form.html',
  styleUrl: './loan-form.scss'
})
export class LoanFormComponent {
  estimatedMonthlyPayment(): any {
    throw new Error('Method not implemented.');
  }
  private readonly fb = inject(FormBuilder);

  @ViewChild(FormGroupDirective) private formDirective?: FormGroupDirective;

  customers = input.required<Customer[]>();
  loading = input<boolean>(false);
  lastResult = input<LoanApplicationResponse | null>(null);
  errorMessage = input<string | null>(null);

  submitForm = output<LoanApplicationRequest>();

  loanForm = this.fb.nonNullable.group({
    requestReference: [this.generateNextReference(), [Validators.required, Validators.pattern(/^SOL-\d+$/)]],
    customerId: ['', [Validators.required]],
    amount: [500000, [Validators.required, Validators.min(10000)]],
    termMonths: [12, [Validators.required, Validators.min(6), Validators.max(60)]]
  });

  setQuickCustomer(customerId: string): void {
    this.loanForm.patchValue({ customerId });
  }

  onSubmit(): void {
    if (this.loanForm.valid) {
      this.submitForm.emit(this.loanForm.getRawValue());
    } else {
      this.loanForm.markAllAsTouched();
    }
  }


  resetForm(): void {
    const nextRef = this.generateNextReference();

    if (this.formDirective) {
      this.formDirective.resetForm({
        requestReference: nextRef,
        customerId: '',
        amount: 500000,
        termMonths: 12
      });
    } else {
      this.loanForm.reset({
        requestReference: nextRef,
        customerId: '',
        amount: 500000,
        termMonths: 12
      });
    }
  }


  private generateNextReference(): string {
    const now = new Date();
    const timeCode = `${now.getHours()}${now.getMinutes()}${now.getSeconds()}`;
    return `SOL-${timeCode}`;
  }
}