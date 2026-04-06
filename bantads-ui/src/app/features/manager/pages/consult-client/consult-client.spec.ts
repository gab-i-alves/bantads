import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultClient } from './consult-client';

describe('ConsultClient', () => {
  let component: ConsultClient;
  let fixture: ComponentFixture<ConsultClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultClient],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
