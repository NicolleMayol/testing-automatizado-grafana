package co.edu.udem.testing.grafana.bdd;

import co.edu.udem.testing.grafana.pages.LoginPage;
import co.edu.udem.testing.grafana.support.Config;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.options.RequestOptions;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Definiciones de pasos de la historia HU-SRE-01 (acceso de solo lectura).
 * Los pasos hablan el lenguaje del negocio; los detalles de UI y API quedan aquí.
 */
public class AccesoSoloLecturaSteps {

    private static final Path EVIDENCE = Paths.get("target", "evidence-bdd");
    /** uid estable por título, para que el tablero se pueda abrir por URL. */
    private static final Map<String, String> DASHBOARD_UIDS = Map.of(
            "Guardia SRE - Servicios críticos", "guardia-sre");
    private static final String EDIT_BUTTON = "data-testid Edit dashboard button";

    private static Playwright playwright;
    private static Browser browser;
    private static GrafanaAdminApi grafana;

    private Scenario scenario;
    private BrowserContext context;
    private Page page;
    private APIResponse lastResponse;
    private final Map<String, APIRequestContext> apiClients = new HashMap<>();

    // ---------- ciclo de vida ----------

    @Before
    public void start(Scenario scenario) {
        this.scenario = scenario;
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(Config.HEADLESS)
                    .setSlowMo(Config.SLOW_MO));
            grafana = new GrafanaAdminApi(playwright);
        }
    }

    @After
    public void finish(Scenario scenario) {
        String name = evidenceName(scenario);
        if (page != null) {
            byte[] png = page.screenshot(new Page.ScreenshotOptions()
                    .setPath(EVIDENCE.resolve("screenshots").resolve(name + ".png"))
                    .setFullPage(true));
            scenario.attach(png, "image/png", name);
            Video video = page.video();
            context.close();
            if (video != null) {
                video.saveAs(EVIDENCE.resolve("videos").resolve(name + ".webm"));
                video.delete();
            }
        }
        if (lastResponse != null) {
            scenario.attach("HTTP " + lastResponse.status() + "\n" + lastResponse.text(), "text/plain", "Respuesta de Grafana");
        }
        apiClients.values().forEach(APIRequestContext::dispose);
    }

    @AfterAll
    public static void closeBrowser() {
        if (grafana != null) {
            grafana.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    // ---------- Dado ----------

    @Dado("que existe el tablero {string}")
    public void queExisteElTablero(String title) {
        grafana.ensureDashboard(uidOf(title), title);
    }

    @Dado("que {string} tiene el rol {string}")
    public void queTieneElRol(String login, String role) {
        grafana.ensureUser(login, role);
    }

    // ---------- Cuando ----------

    @Cuando("{string} inicia sesión en Grafana")
    public void iniciaSesion(String login) {
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(Config.BASE_URL)
                .setLocale("en-US")
                .setViewportSize(1280, 800)
                .setRecordVideoDir(EVIDENCE.resolve("videos"))
                .setRecordVideoSize(1280, 800));
        page = context.newPage();
        new LoginPage(page).open().loginAs(login, GrafanaAdminApi.LAB_USER_PASSWORD);
        page.waitForURL(url -> !url.contains("/login"));
    }

    @Y("abre el tablero {string}")
    public void abreElTablero(String title) {
        page.navigate("/d/" + uidOf(title));
    }

    @Cuando("{string} intenta cambiar el título del tablero {string} por {string}")
    public void intentaCambiarTitulo(String login, String title, String newTitle) {
        lastResponse = api(login).post("/api/dashboards/db", RequestOptions.create()
                .setData(GrafanaAdminApi.dashboardPayload(uidOf(title), newTitle)));
    }

    @Cuando("{string} intenta crear la cuenta {string}")
    public void intentaCrearCuenta(String login, String newLogin) {
        grafana.deleteUserIfExists(newLogin);
        lastResponse = api(login).post("/api/admin/users", RequestOptions.create().setData(Map.of(
                "name", newLogin,
                "email", newLogin + "@lab.local",
                "login", newLogin,
                "password", GrafanaAdminApi.LAB_USER_PASSWORD)));
    }

    // ---------- Entonces ----------

    @Entonces("ve el tablero {string}")
    public void veElTablero(String title) {
        assertThat(page).hasTitle(Pattern.compile(Pattern.quote(title)));
        assertThat(page.getByText("Consulta este tablero durante la guardia")).isVisible();
    }

    @Entonces("la opción de editar el tablero está {string}")
    public void opcionDeEditar(String estado) {
        // Primero se confirma que el tablero cargó, para no evaluar una página a medio pintar.
        veElTablero(titleOf(page));
        Locator edit = page.getByTestId(EDIT_BUTTON);
        switch (estado) {
            case "disponible" -> assertThat(edit).isVisible();
            case "oculta" -> assertThat(edit).hasCount(0);
            default -> throw new IllegalArgumentException("Estado no reconocido: " + estado);
        }
    }

    @Entonces("Grafana rechaza la acción por falta de permisos")
    public void rechazaPorPermisos() {
        assertEquals(403, lastResponse.status(),
                "Se esperaba 403 Forbidden y llegó " + lastResponse.status() + ": " + lastResponse.text());
    }

    @Y("el tablero {string} conserva su título")
    public void conservaSuTitulo(String title) {
        assertEquals(title, grafana.dashboardTitle(uidOf(title)));
    }

    @Y("la cuenta {string} no existe")
    public void laCuentaNoExiste(String login) {
        assertFalse(grafana.userExists(login), "La cuenta " + login + " no debería existir");
    }

    // ---------- utilidades ----------

    private APIRequestContext api(String login) {
        return apiClients.computeIfAbsent(login, grafana::asLabUser);
    }

    private static String uidOf(String title) {
        String uid = DASHBOARD_UIDS.get(title);
        if (uid == null) {
            throw new IllegalArgumentException("Tablero sin uid configurado: " + title);
        }
        return uid;
    }

    /** Título del tablero abierto, deducido del uid de la URL actual. */
    private static String titleOf(Page page) {
        return DASHBOARD_UIDS.entrySet().stream()
                .filter(e -> page.url().contains("/d/" + e.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay un tablero abierto: " + page.url()));
    }

    private static String evidenceName(Scenario scenario) {
        String tag = scenario.getSourceTagNames().stream()
                .filter(t -> t.startsWith("@CA")).findFirst().orElse("@HU").substring(1);
        return (tag + "_linea" + scenario.getLine()).replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
