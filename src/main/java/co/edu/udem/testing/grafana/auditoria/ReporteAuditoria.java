package co.edu.udem.testing.grafana.auditoria;

import java.util.List;

/** Texto del reporte que lee el equipo. */
public final class ReporteAuditoria {

    private ReporteAuditoria() {
    }

    public static String comoTexto(List<Hallazgo> hallazgos) {
        if (hallazgos.isEmpty()) {
            return "Auditoría aprobada: ninguna cuenta supera su rol máximo.";
        }
        StringBuilder texto = new StringBuilder("Auditoría rechazada: ")
                .append(hallazgos.size()).append(" cuenta(s) superan su rol máximo.");
        for (Hallazgo h : hallazgos) {
            texto.append(System.lineSeparator())
                    .append("- [").append(h.severidad()).append("] ").append(h.login())
                    .append(": rol ").append(h.rolActual().nombreGrafana())
                    .append(", máximo permitido ").append(h.rolMaximo().nombreGrafana());
        }
        return texto.toString();
    }
}
