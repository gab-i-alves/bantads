import { Routes } from '@angular/router';
import { Login } from './features/auth/pages/login/login';
import { Dashboard } from './features/clients/pages/dashboard/dashboard';
import { Deposit } from './features/clients/pages/deposit/deposit';
import { Transfer } from './features/clients/pages/transfer/transfer';
import { Withdraw } from './features/clients/pages/withdraw/withdraw';
import { TransactionHistory } from './features/clients/pages/transaction-history/transaction-history';
import { Profile } from './features/auth/pages/profile/profile';

export const routes: Routes = [
    {
        path: '',
        component: Login
    },
    {
        path: 'perfil',
        component: Profile
    },
    {
        path: 'client/dashboard',
        component: Dashboard
    },
    {
        path: 'client/transferir',
        component: Transfer
    },
    {
        path: 'client/sacar',
        component: Withdraw
    },
    {
        path: 'client/depositar',
        component: Deposit
    },
    {
        path: 'client/extrato',
        component: TransactionHistory
    }
];
