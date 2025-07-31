#!/usr/bin/env python3
"""
nullable_metrics.py  –  Precision/Recall for inferred @Nullable annotations.

Usage:
    python nullable_metrics.py /path/to/original /path/to/modified
"""

from __future__ import annotations
import argparse, pathlib, javalang, re, sys
from collections import namedtuple

Hit = namedtuple("Hit", "file line col kind sig")

# ---------------------------------------------------------------------------
# 1.  Walk the tree and record every @Nullable occurrence
# ---------------------------------------------------------------------------
def collect_nullable(root: pathlib.Path) -> set[Hit]:
    hits: set[Hit] = set()

    for fpath in root.rglob("*.java"):
        try:
            with fpath.open(encoding="utf‑8") as fp:
                code = fp.read()
            tree = javalang.parse.parse(code)
        except (javalang.parser.JavaSyntaxError, UnicodeDecodeError) as e:
            print(f"[warn] Skipping {fpath}: {e}", file=sys.stderr)
            continue

        pkg = tree.package.name if tree.package else ""
        imports = {imp.path.split(".")[-1]: imp.path for imp in tree.imports}

        # Helper ------------------------------------------------------------
        def ann_names(ann_list):
            return {a.name.split(".")[-1] for a in (ann_list or [])}

        # Traverse ----------------------------------------------------------
        for _, node in tree:
            # ------------- fields -----------------------------------------
            if isinstance(node, javalang.tree.FieldDeclaration):
                if "Nullable" in ann_names(node.annotations):
                    for decl in node.declarators:
                        pos = decl.position or node.position
                        hits.add(_mk_hit(fpath, pos, "field",
                                         f"{pkg}.{decl.name}"))

            # ------------- methods / ctors ---------------------------------
            elif isinstance(node, (javalang.tree.MethodDeclaration,
                                   javalang.tree.ConstructorDeclaration)):
                # ① annotations on the method itself
                meth_nullable = "Nullable" in ann_names(node.annotations)
                # ② annotations on the return‐type (only if that attr exists)
                rt_node = getattr(node, "return_type", None)
                rt_nullable = (
                    rt_node is not None
                    and hasattr(rt_node, "annotations")
                    and "Nullable" in ann_names(rt_node.annotations)
                )
                if meth_nullable or rt_nullable:
                    pos = (rt_node.position
                           if rt_nullable and rt_node.position
                           else node.position)
                    hits.add(_mk_hit(fpath, pos, "return",
                                     f"{pkg}.{node.name}()"))

                # nullable parameters
                for idx, param in enumerate(node.parameters):
                    if "Nullable" in ann_names(param.annotations):
                        pos = param.position or node.position
                        sig = f"{pkg}.{node.name}():param{idx}"
                        hits.add(_mk_hit(fpath, pos, "param", sig))

            # ------------- local variables (optional) ----------------------
            elif isinstance(node, javalang.tree.LocalVariableDeclaration):
                if "Nullable" in ann_names(node.annotations):
                    for decl in node.declarators:
                        pos = decl.position or node.position
                        sig = f"local:{decl.name}@{pos.line}"
                        hits.add(_mk_hit(fpath, pos, "local", sig))

    return hits


def _mk_hit(f: pathlib.Path, pos, kind, sig) -> Hit:
    return Hit(str(f.relative_to(f.anchor)), pos.line, pos.column or 0, kind, sig)


# ---------------------------------------------------------------------------
# 2.  Compare the two sets
# ---------------------------------------------------------------------------
def metrics(orig: set[Hit], mod: set[Hit]) -> tuple[set[Hit], set[Hit], set[Hit]]:
    """Return (TP‑set, FP‑set, FN‑set)."""
    tp = orig & mod         # true‑positives
    fp = mod - orig         # false‑positives  (inferred but not in truth)
    fn = orig - mod         # false‑negatives  (missed annotations)
    return tp, fp, fn


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("original")
    ap.add_argument("modified")
    args = ap.parse_args()

    orig_dir, mod_dir = map(pathlib.Path, (args.original, args.modified))
    if not (orig_dir.exists() and mod_dir.exists()):
        ap.error("Both project paths must exist.")

    print("Scanning ORIGINAL …")
    orig_hits = collect_nullable(orig_dir)
    print(f"  found {len(orig_hits)} @Nullable occurrences")

    print("Scanning MODIFIED …")
    mod_hits = collect_nullable(mod_dir)
    print(f"  found {len(mod_hits)} @Nullable occurrences")

    tp_set, fp_set, fn_set = metrics(orig_hits, mod_hits)

    # sizes for the report
    tp, fp, fn = map(len, (tp_set, fp_set, fn_set))
    prec = tp / (tp + fp) if tp + fp else 0.0
    rec  = tp / (tp + fn) if tp + fn else 0.0

    print("\n=== Precision / Recall Report ===")
    print(f"True positives : {tp}")
    print(f"False positives: {fp}")
    print(f"False negatives: {fn}")
    print(f"Precision      : {prec:6.3%}")
    print(f"Recall         : {rec:6.3%}")

    # Optional: dump mismatches for manual inspection
    if fp or fn:
        print("\n• False positives (only inferred):")
        for h in sorted(fp_set)[:20]:
            print(f"  {h.file}:{h.line}:{h.col}  {h.kind}  {h.sig}")
        print("• False negatives (missed true @Nullable):")
        for h in sorted(fn_set)[:20]:
            print(f"  {h.file}:{h.line}:{h.col}  {h.kind}  {h.sig}")

if __name__ == "__main__":
    main()
