import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService, GalleryCategory, GalleryImage } from '../../core/services/api.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { FooterComponent } from '../../shared/footer/footer.component';
@Component({selector:'app-gallery',standalone:true,imports:[CommonModule,RouterLink,NavbarComponent,FooterComponent],templateUrl:'./gallery.component.html',styleUrl:'./gallery.component.css'})
export class GalleryComponent implements OnInit {
 category?: GalleryCategory; loading=true; error=false; active?:GalleryImage;
 constructor(private route:ActivatedRoute,public api:ApiService){}
 ngOnInit(){this.route.paramMap.subscribe(params=>{this.loading=true;this.error=false;this.category=undefined;this.api.getGalleryCategory(params.get('slug')||'').subscribe({next:value=>{this.category=value;this.loading=false;},error:()=>{this.loading=false;this.error=true;}});});}
}
