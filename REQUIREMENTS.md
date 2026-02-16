# Marginalia Android — V1 Requirements

## Overview

Android port of the Marginalia iOS reading tracker. Feature parity with the shipped iOS app, adapted to Android conventions and Material Design 3.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Persistence | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| DI | Hilt |
| Barcode scanning | ML Kit Barcode Scanning |
| Text recognition | ML Kit Text Recognition v2 |
| Image loading | Coil |
| Networking | Ktor or Retrofit |
| Camera | CameraX |
| Navigation | Navigation Compose |

No third-party UI component libraries. No Firebase. No accounts or analytics.

---

## Data Model

### Entity: Book

| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | Long (auto) | No | Primary key |
| title | String | No | |
| isbn | String | Yes | |
| coverImageUrl | String | Yes | Remote URL |
| coverImagePath | String | Yes | Local file path for downloaded/selected covers |
| publisher | String | Yes | |
| publishedDate | String | Yes | |
| description | String | Yes | |
| pageCount | Int | Yes | |
| status | String | No | "toRead", "reading", "read" |
| rating | Int | Yes | 1–5, null if unrated |
| dateAdded | Long | No | Epoch millis, default now |
| dateStarted | Long | Yes | |
| dateFinished | Long | Yes | |

### Entity: Author

| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | Long (auto) | No | Primary key |
| name | String | No | Case-preserved |

### Entity: Note

| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | Long (auto) | No | Primary key |
| content | String | No | |
| createdAt | Long | No | Epoch millis |
| bookId | Long | No | Foreign key → Book |

### Entity: Quote

| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | Long (auto) | No | Primary key |
| text | String | No | |
| comment | String | Yes | |
| pageNumber | Int | Yes | |
| createdAt | Long | No | Epoch millis |
| bookId | Long | No | Foreign key → Book |

### Entity: Tag

| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | Long (auto) | No | Primary key |
| name | String | No | Lowercased, unique |
| displayName | String | No | Original case |
| colorName | String | Yes | Enum value for tag colour |

### Junction Tables

| Table | Columns |
|---|---|
| BookAuthorCrossRef | bookId (FK), authorId (FK) — composite PK |
| BookTagCrossRef | bookId (FK), tagId (FK) — composite PK |

### Delete Rules
- Delete Book → cascade delete Notes and Quotes
- Delete Book → remove BookAuthorCrossRef and BookTagCrossRef rows
- Delete Author → remove BookAuthorCrossRef rows (books remain)
- Delete Tag → remove BookTagCrossRef rows (books remain)

### Indices
- Book: title, status, dateAdded
- Author: name
- Tag: name (unique)
- Note: bookId
- Quote: bookId

---

## Screens

### 1. Home Screen

**Purpose:** Dashboard overview of reading activity.

**Layout:**
- App logo + "Home" title header
- **Currently Reading** — horizontal scrolling cards for books with status "reading". Each card shows cover image, title (2 lines, reserves space), author. Tap → Book Detail.
- **Your Library** — status count bar showing To Read / Reading / Read counts. Tap a status → navigates to Library filtered by that status.
- **Latest Notes & Quotes** — horizontal scrolling cards showing the 10 most recent notes and quotes across all books, sorted by date. Each card shows type badge (Note/Quote), date, preview text, and book title.
- **Empty state** — when library has zero books, replace all sections with a "Getting Started" card containing tips: scan a barcode, type an ISBN, add manually, save quotes & notes.

**Toolbar:** overflow menu with Export (CSV), Import (CSV), About.

### 2. Library Screen

**Purpose:** Browse, search, and filter all books.

**Features:**
- Search bar — searches across book titles, author names, note content, quote text/comments
- Tag filter bar — horizontal scrolling tag pills, AND/OR toggle
- Status filter and rating filter via filter menu
- Sort menu — title, date added, rating, author (ascending/descending)
- Paginated results (page size 10, load more on scroll)
- Each book row shows: cover thumbnail, title, author, status badge, rating stars, tags
- Search match indicator showing why a book matched (e.g. "Matched in notes")
- Swipe actions: delete with confirmation
- Tap → Book Detail

