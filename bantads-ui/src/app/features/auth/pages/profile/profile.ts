import { Component } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';

@Component({
  selector: 'app-profile',
  imports: [Header],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  step: number = 1;

  nextStep() {
    if (this.step < 4) this.step++;
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }
}
