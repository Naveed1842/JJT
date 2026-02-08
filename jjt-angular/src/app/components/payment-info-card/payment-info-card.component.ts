import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { PAYMENT_INFO } from '../../shared/config/payment-info';

export interface PaymentInfo {
  accountTitle: string;
  accountNumber: string;
  iban: string;
  bankName: string;
  phone: string[];
  email: string;
  address: string;
}

@Component({
  selector: 'app-payment-info-card',
  standalone: true,
  imports: [CommonModule, CardModule, DividerModule, ButtonModule, TagModule],
  templateUrl: './payment-info-card.component.html',
  styleUrl: './payment-info-card.component.css'
})
export class PaymentInfoCardComponent {
  @Input() childName?: string;
  @Input() startMonth?: string | null;
  info: PaymentInfo = PAYMENT_INFO;
  copiedField: string | null = null;

  async copy(value: string, field: string) {
    try {
      await navigator.clipboard.writeText(value);
      this.copiedField = field;
      setTimeout(() => (this.copiedField = null), 2000);
    } catch (err) {
      console.error('Copy failed', err);
    }
  }
}
