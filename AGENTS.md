# AUDIVERIS - PROJECT KNOWLEDGE BASE

> **Workspace context:** See `../../AGENTS.md` for cross-project info and `../../shared/SHEET_MUSIC.md` for test files.

## Tracking

- **After completing work**: Append summary to `DEVLOG.md`
- **When planning**: Update `TODO.md`  
- **On release**: Update `CHANGELOG.md`

**Generated:** 2026-01-17
**Commit:** e8e303ee8
**Branch:** johann

## PROJECT GOALS

**Primary:** PDF → MusicXML with perfect playback (correct pitches, rhythms, durations)

**Secondary:** + correct visual rendering (stems, beams, articulations, dynamics)

**Tertiary:** + correct lyrics

**Constraint:** Fully automated, no human correction required

**Target repertoire:** TTBB choral and TTBB choral with piano

## OVERVIEW

Audiveris is an open-source **Optical Music Recognition (OMR)** application that transcribes scanned sheet music into MusicXML. Built as a Swing desktop app with a neural network classifier and Tesseract OCR integration.

## STRUCTURE

```
Audiveris/
├── app/                    # Main application (see app/AGENTS.md)
│   └── src/main/java/org/audiveris/omr/
│       ├── sig/            # Symbol Interpretation Graph (see sig/AGENTS.md)
│       ├── sheet/          # Sheet processing pipeline (see sheet/AGENTS.md)
│       ├── ui/             # Swing UI layer (see ui/AGENTS.md)
│       └── ...             # Other packages documented in app/
├── docs/                   # Handbook (Jekyll + PDF via Prince)
├── flatpak/                # Linux Flatpak packaging
├── packaging/              # Cross-platform installers (jpackage)
└── schemas/                # XSD schema documentation
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Entry point | `app/.../Audiveris.java`, `Main.java` | CLI in `CLI.java` |
| Domain model | `sig/inter/`, `sig/relation/` | SIG = core data structure |
| Image processing | `image/`, `sheet/` | Morphology, filtering |
| Symbol recognition | `classifier/`, `glyph/` | Neural net in `basic-classifier.zip` |
| OCR integration | `text/` | Tesseract via Javacpp |
| MusicXML export | `score/` | Uses ProxyMusic library |
| Sheet pipeline | `step/` | Processing steps sequence |
| Configuration | `constant/` | Persistent user settings |

## CONVENTIONS

- **Java 25** minimum (set in `gradle.properties`)
- **4-space indent**, 100-char line limit (Jalopy Sun convention)
- **JAXB** for XML serialization (`.omr` project files)
- **SLF4J + Logback** for logging
- **EventBus** for decoupled event handling
- Resources in `app/res/` NOT `src/main/resources/`
- Generated source: `app/build/generated-src/` (ProgramId.java)

## ANTI-PATTERNS (THIS PROJECT)

- **DO NOT** use `src/main/resources/` - use `app/res/` instead
- **DO NOT** instantiate via `class.newInstance()` - deprecated
- **AVOID** JGoodies PanelBuilder - legacy, use alternatives
- **AVOID** Observer/Observable - use PropertyChangeListener
- Tests exclude: `**/org/audiveris/omr/jaxb/basic/**`, `**/jaxb/facade/**`

## UNIQUE STYLES

- **Inter** classes: Music symbol interpretations with grades (0-1)
- **Relation** classes: Graph edges linking Inters (Support/Exclusion)
- **Constant** pattern: `Constant.Integer`, `Constant.Double` for user-tweakable params
- **Shape** enum: ~600 music symbol types
- **AbstractEntity** base: Most domain objects extend this

## COMMANDS

```bash
# Build
./gradlew build

# Run application
./gradlew run

# Run with custom args
./gradlew run -PcmdLineArgs="--help"

# Run specific main class
./gradlew run -PmainClass=org.audiveris.omr.SomeClass

# Debug
./gradlew debug

# Generate JavaDoc
./gradlew javadoc

# Build installers (requires jpackage)
./gradlew :packaging:jpackage

# Generate Flatpak dependencies
./gradlew :app:flatpakGradleGenerator

# Batch transcribe with debug visualization
./app/build/install/app/bin/Audiveris -batch -debug-images /tmp/debug-output -export -transcribe input.pdf

# Disable OCR (improves note detection for instrumental-focused processing)
./app/build/install/app/bin/Audiveris -batch -constant org.audiveris.omr.text.tesseract.TesseractOCR.useOCR=false -export -transcribe input.pdf
```

## DEBUG VISUALIZATION

The `-debug-images <folder>` flag generates PNG overlays at each processing step with color-coded bounding boxes:

| Color | Category |
|-------|----------|
| Red | Note heads |
| Green | Stems |
| Blue | Beams |
| Orange | Rests |
| Purple | Clefs |
| Pink | Key signatures |
| Cyan | Time signatures |
| Yellow | Flags |
| Brown | Barlines |
| Deep pink | Slurs/ties |
| Turquoise | Text/lyrics |
| Gray | Other |

**Output files:**
- 20 step images: `*_load.png`, `*_binary.png`, ..., `*_page.png`
- Final composite: `*_final.png`
- JSON report: `*_report.json` (counts and avg confidence per category)

## OCR TOGGLE

**Constant:** `org.audiveris.omr.text.tesseract.TesseractOCR.useOCR`

Disabling OCR (`useOCR=false`) has been observed to **improve note detection**:

| Category | With OCR | No OCR |
|----------|----------|--------|
| HEAD | 175 | 341 (+95%) |
| STEM | 175 | 293 (+67%) |
| TEXT | 211 | 27 (-87%) |

**Hypothesis:** OCR bounding boxes overlap with noteheads, causing the classifier to miss notes. For instrumental-focused processing (playback accuracy), disable OCR.

## NOTES

- **Large files**: `PartwiseBuilder.java` (3885 lines), `Book.java` (3086), `BookActions.java` (2835) - complexity hotspots
- **Tesseract native libs**: Loaded via Javacpp, requires `--enable-native-access=ALL-UNNAMED`
- **Cross-platform**: Builds for Windows (.msi), Linux (.deb), macOS (.dmg)
- **Offline mode**: Flatpak uses `dependencies/` folder for air-gapped builds
