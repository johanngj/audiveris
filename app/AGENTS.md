# APP - APPLICATION MODULE

Main Audiveris application. 1053 Java files, ~335k lines.

## STRUCTURE

```
app/
├── build.gradle           # Version 5.9.0, deps, tasks
├── res/                   # Runtime: fonts, classifier, icons
├── dev/                   # Dev: Crystal icons, start scripts
├── config-examples/       # User config templates
└── src/main/java/org/audiveris/omr/
    ├── sig/               # Symbol Interpretation Graph (see sig/AGENTS.md)
    ├── sheet/             # Sheet processing (see sheet/AGENTS.md)
    ├── ui/                # User interface (see ui/AGENTS.md)
    └── ...                # Other packages below
```

## PACKAGE GUIDE

| Package | Files | Purpose |
|---------|-------|---------|
| `sig/` | 215 | Core SIG data structure (documented separately) |
| `sheet/` | 212 | Sheet/book processing pipeline (documented separately) |
| `ui/` | 162 | Swing desktop UI (documented separately) |
| `util/` | 63 | Utilities: Jaxb, XmlUtil, AbstractEntity, OmrExecutors |
| `glyph/` | 45 | Glyph extraction, Shape enum (~600 symbols) |
| `image/` | 42 | Image processing: binarization, morphology, filtering |
| `classifier/` | 39 | Neural network shape classifier (basic-classifier.zip) |
| `score/` | 39 | MusicXML export via ProxyMusic library |
| `math/` | 31 | Math: Line, Circle, histogram, regression |
| `text/` | 21 | OCR via Tesseract/Javacpp |
| `lag/` | 20 | Line Adjacency Graph for connected components |
| `step/` | 19 | Processing steps: LOAD→GRID→HEADS→RHYTHMS→PAGE |
| `moments/` | 14 | Shape descriptors: ART, Legendre moments |
| `run/` | 11 | Run-length encoding for binary images |
| `constant/` | 10 | Constant.Integer/Double for user-tweakable params |
| `check/` | 9 | Validation: Check, CheckSuite, CheckBoard |
| `log/` | 6 | Logback configuration |
| `plugin/` | 4 | External plugin system |

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Main entry | `Audiveris.java`, `Main.java` |
| CLI parsing | `CLI.java` |
| Shape definitions | `glyph/Shape.java` |
| Image binarization | `image/AdaptiveFilter.java`, `image/GlobalFilter.java` |
| Template matching | `image/Template.java`, `image/TemplateFactory.java` |
| Neural classifier | `classifier/BasicClassifier.java` |
| MusicXML output | `score/PartwiseBuilder.java` (3885 lines) |
| Processing steps | `step/Steps.java`, `step/*Step.java` |

## CONVENTIONS

- `AbstractEntity` base class for domain objects with ID
- `Constant` wrapper for user-tweakable parameters
- `@XmlAccessorType(XmlAccessType.NONE)` on JAXB classes
- Resources loaded from `res/` via classloader

## NOTES

- `res/basic-classifier.zip` = trained neural net, overridable in user config
- `dev/icons/crystal/` = UI icons (22x22, 32x32)
- Generated: `build/generated-src/org/audiveris/omr/ProgramId.java`
