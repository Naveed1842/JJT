import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MediaService } from '../../services/media.service';
import { MediaFileResponse } from '../../services/api.models';

@Component({
  selector: 'app-media-image',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="media-image-wrapper" [style.width]="size" [style.height]="size"
         [style.border-radius]="shape === 'circle' ? '50%' : '8px'">
      @if (loading) {
        <div class="media-skeleton"></div>
      } @else if (src) {
        <img
          [src]="src"
          [alt]="alt"
          (error)="onError()"
          (load)="onLoad()"
          [style.opacity]="imageVisible ? '1' : '0'"
          class="media-img"
        />
      } @else {
        <div class="media-fallback" [attr.aria-label]="alt">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"/>
          </svg>
        </div>
      }
    </div>
  `,
  styles: [`
    .media-image-wrapper {
      position: relative;
      overflow: hidden;
      background: #f0ebe3;
      flex-shrink: 0;
    }
    .media-skeleton {
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, #f0ebe3 25%, #e8e0d5 50%, #f0ebe3 75%);
      background-size: 200% 100%;
      animation: shimmer 1.4s infinite;
    }
    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
    .media-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: opacity 0.2s ease;
    }
    .media-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #b0a090;
    }
    .media-fallback svg {
      width: 55%;
      height: 55%;
    }
  `],
})
export class MediaImageComponent implements OnChanges {
  @Input() mediaId: string | null = null;
  @Input() directUrl: string | null = null;
  @Input() alt = '';
  @Input() size = '48px';
  @Input() preferVariant = 'THUMBNAIL_MD';
  /** 'circle' applies border-radius:50%; 'rect' renders square/rectangular */
  @Input() shape: 'circle' | 'rect' = 'circle';

  src: string | null = null;
  loading = false;
  imageVisible = false;

  constructor(private mediaService: MediaService, private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['directUrl'] || changes['mediaId']) {
      this.resolve();
    }
  }

  private resolve(): void {
    if (this.directUrl) {
      this.src = this.directUrl;
      this.loading = false;
      return;
    }
    if (!this.mediaId) {
      this.src = null;
      this.loading = false;
      return;
    }
    this.loading = true;
    this.imageVisible = false;
    this.mediaService.getMedia(this.mediaId).subscribe({
      next: (media: MediaFileResponse) => {
        const preferred = media.variants.find(v => v.variantType === this.preferVariant);
        const fallback = media.variants[0];
        this.src = preferred?.url ?? fallback?.url ?? media.publicUrl ?? null;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.src = null;
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  onLoad(): void {
    this.imageVisible = true;
    this.cdr.markForCheck();
  }

  onError(): void {
    this.src = null;
    this.cdr.markForCheck();
  }
}
