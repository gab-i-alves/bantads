import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SmallCard } from './small-card';

describe('SmallCard', () => {
  let component: SmallCard;
  let fixture: ComponentFixture<SmallCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SmallCard],
    }).compileComponents();

    fixture = TestBed.createComponent(SmallCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