### 3. Book Detail Screen

**Purpose:** View and manage a single book.

**Layout (scrollable):**
- Cover image (large, with camera button to change/add)
- Title, authors, status badge (tappable to cycle), star rating (tappable)
- Metadata: publisher, published date, page count, ISBN, description
- **Tags section** — displayed as coloured chips. Quick-add with autocomplete. Remove via X button.
- **Notes & Quotes section** — tabbed (segmented control equivalent). Horizontal scrolling preview cards.
  - First column: half-height "Add" card + "View all" link underneath
  - Tap card → edit sheet
- **Actions** — Edit button, Delete button (with confirmation)

**Cover image:** download and compress from URL on first load (max 600px width, 0.7 JPEG quality). Store locally.

### 4. All Notes Screen

**Purpose:** View all notes for a book.

**Features:**
- Book header (small cover image + title + author)
- Search bar filtering note content
- Sort menu (Newest First / Oldest First)
- Card-style rows (material surface, rounded corners, padding)
- Expandable text: "More" button when content exceeds 3 lines, "Less" to collapse
- Tap card → edit note
- FAB or toolbar button to add new note

### 5. All Quotes Screen

**Purpose:** View all quotes for a book.

**Features:**
- Book header (small cover image + title + author)
- Search bar filtering across quote text, comments, page numbers
- Sort & Filter menu: sort by date (newest/oldest), filter "Has Comment"
- Card-style rows showing: italic quote text (3 lines), comment (2 lines), page number, date
- Expandable text with More/Less
- Tap card → edit quote
- FAB or toolbar button to add new quote

### 6. Add/Edit Book Screen (BookForm)

**Purpose:** Create or edit a book.

**Sections:**
1. **ISBN** — text field + "Lookup" button + "Scan Barcode" button. On successful lookup, auto-populate all fields.
2. **Title** (required)
3. **Authors** — multi-entry with autocomplete from existing authors
4. **Tags** — multi-entry with autocomplete from existing tags
5. **Cover image** — pick from gallery or camera. Show preview with remove option.
6. **Details** — publisher, published date, description, page count
7. **Status** — picker (To Read / Reading / Read). Auto-set dateStarted when moved to Reading, dateFinished when moved to Read.
8. **Rating** — star selector (1–5, optional)

**Validation:** title is required. All other fields optional.

### 7. ISBN Scanner Screen

**Purpose:** Scan barcode to look up book.

**Implementation:**
- CameraX preview with ML Kit Barcode Scanning
- Detect EAN-13 and EAN-8 (ISBN) formats
- Request camera permission with rationale
- On scan → look up via Book Lookup Service → return result to BookForm
- Cancel button to dismiss

### 8. Quote Editor Sheet

**Purpose:** Create or edit a quote.

**Fields:**
- Quote text (multiline, required)
- Captured page image with text recognition (see OCR section below)
- "Photograph Page" button (camera)
- Comment (multiline, optional)
- Page number (numeric, optional)
- Delete button (edit mode, with confirmation)

### 9. Note Editor Sheet

**Purpose:** Create or edit a note.

**Fields:**
- Content (multiline, required)
- Delete button (edit mode, with confirmation)

### 10. Manage Tags Screen

**Purpose:** CRUD operations on tags.

**Features:**
- List of all tags with book count
- Create new tag (with uniqueness check)
- Rename tag (with conflict detection)
- Colour picker (10 colours: red, orange, yellow, green, mint, teal, blue, purple, pink, brown)
- Delete tag (removes from all books)

### 11. About Dialog

**Purpose:** App info.

**Content:** App icon, app name, data attribution ("Book data provided by Open Library and Google Books"), link to support page.

---

## Services

### Book Lookup Service

**Protocol/interface-based** with two implementations for testing.

**RemoteLookupService:**
1. Query Open Library API: `https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data`
2. If not found, fall back to Google Books API: `https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}`
3. Return: title, authors, isbn, coverImageUrl, publisher, publishedDate, description, pageCount
4. Handle HTTP 429 (rate limited), network errors, not found, decode errors

