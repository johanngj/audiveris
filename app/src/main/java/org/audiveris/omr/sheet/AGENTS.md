# SHEET - Sheet Processing Pipeline

Sheet and book management, recognition pipeline. 212 files.

## SUBPACKAGES

| Package | Files | Purpose |
|---------|-------|---------|
| `ui/` | 33 | Sheet display, zoom, selection, boards |
| `curve/` | 26 | Slur, tie, wedge curve detection |
| `grid/` | 25 | Staff line, barline detection |
| `rhythm/` | 20 | Time slot assignment, voice analysis |
| `stem/` | 15 | Stem detection and head linking |
| `header/` | ~10 | Clef, key, time signature detection |
| `beam/` | ~8 | Beam group detection |
| `note/` | ~8 | Note assembly from components |
| `symbol/` | ~6 | Symbol recognition coordination |
| (root) | ~60 | Core: Book, Sheet, Staff, Scale, SystemInfo |

## KEY CONCEPTS

**Book** = Multi-page score document (3086 lines - complexity hotspot)
- Contains SheetStubs (lazy-loaded sheets)
- Manages transcription state, export

**Sheet** = Single page being processed
- Has Scale, Staff list, SystemInfo list
- Contains SIGraph (see sig/AGENTS.md)

**Scale** = Computed measurements
- interline, beam thickness, stem width
- Derived from staff line analysis

**Staff** = 5-line staff with geometry
- Top/bottom line positions
- Clef, key, time at header

**SystemInfo** = Staves playing together
- Parts (instruments)
- Measures, barlines

## PROCESSING FLOW

```
LOAD    → Image load, binarization
GRID    → Staff lines, systems, barlines
HEADER  → Clef, key signature, time signature
STEMS   → Vertical stem detection
BEAMS   → Beam group detection
HEADS   → Note head template matching
LEDGERS → Ledger line detection
SYMBOLS → Other symbol recognition (via classifier)
RHYTHMS → Time slot assignment, voices
PAGE    → Final assembly, cross-system links
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Document management | `Book.java` |
| Page processing | `Sheet.java` |
| Lazy loading | `SheetStub.java` |
| Staff geometry | `Staff.java`, `StaffManager.java` |
| System grouping | `SystemInfo.java` |
| Scale computation | `Scale.java`, `ScaleBuilder.java` |
| Line detection | `grid/LinesRetriever.java` |
| Curve fitting | `curve/SlurLinker.java` |
| Rhythm analysis | `rhythm/RhythmsStep.java` |

## NOTES

- `BookActions.java` (2835 lines) = UI actions for Book
- Sheet processing coordinated by `step/` package
- SheetStub allows partial loading of large books
- Scale must be computed before any symbol detection
