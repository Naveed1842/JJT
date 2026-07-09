# Roadmap 2.5 — Media Management Platform
## Architecture Document

> **Status:** DESIGN — awaiting approval before implementation begins
> **Supersedes:** `attachments` table designed in Roadmap 5 (V30 migration)
> **Inserts before:** Roadmap 5 implementation

---

## Relation to Roadmap 5

Roadmap 5 (Architecture Freeze, Section 8) included a simple `attachments` table in migration V30. The Media Platform is a richer successor that replaces it. When Roadmap 5 implementation begins, **V30 must skip the `attachments` table** — the Media Platform's migrations provide the equivalent in a superior form. All references in Roadmap 5 to `attachments` map to `media_attachments` in this platform.

---

# SECTION 1 — STORAGE PROVIDER ANALYSIS

## 1.1 Comparison Matrix

| Dimension | AWS S3 + CloudFront | Firebase Storage | Azure Blob Storage | Local (Dev only) |
|---|---|---|---|---|
| **Cost (storage)** | $0.023/GB/mo | $0.026/GB/mo | $0.018/GB/mo | Server disk |
| **Cost (egress)** | $0.09/GB (CloudFront $0.0085) | $0.12/GB | $0.087/GB | Free |
| **Scalability** | Unlimited, proven at exabyte scale | Unlimited (Google infra) | Unlimited | Disk-limited |
| **CDN** | CloudFront — 450+ PoPs, best-in-class | Firebase CDN — ~130 PoPs | Azure CDN — 130+ PoPs | None |
| **Image optimization** | S3 Object Lambda / CloudFront Functions | Cloud Functions only | Azure Image Optimization | Manual only |
| **Pre-signed uploads** | Native (PUT URL, 15min–7d expiry) | Native (resumable, Firebase SDK) | Native (SAS tokens) | N/A |
| **AI integration** | Rekognition, Textract, Comprehend | Vision AI, Document AI | Azure Vision, Form Recognizer | None |
| **Vendor lock-in** | Low — S3 API is the open standard (MinIO, Wasabi, R2 all compatible) | High — Firebase SDK required on client | Medium | None |
| **Spring/Java SDK** | Spring Cloud AWS — first-class, well-documented | Firebase Admin SDK — adequate | Azure SDK for Java — adequate | Java `java.nio.file` |
| **Security** | IAM policies, bucket policies, ACLs, signed URLs, SSE-S3/KMS | Firebase Security Rules, signed URLs, Firebase Auth integration | RBAC, SAS tokens, encryption at rest | Filesystem permissions |
| **Malware scanning** | S3 Event → Lambda → ClamAV or third-party | Storage trigger → Cloud Function | Event Grid → Azure Function | Manual |
| **Resumable uploads** | Multipart upload (built-in for >5MB) | Resumable upload API (built-in) | Block blob (built-in) | N/A |
| **SaaS readiness** | Industry standard, used by most SaaS | Tied to Firebase project (one per app) | Strong enterprise, less startup-friendly | No |
| **Dev/local parity** | MinIO as local S3 emulator (Docker, identical API) | Firebase Emulator Suite | Azurite | Filesystem |
| **Current JJT stack fit** | New dependency | Already using Firebase Hosting | New dependency | Trivial to start |
| **Pricing model** | Pay as you go | Blaze plan (pay as you go) | Pay as you go | Free |

## 1.2 Cost Projection (JJT scale: ~500 children, ~5GB media/year growth)

| Provider | Year 1 (~5GB) | Year 3 (~20GB) | Year 5 (~50GB) |
|---|---|---|---|
| AWS S3 + CloudFront | ~$2/mo | ~$5/mo | ~$8/mo |
| Firebase Storage | ~$2/mo | ~$5/mo | ~$9/mo |
| Azure Blob | ~$1.50/mo | ~$4/mo | ~$7/mo |

At JJT's scale, storage cost is not a meaningful differentiator. The decision is architectural.

## 1.3 Final Recommendation: AWS S3 + CloudFront

**Rationale:**

1. **S3 is the lingua franca of object storage.** The S3 API is an open standard — MinIO, Cloudflare R2, Wasabi, Backblaze B2, and DigitalOcean Spaces all implement it. Switching providers in the future requires only a configuration change, not a code change.

2. **Local development parity via MinIO.** A single `docker-compose` entry gives a local S3-identical environment. Firebase Emulator is heavier and Firebase-specific. MinIO means every developer runs the same storage interface in dev and prod.

3. **CloudFront is the best CDN in the comparison.** 450+ edge locations, Lambda@Edge for on-the-fly image resizing, sub-50ms TTFB globally — critical for a platform delivering child photos to international sponsors.

4. **Rekognition covers the AI roadmap.** Face detection, image moderation, OCR (Textract), and label detection are all native AWS services callable from the same IAM context.

5. **No client-side SDK dependency.** The pre-signed URL pattern means the Angular app makes a plain HTTP PUT — no Firebase Storage SDK, no AWS SDK in the browser bundle.

6. **Firebase lock-in is already a risk to manage, not increase.** JJT uses Firebase Hosting. Extending Firebase to storage deepens that dependency. Keeping storage on S3 provides a hedge.

**Secondary recommendation for teams wanting single-vendor Google:** Firebase Storage with Cloud Functions for image processing. Functionally complete but with higher vendor lock-in and weaker image optimization story.

---

# SECTION 2 — STORAGE ABSTRACTION LAYER

## 2.1 Design Principle

