package co.edu.udem.testing.grafana.auditoria;

import java.util.Arrays;

/** Roles de organización de Grafana, ordenados de menor a mayor privilegio. */
public enum Rol {
    NONE("None"), VIEWER("Viewer"), EDITOR("Editor"), ADMIN("Admin");

    private final String nombreGrafana;

    Rol(String nombreGrafana) {
        this.nombreGrafana = nombreGrafana;
    }

    /** Lee el rol tal como lo devuelve la API de Grafana ("Viewer", "Editor"...). */
    public static Rol desdeGrafana(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }
        String limpio = valor.trim();
        return Arrays.stream(values())
                .filter(r -> r.nombreGrafana.equalsIgnoreCase(limpio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol desconocido: " + valor));
    }

    /** true si este rol da más privilegios que el máximo permitido. */
    public boolean excede(Rol maximo) {
        return compareTo(maximo) > 0;
    }

    public String nombreGrafana() {
        return nombreGrafana;
    }
}
