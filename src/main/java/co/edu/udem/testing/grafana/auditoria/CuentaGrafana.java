package co.edu.udem.testing.grafana.auditoria;

import java.util.Objects;

/** Una cuenta de la organización de Grafana con su rol actual. */
public record CuentaGrafana(String login, Rol rol) {

    public CuentaGrafana {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("La cuenta necesita un login");
        }
        Objects.requireNonNull(rol, "La cuenta necesita un rol");
    }
}
