#!/usr/bin/env bash
# Ejecuta solo los escenarios BDD (Cucumber + Playwright) contra Grafana en Docker.
# Uso: ./run-bdd.sh            (velocidad normal)
#      SLOW_MO=800 ./run-bdd.sh (cámara lenta, para grabar evidencia)
set -euo pipefail
cd "$(dirname "$0")"
rm -rf target/cucumber target/evidence-bdd
docker compose up -d grafana
docker compose run --rm -T -e SLOW_MO="${SLOW_MO:-0}" tests \
  mvn -B -ntp test -Dtest=RunCucumberTest -Dsurefire.failIfNoSpecifiedTests=false
echo
echo "Reporte Cucumber: target/cucumber/reporte-bdd.html"
echo "Evidencia: target/evidence-bdd (videos y capturas por escenario)"
