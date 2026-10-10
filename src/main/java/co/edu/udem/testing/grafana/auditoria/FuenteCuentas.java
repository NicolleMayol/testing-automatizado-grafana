package co.edu.udem.testing.grafana.auditoria;

import java.util.List;

/** De dónde salen las cuentas a auditar (la API de Grafana en producción). */
@FunctionalInterface
public interface FuenteCuentas {
    List<CuentaGrafana> obtenerCuentas();
}
