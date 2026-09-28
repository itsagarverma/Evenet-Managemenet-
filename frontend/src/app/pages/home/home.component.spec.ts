import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { ApiService, GalleryCategory, ManagedService } from '../../core/services/api.service';
import { HomeComponent } from './home.component';

class TrackingIntersectionObserver implements IntersectionObserver {
  static instances: TrackingIntersectionObserver[] = [];

  readonly root: Element | Document | null = null;
  readonly rootMargin = '';
  readonly thresholds: readonly number[] = [];
  readonly observed = new Set<Element>();
  readonly unobserved = new Set<Element>();
  disconnectCalls = 0;

  constructor(private readonly callback: IntersectionObserverCallback) {
    TrackingIntersectionObserver.instances.push(this);
  }

  observe(target: Element): void {
    this.observed.add(target);
  }

  unobserve(target: Element): void {
    this.observed.delete(target);
    this.unobserved.add(target);
  }

  disconnect(): void {
    this.disconnectCalls++;
    this.observed.clear();
  }

  takeRecords(): IntersectionObserverEntry[] {
    return [];
  }

  reveal(target: Element): void {
    this.callback([{ target, isIntersecting: true } as IntersectionObserverEntry], this);
  }
}

describe('HomeComponent scroll reveal', () => {
  let fixture: ComponentFixture<HomeComponent>;
  let servicesResponse: Subject<ManagedService[]>;
  let categoriesResponse: Subject<GalleryCategory[]>;
  let originalIntersectionObserver: typeof IntersectionObserver;

  beforeEach(async () => {
    originalIntersectionObserver = window.IntersectionObserver;
    TrackingIntersectionObserver.instances = [];
    window.IntersectionObserver = TrackingIntersectionObserver as typeof IntersectionObserver;

    servicesResponse = new Subject<ManagedService[]>();
    categoriesResponse = new Subject<GalleryCategory[]>();
    const api = jasmine.createSpyObj<ApiService>('ApiService', [
      'getGalleryCategories', 'getServices', 'getTestimonials', 'getContactSettings', 'getEvents', 'mediaUrl'
    ]);
    api.getGalleryCategories.and.returnValue(categoriesResponse.asObservable());
    api.getServices.and.returnValue(servicesResponse.asObservable());
    api.getTestimonials.and.returnValue(of([]));
    api.getContactSettings.and.returnValue(of({}));
    api.getEvents.and.returnValue(of([]));
    api.mediaUrl.and.callFake((path?: string) => path || 'assets/images/tailored-gallery-1.jpeg');

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([]), { provide: ApiService, useValue: api }]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture?.destroy();
    window.IntersectionObserver = originalIntersectionObserver;
  });

  it('observes static and asynchronously rendered CMS elements and disconnects on destroy', () => {
    const [observer] = TrackingIntersectionObserver.instances;
    expect(TrackingIntersectionObserver.instances.length).toBe(1);

    const staticStory = fixture.nativeElement.querySelector('.story-content') as HTMLElement;
    expect(observer.observed.has(staticStory)).toBeTrue();

    servicesResponse.next([{
      id: 1,
      name: 'Wedding Planning',
      description: 'Planning service',
      imageUrl: 'assets/images/hero-baraat.jpeg',
      published: true,
      displayOrder: 0
    }]);
    categoriesResponse.next([{
      id: 1,
      name: 'Barat',
      slug: 'barat',
      coverImage: 'assets/images/hero-baraat.jpeg',
      published: true,
      displayOrder: 0,
      images: []
    }]);
    fixture.detectChanges();

    const serviceCard = fixture.nativeElement.querySelector('.service-row') as HTMLElement;
    const categoryCard = fixture.nativeElement.querySelector('.gallery-item') as HTMLElement;
    expect(serviceCard).toBeTruthy();
    expect(categoryCard).toBeTruthy();
    expect(observer.observed.has(serviceCard)).toBeTrue();
    expect(observer.observed.has(categoryCard)).toBeTrue();

    observer.reveal(serviceCard);
    observer.reveal(categoryCard);
    expect(serviceCard.classList.contains('visible')).toBeTrue();
    expect(categoryCard.classList.contains('visible')).toBeTrue();
    expect(observer.observed.has(serviceCard)).toBeFalse();
    expect(observer.observed.has(categoryCard)).toBeFalse();

    fixture.destroy();
    expect(observer.disconnectCalls).toBe(1);
  });
});
