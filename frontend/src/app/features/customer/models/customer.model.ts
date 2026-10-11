export interface Customer {
  id: string;
  status: 'ACTIVE' | 'BLOCKED';
  availableCapacity: number;
}