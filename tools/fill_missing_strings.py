#!/usr/bin/env python3
"""Fill missing Android string resources in localized strings.xml files.

Behavior:
- Uses `app/src/main/res/values/strings.xml` as the baseline.
- For each `values-*/strings.xml` (and optionally `values-en-r*`), ensures every baseline entry exists.
- Supports these resource types:
  - <string name="...">
  - <string-array name="..."> (keeps <item> structure)
  - <plurals name="..."> (keeps <item quantity="..."> structure)
- Missing keys are added by copying the English/base structure and text as a placeholder.
- Existing translations are preserved.
- Preserves key attributes from the baseline element (e.g. translatable="false", formatted="false").

Notes:
- This is a pragmatic stopgap so builds/lint won't fail. Real translations can be done later.
- ElementTree doesn't preserve comments/formatting from the original XML.
"""

from __future__ import annotations

import argparse
import glob
import os
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple


SUPPORTED_TAGS = {"string", "string-array", "plurals"}


@dataclass(frozen=True)
class BaselineEntry:
    tag: str
    name: str
    element: ET.Element  # clone source


def _indent(elem: ET.Element, level: int = 0) -> None:
    # Minimal pretty printer for ElementTree
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


def _key_for(child: ET.Element) -> Optional[Tuple[str, str]]:
    tag = child.tag
    if tag not in SUPPORTED_TAGS:
        return None
    name = child.attrib.get("name")
    if not name:
        return None
    return tag, name


def parse_resources_xml(path: str) -> Tuple[ET.ElementTree, ET.Element, Dict[Tuple[str, str], ET.Element]]:
    tree = ET.parse(path)
    root = tree.getroot()
    if root.tag != "resources":
        raise ValueError(f"Unexpected root tag in {path}: {root.tag}")

    existing: Dict[Tuple[str, str], ET.Element] = {}
    for child in root:
        k = _key_for(child)
        if k is None:
            continue
        existing[k] = child
    return tree, root, existing


def _deep_clone(elem: ET.Element) -> ET.Element:
    # ElementTree elements are mutable; clone so we can safely insert.
    return ET.fromstring(ET.tostring(elem, encoding="utf-8"))


def load_baseline(baseline_path: str) -> List[BaselineEntry]:
    _, root, _ = parse_resources_xml(baseline_path)
    items: List[BaselineEntry] = []
    for child in root:
        k = _key_for(child)
        if k is None:
            continue
        tag, name = k
        items.append(BaselineEntry(tag=tag, name=name, element=_deep_clone(child)))
    return items


def ensure_file(path: str) -> None:
    if os.path.exists(path):
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    root = ET.Element("resources")
    tree = ET.ElementTree(root)
    _indent(root)
    tree.write(path, encoding="utf-8", xml_declaration=True)


def fill_missing_in_file(target_path: str, baseline: List[BaselineEntry]) -> int:
    ensure_file(target_path)
    tree, root, existing = parse_resources_xml(target_path)

    added = 0
    for b in baseline:
        k = (b.tag, b.name)
        if k in existing:
            continue
        root.append(_deep_clone(b.element))
        added += 1

    if added:
        _indent(root)
        tree.write(target_path, encoding="utf-8", xml_declaration=True)

    return added


def find_locale_files(res_dir: str) -> List[str]:
    # values-xx/strings.xml plus existing english regional variants
    pattern = os.path.join(res_dir, "values-*", "strings.xml")
    return sorted(glob.glob(pattern))


def _self_test() -> int:
    # Tiny in-memory fixtures to verify string, array, plurals cloning.
    baseline_xml = """<?xml version='1.0' encoding='utf-8'?>
<resources>
  <string name='s1'>Hello</string>
  <string name='s2' translatable='false'>SECRET</string>
  <string-array name='a1'>
    <item>One</item>
    <item>Two</item>
  </string-array>
  <plurals name='p1'>
    <item quantity='one'>%d item</item>
    <item quantity='other'>%d items</item>
  </plurals>
</resources>
"""

    target_xml = """<?xml version='1.0' encoding='utf-8'?>
<resources>
  <string name='s1'>Hola</string>
</resources>
"""

    baseline_root = ET.fromstring(baseline_xml)
    baseline_entries: List[BaselineEntry] = []
    for child in baseline_root:
        k = _key_for(child)
        if k is None:
            continue
        tag, name = k
        baseline_entries.append(BaselineEntry(tag=tag, name=name, element=_deep_clone(child)))

    target_root = ET.fromstring(target_xml)
    existing: Dict[Tuple[str, str], ET.Element] = {}
    for child in target_root:
        k = _key_for(child)
        if k is not None:
            existing[k] = child

    added = 0
    for b in baseline_entries:
        k = (b.tag, b.name)
        if k in existing:
            continue
        target_root.append(_deep_clone(b.element))
        added += 1

    assert added == 3
    # Check that preserved attr exists
    s2 = next(e for e in target_root if e.tag == "string" and e.attrib.get("name") == "s2")
    assert s2.attrib.get("translatable") == "false"
    a1 = next(e for e in target_root if e.tag == "string-array" and e.attrib.get("name") == "a1")
    assert [i.text for i in a1.findall("item")] == ["One", "Two"]
    p1 = next(e for e in target_root if e.tag == "plurals" and e.attrib.get("name") == "p1")
    quantities = {i.attrib.get("quantity"): i.text for i in p1.findall("item")}
    assert quantities == {"one": "%d item", "other": "%d items"}
    return 0


def main(argv: List[str]) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--project", default=os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
    ap.add_argument("--res", default=None, help="Optional path to res folder")
    ap.add_argument("--include-english-regional", action="store_true")
    ap.add_argument("--self-test", action="store_true")
    args = ap.parse_args(argv)

    if args.self_test:
        return _self_test()

    project_root = os.path.abspath(args.project)
    res_dir = os.path.abspath(args.res) if args.res else os.path.join(project_root, "app", "src", "main", "res")
    baseline_path = os.path.join(res_dir, "values", "strings.xml")

    if not os.path.exists(baseline_path):
        print(f"Baseline strings not found: {baseline_path}", file=sys.stderr)
        return 2

    baseline = load_baseline(baseline_path)

    locale_files = find_locale_files(res_dir)

    total_added = 0
    for f in locale_files:
        folder = os.path.basename(os.path.dirname(f))
        is_english_regional = folder.startswith("values-en-")
        if is_english_regional and not args.include_english_regional:
            continue

        added = fill_missing_in_file(f, baseline)
        total_added += added
        if added:
            print(f"{folder}: added {added} entries")

    print(f"Done. Total added: {total_added}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
