# 🎮 Maintenance Log

*Player-friendly changelog covering all updates, bug fixes, and improvements*

---

## 2025-11-25 – Version 1.0.1 "Spawn & Wall Kick"

### Changes
- New pieces now spawn near the top instead of the middle of the board.
- Improved rotation near walls so pieces can rotate even when touching the left or right border.

### Notes
- First update focusing on smoother gameplay feel.

---

## 2025-11-28 – Version 1.0.2 "Stability Pass"

### Changes

- Improved the behaviour of line-clearing so rows fall correctly into place.
- Internal stability upgrades to prepare for future features.

### Notes

- No visible UI differences, but gameplay consistency is improved.

---

## 2025-11-30 – Version 1.0.3 "Game Over & Pause Polish"

### Changes

- Fixed an issue where an extra falling piece appeared after the game ended.
- Pause menu is now fully interactive: Resume, Restart, and Main Menu buttons all work.
- Reworked game pause system for a smoother and more intuitive experience.

### Notes

- Game over and pause behaviour now match players' expectations.

---

## 2025-12-02 – Version 1.0.4 "Grid Alignment"

### Changes

- Falling pieces now stay perfectly aligned with the grid at all times.
- Score display now updates automatically and stays synced with the game.

### Notes

- Improves visual clarity, especially during fast gameplay.

---

## 2025-12-04 – Version 1.0.5 "Invisible Mode & UI Upgrade"

### Changes

- Renamed Hyper Mode → Invisible Mode for clearer understanding.
- Game Over screen redesigned to match the pause menu for consistent visual style.
- Added new internal mode settings to prepare for per-mode tuning in future updates (e.g., ghost visibility, speed).

### Notes

- Game UI now feels more cohesive and polished.

---

## 2025-12-05 – Version 1.0.6 "Ghost Piece Cleanup"

### Changes

- Identified several issues with the ghost piece:
  - Sometimes stuck at the wrong height.
  - Sometimes misaligned by a cell.
  - Sometimes visible in Invisible Mode when it shouldn't be.
- Temporarily simplified the ghost system while a proper fix is designed.

### Notes

- This update removes confusing ghost behaviour until a cleaner implementation is ready.

---

## 2025-12-07 – Version 1.0.7 "Mode-Aware System Planning"

### Changes

- Began restructuring mode-specific logic to make future game modes easier to develop.
- Designed a new configuration system where each mode can define:
  - Drop speed
  - Ghost visibility
  - Grid visibility
  - Landed block visibility

### Notes

- Architectural update preparing the game for cleaner logic and easier expansion.
