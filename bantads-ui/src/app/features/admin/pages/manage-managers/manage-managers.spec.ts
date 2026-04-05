import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageManagers } from './manage-managers';

describe('ManageManagers', () => {
  let component: ManageManagers;
  let fixture: ComponentFixture<ManageManagers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageManagers],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageManagers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
