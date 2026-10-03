package co.edu.udem.testing.grafana.auditoria;

/** Roles de organización de Grafana, ordenados de menor a mayor privilegio. */
public enum Rol {
    NONE, VIEWER, EDITOR, ADMIN;

    public static Rol desdeGrafana(String valor) {
        throw new UnsupportedOperationException("pendiente");
    }

    public boolean excede(Rol maximo) {
        throw new UnsupportedOperationException("pendiente");
    }

    public String nombreGrafana() {
        throw new UnsupportedOperationException("pendiente");
    }
}
