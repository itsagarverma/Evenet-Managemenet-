import { Injectable } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class CsrfTokenService {
  private value: string | null = null;
  set(token: string): void { this.value = token; }
  get(): string | null { return this.value; }
}