**CachingLookupService:**
- Wraps RemoteLookupService
- In-memory cache (LruCache) + disk cache (JSON files in cache directory)
- Thread-safe

**ISBN Validation:** accept 10 or 13 digits, strip non-numeric characters (except trailing X for ISBN-10).

### Search Service

- Build Room queries with dynamic predicates for status, rating filters
- In-memory filtering for relationship searches (authors, notes, quotes, tags)
- Return results with match reasons (title, author, note, quote)
- Sort by: title, dateAdded, rating, author (ascending/descending)

### CSV Export/Import Service

**Export format** — 16 columns:
```
Title,Authors,Status,Rating,Tags,Pages,Publisher,Published Date,Date Added,Date Started,Date Finished,ISBN,Cover Image URL,Description,Notes,Quotes
```

**Field conventions:**
- Authors: comma-separated
- Tags: semicolon-separated
- Notes: semicolon-separated
- Quotes: semicolon-separated, format `Quote text [Comment: ...] (p. N)`
- Dates: locale short format
- Fields containing commas, quotes, or newlines: RFC 4180 quoting

**Import:** parse header row, match columns by name, deduplicate authors/tags against existing data.

---

## Text Recognition (OCR) — Quote Capture

### Flow
1. User taps "Photograph Page" in Quote Editor
2. Camera opens (CameraX), user captures photo
3. Image crop screen: user drags rectangle to select region of interest
4. ML Kit Text Recognition processes the cropped image
5. Recognised text populates the quote text field
6. User can edit before saving

### V1 Implementation (extract all text)
- Use ML Kit Text Recognition v2 (on-device, no network)
- Process cropped image → extract full transcript
- Populate quote text field with result
- User edits/trims as needed

### V2 Enhancement (interactive word selection — stretch goal)
- ML Kit returns `Text.TextBlock` → `Text.Line` → `Text.Element`, each with bounding box
- Custom Compose overlay on the image:
  - Invisible tap targets over each recognised word
  - Tap to start selection, drag to extend
  - Highlight selected word bounding boxes with semi-transparent overlay
  - Selection handles at start/end
- "Use Selection" button → concatenate selected words → quote field
- This is the killer feature differentiator

---

## Image Handling

### Cover Images
- Download from URL, compress to max 600px width, 0.7 JPEG quality
- Store compressed file in app-internal storage
- Load with Coil (memory + disk cache)
- Placeholder: rounded rectangle with book icon

### Camera Images (quote capture)
- Capture via CameraX
- Normalise orientation before processing
- Crop using custom Compose crop overlay with draggable handles
- CPU-bound crop operation on `Dispatchers.Default`

---

## Data & Privacy

- All data stored locally in Room database
- No accounts, no user tracking, no analytics
- Camera permission: used only for barcode scanning and page photography
- Network: used only for ISBN lookup (Open Library / Google Books APIs) and cover image download
- No third-party analytics or advertising SDKs

---

## Error Handling

Define sealed classes:
- **BookLookupError**: InvalidIsbn, NetworkError(cause), NotFound, DecodingError(cause), RateLimited
- **PersistenceError**: SaveFailed(cause), DeleteFailed(cause), QueryFailed(cause)

All errors surfaced to UI via ViewModel state (not exceptions). Snackbar or dialog for user-facing messages.

---

## Accessibility

- `contentDescription` on all icon buttons and images
- `semantics { }` for custom components
- `testTag` on all interactive elements for UI testing
- Support TalkBack navigation
- Use Material 3 semantic typography (no hardcoded font sizes)
- Minimum touch target 48dp
- Sufficient colour contrast (WCAG AA)

---

## V1 Scope

**In scope:**
- All screens listed above
- ISBN barcode scanning + API lookup
- Cover images (download, compress, local storage)
- Notes and quotes with search/sort/filter
- Quote capture via camera + OCR (full text extraction)
- Tags with colours
- Star ratings
- CSV import/export
- Getting Started empty state

**Out of scope (V2+):**
- Interactive word selection on captured page images
- Cloud sync / backup
- Widgets
- Wear OS companion
- Multiple reading sessions per book
- Social/sharing features
- Statistics/analytics dashboard
