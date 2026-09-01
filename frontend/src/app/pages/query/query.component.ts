import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-query',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NavbarComponent],
  templateUrl: './query.component.html',
  styleUrl: './query.component.css'
})
export class QueryComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);

  submitting = false;
  submitError = '';
  showSuccess = false;

  form = this.fb.group({
    fullName: ['', Validators.required],
    phone: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    eventType: ['', Validators.required],
    eventDate: ['', Validators.required],
    cityVenue: ['', Validators.required],
    numberOfGuests: [null as number | null, [Validators.required, Validators.min(1)]],
    budget: [null as number | null, [Validators.required, Validators.min(1)]],
    message: ['', Validators.required]
  });

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.submitError = '';

    const value = this.form.value;

    this.api
      .submitQuery({
        fullName: value.fullName!,
        phone: value.phone!,
        email: value.email!,
        eventType: value.eventType!,
        eventDate: value.eventDate!,
        cityVenue: value.cityVenue!,
        numberOfGuests: value.numberOfGuests!,
        budget: value.budget!,
        message: value.message!
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.showSuccess = true;
        },
        error: () => {
          this.submitting = false;
          this.submitError = 'Something went wrong submitting your query. Please try again in a moment.';
        }
      });
  }

  closeSuccess(): void {
    this.showSuccess = false;
    this.router.navigate(['/']);
  }
}
