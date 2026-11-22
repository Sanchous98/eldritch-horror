#!/usr/bin/env python3
"""Static audit of the authored city plans (no Minecraft classpath needed).

Checks: 24 plan files, unique city ids, halfExtent 340, registry 24/24, required POIs
(QUEST_BOARD/SECRET/STATION), 3+ NPC anchors, POI bounds, and no layout RNG/chunk reads in code.
Run from the repo root: `python3 tools/city_plan_audit.py`.
"""
import glob
import os
import re
import sys

PLAN_DIR = "src/main/java/com/sanchous98/eldritchhorror/world/loc/city/plan"
SKIP = {"CityPlan.java", "CityPlanContext.java", "CityPlanSupport.java", "CityPlans.java"}


def main() -> int:
    files = [f for f in sorted(glob.glob(PLAN_DIR + "/*Plan.java"))
             if os.path.basename(f) not in SKIP]
    problems = []
    ids = []
    for path in files:
        name = os.path.basename(path)[:-5]
        text = open(path, encoding="utf-8").read()
        match = re.search(r'public String cityId\(\)\s*\{\s*return "([^"]+)"', text)
        if not match:
            problems.append(f"{name}: no cityId()")
            continue
        ids.append(match.group(1))
        half = re.search(r"public int halfExtent\(\)\s*\{\s*return (\w+)", text)
        if not half:
            problems.append(f"{name}: no halfExtent()")
        else:
            value = half.group(1)
            if value.isdigit():
                if int(value) != 340:
                    problems.append(f"{name}: halfExtent {value} != 340")
            else:
                const = re.search(rf"static final int {re.escape(value)} = (\d+)", text)
                if not const or int(const.group(1)) != 340:
                    problems.append(f"{name}: halfExtent {value} is not 340")
        pois = re.findall(
            r'new CityPoi\("([^"]+)",\s*CityPoi\.Kind\.(\w+),\s*(-?\d+),\s*(-?\d+),\s*(-?\d+)',
            text)
        if len(pois) < 6:
            problems.append(f"{name}: only {len(pois)} POIs")
        kinds = {kind for _, kind, _, _, _ in pois}
        for required in ("QUEST_BOARD", "SECRET", "STATION"):
            if required not in kinds:
                problems.append(f"{name}: missing {required}")
        if sum(1 for _, kind, _, _, _ in pois if kind == "NPC") < 3:
            problems.append(f"{name}: fewer than 3 NPC anchors")
        for poi_id, _, dx, _, dz in pois:
            if abs(int(dx)) > 340 or abs(int(dz)) > 340:
                problems.append(f"{name}: POI {poi_id} outside +/-340")
        for forbidden in ("RandomSource", "Math.random", "isReplaceable",
                          "chunkMinX", "chunkMinZ"):
            for line in text.splitlines():
                stripped = line.strip()
                if forbidden in line and not stripped.startswith(("*", "//")):
                    problems.append(f"{name}: forbidden {forbidden} in code")
                    break
    registry = open(os.path.join(PLAN_DIR, "CityPlans.java"), encoding="utf-8").read()
    registered = set(re.findall(r"register\(new (\w+Plan)\(\)\)", registry))
    classes = {os.path.basename(f)[:-5] for f in files}
    if len(files) != 24:
        problems.append(f"expected 24 plan files, found {len(files)}")
    if len(set(ids)) != len(ids):
        problems.append("duplicate cityId values")
    if registered != classes:
        problems.append(f"registry mismatch: missing {sorted(classes - registered)}, "
                        f"extra {sorted(registered - classes)}")
    for problem in problems:
        print("FAIL", problem)
    if problems:
        return 1
    print(f"OK: {len(files)} plans, {len(set(ids))} unique ids, registry {len(registered)}/24")
    return 0


if __name__ == "__main__":
    sys.exit(main())
