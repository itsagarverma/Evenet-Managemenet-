import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AdminAuthService } from './admin-auth.service';
import { catchError, map, of } from 'rxjs';
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AdminAuthService); const router = inject(Router);
  return auth.me().pipe(map(() => true), catchError(() => of(router.parseUrl('/admin/login'))));
};
