import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest, HttpEventType } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { switchMap, catchError, filter } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  MediaUploadIntentRequest,
  MediaUploadIntentResponse,
  MediaFileResponse,
  MediaAttachmentResponse,
  AttachmentRole,
  BulkImportJobResponse,
} from './api.models';

export interface UploadProgress {
  state: 'uploading' | 'confirming' | 'done' | 'error';
  percent: number;
  result?: MediaFileResponse;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class MediaService {
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  uploadFile(
    file: File,
    ownerType: string,
    ownerId: string,
    role: AttachmentRole
  ): Observable<UploadProgress> {
    const intent: MediaUploadIntentRequest = {
      ownerType,
      ownerId,
      attachmentRole: role,
      originalName: file.name,
      mimeType: file.type || 'application/octet-stream',
      sizeBytes: file.size,
      visibility: 'PUBLIC',
      sortOrder: 0,
    };

    return this.http
      .post<MediaUploadIntentResponse>(`${this.base}/api/media/upload-intent`, intent)
      .pipe(
        switchMap(intentResp => this.putToStorage(intentResp, file)),
        catchError(err =>
          throwError(() => ({
            state: 'error' as const,
            percent: 0,
            error: err?.message ?? 'Upload failed',
          }))
        )
      );
  }

  private putToStorage(
    intent: MediaUploadIntentResponse,
    file: File
  ): Observable<UploadProgress> {
    const req = new HttpRequest('PUT', intent.uploadUrl, file, {
      headers: new HttpHeaders({ 'Content-Type': file.type || 'application/octet-stream' }),
      reportProgress: true,
    });

    return new Observable<UploadProgress>(observer => {
      this.http.request(req).pipe(
        filter(event =>
          event.type === HttpEventType.UploadProgress ||
          event.type === HttpEventType.Response
        )
      ).subscribe({
        next: event => {
          if (event.type === HttpEventType.UploadProgress) {
            const percent = event.total
              ? Math.round((100 * event.loaded) / event.total)
              : 0;
            observer.next({ state: 'uploading', percent });
          } else if (event.type === HttpEventType.Response) {
            observer.next({ state: 'confirming', percent: 100 });
            this.http
              .post<MediaFileResponse>(`${this.base}/api/media/${intent.mediaId}/confirm`, {})
              .subscribe({
                next: result => {
                  observer.next({ state: 'done', percent: 100, result });
                  observer.complete();
                },
                error: err => {
                  observer.next({ state: 'error', percent: 100, error: err?.message });
                  observer.complete();
                },
              });
          }
        },
        error: err => {
          observer.next({ state: 'error', percent: 0, error: err?.message });
          observer.complete();
        },
      });
    });
  }

  getAttachments(ownerType: string, ownerId: string, role?: AttachmentRole): Observable<MediaAttachmentResponse[]> {
    let url = `${this.base}/api/media/attachments?ownerType=${ownerType}&ownerId=${ownerId}`;
    if (role) url += `&role=${role}`;
    return this.http.get<MediaAttachmentResponse[]>(url);
  }

  getMedia(mediaId: string): Observable<MediaFileResponse> {
    return this.http.get<MediaFileResponse>(`${this.base}/api/media/${mediaId}`);
  }

  deleteAttachment(attachmentId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/media/attachments/${attachmentId}`);
  }

  startBulkImport(file: File): Observable<BulkImportJobResponse> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<BulkImportJobResponse>(`${this.base}/api/media/import`, form);
  }

  getBulkImportJob(jobId: string): Observable<BulkImportJobResponse> {
    return this.http.get<BulkImportJobResponse>(`${this.base}/api/media/import/${jobId}`);
  }
}