The backend must never contain a direct call to `AmazonS3`, `FirebaseStorage`, or `BlobServiceClient`. All storage I/O flows through a single interface. Swapping providers = swapping one Spring `@Bean` in configuration.

## 2.2 Interface Contract

```
StorageProvider (interface)
│
├── upload(StorageUploadRequest) → StorageUploadResult
│     Takes: path hint, content, MIME type, visibility, size
│     Returns: storageRef (opaque key), provider enum, public URL (if public)
│
├── generatePresignedPutUrl(StoragePutRequest) → PresignedPutUrl
│     Takes: path hint, MIME type, max size, expiry duration
│     Returns: uploadUrl (client uploads here), storageRef, expires
│
├── generatePresignedGetUrl(storageRef, expiry) → String
│     Returns: time-limited download URL
│
├── getPublicUrl(storageRef) → String
│     Returns: CDN URL for public assets
│
├── delete(storageRef) → void
│
├── exists(storageRef) → boolean
│
└── copy(sourceRef, destRef) → StorageCopyResult
```

## 2.3 Provider Implementations

```
StorageProvider
    ├── S3StorageProvider          @ConditionalOnProperty(media.storage.provider=s3)
    │     - AmazonS3 client (Spring Cloud AWS)
    │     - CloudFront URL signing for private assets
    │     - Multipart upload for files > 5MB
    │
    ├── FirebaseStorageProvider    @ConditionalOnProperty(media.storage.provider=firebase)
    │     - FirebaseApp Admin SDK
    │     - Google Cloud CDN URLs
    │
    ├── AzureBlobStorageProvider   @ConditionalOnProperty(media.storage.provider=azure)
    │     - BlobServiceClient
    │     - Azure CDN endpoint
    │
    └── LocalStorageProvider       @ConditionalOnProperty(media.storage.provider=local)
          - java.nio.file, serves via /api/media/files/{ref}
          - Dev and test only; not deployed to production
```

## 2.4 Storage Path Convention

All providers use the same path structure. The `storageRef` is the portable key stored in the database. Provider base URLs are configuration — they never enter the `storageRef`.

```
Pattern:  {org_id}/{entity_type}/{entity_id}/{media_id}/{variant_type}.{ext}

Examples:
  org-uuid/children/child-uuid/media-uuid/original.jpg
  org-uuid/children/child-uuid/media-uuid/thumbnail_sm.webp
  org-uuid/expenses/expense-uuid/media-uuid/original.pdf
  org-uuid/campaigns/campaign-uuid/media-uuid/banner.webp
  org-uuid/users/user-uuid/media-uuid/avatar.webp
```

## 2.5 Configuration

```yaml
# application.yml
media:
  storage:
    provider: s3          # s3 | firebase | azure | local
    bucket: jjt-media-prod
    cdn-base-url: https://cdn.sponsorone.app
    region: eu-west-2
    presigned-put-expiry: PT15M     # ISO duration
    presigned-get-expiry: PT1H
    max-upload-size-bytes: 52428800  # 50MB
  processing:
    thumbnail-sizes: [100, 300, 600]   # px, square crop
    responsive-widths: [320, 640, 1024, 1920]
    webp-quality: 82
    jpeg-quality: 85
    min-tx-threshold: 10
  security:
    malware-scan-enabled: false   # set true in prod; requires scan hook
    allowed-mime-types:
      - image/jpeg
      - image/png
      - image/webp
      - image/gif
      - application/pdf
      - video/mp4
      - audio/mpeg
      - application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

---

# SECTION 3 — DATABASE SCHEMA

```sql
-- Core media registry
CREATE TABLE media_files (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID NOT NULL REFERENCES organisations(id),
    storage_ref       VARCHAR(500) NOT NULL,
    storage_provider  VARCHAR(20) NOT NULL,  -- S3, FIREBASE, AZURE, LOCAL
    original_name     VARCHAR(255) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    media_type        VARCHAR(20) NOT NULL,  -- IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER
    size_bytes        BIGINT NOT NULL,
    width_px          INTEGER,               -- images/video only
    height_px         INTEGER,               -- images/video only
    duration_secs     INTEGER,               -- video/audio only
    visibility        VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',  -- PUBLIC, PRIVATE, SIGNED
    status            VARCHAR(20) NOT NULL DEFAULT 'UPLOADING',
                      -- UPLOADING, PROCESSING, READY, FAILED, DELETED, QUARANTINED
    content_hash      VARCHAR(64),           -- SHA-256; populated post-upload
    tags              TEXT[] DEFAULT '{}',
    alt_text          VARCHAR(500),
    metadata          JSONB DEFAULT '{}',    -- EXIF, AI results, custom
    uploaded_by       UUID NOT NULL REFERENCES users(id),
    uploaded_at       TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_media_files_org ON media_files(org_id);
CREATE INDEX idx_media_files_status ON media_files(org_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_media_files_type ON media_files(org_id, media_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_media_files_hash ON media_files(org_id, content_hash) WHERE content_hash IS NOT NULL;
CREATE INDEX idx_media_files_tags ON media_files USING GIN(tags);
CREATE INDEX idx_media_files_metadata ON media_files USING GIN(metadata);

-- Processed variants (thumbnails, WebP, responsive sizes)
CREATE TABLE media_variants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id        UUID NOT NULL REFERENCES media_files(id) ON DELETE CASCADE,
    variant_type    VARCHAR(30) NOT NULL,
                    -- ORIGINAL, OPTIMIZED, THUMBNAIL_SM, THUMBNAIL_MD, THUMBNAIL_LG,
                    -- W320, W640, W1024, W1920, WEBP, COMPRESSED, PREVIEW
    storage_ref     VARCHAR(500) NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    width_px        INTEGER,
    height_px       INTEGER,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (media_id, variant_type)
);

CREATE INDEX idx_media_variants_media ON media_variants(media_id);

-- Polymorphic attachment join — replaces Roadmap 5 'attachments' table
CREATE TABLE media_attachments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id        UUID NOT NULL REFERENCES media_files(id),
    owner_type      VARCHAR(50) NOT NULL,   -- CHILD, SPONSOR, CAMPAIGN, EXPENSE,
                                            -- VENDOR, USER, ORGANISATION, MISSION,
                                            -- PROGRESS_UPDATE, PAYROLL_RUN
    owner_id        UUID NOT NULL,
    attachment_role VARCHAR(50) NOT NULL,   -- PROFILE_PHOTO, GALLERY, BANNER, INVOICE,
                                            -- RECEIPT, CONTRACT, AVATAR, DOCUMENT,
                                            -- BEFORE_PHOTO, AFTER_PHOTO, TIMELINE
    sort_order      INTEGER NOT NULL DEFAULT 0,
    attached_by     UUID NOT NULL REFERENCES users(id),
    attached_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (media_id, owner_type, owner_id, attachment_role)
);

CREATE INDEX idx_media_att_owner ON media_attachments(owner_type, owner_id);
CREATE INDEX idx_media_att_role ON media_attachments(owner_type, owner_id, attachment_role);
CREATE INDEX idx_media_att_media ON media_attachments(media_id);

-- Bulk import jobs
CREATE TABLE media_import_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL REFERENCES organisations(id),
    job_type        VARCHAR(30) NOT NULL DEFAULT 'BULK_CHILD_PHOTOS',
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
                    -- QUEUED, PARSING, MATCHING, UPLOADING, COMPLETE, FAILED, PARTIAL
    manifest_ref    VARCHAR(500),    -- storage ref for the Excel manifest file
    archive_ref     VARCHAR(500),    -- storage ref for the ZIP archive
    total_files     INTEGER DEFAULT 0,
    matched         INTEGER DEFAULT 0,
    uploaded        INTEGER DEFAULT 0,
    skipped         INTEGER DEFAULT 0,
    failed          INTEGER DEFAULT 0,
    error_report    JSONB DEFAULT '[]',  -- [{filename, reason, suggestion}]
    created_by      UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    completed_at    TIMESTAMP
);

