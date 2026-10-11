import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { LoanApplicationRequest, LoanApplicationResponse } from '@features/loan/models/loan.model';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/loans`;
  

  applyForLoan(request: LoanApplicationRequest): Observable<LoanApplicationResponse> {
    return this.http.post<LoanApplicationResponse>(this.apiUrl, request);
  }

  getRecentLoans(): Observable<LoanApplicationResponse[]> {
    return this.http.get<LoanApplicationResponse[]>(this.apiUrl);
  }
}