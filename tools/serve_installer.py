#!/usr/bin/env python3
"""
CareXpress BIZDEV Tracker — Local Wi-Fi Quick Installer & Sideload Server
"""
import glob
import http.server
import os
import socket
import socketserver

PORT = 8085


def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("10.255.255.255", 1))
        return s.getsockname()[0]
    except Exception:
        return "127.0.0.1"
    finally:
        s.close()


class ApkInstallerHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", "/index.html"):
            self.send_response(200)
            self.send_header("Content-type", "text/html; charset=utf-8")
            self.end_headers()
            apks = glob.glob("**/*.apk", recursive=True)
            links = ""
            for apk in apks:
                fname = os.path.basename(apk)
                fsize = os.path.getsize(apk) / (1024 * 1024)
                links += f"""
                <div style="background:#F5FBF7;padding:16px;border-radius:14px;margin-bottom:12px;border:1px solid #E0EAE5;">
                    <h3 style="margin:0 0 6px 0;color:#005046;">{fname}</h3>
                    <p style="margin:0 0 12px 0;color:#5B6F6A;font-size:14px;">{fsize:.2f} MB • Signed Testing Release</p>
                    <a href="/{apk.replace(os.sep, '/')}" style="display:inline-block;background:#005046;color:white;padding:12px 24px;border-radius:10px;text-decoration:none;font-weight:bold;">Download &amp; Install APK</a>
                </div>"""
            html = f"""<!DOCTYPE html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
            <title>CareXpress BIZDEV Tracker Installer</title></head>
            <body style="font-family:sans-serif;background:#F8FAF9;color:#0F2A25;padding:24px;max-width:480px;margin:0 auto;">
            <h1 style="color:#005046;margin-bottom:4px;">CareXpress BIZDEV Tracker</h1>
            <p style="color:#5B6F6A;margin-top:0;">Harare Avenues Field Visit &amp; KPI Tracker</p>
            {links or '<p>No local APK found yet. Run tools/deploy_bluestacks.py first.</p>'}
            </body></html>"""
            self.wfile.write(html.encode("utf-8"))
        else:
            super().do_GET()


if __name__ == "__main__":
    ip = get_local_ip()
    print(f"Serving CareXpress BIZDEV APK installer at http://{ip}:{PORT}/")
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), ApkInstallerHandler) as httpd:
        httpd.serve_forever()
