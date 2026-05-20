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
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        component: Login
    },
    {
        path: 'perfil',
        component: Profile,
        canActivate: [authGuard]
    },
    // Rotas para o cliente
    {
        path: 'client/dashboard',
        component: Dashboard,
        canActivate: [authGuard]
    },
    {
        path: 'client/transferir',
        component: Transfer,
        canActivate: [authGuard]
    },
    {
        path: 'client/sacar',
        component: Withdraw,
        canActivate: [authGuard]
    },
    {
        path: 'client/depositar',
        component: Deposit,
        canActivate: [authGuard]
    },
    {
        path: 'client/extrato',
        component: TransactionHistory,
        canActivate: [authGuard]
    },
    // Rotas para o gerente
    {
        path: 'manager/dashboard',
        component: DashboardManager, 
        canActivate: [authGuard]
    },
    {
        path: 'manager/consultar-clientes',
        component: ConsultAllClients, 
        canActivate: [authGuard]
    },
    {
        path: 'manager/consultar-cliente',
        component: ConsultClient, 
        canActivate: [authGuard]
    },
    {
        path: 'manager/top-clientes',
        component: TopClients, 
        canActivate: [authGuard]
    },
    // Rotas para o admin
    {
        path: 'admin/dashboard',
        component: DashboardAdmin, 
        canActivate: [authGuard]

    },
    {
        path: 'admin/relatorio-clientes',
        component: CustomerReport, 
        canActivate: [authGuard]
    },
    {
        path: 'admin/gerenciar-gerentes',
        component: ManageManagers, 
        canActivate: [authGuard]
    }
];
