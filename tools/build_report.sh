#!/bin/sh
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_dir"
mkdir -p report/build
if command -v latexmk >/dev/null 2>&1; then
    (cd report && latexmk -pdf -halt-on-error -interaction=nonstopmode -outdir=build Report.tex)
else
    docker run --rm --network none -v "$project_dir:/project" -w /project/report texlive/texlive:latest \
        latexmk -pdf -halt-on-error -interaction=nonstopmode -outdir=build Report.tex
fi
cp report/build/Report.pdf submission/Report.pdf
