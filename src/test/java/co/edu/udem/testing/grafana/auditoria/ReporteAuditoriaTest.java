package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class ReporteAuditoriaTest {

    @Test
    void sinHallazgosDiceQueLaAuditoriaEstaAprobada() {
        assertEquals("Auditoría aprobada: ninguna cuenta supera su rol máximo.",
                ReporteAuditoria.comoTexto(List.of()));
    }

    @Test
    void conHallazgosListaCadaCuentaConSuSeveridad() {
        String texto = ReporteAuditoria.comoTexto(List.of(
                new Hallazgo("mario.guardia", Rol.ADMIN, Rol.VIEWER),
                new Hallazgo("laura.guardia", Rol.EDITOR, Rol.VIEWER)));

        assertTrue(texto.startsWith("Auditoría rechazada: 2 cuenta(s) superan su rol máximo."), texto);
        assertTrue(texto.contains("- [ALTA] mario.guardia: rol Admin, máximo permitido Viewer"), texto);
        assertTrue(texto.contains("- [MEDIA] laura.guardia: rol Editor, máximo permitido Viewer"), texto);
    }
}
