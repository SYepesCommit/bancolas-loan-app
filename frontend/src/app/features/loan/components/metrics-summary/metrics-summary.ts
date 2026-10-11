import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-metrics-summary',
  standalone: true,
  imports: [CurrencyPipe, MatIconModule],
  templateUrl: './metrics-summary.html',
  styleUrl: './metrics-summary.scss'
})
export class MetricsSummaryComponent {
  approvedCount = input.required<number>();
  rejectedCount = input.required<number>();
  totalVolume = input.required<number>();
}