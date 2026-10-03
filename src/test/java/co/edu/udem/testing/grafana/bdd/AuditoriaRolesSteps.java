package co.edu.udem.testing.grafana.bdd;

import co.edu.udem.testing.grafana.auditoria.AuditorCli;
import co.edu.udem.testing.grafana.auditoria.AuditorRoles;
import co.edu.udem.testing.grafana.auditoria.ClienteGrafanaApi;
import co.edu.udem.testing.grafana.auditoria.Hallazgo;
import co.edu.udem.testing.grafana.auditoria.PoliticaAccesos;
import co.edu.udem.testing.grafana.support.Config;
import com.microsoft.playwright.Playwright;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Pasos de HU-SRE-02. Ejecutan el mismo código que validan las pruebas unitarias
 * (PoliticaAccesos, AuditorRoles, ClienteGrafanaApi, AuditorCli), pero contra Grafana real.
 */
public class AuditoriaRolesSteps {

    private static Playwright playwright;
    private static GrafanaAdminApi grafana;

    private Scenario scenario;
    private PoliticaAccesos politica;
    private int codigoSalida;
    private List<Hallazgo> hallazgos;

    @Before("@HU-SRE-02")
    public void start(Scenario scenario) {
        this.scenario = scenario;
        if (playwright == null) {
            playwright = Playwright.create();
            grafana = new GrafanaAdminApi(playwright);
        }
    }

    @AfterAll
    public static void close() {
        if (grafana != null) {
            grafana.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Dado("la política de accesos de guardia:")
    public void laPoliticaDeAccesos(DataTable tabla) {
        Properties propiedades = new Properties();
        for (Map<String, String> fila : tabla.asMaps()) {
            String cuenta = fila.get("cuenta");
            String clave = cuenta.equals("por defecto")
                    ? PoliticaAccesos.CLAVE_POR_DEFECTO
                    : PoliticaAccesos.PREFIJO_EXCEPCION + cuenta;
            propiedades.setProperty(clave, fila.get("rol máximo"));
        }
        politica = PoliticaAccesos.desdeProperties(propiedades);
    }

    @Dado("que la organización tiene estas cuentas:")
    public void queLaOrganizacionTieneEstasCuentas(DataTable tabla) {
        Map<String, String> cuentas = new LinkedHashMap<>();
        for (Map<String, String> fila : tabla.asMaps()) {
            cuentas.put(fila.get("cuenta"), fila.get("rol"));
        }
        grafana.syncOrgUsers(cuentas);
    }

    @Cuando("se ejecuta la auditoría de roles")
    public void seEjecutaLaAuditoria() {
        ClienteGrafanaApi cliente = new ClienteGrafanaApi(URI.create(Config.BASE_URL),
                Config.USER, Config.PASSWORD, HttpClient.newHttpClient());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        codigoSalida = AuditorCli.ejecutar(cliente, politica, new PrintStream(bytes, true, StandardCharsets.UTF_8));
        hallazgos = new AuditorRoles().auditar(cliente.obtenerCuentas(), politica);
        scenario.attach("Código de salida: " + codigoSalida + "\n" + bytes.toString(StandardCharsets.UTF_8),
                "text/plain", "Reporte de la auditoría");
    }

    @Entonces("la auditoría queda {string}")
    public void laAuditoriaQueda(String estado) {
        int esperado = switch (estado) {
            case "aprobada" -> AuditorCli.APROBADA;
            case "rechazada" -> AuditorCli.RECHAZADA;
            default -> throw new IllegalArgumentException("Estado no reconocido: " + estado);
        };
        assertEquals(esperado, codigoSalida, "Código de salida de la auditoría");
    }

    @Y("el reporte no tiene hallazgos")
    public void elReporteNoTieneHallazgos() {
        assertTrue(hallazgos.isEmpty(), "Hallazgos inesperados: " + hallazgos);
    }

    @Y("el reporte incluye a {string} con rol {string}, máximo {string} y severidad {string}")
    public void elReporteIncluye(String login, String rol, String maximo, String severidad) {
        Hallazgo h = hallazgos.stream().filter(x -> x.login().equals(login)).findFirst()
                .orElseGet(() -> fail("El reporte no incluye a " + login + ": " + hallazgos));
        assertEquals(rol, h.rolActual().nombreGrafana());
        assertEquals(maximo, h.rolMaximo().nombreGrafana());
        assertEquals(severidad, h.severidad().name());
    }

    @Y("el reporte no incluye a {string}")
    public void elReporteNoIncluye(String login) {
        assertTrue(hallazgos.stream().noneMatch(h -> h.login().equals(login)),
                login + " no debería aparecer en el reporte: " + hallazgos);
    }
}
