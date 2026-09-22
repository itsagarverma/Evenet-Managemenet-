import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { FooterComponent } from '../../shared/footer/footer.component';
import { ApiService, EventItem } from '../../core/services/api.service';

interface HeroClip {
  image: string;
  tagline: string;
  sub: string;
  hasCTA?: boolean;
}

interface GalleryItem {
  title: string;
  image: string;
}

interface ServiceItem {
  title: string;
  desc: string;
  image: string;
}

interface ApproachStep {
  step: string;
  title: string;
  desc: string;
  image: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  events: EventItem[] = [];
  loadingEvents = true;
  eventsError = false;

  activeClip = 0;
  textVisible = true;
  private clipTimer: any;

  heroClips: HeroClip[] = [
    { image: 'assets/images/hero-baraat.jpeg', tagline: 'Your Story Begins Here', sub: 'The Beginning' },
    { image: 'assets/images/tailored-gallery-1.jpeg', tagline: 'Every Detail Beautifully Designed.', sub: 'The Design' },
    { image: 'assets/images/tailored-gallery-2.jpeg', tagline: 'Every Moment Seamlessly Managed.', sub: 'The Experience' },
    { image: 'assets/images/tailored-gallery-3.jpeg', tagline: "Let's Create Your Sneh Moment.", sub: 'The Action', hasCTA: true }
  ];

  galleryItems: GalleryItem[] = [
    { title: 'Barat', image: 'assets/images/hero-baraat.jpeg' },
    { title: 'Haldi', image: 'https://images.unsplash.com/photo-1647949940712-bfcf82015d9b?w=800&h=1000&fit=crop&auto=format' },
    { title: 'Mehendi', image: 'https://images.unsplash.com/photo-1686865604150-43f95d61416c?w=800&h=1000&fit=crop&auto=format' },
    { title: 'Mandap', image: 'assets/images/tailored-gallery-1.jpeg' },
    { title: 'Sangeet', image: 'https://images.unsplash.com/photo-1640745676611-bee05627a23c?w=800&h=1000&fit=crop&auto=format' },
    { title: 'Reception', image: 'assets/images/tailored-gallery-3.jpeg' },
    { title: 'Birthday', image: 'https://images.unsplash.com/photo-1729237261091-bae8eba0c60c?w=800&h=1000&fit=crop&auto=format' },
    { title: 'Show Flow', image: 'assets/images/tailored-gallery-2.jpeg' }
  ];

  services: ServiceItem[] = [
    {
      title: 'Wedding Planning',
      desc: 'From the first date to the final farewell, we orchestrate every element of your celebration with precision, care, and creative vision.',
      image: 'assets/images/hero-baraat.jpeg'
    },
    {
      title: 'D\u00e9cor & Design',
      desc: 'Lush floral installations, curated colour palettes, and immersive lighting \u2014 we design environments that move people.',
      image: 'assets/images/tailored-gallery-1.jpeg'
    },
    {
      title: 'Vendor & Guest Management',
      desc: 'Our curated vendor network brings you India\'s finest photographers, caterers, musicians, and artists.',
      image: 'https://images.unsplash.com/photo-1712314947761-a8d718bd8c32?w=900&h=700&fit=crop&auto=format'
    },
    {
      title: 'Complete Wedding Management',
      desc: 'Our signature full-service package. We take full ownership of every function so you experience pure joy.',
      image: 'assets/images/tailored-gallery-2.jpeg'
    }
  ];

  approachSteps: ApproachStep[] = [
    { step: '01', title: 'We Listen', desc: 'Your vision, your family, your story. We begin by deeply understanding what your wedding should feel like.', image: 'https://images.unsplash.com/photo-1774024050561-4ee0148c8526?w=800&h=600&fit=crop&auto=format' },
    { step: '02', title: 'We Plan', desc: 'A meticulously crafted blueprint for every function and detail \u2014 timelines, budgets, vendor selections, all aligned to your dream.', image: 'https://images.unsplash.com/photo-1469371670807-013ccf25f16a?w=800&h=600&fit=crop&auto=format' },
    { step: '03', title: 'We Design', desc: 'Our creative directors translate your aesthetic vision into breathtaking environments \u2014 florals, lighting, and staging.', image: 'assets/images/tailored-gallery-1.jpeg' },
    { step: '04', title: 'We Execute', desc: 'On the day, our team becomes invisible so you can be fully present. Every vendor, every moment, managed with quiet precision.', image: 'assets/images/hero-baraat.jpeg' }
  ];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getEvents().subscribe({
      next: (events) => { this.events = events; this.loadingEvents = false; },
      error: () => { this.loadingEvents = false; this.eventsError = true; }
    });

    this.clipTimer = setInterval(() => {
      this.textVisible = false;
      setTimeout(() => {
        this.activeClip = (this.activeClip + 1) % this.heroClips.length;
        this.textVisible = true;
      }, 500);
    }, 4000);
  }

  setClip(i: number): void {
    this.activeClip = i;
    this.textVisible = true;
  }

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) entry.target.classList.add('visible');
        });
      },
      { threshold: 0.15 }
    );
    document.querySelectorAll('.fade-up').forEach((el) => observer.observe(el));
  }

  ngOnDestroy(): void {
    if (this.clipTimer) clearInterval(this.clipTimer);
  }
}
