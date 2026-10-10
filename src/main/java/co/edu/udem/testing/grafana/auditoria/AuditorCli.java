package co.edu.udem.testing.grafana.auditoria;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;

/** Punto de entrada: audita y devuelve un código de salida útil en un pipeline. */
public final class AuditorCli {

    public static final int APROBADA = 0;
    public static final int ERROR = 1;
    public static final int RECHAZADA = 2;

    private AuditorCli() {
    }

    public static int ejecutar(FuenteCuentas fuente, PoliticaAccesos politica, PrintStream salida) {
        try {
            List<Hallazgo> hallazgos = new AuditorRoles().auditar(fuente.obtenerCuentas(), politica);
            salida.println(ReporteAuditoria.comoTexto(hallazgos));
            return hallazgos.isEmpty() ? APROBADA : RECHAZADA;
        } catch (RuntimeException e) {
            salida.println("Error en la auditoría: " + e.getMessage());
            return ERROR;
        }
    }

    /**
     * Uso: java ... AuditorCli [politica.properties]
     * Variables: GRAFANA_URL, GRAFANA_USER, GRAFANA_PASSWORD.
     */
    public static void main(String[] args) throws IOException {
        Path archivoPolitica = Path.of(args.length > 0 ? args[0] : "politica-guardia.properties");
        FuenteCuentas fuente = new ClienteGrafanaApi(
                URI.create(System.getenv().getOrDefault("GRAFANA_URL", "http://localhost:3000")),
                System.getenv().getOrDefault("GRAFANA_USER", "admin"),
                System.getenv().getOrDefault("GRAFANA_PASSWORD", ""),
                HttpClient.newHttpClient());
        System.exit(ejecutar(fuente, PoliticaAccesos.desdeArchivo(archivoPolitica), System.out));
    }
}
