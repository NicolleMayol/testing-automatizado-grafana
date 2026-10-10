package co.edu.udem.testing.grafana.auditoria;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Rol máximo permitido por defecto y excepciones por cuenta. */
public final class PoliticaAccesos {

    public static final String CLAVE_POR_DEFECTO = "rol.maximo.por.defecto";
    public static final String PREFIJO_EXCEPCION = "excepcion.";

    private final Rol rolMaximoPorDefecto;
    private final Map<String, Rol> excepciones;

    public PoliticaAccesos(Rol rolMaximoPorDefecto, Map<String, Rol> excepciones) {
        this.rolMaximoPorDefecto = Objects.requireNonNull(rolMaximoPorDefecto, "Falta el rol máximo por defecto");
        this.excepciones = Map.copyOf(excepciones);
    }

    public Rol rolMaximoPara(String login) {
        return excepciones.getOrDefault(login, rolMaximoPorDefecto);
    }

    public static PoliticaAccesos desdeProperties(Properties propiedades) {
        String porDefecto = propiedades.getProperty(CLAVE_POR_DEFECTO);
        if (porDefecto == null) {
            throw new IllegalArgumentException("La política debe definir " + CLAVE_POR_DEFECTO);
        }
        Map<String, Rol> excepciones = new HashMap<>();
        for (String clave : propiedades.stringPropertyNames()) {
            if (clave.startsWith(PREFIJO_EXCEPCION)) {
                String login = clave.substring(PREFIJO_EXCEPCION.length());
                excepciones.put(login, Rol.desdeGrafana(propiedades.getProperty(clave)));
            }
        }
        return new PoliticaAccesos(Rol.desdeGrafana(porDefecto), excepciones);
    }

    public static PoliticaAccesos desdeArchivo(Path archivo) throws IOException {
        Properties propiedades = new Properties();
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            propiedades.load(lector);
        }
        return desdeProperties(propiedades);
    }
}
