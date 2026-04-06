import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditManager } from './edit-manager';

describe('EditManager', () => {
  let component: EditManager;
  let fixture: ComponentFixture<EditManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditManager],
    }).compileComponents();

    fixture = TestBed.createComponent(EditManager);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
