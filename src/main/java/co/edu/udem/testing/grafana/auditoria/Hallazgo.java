package co.edu.udem.testing.grafana.auditoria;

/** Cuenta cuyo rol actual supera el máximo que permite la política. */
public record Hallazgo(String login, Rol rolActual, Rol rolMaximo) {

    public Severidad severidad() {
        throw new UnsupportedOperationException("pendiente");
    }
}
