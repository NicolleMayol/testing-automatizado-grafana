package co.edu.udem.testing.grafana.auditoria;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/** Lee las cuentas de la organización con GET /api/org/users. */
public class ClienteGrafanaApi implements FuenteCuentas {

    public ClienteGrafanaApi(URI urlBase, String usuario, String clave, HttpClient http) {
        throw new UnsupportedOperationException("pendiente");
    }

    @Override
    public List<CuentaGrafana> obtenerCuentas() {
        throw new UnsupportedOperationException("pendiente");
    }

    static List<CuentaGrafana> parsearCuentas(String json) {
        throw new UnsupportedOperationException("pendiente");
    }
}