CREATE INDEX idx_import_jobs_org ON media_import_jobs(org_id, status);

-- Import job item-level log (one row per file in the archive)
CREATE TABLE media_import_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID NOT NULL REFERENCES media_import_jobs(id) ON DELETE CASCADE,
    source_filename VARCHAR(255) NOT NULL,
    matched_entity_type VARCHAR(50),  -- CHILD
    matched_entity_id   UUID,
    match_key       VARCHAR(100),     -- roll_number, child_id, etc.
    status          VARCHAR(20) NOT NULL,  -- MATCHED, UNMATCHED, DUPLICATE, FAILED, SKIPPED
    media_id        UUID REFERENCES media_files(id),
    error_detail    TEXT
);

CREATE INDEX idx_import_items_job ON media_import_items(job_id, status);
```

---

# SECTION 4 — MEDIA ENTITY DESIGN

## 4.1 Media Types and Roles

```
MEDIA_TYPE    │ ATTACHMENT_ROLE examples
──────────────┼──────────────────────────────────────────────────────
IMAGE         │ PROFILE_PHOTO, GALLERY, BANNER, AVATAR, BEFORE_PHOTO,
              │ AFTER_PHOTO, TIMELINE, THUMBNAIL, COVER
DOCUMENT      │ INVOICE, RECEIPT, CONTRACT, REPORT, CERTIFICATE,
              │ ID_DOCUMENT, POLICY, BANK_STATEMENT
VIDEO         │ TESTIMONIAL, PROGRAMME_VIDEO, GALLERY, TIMELINE
AUDIO         │ TESTIMONIAL, INTERVIEW
OTHER         │ (future: AI-generated assets, 3D models)
```

## 4.2 Child-Specific Roles

```
Child
 ├── PROFILE_PHOTO     (1 per child — the primary card image)
 ├── GALLERY           (N — additional photos, ordered by sort_order)
 ├── BEFORE_PHOTO      (N — paired with AFTER_PHOTO for impact stories)
 ├── AFTER_PHOTO       (N)
 └── TIMELINE          (N — dated progression photos)

Selection rule: PROFILE_PHOTO is the single row where
  owner_type='CHILD' AND attachment_role='PROFILE_PHOTO'.
  If multiple exist (migration edge case), sort_order=0 wins.
  If none exist, the UI renders the default placeholder SVG.
