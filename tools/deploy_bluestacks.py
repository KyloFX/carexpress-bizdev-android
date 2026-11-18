#!/usr/bin/env python3
"""
CareXpress BIZDEV Tracker — BlueStacks ADB Auto-Deployer & Release Downloader
Connects to BlueStacks (HD-Adb.exe), downloads the latest signed APK from GitHub Releases
(or uses a local APK under releases/ or app/build/outputs/apk), installs it with -r, and launches MainActivity.
"""
import glob
import json
import os
import subprocess
import sys
import urllib.request

ADB_PATHS = [
    r"C:\Program Files\BlueStacks_nxt\HD-Adb.exe",
    r"C:\Program Files (x86)\BlueStacks_nxt\HD-Adb.exe",
    "adb",
]
BLUESTACKS_PORTS = [5555, 5554, 5565, 5575]
PACKAGE_NAME = "com.carexpress.bizdev"
MAIN_ACTIVITY = "com.carexpress.bizdev/.MainActivity"


def find_adb() -> str:
    for p in ADB_PATHS:
        if os.path.exists(p):
            return f'"{p}"'
    return "adb"


def find_or_download_apk(pat: str | None = None) -> str:
    os.makedirs("releases", exist_ok=True)
    candidates = (
        glob.glob("releases/*.apk")
        + glob.glob("app/build/outputs/apk/**/*.apk", recursive=True)
        + glob.glob("*.apk")
    )
    if candidates:
        candidates.sort(key=os.path.getmtime, reverse=True)
        return candidates[0]

    url = "https://api.github.com/repos/KyloFX/carexpress-bizdev-android/releases/latest"
    headers = {"Accept": "application/vnd.github.v3+json", "User-Agent": "CareXpress-Bizdev-Deployer"}
    if pat:
        headers["Authorization"] = f"token {pat}"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req) as res:
        rel = json.loads(res.read().decode("utf-8"))
    assets = rel.get("assets", [])
    apk_asset = next((a for a in assets if a["name"].endswith(".apk")), None)
    if not apk_asset:
        raise RuntimeError("No .apk asset found in latest GitHub release")

    dl_url = apk_asset["url"] if pat else apk_asset["browser_download_url"]
    dl_headers = {"User-Agent": "CareXpress-Bizdev-Deployer"}
    if pat:
        dl_headers["Authorization"] = f"token {pat}"
        dl_headers["Accept"] = "application/octet-stream"
    dest = os.path.join("releases", apk_asset["name"])
    print(f"Downloading {apk_asset['name']} -> {dest} ...")
    dl_req = urllib.request.Request(dl_url, headers=dl_headers)
    with urllib.request.urlopen(dl_req) as r, open(dest, "wb") as out:
        out.write(r.read())
    return dest


def get_active_devices(adb: str) -> list[str]:
    dev_out = subprocess.run(f"{adb} devices", shell=True, text=True, capture_output=True).stdout
    print(dev_out.strip())
    devices = []
    for line in dev_out.splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            devices.append(parts[0])
    return devices


def main():
    adb = find_adb()
    print(f"Using ADB: {adb}")
    for port in BLUESTACKS_PORTS:
        subprocess.run(f"{adb} connect 127.0.0.1:{port}", shell=True, capture_output=True)

    devices = get_active_devices(adb)
    if not devices:
        print("No active BlueStacks/ADB devices found. Ensure BlueStacks is running with ADB enabled.")
        sys.exit(1)

    # Prefer 127.0.0.1:5555 when both 127.0.0.1:5555 and emulator-5554 point to BlueStacks
    target_dev = "127.0.0.1:5555" if "127.0.0.1:5555" in devices else devices[0]
    pat = os.environ.get("GITHUB_TOKEN")
    apk = sys.argv[1] if len(sys.argv) > 1 else find_or_download_apk(pat)
    print(f"Installing APK on {target_dev}: {apk}")
    res = subprocess.run(f'{adb} -s {target_dev} install -r "{apk}"', shell=True, text=True)
    if res.returncode == 0:
        print(f"Launching CareXpress BIZDEV Tracker on {target_dev}...")
        subprocess.run(f"{adb} -s {target_dev} shell am start -n {MAIN_ACTIVITY}", shell=True)


if __name__ == "__main__":
    main()
