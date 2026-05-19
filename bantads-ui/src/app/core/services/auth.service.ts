import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { LoginModel } from '../models/login.model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);

  private ApiBaseUrl = 'http://localhost:3000';

  login(email: string, password: string) {
    return this.http.post<LoginModel>(
      `${this.ApiBaseUrl}/auth/login`,
      { email, password }
    );
  }

  handleLoginSuccess(response: LoginModel) {
    localStorage.setItem('token', response.access_token);
    localStorage.setItem('role', response.role);

    this.redirectByRole(response.role);
  }

  redirectByRole(role: string) {
    switch (role) {
      case 'GERENTE':
        this.router.navigate(['/manager/dashboard']);
        break;
      case 'CLIENTE':
        this.router.navigate(['client/dashboard']);
        break;
      case 'ADMINISTRADOR':
        this.router.navigate(['/admin/dashboard']);
        break;
      default:
        this.router.navigate(['/']);
    }
  }

  getToken() {
    return localStorage.getItem('token');
  }

  getRole() {
    return localStorage.getItem('role');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