```

## 4.3 Visibility Model

| Visibility | Storage | URL type | Who can access |
|---|---|---|---|
| `PUBLIC` | S3 public ACL / CloudFront no-auth | Permanent CDN URL | Anyone — cached at edge |
| `PRIVATE` | S3 private object | Pre-signed GET URL, 1hr expiry | Authenticated users with role permission |
| `SIGNED` | S3 private object | Pre-signed GET URL, configurable expiry | Any bearer of the URL (for email links, sponsor portals) |

Rule: Child profile photos and gallery images are `PUBLIC` by default (they appear on the public /children page). Documents (invoices, receipts, contracts) are always `PRIVATE`. Sponsor documents are `SIGNED` (emailed link).

## 4.4 Variant Generation Matrix

| Input MIME type | Variants generated |
|---|---|
| image/jpeg, image/png, image/webp | THUMBNAIL_SM (100px), THUMBNAIL_MD (300px), THUMBNAIL_LG (600px), W640, W1024, WEBP (all widths as WebP) |
| image/gif | THUMBNAIL_SM, THUMBNAIL_MD only (no WebP to preserve animation) |
| application/pdf | PREVIEW (first-page rasterised to PNG, then thumbnailed) |
| video/mp4 | THUMBNAIL_SM (poster frame at 1s), PREVIEW (poster frame at 1s) |
| audio/mpeg | None |
| Other | None |

Processing is **asynchronous**. The `media_files.status` column tracks state. The original always uploads first; variants follow. The UI renders the original while variants are being processed, then switches to the optimized variant once `READY`.

---

# SECTION 5 — UPLOAD WORKFLOW

## 5.1 Standard Upload (single file, any entity)

```
Browser / Admin UI                  Backend (Spring Boot)            AWS S3
      │                                      │                          │
      │─ POST /api/media/upload-intent ──────►│                          │
      │  {ownerType, ownerId, role,           │                          │
      │   mimeType, sizeBytes, filename}      │                          │
      │                                      │─ validatePermission()    │
      │                                      │─ validateMimeType()      │
      │                                      │─ validateSize()          │
      │                                      │─ generateStorageRef()    │
      │                                      │─ S3.generatePresignedPut()──►
      │                                      │◄─ presignedPutUrl ───────────
      │                                      │─ INSERT media_files (UPLOADING)
      │◄─ {mediaId, uploadUrl, storageRef} ───│                          │
      │                                      │                          │
      │─ PUT {uploadUrl} ───────────────────────────────────────────────►│
      │  Content-Type: image/jpeg            │                          │
      │  (raw file bytes — never touches backend)                        │
      │◄─ 200 ────────────────────────────────────────────────────────────
      │                                      │                          │
      │─ POST /api/media/{mediaId}/confirm ──►│                          │
      │  {contentHash?}                      │─ S3.headObject() → size  │
      │                                      │─ UPDATE media_files      │
      │                                      │  status=PROCESSING        │
      │                                      │─ publishToProcessingQueue()
      │◄─ {mediaId, status: PROCESSING} ──────│                          │
      │                                      │                          │
      │         [async — processing worker]  │                          │
      │                                      │─ generateThumbnails()    │
      │                                      │─ convertToWebP()         │
      │                                      │─ S3.putObject (variants) ───►
      │                                      │─ INSERT media_variants   │
      │                                      │─ UPDATE media_files      │
      │                                      │  status=READY             │
      │                                      │                          │
      │─ GET /api/media/{mediaId} ───────────►│                          │
      │◄─ {status: READY, variants: [...]} ───│                          │
```

**Key design decisions:**
- The file bytes never pass through the Java backend. The backend issues a presigned URL; the browser uploads directly to S3. This eliminates the Heroku 30-second request timeout constraint and reduces backend memory pressure.
- `confirm` step verifies the upload actually landed (via S3 HeadObject) before marking the record active.
- Async processing prevents the confirm step from timing out on large files or slow variant generation.

## 5.2 Upload Intent Response DTO

```json
{
  "mediaId": "uuid",
  "uploadUrl": "https://jjt-media-prod.s3.eu-west-2.amazonaws.com/...",
  "storageRef": "org-uuid/children/child-uuid/media-uuid/original.jpg",
  "expiresAt": "2026-07-07T10:15:00Z",
  "maxBytes": 52428800
}
```

## 5.3 Avatar / Profile Photo Replace Flow

When a new PROFILE_PHOTO is uploaded for a child:
1. Issue upload intent as normal
2. On confirm, set new `media_files.status = READY`
3. In the same transaction: update `media_attachments` — the old PROFILE_PHOTO row gets `attachment_role = 'GALLERY'` and `sort_order = 999` (it becomes a gallery image, not deleted — photo history is preserved)
4. Insert new `media_attachments` row with `role = PROFILE_PHOTO, sort_order = 0`
5. Schedule old original for soft-deletion after 90 days if the org has no retention policy requiring it

## 5.4 Large File Upload (>5MB via Multipart)

For files exceeding 5MB (PDFs, videos), the backend initiates an S3 multipart upload and returns part-specific presigned URLs. The client uploads parts in parallel, then calls `complete-multipart`. The backend completes the multipart upload on S3 and proceeds with the confirm flow. This is abstracted behind `StorageProvider.generatePresignedPutUrl()` — the S3 implementation selects single PUT vs multipart based on size.

---

# SECTION 6 — BULK IMPORT WORKFLOW

## 6.1 Use Case

An admin has a ZIP archive of child photos (filenames like `ROLL-001234.jpg`, `ROLL-001235_before.jpg`) and an Excel manifest (`import.xlsx`) mapping roll numbers to child IDs and optional photo roles.

## 6.2 Matching Rules (configurable per import job)

```
Rule priority (first match wins):
  1. Exact match on roll_number embedded in filename  (e.g. ROLL-001234.jpg)
  2. Exact match on child_id embedded in filename     (e.g. child_2f3a...jpg)
  3. Excel manifest lookup: filename column → child_id / roll_number column
  4. UNMATCHED — reported in error_report, no upload
