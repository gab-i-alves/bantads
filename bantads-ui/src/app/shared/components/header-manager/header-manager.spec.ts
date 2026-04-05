import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderManager } from './header-manager';

describe('HeaderManager', () => {
  let component: HeaderManager;
  let fixture: ComponentFixture<HeaderManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderManager],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderManager);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
