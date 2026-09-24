import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CsrfTokenService } from './csrf-token.service';

function cookie(name: string): string | null {
  const item = document.cookie.split('; ').find(value => value.startsWith(`${name}=`));
  return item ? decodeURIComponent(item.substring(name.length + 1)) : null;
}

export const apiCredentialsInterceptor: HttpInterceptorFn = (request, next) => {
  const http = inject(HttpClient);
  const tokens = inject(CsrfTokenService);
  const mutating = !['GET', 'HEAD', 'OPTIONS'].includes(request.method);
  const send = (token: string | null) => next(request.clone({
    withCredentials: true,
    ...(token && mutating ? { setHeaders: { 'X-XSRF-TOKEN': token } } : {})
  }));
  const token = tokens.get() || cookie('XSRF-TOKEN');
  if (mutating && !token && !request.url.endsWith('/api/auth/csrf')) {
    return http.get<{ token: string }>(`${environment.apiUrl}/api/auth/csrf`, { withCredentials: true }).pipe(
      switchMap(response => { tokens.set(response.token); return send(response.token); })
    );
  }
  return send(token);
};
