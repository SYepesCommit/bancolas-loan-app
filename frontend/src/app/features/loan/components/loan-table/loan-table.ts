import { Component, input } from '@angular/core';
import { CommonModule  } from '@angular/common';

import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';

import { LoanApplicationResponse } from '@features/loan/models/loan.model';

@Component({
  selector: 'app-loan-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatIconModule],
  templateUrl: './loan-table.html',
  styleUrl: './loan-table.scss'
})
export class LoanTableComponent {
  loans = input.required<LoanApplicationResponse[]>();
  displayedColumns = ['id', 'requestReference', 'customerId', 'amount', 'termMonths', 'status', 'processedAt'];
}