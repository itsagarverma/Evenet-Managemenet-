import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EventItem {
  id: number;
  title: string;
  description: string;
  location: string;
  eventDate: string;
}

export interface QueryRequest {
  fullName: string;
  phone: string;
  email: string;
  eventType: string;
  eventDate: string;
  cityVenue: string;
  budget: number;
  numberOfGuests: number;
  specialRequirements?: string;
  message: string;
}

export interface QueryResponse extends QueryRequest {
  id: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getEvents(): Observable<EventItem[]> {
    return this.http.get<EventItem[]>(`${this.baseUrl}/events`);
  }

  getEventById(id: number): Observable<EventItem> {
    return this.http.get<EventItem>(`${this.baseUrl}/events/${id}`);
  }

  submitQuery(query: QueryRequest): Observable<QueryResponse> {
    return this.http.post<QueryResponse>(`${this.baseUrl}/queries`, query);
  }
}
