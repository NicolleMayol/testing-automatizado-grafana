package co.edu.udem.testing.grafana.auditoria;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Lee las cuentas de la organización con GET /api/org/users. */
public class ClienteGrafanaApi implements FuenteCuentas {

    private static final String RUTA_USUARIOS = "/api/org/users";

    private final URI urlUsuarios;
    private final String autorizacion;
    private final HttpClient http;

    public ClienteGrafanaApi(URI urlBase, String usuario, String clave, HttpClient http) {
        this.urlUsuarios = urlBase.resolve(RUTA_USUARIOS);
        this.autorizacion = "Basic " + Base64.getEncoder()
                .encodeToString((usuario + ":" + clave).getBytes(StandardCharsets.UTF_8));
        this.http = http;
    }

    @Override
    public List<CuentaGrafana> obtenerCuentas() {
        HttpRequest pedido = HttpRequest.newBuilder(urlUsuarios)
                .header("Authorization", autorizacion)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo consultar Grafana en " + urlUsuarios + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consulta a Grafana interrumpida", e);
        }
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("Grafana respondió HTTP " + respuesta.statusCode()
                    + " al listar usuarios: " + respuesta.body());
        }
        return parsearCuentas(respuesta.body());
    }

    static List<CuentaGrafana> parsearCuentas(String json) {
        JsonElement raiz = JsonParser.parseString(json);
        if (!raiz.isJsonArray()) {
            throw new IllegalStateException("Respuesta inesperada de Grafana: se esperaba una lista de usuarios");
        }
        List<CuentaGrafana> cuentas = new ArrayList<>();
        for (JsonElement elemento : raiz.getAsJsonArray()) {
            JsonObject usuario = elemento.getAsJsonObject();
            cuentas.add(new CuentaGrafana(usuario.get("login").getAsString(),
                    Rol.desdeGrafana(usuario.get("role").getAsString())));
        }
        return cuentas;
    }
}
