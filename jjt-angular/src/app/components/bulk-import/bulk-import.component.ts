import {
  Component,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MediaService } from '../../services/media.service';
import { BulkImportJobResponse } from '../../services/api.models';
import { Subscription, interval, switchMap, takeWhile } from 'rxjs';

type Phase = 'idle' | 'uploading' | 'polling' | 'done' | 'error';

@Component({
  selector: 'app-bulk-import',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div style="border:1px solid #e3dccd;border-radius:10px;padding:16px;background:#faf7f2;">
      <p style="font-size:11px;font-weight:700;color:#8a958d;text-transform:uppercase;
                letter-spacing:.06em;margin:0 0 10px;">Bulk Photo Import (ZIP)</p>
      <p style="font-size:12px;color:#5a6b62;margin:0 0 12px;line-height:1.5;">
        Upload a ZIP of child photos. Each filename stem must match a child's roll number
        (e.g. <code style="background:#ece4d6;padding:1px 4px;border-radius:3px;">A123.jpg</code>).
        Existing profile photos are replaced; duplicate images are detected and skipped.
      </p>

      @if (phase === 'idle' || phase === 'error') {
        <label style="display:inline-block;cursor:pointer;">
          <input type="file" accept=".zip" style="display:none;" (change)="onFile($event)" />
          <span style="display:inline-block;padding:7px 14px;border-radius:6px;
                       background:#2f5d4f;color:#fff;font-size:12.5px;font-weight:600;
                       cursor:pointer;">
            Choose ZIP…
          </span>
        </label>
        @if (phase === 'error') {
          <p style="color:#b84040;font-size:12px;margin:8px 0 0;">{{ errorMsg }}</p>
        }
      }

      @if (phase === 'uploading') {
        <p style="font-size:12.5px;color:#5a6b62;margin:0;">Uploading…</p>
      }

      @if (phase === 'polling' && job) {
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:10px;">
          <span style="font-size:12.5px;color:#5a6b62;">Processing ({{ job.status }})…</span>
          <span style="width:14px;height:14px;border:2px solid #2f5d4f;
                       border-top-color:transparent;border-radius:50%;
                       display:inline-block;animation:spin .7s linear infinite;"></span>
        </div>
        <div style="display:flex;gap:12px;flex-wrap:wrap;font-size:11.5px;color:#5a6b62;">
          <span>Total: <strong>{{ job.totalFiles }}</strong></span>
          <span>Matched: <strong>{{ job.matched }}</strong></span>
          <span>Uploaded: <strong style="color:#2f5d4f;">{{ job.uploaded }}</strong></span>
          <span>Skipped: <strong>{{ job.skipped }}</strong></span>
          @if (job.failed > 0) {
            <span>Failed: <strong style="color:#b84040;">{{ job.failed }}</strong></span>
          }
        </div>
      }

      @if (phase === 'done' && job) {
        <div style="background:#eef5f2;border:1px solid #c3d9cf;border-radius:8px;padding:12px;margin-bottom:10px;">
          <p style="font-size:12.5px;font-weight:600;color:#1c352c;margin:0 0 6px;">
            {{ job.status === 'COMPLETED' ? 'Import complete' : 'Import completed with errors' }}
          </p>
          <div style="display:flex;gap:16px;flex-wrap:wrap;font-size:12px;color:#3a5a4e;">
            <span>{{ job.totalFiles }} files processed</span>
            <span style="color:#2f5d4f;font-weight:600;">{{ job.uploaded }} uploaded</span>
            <span>{{ job.skipped }} skipped (duplicate)</span>
            @if (job.failed > 0) {
              <span style="color:#b84040;font-weight:600;">{{ job.failed }} failed</span>
            }
          </div>
        </div>

        @if (job.errors.length > 0) {
          <div style="overflow-x:auto;margin-bottom:10px;">
            <table style="width:100%;border-collapse:collapse;font-size:11.5px;">
              <thead>
                <tr style="background:#ece4d6;">
                  <th style="text-align:left;padding:5px 8px;font-weight:600;color:#1c352c;">Filename</th>
                  <th style="text-align:left;padding:5px 8px;font-weight:600;color:#1c352c;">Reason</th>
                </tr>
              </thead>
              <tbody>
                @for (err of job.errors; track err.filename) {
                  <tr style="border-top:1px solid #e3dccd;">
                    <td style="padding:4px 8px;color:#5a6b62;font-family:monospace;">{{ err.filename }}</td>
                    <td style="padding:4px 8px;color:#b84040;">{{ err.reason }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <button (click)="downloadErrors()"
                  style="font-size:11.5px;padding:5px 10px;border:1px solid #b84040;border-radius:5px;
                         background:transparent;color:#b84040;cursor:pointer;font-weight:600;">
            Download error CSV
          </button>
        }

        <button (click)="reset()"
                style="margin-left:8px;font-size:11.5px;padding:5px 10px;border:1px solid #2f5d4f;
                       border-radius:5px;background:transparent;color:#2f5d4f;cursor:pointer;font-weight:600;">
          Import another
        </button>
      }
    </div>

    <style>
      @keyframes spin { to { transform: rotate(360deg); } }
    </style>
  `,
})
export class BulkImportComponent implements OnDestroy {
  phase: Phase = 'idle';
  job: BulkImportJobResponse | null = null;
  errorMsg = '';

  private pollSub?: Subscription;

  constructor(private mediaService: MediaService, private cdr: ChangeDetectorRef) {}

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  onFile(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.phase = 'uploading';
    this.cdr.markForCheck();

    this.mediaService.startBulkImport(file).subscribe({
      next: job => {
        this.job = job;
        this.phase = 'polling';
        this.cdr.markForCheck();
        this.startPolling(job.id);
      },
      error: err => {
        this.phase = 'error';
        this.errorMsg = err?.error?.message ?? err?.message ?? 'Upload failed';
        this.cdr.markForCheck();
      },
    });
  }

  private startPolling(jobId: string): void {
    this.pollSub?.unsubscribe();
    this.pollSub = interval(2000)
      .pipe(
        switchMap(() => this.mediaService.getBulkImportJob(jobId)),
        takeWhile(
          j => j.status === 'QUEUED' || j.status === 'PROCESSING',
          true // emit the terminal value too
        )
      )
      .subscribe({
        next: j => {
          this.job = j;
          const done = j.status !== 'QUEUED' && j.status !== 'PROCESSING';
          if (done) {
            this.phase = 'done';
            this.pollSub?.unsubscribe();
          }
          this.cdr.markForCheck();
        },
        error: () => {
          this.phase = 'error';
          this.errorMsg = 'Lost connection while polling job status.';
          this.cdr.markForCheck();
        },
      });
  }

  downloadErrors(): void {
    if (!this.job?.errors.length) return;
    const lines = ['filename,reason', ...this.job.errors.map(e => `"${e.filename}","${e.reason}"`)];
    const blob = new Blob([lines.join('\n')], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `import-errors-${this.job.id.slice(0, 8)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  reset(): void {
    this.pollSub?.unsubscribe();
    this.phase = 'idle';
    this.job = null;
    this.errorMsg = '';
    this.cdr.markForCheck();
  }
}
