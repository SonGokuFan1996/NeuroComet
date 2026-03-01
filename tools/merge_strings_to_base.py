#!/usr/bin/env python3
"""Merge localized-only <string> keys into the base `values/strings.xml`.

Why:
Android requires that any key present in a localized resource also exists in the default locale.
If translators added extra keys to some `values-xx/strings.xml` but the base file never got them,
resource compilation fails.

What this does:
- Scans `app/src/main/res/values-*/strings.xml`.
- Collects any <string name="..."> keys missing from base `values/strings.xml`.
- Adds them to base with English placeholder text (from any locale's current text if available,
  otherwise the key name).

This keeps existing base strings untouched.
"""

from __future__ import annotations

import argparse
import glob
import os
import sys
import xml.etree.ElementTree as ET
from typing import Dict, List, Optional, Set, Tuple


def _indent(elem: ET.Element, level: int = 0) -> None:
    i = "\n" + level * "    "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "    "
        for child in elem:
            _indent(child, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


def parse(path: str) -> Tuple[ET.ElementTree, ET.Element, Dict[str, ET.Element]]:
    tree = ET.parse(path)
    root = tree.getroot()
    if root.tag != "resources":
        raise ValueError(f"Unexpected root tag in {path}: {root.tag}")
    items: Dict[str, ET.Element] = {}
    for child in root:
        if child.tag == "string" and "name" in child.attrib:
            items[child.attrib["name"]] = child
    return tree, root, items


def main(argv: List[str]) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--project", default=os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
    args = ap.parse_args(argv)

    project_root = os.path.abspath(args.project)
    res_dir = os.path.join(project_root, "app", "src", "main", "res")
    base_path = os.path.join(res_dir, "values", "strings.xml")

    if not os.path.exists(base_path):
        print(f"Base strings not found: {base_path}", file=sys.stderr)
        return 2

    base_tree, base_root, base_items = parse(base_path)

    locale_paths = sorted(glob.glob(os.path.join(res_dir, "values-*", "strings.xml")))

    missing: Set[str] = set()
    sample_text: Dict[str, str] = {}
    sample_translatable: Dict[str, str] = {}

    for p in locale_paths:
        _, r, items = parse(p)
        for name, el in items.items():
            if name not in base_items:
                missing.add(name)
                if name not in sample_text:
                    sample_text[name] = el.text or ""
                    if "translatable" in el.attrib:
                        sample_translatable[name] = el.attrib["translatable"]

    if not missing:
        print("No missing keys in base. Nothing to do.")
        return 0

    for name in sorted(missing):
        el = ET.Element("string", {"name": name})
        if name in sample_translatable:
            el.set("translatable", sample_translatable[name])
        text = sample_text.get(name)
        el.text = text if text is not None else name
        base_root.append(el)

    _indent(base_root)
    base_tree.write(base_path, encoding="utf-8", xml_declaration=True)

    print(f"Added {len(missing)} keys to base strings.xml")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))

