package co.edu.udem.testing.grafana.auditoria;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Rol máximo permitido por defecto y excepciones por cuenta. */
public final class PoliticaAccesos {

    public static final String CLAVE_POR_DEFECTO = "rol.maximo.por.defecto";
    public static final String PREFIJO_EXCEPCION = "excepcion.";

    public PoliticaAccesos(Rol rolMaximoPorDefecto, Map<String, Rol> excepciones) {
        throw new UnsupportedOperationException("pendiente");
    }

    public Rol rolMaximoPara(String login) {
        throw new UnsupportedOperationException("pendiente");
    }

    public static PoliticaAccesos desdeProperties(Properties propiedades) {
        throw new UnsupportedOperationException("pendiente");
    }

    public static PoliticaAccesos desdeArchivo(Path archivo) throws IOException {
        throw new UnsupportedOperationException("pendiente");
    }
}
