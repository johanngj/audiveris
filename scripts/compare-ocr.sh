#!/bin/bash
# Compare Audiveris transcription with and without OCR
# Usage: ./compare-ocr.sh <pdf_file> [sheets]
# Example: ./compare-ocr.sh /path/to/score.pdf 1

set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <pdf_file> [sheets]"
    echo "  sheets: page numbers to process (default: 1)"
    exit 1
fi

PDF="$1"
SHEETS="${2:-1}"
BASENAME=$(basename "$PDF" .pdf)
OUTPUT_DIR="/tmp/audiveris-compare/${BASENAME}"
AUDIVERIS="$(dirname "$0")/../app/build/install/app/bin/Audiveris"

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/with-ocr" "$OUTPUT_DIR/without-ocr"

echo "=== Transcribing WITH OCR ==="
"$AUDIVERIS" -batch \
    -sheets "$SHEETS" \
    -debug-images "$OUTPUT_DIR/with-ocr" \
    -export \
    -output "$OUTPUT_DIR/with-ocr" \
    -transcribe "$PDF" 2>&1 | tee "$OUTPUT_DIR/with-ocr/log.txt"

echo ""
echo "=== Transcribing WITHOUT OCR ==="
"$AUDIVERIS" -batch \
    -sheets "$SHEETS" \
    -constant org.audiveris.omr.text.tesseract.TesseractOCR.useOCR=false \
    -debug-images "$OUTPUT_DIR/without-ocr" \
    -export \
    -output "$OUTPUT_DIR/without-ocr" \
    -transcribe "$PDF" 2>&1 | tee "$OUTPUT_DIR/without-ocr/log.txt"

echo ""
echo "=== Results ==="
echo "Output directory: $OUTPUT_DIR"
echo ""
echo "With OCR:"
cat "$OUTPUT_DIR/with-ocr/${BASENAME}_01_report.json" 2>/dev/null || echo "No report found"
echo ""
echo "Without OCR:"
cat "$OUTPUT_DIR/without-ocr/${BASENAME}_01_report.json" 2>/dev/null || echo "No report found"
echo ""
echo "To compare visually:"
echo "  open $OUTPUT_DIR/with-ocr/${BASENAME}_01_final.png"
echo "  open $OUTPUT_DIR/without-ocr/${BASENAME}_01_final.png"
