import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface ServiceItem {
  title: string;
  image: string;
  description: string;
}

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './services.component.html',
  styleUrl: './services.component.css'
})
export class ServicesComponent implements AfterViewInit {
  @ViewChild('track') trackRef!: ElementRef<HTMLDivElement>;

  services: ServiceItem[] = [
    {
      title: 'Weddings',
      image: 'https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=600&q=80',
      description: 'From intimate ceremonies to grand celebrations, we bring your dream wedding to life with unparalleled elegance.'
    },
    {
      title: 'Hospitality',
      image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&q=80',
      description: 'Ensuring every guest feels welcomed, comfortable, and well taken care of — from arrival to departure.'
    },
    {
      title: 'Logistics',
      image: 'https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=600&q=80',
      description: 'Seamless pickup and drop services, ensuring your guests travel comfortably and feel truly special.'
    },
    {
      title: 'Show Flow',
      image: 'https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=600&q=80',
      description: 'From entries to performances and ceremonies, we design and manage the entire event flow seamlessly.'
    },
    {
      title: 'Material & Gifting',
      image: 'https://images.unsplash.com/photo-1549488344-cbb6c34f0bc9?w=600&q=80',
      description: 'End-to-end material management and curated gifting services ensuring elegance and a memorable experience.'
    }
  ];

  private isDown = false;
  private startX = 0;
  private scrollLeft = 0;

  constructor(private router: Router) {}

  ngAfterViewInit(): void {
    const track = this.trackRef.nativeElement;

    track.addEventListener('mousedown', (e: MouseEvent) => {
      this.isDown = true;
      this.startX = e.pageX - track.offsetLeft;
      this.scrollLeft = track.scrollLeft;
    });
    track.addEventListener('mouseleave', () => (this.isDown = false));
    track.addEventListener('mouseup', () => (this.isDown = false));
    track.addEventListener('mousemove', (e: MouseEvent) => {
      if (!this.isDown) return;
      e.preventDefault();
      const x = e.pageX - track.offsetLeft;
      track.scrollLeft = this.scrollLeft - (x - this.startX) * 1.5;
    });
  }

  scroll(direction: number): void {
    this.trackRef.nativeElement.scrollBy({ left: direction * 340, behavior: 'smooth' });
  }

  goToQuery(): void {
    this.router.navigate(['/query']);
  }
}
