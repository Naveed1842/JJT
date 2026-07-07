import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MediaService, UploadProgress } from '../../services/media.service';
import { MediaFileResponse, AttachmentRole } from '../../services/api.models';

const ACCEPTED_MIME = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
const MAX_BYTES = 10 * 1024 * 1024; // 10 MB

@Component({
  selector: 'app-media-upload',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="upload-zone"
      [class.drag-over]="dragOver"
      [class.uploading]="progress !== null && progress.state !== 'done'"
      [class.compact]="compact"
      (dragover)="onDragOver($event)"
      (dragleave)="onDragLeave()"
      (drop)="onDrop($event)"
      (click)="fileInput.click()"
    >
      <input
        #fileInput
        type="file"
        [accept]="acceptAttr"
        style="display:none"
        (change)="onFileSelected($event)"
      />

      @if (progress === null) {
        <div class="upload-idle">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5m-13.5-9L12 3m0 0 4.5 4.5M12 3v13.5"/>
          </svg>
          <span class="upload-label">{{ label }}</span>
          <span class="upload-hint">JPG, PNG, WebP · max 10 MB</span>
        </div>
      } @else if (progress.state === 'uploading' || progress.state === 'confirming') {
        <div class="upload-progress">
          <div class="progress-bar">
            <div class="progress-fill" [style.width.%]="progress.percent"></div>
          </div>
          <span class="progress-label">
            {{ progress.state === 'confirming' ? 'Processing…' : progress.percent + '%' }}
          </span>
        </div>
      } @else if (progress.state === 'done') {
        <div class="upload-done">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="done-icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/>
          </svg>
          <span>Uploaded</span>
        </div>
      } @else if (progress.state === 'error') {
        <div class="upload-error">
          <span>{{ progress.error ?? 'Upload failed — try again' }}</span>
        </div>
      }
    </div>

    @if (validationError) {
      <p class="upload-validation-error">{{ validationError }}</p>
    }
  `,
  styles: [`
    .upload-zone {
      border: 2px dashed #c8bfb0;
      border-radius: 8px;
      padding: 24px 16px;
      text-align: center;
      cursor: pointer;
      transition: border-color 0.15s, background 0.15s;
      background: #faf7f2;
      user-select: none;
    }
    .upload-zone.compact { padding: 8px 10px; }
    .upload-zone.compact .upload-icon { width: 16px; height: 16px; margin-bottom: 2px; }
    .upload-zone.compact .upload-label { font-size: 11px; }
    .upload-zone.compact .upload-hint { display: none; }
    .upload-zone:hover, .upload-zone.drag-over {
      border-color: #8b6f47;
      background: #f5ede0;
    }
    .upload-zone.uploading { cursor: default; }
    .upload-icon { width: 32px; height: 32px; color: #8b6f47; margin-bottom: 8px; }
    .upload-idle { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .upload-label { font-size: 14px; font-weight: 600; color: #3d2b1f; }
    .upload-hint { font-size: 12px; color: #7a6a5a; }
    .upload-progress { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 8px 0; }
    .progress-bar { width: 100%; height: 6px; background: #e0d5c8; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: #8b6f47; border-radius: 3px; transition: width 0.1s linear; }
    .progress-label { font-size: 12px; color: #5a4a3a; }
    .upload-done { display: flex; align-items: center; gap: 8px; justify-content: center; color: #4a7a4a; font-size: 14px; font-weight: 600; }
    .done-icon { width: 20px; height: 20px; }
    .upload-error { color: #b84040; font-size: 13px; }
    .upload-validation-error { margin: 4px 0 0; font-size: 12px; color: #b84040; }
  `],
})
export class MediaUploadComponent {
  @Input() ownerType = '';
  @Input() ownerId = '';
  @Input() role: AttachmentRole = 'GALLERY';
  @Input() label = 'Click or drag to upload photo';
  @Input() compact = false;
  @Output() uploaded = new EventEmitter<MediaFileResponse>();

  dragOver = false;
  progress: UploadProgress | null = null;
  validationError: string | null = null;

  readonly acceptAttr = ACCEPTED_MIME.join(',');

  constructor(private mediaService: MediaService, private cdr: ChangeDetectorRef) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = true;
  }

  onDragLeave(): void {
    this.dragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) this.upload(file);
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.upload(file);
    (event.target as HTMLInputElement).value = '';
  }

  private upload(file: File): void {
    this.validationError = null;

    if (!ACCEPTED_MIME.includes(file.type)) {
      this.validationError = 'Only JPG, PNG, WebP, and GIF images are accepted.';
      return;
    }
    if (file.size > MAX_BYTES) {
      this.validationError = 'File is too large — maximum size is 10 MB.';
      return;
    }

    this.progress = { state: 'uploading', percent: 0 };
    this.cdr.markForCheck();

    this.mediaService.uploadFile(file, this.ownerType, this.ownerId, this.role).subscribe({
      next: prog => {
        this.progress = prog;
        if (prog.state === 'done' && prog.result) {
          this.uploaded.emit(prog.result);
        }
        this.cdr.markForCheck();
      },
      error: err => {
        this.progress = { state: 'error', percent: 0, error: err?.error ?? 'Upload failed' };
        this.cdr.markForCheck();
      },
    });
  }

  reset(): void {
    this.progress = null;
    this.validationError = null;
    this.cdr.markForCheck();
  }
}
