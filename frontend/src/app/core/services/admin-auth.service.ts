import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CsrfTokenService } from './csrf-token.service';

@Injectable({ providedIn: 'root' })
export class AdminAuthService {
  private readonly base = `${environment.apiUrl}/api/auth`;
  constructor(private http: HttpClient, private tokens: CsrfTokenService) {}
  csrf(): Observable<{ token: string }> { return this.http.get<{ token: string }>(`${this.base}/csrf`).pipe(tap(result => this.tokens.set(result.token))); }
  login(email: string, password: string): Observable<{ email: string }> {
    return this.csrf().pipe(switchMap(() => this.http.post<{ email: string }>(`${this.base}/login`, { email, password })));
  }
  logout(): Observable<void> { return this.http.post<void>(`${this.base}/logout`, {}); }
  me(): Observable<{ email: string }> { return this.http.get<{ email: string }>(`${this.base}/me`); }
}
