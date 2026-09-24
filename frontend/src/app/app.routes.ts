import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { QueryComponent } from './pages/query/query.component';
import { GalleryComponent } from './pages/gallery/gallery.component';
import { AdminLoginComponent } from './admin/admin-login.component';
import { AdminComponent } from './admin/admin.component';
import { adminGuard } from './core/services/admin.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'The Sneh Moments - Wedding Organizer' },
  { path: 'query', component: QueryComponent, title: 'Plan Your Event - The Sneh Moments' },
  { path: 'our-work/:slug', component: GalleryComponent, title: 'Our Work - The Sneh Moments' },
  { path: 'admin/login', component: AdminLoginComponent, title: 'Admin Login - The Sneh Moments' },
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard], title: 'Admin - The Sneh Moments' },
  { path: '**', redirectTo: '' }
];
