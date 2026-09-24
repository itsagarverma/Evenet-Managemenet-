import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService, ContactSettings } from '../../core/services/api.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent implements OnInit {
  year = new Date().getFullYear();
  contact: ContactSettings = { email: 'thesnehmoments@gmail.com', phone: '+91 9302259211', whatsapp: '+91 9302259211', whatsappUrl: 'https://wa.me/919302259211', instagram: 'https://www.instagram.com/thesnehmoments', website: 'https://thesnehmoments.in/', address: 'Bhopal, Madhya Pradesh' };
  constructor(private api: ApiService) {}
  ngOnInit(): void { this.api.getContactSettings().subscribe({ next: settings => this.contact = { ...this.contact, ...settings } }); }
}
