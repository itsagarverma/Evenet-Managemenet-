import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { QueryComponent } from './pages/query/query.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'The Sneh Moments - Wedding Organizer' },
  { path: 'query', component: QueryComponent, title: 'Plan Your Event - The Sneh Moments' },
  { path: '**', redirectTo: '' }
];
