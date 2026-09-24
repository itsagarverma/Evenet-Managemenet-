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
  email?: string;
  eventType?: string;
  cityVenue?: string;
  message?: string;
}

export interface QueryResponse extends QueryRequest {
  id: number;
}
export interface QueryReceipt { id: number; }

export interface GalleryImage { id: number; url: string; altText: string; displayOrder: number; published: boolean; }
export interface GalleryCategory { id: number; name: string; slug: string; description?: string; coverImage?: string; published: boolean; displayOrder: number; images?: GalleryImage[]; }
export interface ManagedService { id: number; name: string; description: string; imageUrl?: string; published: boolean; displayOrder: number; }
export interface TestimonialItem { id: number; name: string; eventType?: string; location?: string; review: string; published: boolean; displayOrder: number; }
export interface ContactSettings { email?: string; phone?: string; whatsapp?: string; whatsappUrl?: string; instagram?: string; facebook?: string; website?: string; address?: string; }

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

  submitQuery(query: QueryRequest): Observable<QueryReceipt> {
    return this.http.post<QueryReceipt>(`${this.baseUrl}/queries`, query);
  }

  getGalleryCategories(): Observable<GalleryCategory[]> { return this.http.get<GalleryCategory[]>(`${this.baseUrl}/api/gallery/categories`); }
  getGalleryCategory(slug: string): Observable<GalleryCategory> { return this.http.get<GalleryCategory>(`${this.baseUrl}/api/gallery/categories/${encodeURIComponent(slug)}`); }
  getServices(): Observable<ManagedService[]> { return this.http.get<ManagedService[]>(`${this.baseUrl}/api/services`); }
  getTestimonials(): Observable<TestimonialItem[]> { return this.http.get<TestimonialItem[]>(`${this.baseUrl}/api/testimonials`); }
  getContactSettings(): Observable<ContactSettings> { return this.http.get<ContactSettings>(`${this.baseUrl}/api/contact-settings`); }
  mediaUrl(path?: string): string { return path ? (path.startsWith('http') || path.startsWith('assets/') ? path : `${this.baseUrl}${path}`) : 'assets/images/tailored-gallery-1.jpeg'; }
}
