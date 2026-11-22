import os
import sys
import time
import subprocess
import xml.etree.ElementTree as ET
from PIL import Image

def find_adb():
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    default_adb = os.path.join(local_app_data, "Android", "Sdk", "platform-tools", "adb.exe")
    if os.path.exists(default_adb):
        return default_adb
    return "adb"

ADB = find_adb()

def run_adb(args):
    cmd = [ADB] + args
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return res.stdout.strip()

def adb_screencap(dest_path):
    os.makedirs(os.path.dirname(os.path.abspath(dest_path)), exist_ok=True)
    remote_path = "/data/local/tmp/screen_tmp.png"
    run_adb(["shell", "screencap", "-p", remote_path])
    run_adb(["pull", remote_path, dest_path])
    run_adb(["shell", "rm", remote_path])
    if os.path.exists(dest_path):
        size = os.path.getsize(dest_path)
        print(f"Captured {os.path.basename(dest_path)} ({size} bytes)")
    else:
        print(f"FAILED to capture {dest_path}")

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])

def swipe(x1, y1, x2, y2, duration=300):
    run_adb(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

def dump_ui():
    remote_dump = "/data/local/tmp/window_dump.xml"
    run_adb(["shell", "uiautomator", "dump", remote_dump])
    tmp = "scripts/tmp_dump.xml"
    run_adb(["pull", remote_dump, tmp])
    if os.path.exists(tmp):
        try:
            tree = ET.parse(tmp)
            return tree.getroot()
        except:
            return None
    return None

def find_bounds_by_text(root, text):
    if root is None:
        return None
    for n in root.iter():
        node_text = n.attrib.get("text", "")
        node_desc = n.attrib.get("content-desc", "")
        if text.lower() in node_text.lower() or text.lower() in node_desc.lower():
            bounds = n.attrib.get("bounds", "")
            if bounds.startswith("["):
                parts = bounds.replace("][", ",").replace("[", "").replace("]", "").split(",")
                if len(parts) == 4:
                    x1, y1, x2, y2 = map(int, parts)
                    return (x1 + x2) // 2, (y1 + y2) // 2
    return None

def main():
    print(f"Using ADB: {ADB}")
    out_dir = "docs/screenshots"
    os.makedirs(out_dir, exist_ok=True)

    # Clean stale activity states and start fresh
    run_adb(["shell", "am", "force-stop", "com.scrollthegita"])
    run_adb(["shell", "am", "force-stop", "com.scrollthegita.test"])
    run_adb(["shell", "wm", "dismiss-keyguard"])
    run_adb(["shell", "input", "keyevent", "82"])
    time.sleep(1)

    print("Launching MainActivity...")
    run_adb(["shell", "am", "start", "-n", "com.scrollthegita/.app.MainActivity"])
    time.sleep(2.5)

    # 1. Handle onboarding if displayed
    for _ in range(5):
        root = dump_ui()
        btn = find_bounds_by_text(root, "Continue") or find_bounds_by_text(root, "Begin Reading")
        if btn:
            print(f"Advancing onboarding at {btn}...")
            tap(btn[0], btn[1])
            time.sleep(1.5)
        else:
            break

    time.sleep(2)

    # 2. Capture 01_feed_dark_karma.png (Feed, Dark Theme, Shloka 2.47)
    print("Capturing 01_feed_dark_karma.png...")
    adb_screencap(os.path.join(out_dir, "01_feed_dark_karma.png"))
    time.sleep(1)

    # 3. Capture 04_understand_shloka_sheet.png
    print("Opening Understand This Shloka...")
    root = dump_ui()
    understand_btn = find_bounds_by_text(root, "Understand this shloka") or (540, 2100)
    tap(understand_btn[0], understand_btn[1])
    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "04_understand_shloka_sheet.png"))
    # Dismiss bottom sheet
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(1.5)

    # 4. Open Menu -> Journeys
    print("Opening Journeys...")
    root = dump_ui()
    menu_btn = find_bounds_by_text(root, "Open menu") or (980, 180)
    tap(menu_btn[0], menu_btn[1])
    time.sleep(1.5)
    root = dump_ui()
    journeys_opt = find_bounds_by_text(root, "Reading journeys") or (540, 1850)
    tap(journeys_opt[0], journeys_opt[1])
    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "05_curated_journeys.png"))
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(1.5)

    # 5. Open Menu -> Saved Shlokas
    print("Opening Saved library...")
    root = dump_ui()
    menu_btn = find_bounds_by_text(root, "Open menu") or (980, 180)
    tap(menu_btn[0], menu_btn[1])
    time.sleep(1.5)
    root = dump_ui()
    saved_opt = find_bounds_by_text(root, "Saved shlokas") or (540, 1920)
    tap(saved_opt[0], saved_opt[1])
    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "07_saved_library.png"))
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(1.5)

    # 6. Open Menu -> Chapters -> Search
    print("Opening Search...")
    root = dump_ui()
    menu_btn = find_bounds_by_text(root, "Open menu") or (980, 180)
    tap(menu_btn[0], menu_btn[1])
    time.sleep(1.5)
    root = dump_ui()
    chapters_opt = find_bounds_by_text(root, "Chapters") or (540, 1780)
    tap(chapters_opt[0], chapters_opt[1])
    time.sleep(2)
    root = dump_ui()
    search_icon = find_bounds_by_text(root, "Search") or (980, 180)
    tap(search_icon[0], search_icon[1])
    time.sleep(1.5)
    run_adb(["shell", "input", "text", "karma"])
    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "06_offline_search.png"))
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(1)
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(1.5)

    # 7. Open Menu -> Settings
    print("Opening Settings...")
    root = dump_ui()
    menu_btn = find_bounds_by_text(root, "Open menu") or (980, 180)
    tap(menu_btn[0], menu_btn[1])
    time.sleep(1.5)
    root = dump_ui()
    settings_opt = find_bounds_by_text(root, "Settings") or (540, 2000)
    tap(settings_opt[0], settings_opt[1])
    time.sleep(2)

    # Set theme to Parchment
    root = dump_ui()
    parchment_btn = find_bounds_by_text(root, "Parchment")
    if parchment_btn:
        tap(parchment_btn[0], parchment_btn[1])
        time.sleep(1)

    # Scroll down to reveal ambient background style cards
    swipe(500, 1800, 500, 900, 400)
    time.sleep(1.5)
    adb_screencap(os.path.join(out_dir, "08_settings_ambient_backgrounds.png"))

    # Select Parchment grain style
    root = dump_ui()
    grain_btn = find_bounds_by_text(root, "Parchment grain")
    if grain_btn:
        tap(grain_btn[0], grain_btn[1])
        time.sleep(1)

    # Back to feed (now in Parchment theme)
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(2)

    # 8. Swipe to Shloka 2.48
    print("Swiping to Shloka 2.48...")
    swipe(500, 1800, 500, 500, 350)
    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "02_feed_parchment_grain.png"))

    # 9. Trigger Reflection Pause dialog
    print("Triggering reflection pause dialog...")
    for i in range(6):
        swipe(500, 1800, 500, 500, 280)
        time.sleep(1.4)

    time.sleep(2)
    adb_screencap(os.path.join(out_dir, "03_reflection_pause.png"))

    # Cleanup temp files
    if os.path.exists("scripts/tmp_dump.xml"):
        os.remove("scripts/tmp_dump.xml")

    print("\nCapture complete! Verifying images...")
    for filename in [
        "01_feed_dark_karma.png",
        "02_feed_parchment_grain.png",
        "03_reflection_pause.png",
        "04_understand_shloka_sheet.png",
        "05_curated_journeys.png",
        "06_offline_search.png",
        "07_saved_library.png",
        "08_settings_ambient_backgrounds.png",
    ]:
        filepath = os.path.join(out_dir, filename)
        if os.path.exists(filepath):
            img = Image.open(filepath)
            print(f"  {filename}: {img.size}, {os.path.getsize(filepath):,} bytes")
        else:
            print(f"  MISSING: {filename}")

if __name__ == "__main__":
    main()
