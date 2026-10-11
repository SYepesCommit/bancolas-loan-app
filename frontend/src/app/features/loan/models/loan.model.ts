export type LoanStatus = 'APPROVED' | 'REJECTED';

export interface LoanApplicationRequest {
  requestReference: string;
  customerId: string;
  amount: number;
  termMonths: number;
}

export interface LoanApplicationResponse {
  id: number;
  requestReference: string;
  customerId: string;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  rejectionReason?: string;
  processedAt: string;
}