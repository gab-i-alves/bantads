import { Routes } from '@angular/router';
import { Login } from './features/auth/pages/login/login';
import { Dashboard } from './features/clients/pages/dashboard/dashboard';
import { Deposit } from './features/clients/pages/deposit/deposit';
import { Transfer } from './features/clients/pages/transfer/transfer';
import { Withdraw } from './features/clients/pages/withdraw/withdraw';
import { TransactionHistory } from './features/clients/pages/transaction-history/transaction-history';
import { Profile } from './features/auth/pages/profile/profile';
import { DashboardManager } from './features/manager/pages/dashboard/dashboard';
import { ConsultAllClients } from './features/manager/pages/consult-all-clients/consult-all-clients';
import { ConsultClient } from './features/manager/pages/consult-client/consult-client';
import { TopClients } from './features/manager/pages/top-clients/top-clients';
import { DashboardAdmin } from './features/admin/pages/dashboard/dashboard';
import { CustomerReport } from './features/admin/pages/customer-report/customer-report';
import { ManageManagers } from './features/admin/pages/manage-managers/manage-managers';

export const routes: Routes = [
    {
        path: '',
        component: Login
    },
    {
        path: 'perfil',
        component: Profile
    },
    // Rotas para o cliente
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
    },
    // Rotas para o gerente
    {
        path: 'manager/dashboard',
        component: DashboardManager
    },
    {
        path: 'manager/consultar-clientes',
        component: ConsultAllClients
    },
    {
        path: 'manager/consultar-cliente',
        component: ConsultClient
    },
    {
        path: 'manager/top-clientes',
        component: TopClients
    },
    // Rotas para o admin
    {
        path: 'admin/dashboard',
        component: DashboardAdmin
    },
    {
        path: 'admin/relatorio-clientes',
        component: CustomerReport
    },
    {
        path: 'admin/gerenciar-gerentes',
        component: ManageManagers
    }
];
