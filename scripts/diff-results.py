#!/usr/bin/env python3
# Usage: ./diff-results.py <compare_output_dir>
# Example: ./diff-results.py /tmp/audiveris-compare/frelsi-eg-finn-ttbb

import sys
import json
from pathlib import Path


def load_report(path):
    if not path.exists():
        return None
    with open(path) as f:
        return json.load(f)


def diff_reports(with_ocr, without_ocr):
    all_categories = set(with_ocr.get("categories", {}).keys()) | set(
        without_ocr.get("categories", {}).keys()
    )

    print(
        f"{'Category':<12} {'With OCR':>12} {'Without OCR':>12} {'Δ Count':>10} {'Δ Grade':>10}"
    )
    print("-" * 58)

    for cat in sorted(all_categories):
        with_data = with_ocr.get("categories", {}).get(cat, {"count": 0, "avgGrade": 0})
        without_data = without_ocr.get("categories", {}).get(
            cat, {"count": 0, "avgGrade": 0}
        )

        w_count = with_data.get("count", 0)
        wo_count = without_data.get("count", 0)
        w_grade = with_data.get("avgGrade", 0)
        wo_grade = without_data.get("avgGrade", 0)

        delta_count = wo_count - w_count
        delta_grade = wo_grade - w_grade

        delta_str = f"+{delta_count}" if delta_count > 0 else str(delta_count)
        grade_str = f"+{delta_grade:.3f}" if delta_grade > 0 else f"{delta_grade:.3f}"

        print(f"{cat:<12} {w_count:>12} {wo_count:>12} {delta_str:>10} {grade_str:>10}")


def main():
    if len(sys.argv) < 2:
        print("Usage: diff-results.py <compare_output_dir>")
        sys.exit(1)

    output_dir = Path(sys.argv[1])

    with_ocr_reports = list((output_dir / "with-ocr").glob("*_report.json"))
    without_ocr_reports = list((output_dir / "without-ocr").glob("*_report.json"))

    if not with_ocr_reports or not without_ocr_reports:
        print("No report files found")
        sys.exit(1)

    for with_path in sorted(with_ocr_reports):
        sheet_name = with_path.name
        without_path = output_dir / "without-ocr" / sheet_name

        if not without_path.exists():
            continue

        print(f"\n=== {sheet_name} ===\n")

        with_ocr = load_report(with_path)
        without_ocr = load_report(without_path)

        if with_ocr and without_ocr:
            diff_reports(with_ocr, without_ocr)

    print("\n=== Visual Comparison ===")
    import shutil
    import subprocess

    compare_dir = output_dir / "compare"
    compare_dir.mkdir(exist_ok=True)

    for with_path in sorted((output_dir / "with-ocr").glob("*_final.png")):
        sheet_name = with_path.stem.replace("_final", "")
        without_path = output_dir / "without-ocr" / with_path.name
        if without_path.exists():
            with_labeled = compare_dir / f"{sheet_name}_1_WITH_OCR.png"
            without_labeled = compare_dir / f"{sheet_name}_2_WITHOUT_OCR.png"
            shutil.copy(with_path, with_labeled)
            shutil.copy(without_path, without_labeled)
            print(f"  Top:    {with_labeled.name}")
            print(f"  Bottom: {without_labeled.name}")
            subprocess.run(
                ["open", "-a", "Preview", str(with_labeled), str(without_labeled)]
            )


if __name__ == "__main__":
    main()
