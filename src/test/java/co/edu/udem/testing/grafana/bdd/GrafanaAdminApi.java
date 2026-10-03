package co.edu.udem.testing.grafana.bdd;

import co.edu.udem.testing.grafana.support.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Prepara los datos de prueba (tablero y cuentas) con la API HTTP de Grafana
 * usando la cuenta de administración del laboratorio.
 */
final class GrafanaAdminApi {

    static final String LAB_USER_PASSWORD = "GuardiaLab2026";

    private final Playwright playwright;
    private final APIRequestContext admin;

    GrafanaAdminApi(Playwright playwright) {
        this.playwright = playwright;
        this.admin = as(Config.USER, Config.PASSWORD);
    }

    /** Contexto HTTP autenticado con Basic Auth para cualquier cuenta. */
    APIRequestContext as(String login, String password) {
        String token = Base64.getEncoder()
                .encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));
        return playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(Config.BASE_URL)
                .setExtraHTTPHeaders(Map.of("Authorization", "Basic " + token)));
    }

    APIRequestContext asLabUser(String login) {
        return as(login, LAB_USER_PASSWORD);
    }

    /** Crea o reemplaza el tablero y devuelve su uid. */
    String ensureDashboard(String uid, String title) {
        APIResponse res = admin.post("/api/dashboards/db",
                RequestOptions.create().setData(dashboardPayload(uid, title)));
        check(res, 200, "crear el tablero " + title);
        return uid;
    }

    static Map<String, Object> dashboardPayload(String uid, String title) {
        Map<String, Object> panel = Map.of(
                "id", 1,
                "type", "text",
                "title", "Runbook de guardia",
                "gridPos", Map.of("x", 0, "y", 0, "w", 24, "h", 6),
                "options", Map.of("mode", "markdown",
                        "content", "Consulta este tablero durante la guardia. Escala al líder SRE si un servicio crítico falla."));
        Map<String, Object> dashboard = Map.of(
                "uid", uid,
                "title", title,
                "editable", true,
                "schemaVersion", 39,
                "panels", List.of(panel));
        return Map.of("dashboard", dashboard, "overwrite", true);
    }

    String dashboardTitle(String uid) {
        APIResponse res = admin.get("/api/dashboards/uid/" + uid);
        check(res, 200, "leer el tablero " + uid);
        return json(res).getAsJsonObject("dashboard").get("title").getAsString();
    }

    /** Deja la cuenta creada desde cero con el rol pedido en la organización. */
    void ensureUser(String login, String role) {
        deleteUserIfExists(login);
        APIResponse created = admin.post("/api/admin/users", RequestOptions.create().setData(Map.of(
                "name", login,
                "email", login + "@lab.local",
                "login", login,
                "password", LAB_USER_PASSWORD)));
        check(created, 200, "crear la cuenta " + login);
        int id = json(created).get("id").getAsInt();
        APIResponse patched = admin.patch("/api/org/users/" + id,
                RequestOptions.create().setData(Map.of("role", role)));
        check(patched, 200, "asignar el rol " + role + " a " + login);
    }

    boolean userExists(String login) {
        return admin.get("/api/users/lookup?loginOrEmail=" + login).status() == 200;
    }

    void deleteUserIfExists(String login) {
        APIResponse res = admin.get("/api/users/lookup?loginOrEmail=" + login);
        if (res.status() == 200) {
            int id = json(res).get("id").getAsInt();
            check(admin.delete("/api/admin/users/" + id), 200, "borrar la cuenta " + login);
        }
    }

    void dispose() {
        admin.dispose();
    }

    private static JsonObject json(APIResponse res) {
        return JsonParser.parseString(res.text()).getAsJsonObject();
    }

    private static void check(APIResponse res, int expected, String action) {
        if (res.status() != expected) {
            throw new IllegalStateException("No se pudo " + action + ": HTTP " + res.status() + " " + res.text());
        }
    }
}
