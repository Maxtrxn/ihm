#!/usr/bin/env bash

# Fichier de sortie
OUTFILE="all_code.txt"

# Vide le fichier de sortie
> "$OUTFILE"

# Parcours tous les fichiers .java et .json (optionnel : tous les fichiers si tu préfères),
# en excluant .git et le fichier de sortie lui-même
find . \
  -type f \
  \( -name "*.java" -o -name "*.json" \) \
  ! -path "./.git/*" \
  ! -name "$OUTFILE" \
| sort \
| while read -r f; do
  echo "début fichier $f" >> "$OUTFILE"
  cat "$f"                     >> "$OUTFILE"
  echo "fin fichier $f"       >> "$OUTFILE"
  printf "\n%.0s" {1..5}      >> "$OUTFILE"
done

echo "Export terminé : consulte $OUTFILE"

