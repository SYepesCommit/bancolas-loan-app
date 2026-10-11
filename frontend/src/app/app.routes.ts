import { Routes } from '@angular/router';
import { LoanDashboardComponent } from '@features/loan/pages/loan-dashboard/loan-dashboard.component';

export const routes: Routes = [
  { path: '', component: LoanDashboardComponent },
  { path: '**', redirectTo: '' }
];