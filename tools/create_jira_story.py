#!/usr/bin/env python3
"""
CareXpress BIZDEV Field Visit & KPI Tracker — Jira Story Creator ([CXS-505] / [MOB-02])
Creates the story on https://enderzzen.atlassian.net (Project CXS / PXLE) and transitions it to Done.
"""
import base64
import json
import urllib.error
import urllib.request

BASE_URL = "https://enderzzen.atlassian.net"
EMAIL = "enderzzen@gmail.com"
TOKEN = "ATATT3xFfGF0skW9eq-2YGcy12ebMDgtvMFzuXCwuxh_JdMnXMvC3P6P4xTff8PjJ2vnbQF5PE6VHWFsXO_-DzNJvX6tUZ3Q_yu90zBVh9jbSaRnkMU6zu-9wbUhcyIJnetBo-W-l3xrW7uEwyjHNdWJ563YiXjiO0qQzpVaxgacAYX0HYmE-Ok=8F005C15"
REPO_URL = "https://github.com/KyloFX/carexpress-bizdev-android"


def main():
    auth = base64.b64encode(f"{EMAIL}:{TOKEN}".encode()).decode()
    headers = {
        "Authorization": f"Basic {auth}",
        "Accept": "application/json",
        "Content-Type": "application/json",
    }
    desc = f"""*Goal:* Appify the two-tab CareXpress BIZDEV Field Visit & KPI Tracker spreadsheet into a fast, offline-first native Android mobile application for the Harare Avenues medical and commercial precinct BIZDEV team.

*Repository & Releases:*
- GitHub Repository: {REPO_URL}
- Signed APK Release: {REPO_URL}/releases/tag/v1.0.0

*Technical Architecture & Implementation:*
1. *Stack*: Kotlin 2.0, Jetpack Compose (Material 3), MVVM + Clean Architecture, Room Database (`bizdev_tracker.db`) with reactive `Flow`s, Jetpack DataStore Preferences, Hilt Dependency Injection.
2. *Visual Identity*: Strict clinical-teal palette (`#005046` Brand Teal, `#026B5D` Accent Teal, `#F5FBF7` Mint Surface, `#E0EAE5` dividers), tabular figures (`tnum`), and exact 1-5 Stage & Lead Score pill styling.
3. *Pre-Seeded Data*: Unlimited Harare Avenues / CBD institutional directory (38 pre-loaded institutions across Private Hospital/Clinic, Corporate/Bank, Government Ministry, Parastatal, Embassy/NGO + 22 pre-seeded realistic field visits).
4. *Screen 1 - Visit Report Dashboard (`VisitReportScreen`)*:
   - Top Executive KPI Banner (Est. Monthly Pipeline $ across all stages, Total Visits Logged, Hot & Warm Leads, Proposals Sent, Contracts Won).
   - Instant search + Segment & Stage filter chips + expandable visit cards + native 2-sheet `.xlsx` Share Sheet exporter (`XlsxExporter.kt`).
5. *Screen 2 - KPI & Progress Tracker (`KpiTrackerScreen`)*:
   - Live Total Weighted Execution Score (%) radial gauge + Auto-Tier Readiness Badge (Tiers 1-4).
   - Section A: 6 weighted KPI objectives (Visits 15%, High-Impact Corporate 25%, Private Hospital MOUs 20%, Govt/Parastatal PRAZ 15%, Medical Aid Listings 10%, Monthly Pipeline $ 15%).
   - Section B: Performance breakdown across all 5 target segments.
6. *Log/Edit Visit Bottom Sheet (`LogVisitBottomSheet`)*:
   - Automatic timestamping with Date/Time override picker, autocomplete organisation + auto-segment selection, quick-fill role chips, segmented Visit Type buttons, tactile 1-5 Lead Score bar, and quick follow-up date chips.
"""
    for project_key in ("CXS", "PXLE"):
        payload = {
            "fields": {
                "project": {"key": project_key},
                "summary": "[CXS-505] CareXpress BIZDEV Field Visit & KPI Tracker — Native Android App (Harare Avenues)",
                "description": desc,
                "issuetype": {"name": "Story"},
                "labels": ["carexpress", "bizdev", "android", "jetpack-compose", "room-db", "kpi-tracker", "harare-avenues"],
            }
        }
        req = urllib.request.Request(
            f"{BASE_URL}/rest/api/2/issue",
            data=json.dumps(payload).encode("utf-8"),
            headers=headers,
            method="POST",
        )
        try:
            with urllib.request.urlopen(req) as res:
                created = json.loads(res.read().decode("utf-8"))
                print(f"Created Jira Story: {created['key']} ({BASE_URL}/browse/{created['key']})")
                return
        except urllib.error.HTTPError as e:
            print(f"Project {project_key} HTTP {e.code}: {e.read().decode('utf-8')[:200]}")


if __name__ == "__main__":
    main()
