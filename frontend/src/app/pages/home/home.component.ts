import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { FooterComponent } from '../../shared/footer/footer.component';
import { ApiService, ContactSettings, EventItem, GalleryCategory, TestimonialItem } from '../../core/services/api.service';

interface HeroClip {
  image: string;
  tagline: string;
  sub: string;
  hasCTA?: boolean;
}

interface GalleryItem {
  title: string;
  image: string;
  slug: string;
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
  testimonials: TestimonialItem[] = [];
  activeTestimonial = 0;
  testimonialVisible = true;
  testimonialsLoading = true;
  testimonialsError = false;
  contact: ContactSettings = { whatsapp: '+91 9302259211', whatsappUrl: 'https://wa.me/919302259211' };
  loadingEvents = true;
  eventsError = false;
  servicesLoading = true;
  servicesError = false;

  activeClip = 0;
  textVisible = true;
  private clipTimer: any;
  private testimonialTransitionTimer: ReturnType<typeof setTimeout> | undefined;

  heroClips: HeroClip[] = [
    { image: 'assets/images/hero-baraat.jpeg', tagline: 'Your Story Begins Here', sub: 'The Beginning' },
    { image: 'assets/images/tailored-gallery-1.jpeg', tagline: 'Every Detail Beautifully Designed.', sub: 'The Design' },
    { image: 'assets/images/tailored-gallery-2.jpeg', tagline: 'Every Moment Seamlessly Managed.', sub: 'The Experience' },
    { image: 'assets/images/tailored-gallery-3.jpeg', tagline: "Let's Create Your Sneh Moment.", sub: 'The Action', hasCTA: true }
  ];

  galleryItems: GalleryItem[] = [

  ];
  galleryLoading = true;
  galleryError = false;

  services: ServiceItem[] = [];

  approachSteps: ApproachStep[] = [
    { step: '01', title: 'We Listen', desc: 'Your vision, your family, your story. We begin by deeply understanding what your wedding should feel like.', image: 'https://images.unsplash.com/photo-1774024050561-4ee0148c8526?w=800&h=600&fit=crop&auto=format' },
    { step: '02', title: 'We Plan', desc: 'A meticulously crafted blueprint for every function and detail \u2014 timelines, budgets, vendor selections, all aligned to your dream.', image: 'https://images.unsplash.com/photo-1469371670807-013ccf25f16a?w=800&h=600&fit=crop&auto=format' },
    { step: '03', title: 'We Design', desc: 'Our creative directors translate your aesthetic vision into breathtaking environments \u2014 florals, lighting, and staging.', image: 'assets/images/tailored-gallery-1.jpeg' },
    { step: '04', title: 'We Execute', desc: 'On the day, our team becomes invisible so you can be fully present. Every vendor, every moment, managed with quiet precision.', image: 'assets/images/hero-baraat.jpeg' }
  ];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getGalleryCategories().subscribe({
      next: categories => { this.galleryItems = categories.map(c => ({ title: c.name, slug: c.slug, image: this.api.mediaUrl(c.coverImage) })); this.galleryLoading = false; },
      error: () => { this.galleryLoading = false; this.galleryError = true; }
    });
    this.api.getServices().subscribe({
      next: items => { this.services = items.map(item => ({ title: item.name, desc: item.description || '', image: this.api.mediaUrl(item.imageUrl) })); this.servicesLoading = false; },
      error: () => { this.servicesLoading = false; this.servicesError = true; }
    });
    this.api.getTestimonials().subscribe({
      next: items => { this.testimonials = items; this.testimonialsLoading = false; this.activeTestimonial = 0; },
      error: () => { this.testimonialsLoading = false; this.testimonialsError = true; }
    });
    this.api.getContactSettings().subscribe({ next: value => this.contact = { ...this.contact, ...value } });
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

  showTestimonial(index: number): void {
    if (!this.testimonials.length) return;
    this.activeTestimonial = (index + this.testimonials.length) % this.testimonials.length;
    this.testimonialVisible = false;
    if (this.testimonialTransitionTimer) clearTimeout(this.testimonialTransitionTimer);
    this.testimonialTransitionTimer = setTimeout(() => this.testimonialVisible = true, 20);
  }

  testimonialTouchStart(event: TouchEvent): void { this.touchStartX = event.changedTouches[0]?.screenX ?? 0; }
  testimonialTouchEnd(event: TouchEvent): void {
    const delta = (event.changedTouches[0]?.screenX ?? this.touchStartX) - this.touchStartX;
    if (Math.abs(delta) > 45) this.showTestimonial(this.activeTestimonial + (delta < 0 ? 1 : -1));
  }
  whatsappLink(): string { return this.contact.whatsappUrl || 'https://wa.me/919302259211'; }
  whatsappLabel(): string { return this.contact.whatsapp || '+91 9302259211'; }

  private touchStartX = 0;

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
    if (this.testimonialTransitionTimer) clearTimeout(this.testimonialTransitionTimer);
  }
}
