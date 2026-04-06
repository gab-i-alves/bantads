import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AdminService {

  private key = 'admins';

  constructor() {
    this.initMock();
  }

  private initMock() {
    if (!localStorage.getItem(this.key)) {
      const admins = [
        {
          idAdmin: 1,
          nome: 'thiago admin',
          email: 'admin@admin',
          senha: '123456'
        }
      ];

      localStorage.setItem(this.key, JSON.stringify(admins));
    }
  }

  // utils
  private getDB(): any[] {
    return JSON.parse(localStorage.getItem(this.key) || '[]');
  }

  private saveDB(admin: any[]) {
    localStorage.setItem(this.key, JSON.stringify(admin));
  }

  //funcionalidades
  getAdmins() {
    return this.getDB();
  }

}
