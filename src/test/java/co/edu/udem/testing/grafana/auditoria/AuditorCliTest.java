package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("AuditorCli: código de salida para el pipeline")
class AuditorCliTest {

    private final PoliticaAccesos politica = new PoliticaAccesos(Rol.VIEWER, Map.of("admin", Rol.ADMIN));
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private String texto() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void devuelveAprobadaSinHallazgos() {
        int codigo = AuditorCli.ejecutar(() -> List.of(new CuentaGrafana("admin", Rol.ADMIN)), politica, salida);
        assertEquals(AuditorCli.APROBADA, codigo);
        assertTrue(texto().contains("aprobada"), texto());
    }

    @Test
    void devuelveRechazadaConHallazgosEImprimeElReporte() {
        int codigo = AuditorCli.ejecutar(() -> List.of(new CuentaGrafana("laura.guardia", Rol.EDITOR)), politica, salida);
        assertEquals(AuditorCli.RECHAZADA, codigo);
        assertTrue(texto().contains("laura.guardia"), texto());
    }

    @Test
    void devuelveErrorSiNoPuedeLeerLasCuentas() {
        int codigo = AuditorCli.ejecutar(() -> {
            throw new IllegalStateException("Grafana respondió HTTP 401");
        }, politica, salida);
        assertEquals(AuditorCli.ERROR, codigo);
        assertTrue(texto().contains("HTTP 401"), texto());
    }
}
