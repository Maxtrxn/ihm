#!/usr/bin/env bash

# Fichier de sortie
OUTFILE="all_code.txt"

# Initialise (vide) le fichier de sortie
> "$OUTFILE"

# Parcours tous les .java, triés, et extrait leur contenu avec balises + 5 lignes vides
find . -type f -name "*.java" | sort | while read -r f; do
  echo "début fichier $f" >> "$OUTFILE"
  cat "$f" >> "$OUTFILE"
  echo "fin fichier $f" >> "$OUTFILE"
  # 5 retours à la ligne
  printf "\n%.0s" {1..5} >> "$OUTFILE"
done

echo "Export terminé : consulte $OUTFILE"
