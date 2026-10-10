package co.edu.udem.testing.grafana.auditoria;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("ClienteGrafanaApi: lectura de cuentas por HTTP")
class ClienteGrafanaApiTest {

    private static final String JSON_ORG_USERS = """
            [
              {"orgId":1,"userId":1,"login":"admin","email":"admin@localhost","role":"Admin"},
              {"orgId":1,"userId":2,"login":"laura.guardia","email":"laura@lab.local","role":"Viewer"}
            ]""";

    private HttpServer servidor;
    private final AtomicReference<String> autorizacionRecibida = new AtomicReference<>();
    private final AtomicReference<String> rutaRecibida = new AtomicReference<>();

    @BeforeEach
    void levantarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
    }

    @AfterEach
    void apagarServidor() {
        servidor.stop(0);
    }

    private void responder(int estado, String cuerpo) {
        servidor.createContext("/", intercambio -> {
            autorizacionRecibida.set(intercambio.getRequestHeaders().getFirst("Authorization"));
            rutaRecibida.set(intercambio.getRequestURI().getPath());
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(estado, bytes.length);
            try (OutputStream out = intercambio.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    private ClienteGrafanaApi cliente() {
        URI base = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort());
        return new ClienteGrafanaApi(base, "admin", "secreta", HttpClient.newHttpClient());
    }

    @Test
    void leeLasCuentasConBasicAuth() {
        responder(200, JSON_ORG_USERS);

        List<CuentaGrafana> cuentas = cliente().obtenerCuentas();

        assertEquals(List.of(new CuentaGrafana("admin", Rol.ADMIN),
                new CuentaGrafana("laura.guardia", Rol.VIEWER)), cuentas);
        assertEquals("/api/org/users", rutaRecibida.get());
        String esperado = "Basic " + Base64.getEncoder()
                .encodeToString("admin:secreta".getBytes(StandardCharsets.UTF_8));
        assertEquals(esperado, autorizacionRecibida.get());
    }

    @Test
    void fallaConUnMensajeClaroSiGrafanaRechazaLasCredenciales() {
        responder(401, "{\"message\":\"Unauthorized\"}");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> cliente().obtenerCuentas());
        assertTrue(e.getMessage().contains("401"), e.getMessage());
    }

    @Test
    void parseaElJsonDeGrafana() {
        assertEquals(2, ClienteGrafanaApi.parsearCuentas(JSON_ORG_USERS).size());
    }

    @Test
    void rechazaUnaRespuestaQueNoEsUnaLista() {
        assertThrows(IllegalStateException.class, () -> ClienteGrafanaApi.parsearCuentas("{\"message\":\"x\"}"));
    }

    @Test
    void fallaSiNoPuedeConectarse() {
        ClienteGrafanaApi sinServidor = new ClienteGrafanaApi(URI.create("http://127.0.0.1:1"),
                "admin", "secreta", HttpClient.newHttpClient());
        assertThrows(IllegalStateException.class, sinServidor::obtenerCuentas);
    }
}
