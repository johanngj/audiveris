# UI - User Interface

Swing desktop UI with BSAF framework. 162 files.

## SUBPACKAGES

| Package | Files | Purpose |
|---------|-------|---------|
| `symbol/` | 50 | Music symbol rendering (MusicFont, ShapeSymbol) |
| `util/` | 28 | UI utilities, drag-drop, ModelessOptionPane |
| `field/` | 16 | Custom fields: LDoubleField, SpinnerUtil |
| `selection/` | 13 | Entity selection, SelectionService |
| `action/` | ~8 | BSAF actions for menus/toolbar |
| `view/` | ~6 | Zoom, scroll, rubber band |
| `treetable/` | ~4 | Tree-table widget |
| `dnd/` | ~3 | Drag-and-drop support |
| `resources/` | 16 | Properties files, icons |
| (root) | ~20 | MainGui, OmrGui, Board, BoardsPane |

## KEY CONCEPTS

**MainGui** = Application main frame
- Extends BSAF SingleFrameApplication
- Manages menus, toolbar, boards

**Board** = Dockable info panel
- Shows details for selected entity
- Examples: PixelBoard, GlyphBoard, InterBoard

**BoardsPane** = Container for Boards
- Tabbed layout for entity inspection

**Selection** = Observer for selected entities
- SelectionService notifies listeners
- UI components update on selection change

## PATTERNS

- **BSAF** (Better Swing Application Framework) for lifecycle
- **PropertyChangeListener** for UI updates (not Observer/Observable)
- **Crystal icons** from `dev/icons/crystal/`
- **JGoodies Forms** for layouts (legacy - avoid in new code)

## WHERE TO LOOK

| Task | File |
|------|------|
| Main window | `MainGui.java`, `OmrGui.java` |
| Board panels | `Board.java`, `BoardsPane.java` |
| Selection | `selection/SelectionService.java` |
| Music symbols | `symbol/MusicFont.java`, `symbol/ShapeSymbol.java` |
| Actions | `action/Actions.java` |
| Field utils | `field/LDoubleField.java`, `field/SpinnerUtil.java` |

## NOTES

- Sheet display in `sheet/ui/SheetView.java` (not this package)
- SIG editing UI in `sig/ui/` (not this package)
- Icon resources: `.properties` files in `resources/` subpackages
- Avoid JGoodies PanelBuilder for new code
