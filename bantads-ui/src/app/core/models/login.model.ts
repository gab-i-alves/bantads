import { Cliente } from './cliente.model';

export interface LoginModel {
    access_token: string;
    token_type: string;
    role: string;
    tipo?: string;
    account_id?: string;
    usuario?: Cliente;
}
