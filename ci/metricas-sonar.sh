#!/usr/bin/env bash
# Consulta SonarCloud y deja medidas, quality gate e issues como anotaciones y resumen del job.
set -uo pipefail
api="https://sonarcloud.io/api"
auth=(-u "${SONAR_TOKEN}:")
claves="alert_status,coverage,line_coverage,branch_coverage,tests,test_execution_time,bugs,vulnerabilities,security_hotspots,code_smells,sqale_index,sqale_debt_ratio,sqale_rating,reliability_rating,security_rating,duplicated_lines_density,ncloc"
medidas=$(curl -s "${auth[@]}" "${api}/measures/component?component=${SONAR_PROJECT}&metricKeys=${claves}" \
  | jq -r '[.component.measures[] | "\(.metric)=\(.value)"] | join(" | ")')
gate=$(curl -s "${auth[@]}" "${api}/qualitygates/project_status?projectKey=${SONAR_PROJECT}" \
  | jq -r '.projectStatus as $p | "estado=\($p.status) | " + ([$p.conditions[] | "\(.metricKey) \(.comparator) \(.errorThreshold): \(.actualValue) (\(.status))"] | join(" ; "))')
issues=$(curl -s "${auth[@]}" "${api}/issues/search?componentKeys=${SONAR_PROJECT}&resolved=false&ps=100" \
  | jq -r '.issues[] | "\(.type)|\(.severity)|\(.rule)|\(.component | sub("^[^:]*:"; "")):\(.line // "-")|\(.message)|\(.debt // "-")"')
hotspots=$(curl -s "${auth[@]}" "${api}/hotspots/search?projectKey=${SONAR_PROJECT}&ps=100" \
  | jq -r '.hotspots[]? | "HOTSPOT|\(.vulnerabilityProbability)|\(.ruleKey)|\(.component | sub("^[^:]*:"; "")):\(.line // "-")|\(.message)|-"')
echo "::notice title=Sonar medidas::${medidas}"
echo "::notice title=Sonar quality gate::${gate}"
todo=$(printf '%s\n%s\n' "$issues" "$hotspots" | sed '/^$/d')
cuerpo=$(printf '%s' "$todo" | awk 'BEGIN{ORS="%0A"} {print}')
echo "::notice title=Sonar issues ($(printf '%s\n' "$todo" | sed '/^$/d' | wc -l))::${cuerpo}"
{
  echo "### SonarCloud"
  echo ""
  echo "**Quality gate:** ${gate}"
  echo ""
  echo "**Medidas:** ${medidas}"
  echo ""
  echo "| Tipo | Severidad | Regla | Ubicación | Mensaje | Deuda |"
  echo "|---|---|---|---|---|---|"
  printf '%s\n' "$todo" | sed '/^$/d' | awk -F'|' '{printf "| %s | %s | %s | %s | %s | %s |\n", $1, $2, $3, $4, $5, $6}'
} >> "${GITHUB_STEP_SUMMARY:-/dev/stdout}"
