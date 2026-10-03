package co.edu.udem.testing.grafana.auditoria;

/** Una cuenta de la organización de Grafana con su rol actual. */
public record CuentaGrafana(String login, Rol rol) {

    public CuentaGrafana {
        throw new UnsupportedOperationException("pendiente");
    }
}
