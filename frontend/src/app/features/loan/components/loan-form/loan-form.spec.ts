import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoanFormComponent } from './loan-form';
import { Customer } from '@features/customer/models/customer.model';

describe('LoanFormComponent', () => {
  let component: LoanFormComponent;
  let fixture: ComponentFixture<LoanFormComponent>;

  const mockCustomers: Customer[] = [
    { id: 'USR-10',  availableCapacity: 220000, status: 'ACTIVE' },
    { id: 'USR-20',  availableCapacity: 2000000, status: 'ACTIVE' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanFormComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoanFormComponent);
    component = fixture.componentInstance;
    
    // Asignar los inputs obligatorios
    fixture.componentRef.setInput('customers', mockCustomers);
    fixture.detectChanges();
  });

  it('debe crear el componente correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe inicializar el formulario con los valores predeterminados', () => {
    const formValue = component.loanForm.getRawValue();
    
    expect(formValue).toEqual({
      requestReference: expect.stringMatching(/^SOL-\d+$/),
      customerId: '',
      amount: 500000,
      termMonths: 12
    });
  });

  it('debe rechazar un monto inferior a $10.000 COP', () => {
    component.loanForm.patchValue({ amount: 5000 });
    expect(component.loanForm.get('amount')?.invalid).toBe(true);
  });

  it('debe rechazar una referencia con formato incorrecto', () => {
    component.loanForm.patchValue({ requestReference: 'INVALIDO-123' });
    expect(component.loanForm.get('requestReference')?.invalid).toBe(true);
  });

  it('debe rechazar un plazo inferior a 6 meses', () => {
    component.loanForm.patchValue({ termMonths: 3 });
    expect(component.loanForm.get('termMonths')?.invalid).toBe(true);
  });

  it('debe rechazar un plazo superior a 60 meses', () => {
    component.loanForm.patchValue({ termMonths: 72 });
    expect(component.loanForm.get('termMonths')?.invalid).toBe(true);
  });

  it('debe seleccionar rápidamente un cliente', () => {
    component.setQuickCustomer('USR-10');
    expect(component.loanForm.get('customerId')?.value).toBe('USR-10');
  });

  it('debe emitir submitForm con el payload correcto cuando el formulario es válido', () => {
    let emittedPayload: any = null;
    component.submitForm.subscribe((payload) => {
      emittedPayload = payload;
    });

    component.loanForm.patchValue({
      customerId: 'USR-10',
      amount: 150000,
      termMonths: 12
    });

    component.onSubmit();

    expect(emittedPayload).toEqual({
      requestReference: expect.stringMatching(/^SOL-\d+$/),
      customerId: 'USR-10',
      amount: 150000,
      termMonths: 12
    });
  });

  it('no debe emitir submitForm cuando el formulario es inválido', () => {
    let emitted = false;
    component.submitForm.subscribe(() => {
      emitted = true;
    });

    component.loanForm.patchValue({ customerId: '' }); // Inválido sin cliente
    component.onSubmit();

    expect(emitted).toBe(false);
  });

  it('debe reiniciar el formulario con los valores predeterminados y un nuevo consecutivo', () => {
    component.loanForm.patchValue({
      customerId: 'USR-10',
      amount: 800000,
      termMonths: 24
    });

    component.resetForm();

    const formValue = component.loanForm.getRawValue();
    expect(formValue).toEqual({
      requestReference: expect.stringMatching(/^SOL-\d+$/),
      customerId: '',
      amount: 500000,
      termMonths: 12
    });
  });
});