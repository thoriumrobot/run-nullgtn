#!/usr/bin/env python3
"""
nullable_pos_metrics.py  ── Precision/recall of @Nullable *positions*

USAGE
-----
    python nullable_pos_metrics.py  /path/to/original  /path/to/modified \
           [--line‑only]

OPTIONS
-------
    --line-only        Match on file + LINE instead of file + LINE:COL.
                       Helpful if your formatter shifts columns but not rows.

DEPENDENCIES
------------
    pip install javalang
"""

import argparse
from pathlib import Path
from typing import Set

import javalang


# ──────────────────────────────────────────────────────────────────────────
# Collector
# ──────────────────────────────────────────────────────────────────────────
def collect_nullable_positions(root: Path, line_only: bool) -> Set[str]:
    """
    Return a set of 'file:line[:column]' strings for every @Nullable
    annotation in the subtree rooted at *root*.
    """
    hits: Set[str] = set()

    for file in root.rglob("*.java"):
        rel_path = file.relative_to(root)
        try:
            tree = javalang.parse.parse(
                file.read_text(encoding="utf‑8", errors="ignore")
            )
        except (javalang.parser.JavaSyntaxError, IndexError):
            # javalang is lightweight and may choke on exotic syntax.
            continue

        for _, ann in tree.filter(javalang.tree.Annotation):
            # Accept javax.annotation.Nullable, org.jetbrains.annotations.Nullable, etc.
            if not ann.name.split(".")[-1].endswith("Nullable"):
                continue
            if ann.position is None:           # defensive: should rarely happen
                continue

            if line_only:
                key = f"{rel_path}:{ann.position.line}"
            else:
                key = f"{rel_path}:{ann.position.line}:{ann.position.column}"
            hits.add(key)

    return hits


# ──────────────────────────────────────────────────────────────────────────
# Metrics
# ──────────────────────────────────────────────────────────────────────────
def precision_recall_f1(truth: Set[str], predicted: Set[str]):
    tp = len(truth & predicted)
    fp = len(predicted - truth)
    fn = len(truth - predicted)

    prec = tp / (tp + fp) if tp + fp else 0.0
    rec  = tp / (tp + fn) if tp + fn else 0.0
    f1   = 2 * prec * rec / (prec + rec) if prec + rec else 0.0
    return tp, fp, fn, prec, rec, f1


def pretty_report(tp, fp, fn, prec, rec, f1, truth_cnt, pred_cnt, line_only):
    gran = "file + LINE" if line_only else "file + LINE:COL"
    print("\n==========  @Nullable position‑level evaluation  ==========")
    print(f" Matching granularity       : {gran}")
    print("-----------------------------------------------------------")
    print(f" Ground‑truth annotations   : {truth_cnt:>6}")
    print(f" Inferred annotations       : {pred_cnt:>6}")
    print(f" True Positives (TP)        : {tp:>6}")
    print(f" False Positives (FP)       : {fp:>6}")
    print(f" False Negatives (FN)       : {fn:>6}")
    print("-----------------------------------------------------------")
    print(f" Precision                  : {prec:.4f}")
    print(f" Recall                     : {rec:.4f}")
    print(f" F1‑score                   : {f1:.4f}")
    print("===========================================================\n")


# ──────────────────────────────────────────────────────────────────────────
# CLI
# ──────────────────────────────────────────────────────────────────────────
def main():
    ap = argparse.ArgumentParser(description="Compare @Nullable positions.")
    ap.add_argument("original", type=Path,
                    help="Path to *original* project (ground truth).")
    ap.add_argument("modified", type=Path,
                    help="Path to *modified* project (annotations inferred).")
    ap.add_argument("--line-only", action="store_true",
                    help="Ignore columns; match on file+line only.")
    args = ap.parse_args()

    truth_pos = collect_nullable_positions(args.original.resolve(), args.line_only)
    pred_pos  = collect_nullable_positions(args.modified.resolve(), args.line_only)

    metrics = precision_recall_f1(truth_pos, pred_pos)
    pretty_report(*metrics, truth_cnt=len(truth_pos),
                  pred_cnt=len(pred_pos), line_only=args.line_only)


if __name__ == "__main__":
    main()