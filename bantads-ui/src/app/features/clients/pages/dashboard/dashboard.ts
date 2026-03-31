import { Component } from '@angular/core';
import { CashBalance } from '../../components/cash-balance/cash-balance';
import { SmallCard } from '../../components/small-card/small-card';
import { ExtratoTable } from '../../components/extrato-table/extrato-table';
import { Header } from '../../../../shared/components/header/header';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [CashBalance, SmallCard, ExtratoTable, Header, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard { }
