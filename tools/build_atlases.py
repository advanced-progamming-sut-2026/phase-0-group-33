import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "assets")
ATLAS_DIR = os.path.join(ASSETS, "ATLASES")
RESOURCES = os.path.join(ASSETS, "RESOURCES.json")
RESOLUTION = "768"

HEADER = "\nsize: {w}, {h}\nformat: RGBA8888\nfilter: Linear, Linear\nrepeat: none\n"

REGION = (
    "{name}\n"
    "  rotate: false\n"
    "  xy: {x}, {y}\n"
    "  size: {w}, {h}\n"
    "  orig: {ow}, {oh}\n"
    "  offset: {ox}, {oy}\n"
    "  index: -1\n"
)


def page_name(path):
    return path.replace("\\", "/").split("/")[-1].upper()


def collect():
    with open(RESOURCES, encoding="utf-8") as handle:
        data = json.load(handle)

    pages = {}
    for group in data["groups"]:
        if group.get("type") != "simple" or group.get("res") != RESOLUTION:
            continue
        local = {}
        for res in group.get("resources", []):
            if res.get("atlas"):
                local[res["id"]] = page_name(res["path"])
                pages.setdefault(local[res["id"]], {
                    "width": res["width"],
                    "height": res["height"],
                    "regions": [],
                })
        for res in group.get("resources", []):
            parent = res.get("parent")
            if parent is None or parent not in local:
                continue
            page = pages[local[parent]]
            page["regions"].append({
                "name": res["id"],
                "x": res["ax"],
                "y": res["ay"],
                "w": res["aw"],
                "h": res["ah"],
                "ox": res.get("x", 0),
                "oy": res.get("y", 0),
            })
    return pages


def write_atlases(pages):
    written = 0
    regions = 0
    for name, page in sorted(pages.items()):
        png = os.path.join(ATLAS_DIR, name + ".PNG")
        if not os.path.exists(png):
            continue
        lines = ["", name + ".PNG", HEADER.format(w=page["width"], h=page["height"])]
        seen = set()
        for region in sorted(page["regions"], key=lambda r: r["name"]):
            if region["name"] in seen:
                continue
            seen.add(region["name"])
            regions += 1
            lines.append(REGION.format(
                name=region["name"],
                x=region["x"], y=region["y"],
                w=region["w"], h=region["h"],
                ow=region["w"], oh=region["h"],
                ox=0, oy=0,
            ))
        target = os.path.join(ATLAS_DIR, name + ".atlas")
        with open(target, "w", encoding="utf-8") as handle:
            handle.write("".join(lines))
        written += 1
    return written, regions


def main():
    written, regions = write_atlases(collect())
    print("atlases: %d   regions: %d" % (written, regions))
    return 0


if __name__ == "__main__":
    sys.exit(main())
