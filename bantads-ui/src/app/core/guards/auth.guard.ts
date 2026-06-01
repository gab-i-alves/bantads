import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        return router.createUrlTree(['/']);
    }

    const role = authService.getRole();
    const url = state.url;

    const permissions: Record<string, string> = {
        '/client': 'CLIENTE',
        '/admin': 'ADMINISTRADOR',
        '/manager': 'GERENTE'
    };

    for (const prefix in permissions) {
        if (url.startsWith(prefix)) {
            return role === permissions[prefix]
                ? true
                : router.createUrlTree(['/']);
        }
    }

    return true;
};