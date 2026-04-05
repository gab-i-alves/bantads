import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewManager } from './new-manager';

describe('NewManager', () => {
  let component: NewManager;
  let fixture: ComponentFixture<NewManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewManager],
    }).compileComponents();

    fixture = TestBed.createComponent(NewManager);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