```

Role detection from filename suffixes (configurable):
```
_before  → BEFORE_PHOTO
_after   → AFTER_PHOTO
_profile → PROFILE_PHOTO (default if no suffix and child has no existing profile photo)
_gallery or no suffix → GALLERY
```

## 6.3 Bulk Import Flow

```
Admin Browser              Backend                      S3 / DB
    │                         │                             │
    │─ POST /api/media/import ►│                            │
    │  multipart:              │                            │
    │   manifest: import.xlsx  │                            │
    │   archive: photos.zip    │                            │
    │                         │─ validateFileTypes()        │
    │                         │─ store manifest → S3 ───────►
    │                         │─ store archive → S3 ────────►
    │                         │─ INSERT media_import_jobs   │
    │                         │  status=QUEUED              │
    │◄─ {jobId, status: QUEUED}│                            │
    │                         │                            │
    │       [async job worker] │                            │
    │                         │─ fetch archive from S3 ◄────
    │                         │─ parse Excel manifest       │
    │                         │─ extract ZIP entries        │
    │                         │─ for each image:            │
    │                         │   matchToChild()            │
    │                         │   detectRole()              │
    │                         │   checkDuplicate()          │
    │                         │   INSERT media_import_items │
    │                         │   upload to S3 ─────────────►
    │                         │   INSERT media_files        │
    │                         │   INSERT media_attachments  │
    │                         │   enqueue variant generation│
    │                         │─ UPDATE media_import_jobs   │
    │                         │  status=COMPLETE            │
    │                         │                            │
    │─ GET /api/media/import/{jobId} ─────────────────────► │
    │◄─ {status, matched, failed, errorReport[]} ──────────  │
```

## 6.4 Duplicate Detection

Before uploading each file:
1. Compute SHA-256 hash of the file bytes
2. Query `media_files` for existing `content_hash = hash AND org_id = orgId`
3. If found AND already attached to the same child with the same role → mark `status=DUPLICATE`, skip
4. If found AND attached to a different child → mark `status=DUPLICATE`, add to error report with the existing attachment's child reference
5. If found AND not attached to this role → re-attach (no re-upload needed; creates new `media_attachments` row pointing to existing `media_files` row)

## 6.5 Error Report Format

```json
[
  {
    "filename": "ROLL-99999.jpg",
    "status": "UNMATCHED",
    "reason": "No child found with roll_number ROLL-99999",
    "suggestion": "Verify roll number in manifest"
  },
  {
    "filename": "ROLL-001234.jpg",
    "status": "DUPLICATE",
    "reason": "Identical file already attached to child as PROFILE_PHOTO",
    "existingMediaId": "uuid"
  },
  {
    "filename": "corrupted.jpg",
    "status": "FAILED",
    "reason": "File could not be decoded as image/jpeg"
  }
]
```

The error report is stored in `media_import_jobs.error_report` (JSONB) and exposed via the API. The admin UI renders it as a downloadable CSV.

## 6.6 Retry

Failed items can be retried individually via `POST /api/media/import/{jobId}/retry-item/{itemId}` or the entire job can be re-queued after fixing the source files.

---

# SECTION 7 — SECURITY MODEL

## 7.1 Upload Permission Matrix

| Calling role | Can upload to | Allowed owner types |
|---|---|---|
| JJT_ADMIN | Any org | CHILD, SPONSOR, CAMPAIGN, EXPENSE, VENDOR, USER, ORGANISATION, MISSION |
| ORG_ADMIN | Own org | CHILD, SPONSOR, CAMPAIGN, EXPENSE, VENDOR, ORGANISATION |
| SPONSOR | Own profile | USER (own avatar only) |
| Public (unauthenticated) | None | — |

## 7.2 Read Permission Matrix

| Asset visibility | Who can get a URL |
|---|---|
| PUBLIC | Anyone — CDN delivers directly, no backend call needed |
| PRIVATE | Authenticated user with at least ORG_ADMIN role, or SPONSOR accessing their own child's documents |
| SIGNED | Bearer of the signed URL (time-limited) |

## 7.3 Pre-signed GET URL Lifecycle

```
Backend issues pre-signed GET URLs — never permanent.

PRIVATE assets:
  - Expiry: 1 hour for in-page viewing, 24 hours for email/download links
  - Generated on demand per request (not stored in DB)
  - After expiry: re-request from /api/media/{mediaId}/url

SIGNED assets (sponsor document sharing):
  - Expiry: configurable, default 7 days
  - Share token stored in DB as a one-time reference (can be revoked)
  - Revocation: DELETE /api/media/{mediaId}/share/{shareToken}
```

## 7.4 Malware Scanning Hook

```
Upload complete
    │
    ├── [media.security.malware-scan-enabled = true]
    │     media_files.status = SCANNING
    │     Trigger: S3 Event → Lambda → ClamAV / third-party API
    │     Result callback: POST /api/internal/media/{mediaId}/scan-result
    │       {verdict: CLEAN | THREAT}
    │     CLEAN  → status = PROCESSING (proceed to variant generation)
    │     THREAT → status = QUARANTINED
    │               media_files.metadata.threat = {engine, signature, detectedAt}
    │               alert raised: MEDIA_THREAT_DETECTED
    │               file NOT served; admin notified
    │
    └── [media.security.malware-scan-enabled = false]
          Skip directly to PROCESSING
