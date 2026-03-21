import { Routes } from '@angular/router';
import { Login } from './features/auth/pages/login/login';
import { Dashboard } from './features/clients/pages/dashboard/dashboard';

export const routes: Routes = [
    {
        path: '',
        component: Login
    },
    {
        path: 'client/dashboard',
        component: Dashboard
    },
];
