# CareXpress BIZDEV Field Visit & KPI Tracker (`com.carexpress.bizdev`)

Offline-first native Android application (Kotlin 2.0 + Jetpack Compose Material 3 + Room + Hilt + MVVM) for CareXpress Pharmacy's Business Development field team operating in the Harare Avenues medical and commercial precinct.

## Features
- **Offline-First Room Database (`bizdev_tracker.db`)** with reactive `Flow` queries and pre-seeded Harare Avenues / CBD target institutions across all 5 segments (`Private Hospital/Clinic`, `Corporate/Bank`, `Government Ministry`, `Parastatal`, `Embassy/NGO`).
- **Executive Clinical-Teal Palette**: `#005046` primary dark teal, `#026B5D` secondary accent teal, `#F5FBF7` mint card surface, `#F8FAF9` / `#FFFFFF` clean light surfaces, `#E0EAE5` subtle dividers, tabular figures (`tnum`), and exact stage & lead score pill styling.
- **Screen 1 — Visit Report Dashboard (`VisitReportScreen`)**:
  - Top Executive KPI Banner (`Est. Monthly Pipeline ($)` across all stages, `Total Visits Logged`, `Hot & Warm Leads`, `Proposals Sent`, `Contracts Won`).
  - Instant search by organisation, key person, or role + horizontal filter chips by Segment and Stage.
  - Expandable visit cards with overdue follow-up highlighting and one-tap edit modal.
  - Native two-sheet `.xlsx` Excel workbook exporter (`Visit Report` + `KPI Tracker`) via Android Share Sheet.
- **Screen 2 — KPI & Progress Tracker (`KpiTrackerScreen`)**:
  - Live `Total Weighted Execution Score (%)` radial gauge and Auto-Tier Readiness Badge (`Tier 1` through `Tier 4`).
  - Section A: 6 weighted BIZDEV objectives with target vs. actual, capped/uncapped progress, weighted score contribution, status tags (`Exceeded`, `On Track`, `In Progress`, `Needs Focus`), and interactive `+/-` stepper for Medical Aid Direct Billing Network Listings (persisted in Jetpack DataStore).
  - Section B: Performance breakdown across all 5 target segments (Visits, Proposals, Won, Conversion %, Covered Lives, Pipeline $).
- **One-Handed Field Entry Modal (`LogVisitBottomSheet`)**:
  - Automatic date/time stamp upon opening with optional Date/Time override picker for back-logging.
  - Autocomplete organisation selector (38 pre-loaded Harare Avenues/CBD institutions + unlimited custom organisations) with automatic Segment selection.
  - Quick-fill role chips, segmented Visit Type buttons, color-coded Stage selector, side-by-side Covered Lives & Monthly Value inputs, tactile 1–5 Lead Score bar, multi-line notes, and quick follow-up date chips (`+2 Days`, `+1 Week`, `Next Month`, `Custom Date`).

## Testing & Deployment

### Deploy to BlueStacks Emulator
```bash
python tools/deploy_bluestacks.py
```

### Trigger New Signed Testing Release on GitHub Actions
```bash
python tools/create_release.py v1.0.0 "Initial production release"
```