```

## 7.5 Content Validation (application layer, always on)

Before issuing a presigned PUT URL:
- MIME type checked against `media.security.allowed-mime-types` whitelist
- File size checked against `media.storage.max-upload-size-bytes`
- `storageRef` path is server-generated — clients never specify their own path (prevents path traversal)
- S3 bucket has a bucket policy denying all public PutObject except through the presigned URL mechanism

---

# SECTION 8 — PERFORMANCE STRATEGY

## 8.1 Serving Architecture

```
Browser
  │
  ├── [PUBLIC asset]
  │     Angular <img> src → CloudFront CDN URL (no auth, edge-cached)
  │     Cache-Control: max-age=31536000, immutable  (variants are content-addressed)
  │     CDN TTL: 365 days for variants (they never mutate — new upload = new mediaId)
  │
  └── [PRIVATE asset]
        Angular component requests: GET /api/media/{mediaId}/url
        Backend returns presigned URL (1hr)
        <img> renders via presigned URL (CloudFront or direct S3)
        Frontend caches the URL in memory for up to 55min (5min buffer before expiry)
```

## 8.2 Image Delivery Strategy

```
<picture> element on all image rendering (Angular component):

  <source srcset="[W320 WebP] 320w, [W640 WebP] 640w, [W1024 WebP] 1024w"
          type="image/webp" sizes="(max-width: 640px) 100vw, 50vw">
  <source srcset="[W320 JPEG] 320w, [W640 JPEG] 640w, [W1024 JPEG] 1024w"
          type="image/jpeg">
  <img src="[THUMBNAIL_MD]" alt="[alt_text]" loading="lazy">
```

The Angular `<app-media-image>` component handles:
- Responsive srcset selection based on viewport
- `loading="lazy"` attribute
- Placeholder skeleton while loading
- Fallback to placeholder SVG on error

## 8.3 Thumbnail Generation

Server-side (Spring Boot), using **Thumbnailator** (lightweight Java library, no native deps):
- Input: original from S3
- Output: THUMBNAIL_SM (100×100 crop), THUMBNAIL_MD (300×300 crop), THUMBNAIL_LG (600×600 crop)
- WebP conversion: via `libwebp` CLI call or **JVM WebP encoder** (webp4j)
- Responsive widths: resize to width, preserve aspect ratio

Processing runs in a `@Async` thread pool (`media-processor-pool`, size=4). Jobs are managed via an in-process queue backed by Spring's `TaskExecutor`. For scale beyond ~100 concurrent uploads/day, this queue moves to an SQS queue (infrastructure change only — application code calls `processingQueue.submit(jobId)`).

## 8.4 Pagination and Lazy Loading

Gallery API: cursor-based pagination using `sort_order`:
```
GET /api/media/attachments?ownerType=CHILD&ownerId={id}&role=GALLERY&limit=12&after={cursor}
```

- Returns `{items[], nextCursor, hasMore}`
- Angular gallery component renders first 12, loads more on scroll (Intersection Observer)
- Thumbnails are always served (fast load), full-resolution on click

## 8.5 Caching Strategy

| Layer | What is cached | TTL | Invalidation |
|---|---|---|---|
| CDN (CloudFront) | PUBLIC variants | 365 days (immutable) | New upload creates new mediaId/storageRef — old URLs naturally expire from CDN |
| Browser | Static assets (WebP variants) | From CDN Cache-Control header | N/A |
| Angular service | Presigned GET URL per mediaId | 55 minutes | On demand (user logs out, or component destroy) |
| Backend | None | — | DB is source of truth |

---

# SECTION 9 — AI READINESS

The following extension points are reserved but not implemented. Each hooks into the async processing pipeline.

## 9.1 Reserved Metadata Keys

All AI results are stored in `media_files.metadata` under namespaced keys:

```json
{
  "ai": {
    "quality": {
      "score": 0.87,
      "blur": false,
      "dark": false,
      "engine": "rekognition",
      "analysedAt": "2026-07-07T10:00:00Z"
    },
    "moderation": {
      "safe": true,
      "labels": [],
      "confidence": 0.99
    },
    "labels": ["child", "school", "classroom"],
    "faces": { "count": 1, "minAge": 8, "maxAge": 12 },
    "ocr": { "text": "", "language": "ur" },
    "altText": {
      "generated": "A child in school uniform sitting at a desk",
      "confidence": 0.82
    },
    "cropSuggestion": { "x": 120, "y": 45, "width": 300, "height": 300 }
  }
}
```

## 9.2 AI Hook Interface

```
MediaAiHook (interface)
  ├── analyse(mediaId, storageRef, mimeType) → AiAnalysisResult
  └── getSupportedMediaTypes() → Set<MediaType>

Implementations (all deferred to Roadmap 6+):
  ├── RekognitionModerationHook     → moderation labels, unsafe content detection
  ├── RekognitionLabelHook          → general labels for auto-tagging
  ├── RekognitionFaceHook           → face count (not identification — privacy rule)
  ├── TextractOcrHook               → text extraction from documents
  ├── QualityAnalysisHook           → blur, brightness, composition score
  ├── AutoCropHook                  → crop suggestion for profile photos
  └── AltTextGenerationHook         → accessibility text via Claude API
```

Hooks are called from the async processing pipeline after variant generation. Failed hooks do NOT fail the media record — they log a warning and leave the metadata key absent. The media item is always `READY` once variants exist, regardless of AI hook completion.

## 9.3 Image Moderation Gate (reserved, not active)

When `RekognitionModerationHook` is enabled:
- Verdict `UNSAFE` → `media_files.status = QUARANTINED` (same as malware)
- Verdict `REVIEW` → alert raised, admin review required before auto-publish
- Verdict `SAFE` → proceed normally

This gate matters specifically for the public children page — images should be reviewed before they appear to international sponsors.

---

# SECTION 10 — BACKEND API SPECIFICATION

```
# Upload workflow
POST   /api/media/upload-intent           Request upload slot, get presigned URL
POST   /api/media/{mediaId}/confirm        Confirm upload landed, trigger processing
GET    /api/media/{mediaId}                Get media record + variant list
GET    /api/media/{mediaId}/url            Get presigned GET URL for PRIVATE asset
DELETE /api/media/{mediaId}                Soft-delete (sets deleted_at, schedules S3 delete)

