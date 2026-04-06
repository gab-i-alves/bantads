import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListClients } from './list-clients';

describe('ListClients', () => {
  let component: ListClients;
  let fixture: ComponentFixture<ListClients>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListClients],
    }).compileComponents();

    fixture = TestBed.createComponent(ListClients);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
