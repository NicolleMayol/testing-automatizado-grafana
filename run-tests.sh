#!/usr/bin/env bash
# Levanta Grafana, espera a que esté sano y ejecuta la suite JUnit 5 + Playwright.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf target/evidence target/evidence-bdd target/cucumber
docker compose up -d grafana
docker compose run --rm -T tests
echo
echo "Evidencia: target/evidence, target/evidence-bdd y target/surefire-reports"
echo "Reporte Cucumber: target/cucumber/reporte-bdd.html"