# Attachments
GET    /api/media/attachments              ?ownerType=&ownerId=&role=&limit=&after=
POST   /api/media/attachments              Attach existing media to an entity
DELETE /api/media/attachments/{id}         Detach (does not delete media_file)
PATCH  /api/media/attachments/reorder      [{id, sortOrder}] — reorder gallery

# Bulk import
POST   /api/media/import                   Start bulk import job (multipart: manifest + archive)
GET    /api/media/import/{jobId}           Job status + progress + error report
GET    /api/media/import/{jobId}/report.csv Download error report as CSV
POST   /api/media/import/{jobId}/retry-item/{itemId}

# Internal (called by scan/processing workers)
POST   /api/internal/media/{mediaId}/scan-result    {verdict: CLEAN|THREAT}
POST   /api/internal/media/{mediaId}/variant-ready  {variantType, storageRef, ...}
POST   /api/internal/media/{mediaId}/processing-failed {reason}

# Admin
GET    /api/admin/media                    ?orgId=&mediaType=&status=&page=
GET    /api/admin/media/{mediaId}/quarantine  Release quarantined item after review
DELETE /api/admin/media/{mediaId}/hard      Hard-delete (removes S3 object + DB row)
```

---

# SECTION 11 — FRONTEND ARCHITECTURE

## 11.1 Angular Components

```
MediaModule (shared)
  ├── MediaImageComponent          <app-media-image [mediaId]="..." [role]="..." />
  │     - Responsive <picture> with srcset
  │     - Skeleton placeholder during load
  │     - Error state → default placeholder SVG
  │     - Lazy loading via IntersectionObserver
  │
  ├── MediaGalleryComponent        <app-media-gallery [ownerType]="..." [ownerId]="..." />
  │     - Paginated grid with infinite scroll
  │     - Click → full-size lightbox
  │     - Drag-to-reorder (admin only)
  │
  ├── MediaUploadComponent         <app-media-upload [ownerType]="..." [ownerId]="..." [role]="..." />
  │     - Drag-and-drop + click-to-browse
  │     - Progress bar (upload to S3)
  │     - Thumbnail preview before confirm
  │     - Calls upload-intent → PUT → confirm
  │
  ├── BulkImportComponent          Admin only — /admin/media/import
  │     - File pickers for manifest + archive
  │     - Progress polling
  │     - Error report table + CSV download
  │
  └── MediaService                 service singleton
        - uploadFile(request) → Observable<MediaUploadProgress>
        - getAttachments(ownerType, ownerId, role) → Observable<MediaAttachment[]>
        - getPresignedUrl(mediaId) → cached Observable<string>
        - deleteAttachment(id) → Observable<void>
        - reorderGallery(attachments) → Observable<void>
