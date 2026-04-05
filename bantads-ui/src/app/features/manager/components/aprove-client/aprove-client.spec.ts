import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AproveClient } from './aprove-client';

describe('AproveClient', () => {
  let component: AproveClient;
  let fixture: ComponentFixture<AproveClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AproveClient],
    }).compileComponents();

    fixture = TestBed.createComponent(AproveClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
