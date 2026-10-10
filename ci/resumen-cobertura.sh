#!/usr/bin/env bash
# Publica cobertura JaCoCo y conteo de pruebas como resumen y anotación del job.
set -uo pipefail
etapa="${1:-pruebas}"
csv="target/site/jacoco/jacoco.csv"
tests=0; fallos=0; errores=0; omitidas=0
for f in target/surefire-reports/TEST-*.xml; do
  [ -f "$f" ] || continue
  linea=$(grep -m1 -o '<testsuite[^>]*>' "$f")
  t=$(echo "$linea" | grep -o ' tests="[0-9]*"' | grep -o '[0-9]*'); tests=$((tests + ${t:-0}))
  x=$(echo "$linea" | grep -o ' failures="[0-9]*"' | grep -o '[0-9]*'); fallos=$((fallos + ${x:-0}))
  x=$(echo "$linea" | grep -o ' errors="[0-9]*"' | grep -o '[0-9]*'); errores=$((errores + ${x:-0}))
  x=$(echo "$linea" | grep -o ' skipped="[0-9]*"' | grep -o '[0-9]*'); omitidas=$((omitidas + ${x:-0}))
done
if [ -f "$csv" ]; then
  read -r lm lc bm bc cl <<<"$(awk -F, 'NR>1 {lm+=$8; lc+=$9; bm+=$6; bc+=$7; c++} END {print lm, lc, bm, bc, c}' "$csv")"
  lp=$(awk -v m="$lm" -v c="$lc" 'BEGIN { if (m+c>0) printf "%.1f", 100*c/(m+c); else print "n/a" }')
  bp=$(awk -v m="$bm" -v c="$bc" 'BEGIN { if (m+c>0) printf "%.1f", 100*c/(m+c); else print "n/a" }')
  cov="líneas ${lp}% (${lc}/$((lm+lc))), ramas ${bp}% (${bc}/$((bm+bc))), ${cl} clases"
else
  cov="sin reporte JaCoCo"
fi
msg="Pruebas: ${tests} ejecutadas, ${fallos} fallos, ${errores} errores, ${omitidas} omitidas | Cobertura auditor: ${cov}"
echo "::notice title=${etapa}::${msg}"
# Cada prueba fallida como anotación (los logs del job no siempre están a mano).
for f in target/surefire-reports/TEST-*.xml; do
  [ -f "$f" ] || continue
  python3 - "$f" <<'PY'
import sys, xml.etree.ElementTree as ET
for tc in ET.parse(sys.argv[1]).getroot().iter("testcase"):
    for tipo in ("failure", "error"):
        for e in tc.findall(tipo):
            msg = (e.get("message") or e.text or "").strip().replace("\n", " ")[:600]
            print(f"::error title={tipo}: {tc.get('name','')[:80]}::{tc.get('classname','')} - {msg}")
PY
done
{
  echo "### ${etapa}"
  echo ""
  echo "| Pruebas | Fallos | Errores | Omitidas | Cobertura (paquete auditoria) |"
  echo "|---|---|---|---|---|"
  echo "| ${tests} | ${fallos} | ${errores} | ${omitidas} | ${cov} |"
  if [ -f "$csv" ]; then
    echo ""
    echo "| Clase | Líneas cubiertas | Ramas cubiertas |"
    echo "|---|---|---|"
    awk -F, 'NR>1 {printf "| %s | %d/%d | %d/%d |\n", $3, $9, $8+$9, $7, $6+$7}' "$csv"
  fi
} >> "${GITHUB_STEP_SUMMARY:-/dev/stdout}"