```

## 11.2 Integration Points

- **Child card (`one-child-at-a-time`, `children` page):** Replace hardcoded `P1.png`/`P2.png` static images with `<app-media-image [mediaId]="child.profilePhotoMediaId" />`. If `profilePhotoMediaId` is null → placeholder SVG.
- **Child detail (admin):** `<app-media-gallery ownerType="CHILD" [ownerId]="child.id" />` below the child form.
- **Admin child list:** PROFILE_PHOTO thumbnail in each row.
- **Campaign banners:** `<app-media-image ownerType="CAMPAIGN" [ownerId]="campaign.id" role="BANNER" />`
- **Expense attachments:** `<app-media-upload>` embedded in the expense modal.
- **User avatars:** `<app-media-image ownerType="USER" [ownerId]="user.id" role="AVATAR" />` in header.

---

# SECTION 12 — MIGRATION PLAN

| Migration | Contents |
|---|---|
| **V_M1** (inserts before Roadmap 5 V30) | `media_files` + `media_variants` + `media_attachments` tables + all indexes |
| **V_M2** | `media_import_jobs` + `media_import_items` tables |
| **V_M3** | Add `profile_photo_media_id UUID FK → media_files` (nullable) to `children` table — convenience denorm for fast profile photo lookup without JOIN |
| **V_M4** | Add `avatar_media_id UUID FK → media_files` (nullable) to `users` table |
| **V_M5** | Migrate existing hardcoded `children.photo_url` column (if one is added before this milestone) — scoped to actual state at migration time |

**Flyway sequencing:** The Media Platform migrations are numbered in the V25 range. Specifically, they must run before Roadmap 5's V30 (which originally defined the simpler `attachments` table). The final sequence will be:

```
V25: account_categories (R5)
V26: cost_centres (R5)
V27: financial_periods (R5)
V28: people + payroll_profiles (R5)
V29: vendors (R5)
V30: media_files + media_variants + media_attachments (Media Platform — replaces R5 V30)
V31: media_import_jobs + media_import_items (Media Platform)
V32: children.profile_photo_media_id + users.avatar_media_id (Media Platform)
V33: expenses + approval_requests + approval_check_results (R5 — was V30, renumbered)
V34: financial_transactions (R5 — was V31, renumbered)
V35: budgets (R5 — was V32, renumbered)
V36: mission_nodes + impact_metrics (R5 — was V33, renumbered)
V37: transparency_snapshots (R5 — was V34, renumbered)
V38: indexes + constraints hardening (R5 — was V35, renumbered)
```

---

# SECTION 13 — DEVELOPMENT MILESTONES

## M1 — Storage Foundation (~1 week)

- Add Spring Cloud AWS dependency + configuration
- `LocalStorageProvider` implementation (dev)
- `S3StorageProvider` implementation (prod)
- `StorageProvider` interface + `MediaStorageConfig` bean selection
- V_M1 migration: `media_files`, `media_variants`, `media_attachments`
- `MediaService` (upload-intent, confirm, delete)
- `MediaController` (upload-intent, confirm, get, url, delete)
- `MediaUploadComponent` (Angular) — single file, progress bar, confirm
- `MediaImageComponent` (Angular) — renders PUBLIC variant with fallback
- Integration: child profile photo upload in admin child form

**Definition of done:** Admin can upload a child profile photo; it appears on the public /children page from S3/CloudFront.

## M2 — Variants + Gallery (~4 days)

- Async processing pipeline (`@Async` + `TaskExecutor`)
- Thumbnailator integration for thumbnail generation
- WebP conversion
- `media_variants` population after processing
- `MediaGalleryComponent` (Angular) — paginated grid, lightbox, drag-to-reorder
- V_M2 migration: `media_import_jobs`, `media_import_items`
- Integration: child gallery section in admin

**Definition of done:** Uploaded photo triggers variant generation; gallery renders responsive WebP with lazy loading.

## M3 — Bulk Import (~4 days)

- `BulkImportService` with ZIP parsing (Java `ZipInputStream`), Excel parsing (Apache POI)
- Matching engine (configurable rules)
- Duplicate detection via SHA-256 hash
- `media_import_jobs` + `media_import_items` state machine
- `BulkImportComponent` (Angular) — progress, error report, CSV download
- Error report CSV generation

**Definition of done:** Admin uploads a ZIP of 200 photos + Excel manifest; matched photos are attached to children; error report identifies unmatched files.

## M4 — Security Hardening + Signed URLs (~2 days)

- PRIVATE visibility + presigned GET URL generation
- Signed URL sharing (sponsor document links)
- Malware scanning hook interface (stub — actual scanner opt-in)
- MIME type + size guards at upload-intent
- Role-based attachment permission enforcement

## M5 — AI Seams (~1 day)

- `MediaAiHook` interface
- `AiHookRegistry` (empty implementations for Rekognition hooks)
- Hook invocation point in processing pipeline (after variant generation)
- Metadata keys documented and stubbed in `media_files.metadata`

---

# SECTION 14 — RISK REGISTER

| ID | Risk | Probability | Impact | Mitigation |
|---|---|---|---|---|
| MR1 | Heroku's 30-second request timeout kills large file uploads | High (without presigned URLs) | High | Pre-signed URL pattern: file bytes go directly to S3, never through Heroku. Eliminated by design. |
| MR2 | Variant generation OOMs on Heroku's 512MB dyno | Medium | Medium | Processing is async and bounded (4-thread pool). Images are streamed, not held fully in memory. Thumbnailator is stream-based. Large video processing deferred to M5+. |
| MR3 | S3 costs spike from accidental public bucket + hotlinking | Low | Medium | Bucket policy: public GET only via CloudFront; direct S3 access denied. CloudFront has per-IP rate limiting available. |
| MR4 | Bulk import ZIP bomb (malicious large archive) | Low | Medium | Size limit on archive (500MB max). ZIP entry count limit (1000 files). Entry size limit during extraction. |
| MR5 | SHA-256 hash collision false-positive on duplicate detection | Negligible | Low | SHA-256 collision is cryptographically infeasible. Accepted. |
| MR6 | MinIO (local dev) diverges from S3 behaviour | Low | Low | MinIO tracks S3 API closely. Any divergence found in dev is a test against the abstraction layer — worth finding early. |
| MR7 | CloudFront cache serving stale deleted content | Low | Medium | On delete: CloudFront invalidation issued for the affected paths. `/*` invalidation is free for first 1000/month. |
| MR8 | Roadmap 5 migration numbering conflict (V30 collision) | High | High | Roadmap 5 V30+ must be renumbered as documented in Section 12. This is an explicit pre-implementation gate. |
| MR9 | Child photos served publicly without consent verification | Medium | High | Visibility defaults to PUBLIC for child photos only (as the /children page already shows children publicly). If GDPR/data protection requirements change, visibility can be set to PRIVATE per child — the model supports it. |

---

# ARCHITECTURE FREEZE — APPROVAL CHECKLIST

Before M1 implementation begins, confirm:

- [ ] AWS S3 + CloudFront chosen as production storage provider (or override with rationale)
- [ ] MinIO agreed as local development storage provider
- [ ] S3 bucket naming and region confirmed (`jjt-media-prod`, `eu-west-2`)
- [ ] CloudFront distribution URL confirmed (`cdn.sponsorone.app`)
- [ ] Roadmap 5 migration renumbering plan (Section 12) acknowledged
- [ ] `media_attachments` replaces the Roadmap 5 `attachments` table — confirmed with Roadmap 5 implementation lead
- [ ] Maximum upload size confirmed (50MB default)
- [ ] Child photo visibility default confirmed (PUBLIC — consistent with existing public /children page)
- [ ] Malware scanning: enabled in prod from day one, or deferred?
- [ ] Apache POI and Thumbnailator added to `pom.xml` dependencies confirmed
- [ ] Spring Cloud AWS version confirmed compatible with Spring Boot version in use
