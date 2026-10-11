
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';


import { LoanService } from './loan.service';
import { environment } from 'src/environments/environment.development';
import { LoanApplicationRequest, LoanApplicationResponse } from '../models/loan.model';

describe('LoanService', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/loans`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoanService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LoanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debe obtener el historial de préstamos mediante GET', () => {
    const mockLoans: LoanApplicationResponse[] = [
      {
        id: 1,
        requestReference: 'SOL-001',
        customerId: 'USR-10',
        amount: 500000,
        termMonths: 12,
        status: 'APPROVED',
        processedAt: '2026-10-10T10:00:00Z'
      }
    ];

    service.getRecentLoans().subscribe({
      next: (loans) => {
        expect(loans).toHaveLength(1);
        expect(loans[0].requestReference).toBe('SOL-001');
        expect(loans[0].status).toBe('APPROVED');
      }
    });

    const req = httpMock.expectOne(apiUrl);

    expect(req.request.method).toBe('GET');

    req.flush(mockLoans);
  });

  it('debe enviar una solicitud de préstamo mediante POST', () => {
    const payload: LoanApplicationRequest = {
      requestReference: 'SOL-002',
      customerId: 'USR-20',
      amount: 300000,
      termMonths: 24
    };

    const mockResponse: LoanApplicationResponse = {
      id: 2,
      ...payload,
      status: 'APPROVED',
      processedAt: '2026-10-10T11:00:00Z'
    };

    service.applyForLoan(payload).subscribe({
      next: (response) => {
        expect(response.status).toBe('APPROVED');
        expect(response.id).toBe(2);
      }
    });

    const req = httpMock.expectOne(apiUrl);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);
  });
});
