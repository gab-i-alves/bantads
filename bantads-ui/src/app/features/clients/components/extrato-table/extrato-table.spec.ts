import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExtratoTable } from './extrato-table';

describe('ExtratoTable', () => {
  let component: ExtratoTable;
  let fixture: ComponentFixture<ExtratoTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExtratoTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ExtratoTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
