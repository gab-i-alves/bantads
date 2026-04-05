import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultAllClients } from './consult-all-clients';

describe('ConsultAllClients', () => {
  let component: ConsultAllClients;
  let fixture: ComponentFixture<ConsultAllClients>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultAllClients],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultAllClients);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
