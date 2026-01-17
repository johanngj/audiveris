# SIG - Symbol Interpretation Graph

Core data structure. Directed graph of music symbol interpretations per sheet. 215 files.

## SUBPACKAGES

| Package | Files | Purpose |
|---------|-------|---------|
| `inter/` | 95 | Inter classes - music symbol interpretations |
| `relation/` | 67 | Relation classes - edges linking Inters |
| `ui/` | 43 | SIG visualization and manual editing |
| (root) | ~10 | SIGraph, GradeImpacts, SigReducer |

## KEY CONCEPTS

**SIGraph** = JGraphT directed graph, one per Sheet
- Vertices: Inters (interpreted symbols)
- Edges: Relations (connections between symbols)

**Inter** (Interpretation) = Recognized music symbol
- `intrinsicGrade`: Standalone confidence (0.0-1.0)
- `contextualGrade`: Confidence with relations factored in
- Has Shape, bounding box, staff assignment
- Examples: HeadInter, StemInter, BeamInter, RestInter, ClefInter

**Relation** = Edge between two Inters
- **Support**: Positive connection, boosts contextual grade
  - HeadStemRelation, BeamStemRelation, AugmentationRelation
- **Exclusion**: Negative, marks incompatible interpretations
  - Exclusion.Overlap, Exclusion.Manual

## HIERARCHY

```
Inter (interface)
└── AbstractInter
    ├── AbstractChordInter (heads with stems)
    ├── AbstractNoteInter (note heads)
    ├── AbstractBeamInter (beams, hooks)
    ├── AbstractFlagInter (flags)
    └── ... (~95 concrete Inter classes)

Relation (interface)
└── AbstractRelation
    ├── Support (abstract)
    │   └── HeadStemRelation, BeamStemRelation, ...
    └── Exclusion
```

## WHERE TO LOOK

| Task | File |
|------|------|
| Graph operations | `SIGraph.java` |
| Inter base | `inter/Inter.java`, `inter/AbstractInter.java` |
| Relation base | `relation/Relation.java`, `relation/AbstractRelation.java` |
| Grade computation | `GradeImpacts.java` |
| Graph reduction | `SigReducer.java` |
| Inter editing UI | `ui/InterController.java` |
| Relation editing | `ui/RelationService.java` |

## PATTERNS

- Inter created with intrinsic grade from classifier
- Relations added by recognition steps
- `SigReducer` removes low-grade or conflicting Inters
- Final SIG contains "winning" interpretations per location

## NOTES

- Inter.getId() for stable XML serialization
- AbstractInter.getGrade() returns contextual (default) or intrinsic
- Shape enum (in `glyph/`) defines all ~600 symbol types
- SIG persisted in `.omr` files via JAXB
