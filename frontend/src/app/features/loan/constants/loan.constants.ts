export const LOAN_DISPLAYED_COLUMNS = [
  'id',
  'requestReference',
  'customerId',
  'amount',
  'termMonths',
  'status',
  'processedAt'
] as const;

export type LoanColumnKey = typeof LOAN_DISPLAYED_COLUMNS[number];