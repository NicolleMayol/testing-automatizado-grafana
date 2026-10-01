package co.edu.udem.testing.grafana.api;

import co.edu.udem.testing.grafana.support.Config;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Escenario E2 - API HTTP de Grafana")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HealthApiTest {

    private static Playwright playwright;
    private static APIRequestContext anonymous;
    private static APIRequestContext authenticated;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        anonymous = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(Config.BASE_URL));
        // Corrida 1: setHttpCredentials() falló (401). Playwright solo envía las credenciales
        // si el servidor responde 401 con "WWW-Authenticate", y la API de Grafana no lo envía.
        // Se envía el encabezado Basic Auth de forma explícita en cada petición.
        String token = Base64.getEncoder().encodeToString(
                (Config.USER + ":" + Config.PASSWORD).getBytes(StandardCharsets.UTF_8));
        authenticated = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(Config.BASE_URL)
                .setExtraHTTPHeaders(Map.of("Authorization", "Basic " + token)));
    }

    @AfterAll
    static void tearDown() {
        anonymous.dispose();
        authenticated.dispose();
        playwright.close();
    }

    @Test
    @Order(1)
    @DisplayName("TC-04 GET /api/health responde 200 y base de datos 'ok' (RF-04)")
    void healthOk() {
        APIResponse res = anonymous.get("/api/health");
        assertEquals(200, res.status());
        assertTrue(res.text().replace(" ", "").contains("\"database\":\"ok\""), res.text());
    }

    @Test
    @Order(2)
    @DisplayName("TC-05 GET /api/user sin credenciales responde 401 (RF-05)")
    void apiSinCredenciales() {
        assertEquals(401, anonymous.get("/api/user").status());
    }

    @Test
    @Order(3)
    @DisplayName("TC-06 GET /api/user con Basic Auth responde 200 y el usuario correcto (RF-05)")
    void apiConCredenciales() {
        APIResponse res = authenticated.get("/api/user");
        assertEquals(200, res.status());
        assertTrue(res.text().contains("\"login\":\"" + Config.USER + "\""), res.text());
    }
}
