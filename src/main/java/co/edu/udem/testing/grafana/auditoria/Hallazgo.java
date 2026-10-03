package co.edu.udem.testing.grafana.auditoria;

/** Cuenta cuyo rol actual supera el máximo que permite la política. */
public record Hallazgo(String login, Rol rolActual, Rol rolMaximo) {

    /** Un Admin no autorizado puede cambiar permisos y usuarios: por eso pesa más. */
    public Severidad severidad() {
        return rolActual == Rol.ADMIN ? Severidad.ALTA : Severidad.MEDIA;
    }
}
