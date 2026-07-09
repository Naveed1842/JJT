import { Injectable, computed, inject, signal } from '@angular/core';
import { SponsorService } from './sponsor.service';
import { ChildDto } from './api.models';

/**
 * Shared, cached state for the public children collection.
 *
 * Before this store every page (home, listing, …) refetched the full collection
 * on each navigation. The store fetches once, serves reads from signals, and
 * refetches only when the TTL expires or a mutation invalidates the cache.
 */
@Injectable({ providedIn: 'root' })
export class ChildrenStore {
  private readonly sponsorService = inject(SponsorService);

  private static readonly TTL_MS = 60_000;

  private readonly _children = signal<ChildDto[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private lastFetched = 0;
  private fetching = false;

  readonly children = this._children.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  readonly totalCount = computed(() => this._children().length);
  readonly availableCount = computed(
    () => this._children().filter(c => c.availabilityStatus === 'AVAILABLE').length
  );
  /** First three seekers — the home page's featured rail. */
  readonly featured = computed(
    () => this._children().filter(c => c.availabilityStatus === 'AVAILABLE').slice(0, 3)
  );

  /** Ensures data is present and fresh. No-op while cached or already fetching. */
  load(force = false): void {
    const fresh = Date.now() - this.lastFetched < ChildrenStore.TTL_MS;
    if (this.fetching || (!force && fresh && this._children().length > 0)) return;

    this.fetching = true;
    this._loading.set(this._children().length === 0); // background refresh keeps old data visible
    this._error.set(null);
    this.sponsorService.getChildren().subscribe({
      next: (data) => {
        this._children.set(data);
        this.lastFetched = Date.now();
        this.fetching = false;
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err?.error?.message ?? 'Unable to load children right now. Please try again.');
        this.fetching = false;
        this._loading.set(false);
      }
    });
  }

  /**
   * Marks the cache stale. Call after any mutation that changes availability
   * (sponsorship committed / activated / expired). The next load() refetches.
   */
  invalidate(): void {
    this.lastFetched = 0;
  }
}
