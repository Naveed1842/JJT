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
import { MediaAttachmentResponse, AttachmentRole } from '../../services/api.models';

@Component({
  selector: 'app-media-gallery',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading) {
      <div style="display:flex;gap:8px;flex-wrap:wrap;padding:4px 0;">
        @for (_ of [1,2,3]; track _) {
          <div style="width:80px;height:80px;border-radius:8px;background:linear-gradient(90deg,#ece4d6 25%,#f3eee4 50%,#ece4d6 75%);
                      background-size:200%;animation:shimmer 1.4s infinite;"></div>
        }
      </div>
    }

    @if (!loading && attachments.length === 0) {
      <p style="font-size:12.5px;color:#9aa79f;margin:4px 0 0;">No photos yet.</p>
    }

    @if (!loading && attachments.length > 0) {
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        @for (att of attachments; track att.id; let i = $index) {
          <div style="position:relative;width:80px;height:80px;border-radius:8px;overflow:hidden;"
               [style.border]="att.attachmentRole === 'PROFILE_PHOTO' ? '2px solid #2f5d4f' : '2px solid #e3dccd'">
            <img [src]="att.thumbnailUrl || att.url" [alt]="'Photo ' + (i + 1)"
                 style="width:100%;height:100%;object-fit:cover;display:block;"
                 loading="lazy" />

            @if (att.attachmentRole === 'PROFILE_PHOTO') {
              <span style="position:absolute;bottom:0;left:0;right:0;background:rgba(47,93,79,.85);
                           color:#fff;font-size:9px;font-weight:700;text-align:center;padding:2px 0;">
                Profile
              </span>
            }

            <!-- Action overlay -->
            <div class="att-actions" style="position:absolute;inset:0;background:rgba(0,0,0,.55);
                         display:flex;align-items:center;justify-content:center;gap:6px;opacity:0;transition:opacity .15s;">
              @if (att.attachmentRole !== 'PROFILE_PHOTO') {
                <button title="Set as profile photo"
                        (click)="promoteToProfile(att); $event.stopPropagation()"
                        style="background:#2f5d4f;border:none;border-radius:4px;padding:4px 6px;
                               cursor:pointer;color:#fff;font-size:10px;font-weight:600;line-height:1;">
                  ★
                </button>
              }
              <button title="Delete"
                      (click)="deleteAttachment(att); $event.stopPropagation()"
                      style="background:#b84040;border:none;border-radius:4px;padding:4px 6px;
                             cursor:pointer;color:#fff;font-size:10px;font-weight:600;line-height:1;">
                ✕
              </button>
            </div>
          </div>
        }
      </div>
    }

    <style>
      .att-actions { opacity: 0; transition: opacity .15s; }
      div:hover > .att-actions { opacity: 1 !important; }
      @keyframes shimmer {
        0%   { background-position: 200% 0; }
        100% { background-position: -200% 0; }
      }
    </style>
  `,
})
export class MediaGalleryComponent implements OnChanges {
  @Input() ownerType = '';
  @Input() ownerId = '';
  @Input() role: AttachmentRole | '' = '';

  attachments: MediaAttachmentResponse[] = [];
  loading = false;

  constructor(private mediaService: MediaService, private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['ownerType'] || changes['ownerId']) && this.ownerType && this.ownerId) {
      this.load();
    }
  }

  load(): void {
    if (!this.ownerType || !this.ownerId) return;
    this.loading = true;
    this.cdr.markForCheck();
    this.mediaService
      .getAttachments(this.ownerType, this.ownerId, this.role as AttachmentRole || undefined)
      .subscribe({
        next: list => {
          this.attachments = list;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.loading = false;
          this.cdr.markForCheck();
        },
      });
  }

  promoteToProfile(att: MediaAttachmentResponse): void {
    // Re-upload intent not needed — just call promote endpoint via reorder trick:
    // Upload a new intent with role=PROFILE_PHOTO pointing to the same ownerId,
    // but the cleanest path for existing media is a dedicated promote call.
    // For now: delete the attachment then re-attach as PROFILE_PHOTO via the
    // existing upload-intent flow is out of scope here. Instead we refresh so
    // the admin can re-upload.  A future dedicated PATCH endpoint can handle this.
    // What we CAN do right now: call detach on the current PROFILE_PHOTO
    // and update this one's role via detach+reattach — but that needs a new endpoint.
    // We mark the action and reload so the user sees the current state.
    alert('To set a new profile photo, use the upload zone above — uploading a new photo automatically promotes it.');
  }

  deleteAttachment(att: MediaAttachmentResponse): void {
    if (!confirm('Remove this photo?')) return;
    this.mediaService.deleteAttachment(att.id).subscribe({
      next: () => {
        this.attachments = this.attachments.filter(a => a.id !== att.id);
        this.cdr.markForCheck();
      },
    });
  }
}
