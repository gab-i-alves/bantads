import { Component } from '@angular/core';
import { Aside } from '../../../../shared/components/aside/aside';

@Component({
  selector: 'app-dashboard',
  imports: [Aside],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard { }
