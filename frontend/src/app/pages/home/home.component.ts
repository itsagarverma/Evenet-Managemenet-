import { AfterViewInit, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { FooterComponent } from '../../shared/footer/footer.component';
import { ServicesComponent } from '../../shared/services/services.component';
import { ApiService, EventItem } from '../../core/services/api.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent, ServicesComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, AfterViewInit {
  events: EventItem[] = [];
  loadingEvents = true;
  eventsError = false;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getEvents().subscribe({
      next: (events) => {
        this.events = events;
        this.loadingEvents = false;
      },
      error: () => {
        this.loadingEvents = false;
        this.eventsError = true;
      }
    });
  }

  ngAfterViewInit(): void {
    // Scroll reveal animation, same behaviour as the original script.js
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.15 }
    );

    document.querySelectorAll('.fade-up, .fade-in').forEach((el) => observer.observe(el));
  }
}
