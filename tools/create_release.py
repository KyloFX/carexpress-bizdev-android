#!/usr/bin/env python3
"""
CareXpress BIZDEV Field Visit & KPI Tracker — Release Tagging & CI/CD Push Automation
Creates a semantic version tag and pushes it to GitHub to trigger GitHub Actions APK build.
"""
import json
import subprocess
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def run_cmd(cmd: str) -> str:
    print(f">> {cmd}")
    res = subprocess.run(cmd, shell=True, text=True, capture_output=True)
    if res.stdout:
        print(res.stdout.strip())
    if res.returncode != 0:
        print(f"Error ({res.returncode}): {res.stderr.strip()}", file=sys.stderr)
        sys.exit(res.returncode)
    return res.stdout.strip()


def main():
    tag = sys.argv[1].strip() if len(sys.argv) >= 2 else "v1.0.0"
    if not tag.startswith("v"):
        tag = f"v{tag}"
    notes = sys.argv[2] if len(sys.argv) > 2 else f"CareXpress BIZDEV Field Visit & KPI Tracker {tag}"

    clean_ver = tag.lstrip("v")
    version_data = {
        "version": clean_ver,
        "tag_name": tag,
        "name": f"CareXpress BIZDEV Tracker {tag}",
        "body": notes,
        "download_url": f"https://github.com/KyloFX/carexpress-bizdev-android/releases/download/{tag}/carexpress-bizdev-{tag}.apk",
        "apk_name": f"carexpress-bizdev-{tag}.apk",
        "assets": [
            {
                "name": f"carexpress-bizdev-{tag}.apk",
                "browser_download_url": f"https://github.com/KyloFX/carexpress-bizdev-android/releases/download/{tag}/carexpress-bizdev-{tag}.apk",
                "size": 0,
                "content_type": "application/vnd.android.package-archive",
            }
        ],
    }
    with open("version.json", "w", encoding="utf-8") as f:
        json.dump(version_data, f, indent=2)

    status = run_cmd("git status --porcelain")
    if status:
        run_cmd("git add -A")
        run_cmd(f'git commit -m "chore: prepare release {tag}"')

    run_cmd("git push origin HEAD --force")
    subprocess.run(f"git tag -d {tag}", shell=True, capture_output=True)
    subprocess.run(f"git push origin :refs/tags/{tag}", shell=True, capture_output=True)
    run_cmd(f'git tag -a {tag} -m "{notes}"')
    run_cmd(f"git push origin {tag}")
    print(f"Tag {tag} pushed! GitHub Actions is building the signed APK.")


if __name__ == "__main__":
    main()
