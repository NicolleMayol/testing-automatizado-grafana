package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("PoliticaAccesos: rol máximo por defecto y excepciones")
class PoliticaAccesosTest {

    @Test
    void sinExcepcionAplicaElRolPorDefecto() {
        PoliticaAccesos politica = new PoliticaAccesos(Rol.VIEWER, Map.of());
        assertEquals(Rol.VIEWER, politica.rolMaximoPara("laura.guardia"));
    }

    @Test
    void laExcepcionTienePrioridadSobreElRolPorDefecto() {
        PoliticaAccesos politica = new PoliticaAccesos(Rol.VIEWER, Map.of("andres.sre", Rol.EDITOR));
        assertEquals(Rol.EDITOR, politica.rolMaximoPara("andres.sre"));
        assertEquals(Rol.VIEWER, politica.rolMaximoPara("laura.guardia"));
    }

    @Test
    void noCambiaSiSeModificaElMapaOriginal() {
        Map<String, Rol> excepciones = new HashMap<>(Map.of("andres.sre", Rol.EDITOR));
        PoliticaAccesos politica = new PoliticaAccesos(Rol.VIEWER, excepciones);
        excepciones.put("laura.guardia", Rol.ADMIN);
        assertEquals(Rol.VIEWER, politica.rolMaximoPara("laura.guardia"));
    }

    @Test
    void exigeUnRolPorDefecto() {
        assertThrows(NullPointerException.class, () -> new PoliticaAccesos(null, Map.of()));
    }

    @Test
    void seLeeDesdePropiedades() {
        Properties p = new Properties();
        p.setProperty("rol.maximo.por.defecto", "Viewer");
        p.setProperty("excepcion.andres.sre", "Editor");
        p.setProperty("excepcion.admin", "Admin");
        p.setProperty("otra.clave", "se ignora");

        PoliticaAccesos politica = PoliticaAccesos.desdeProperties(p);

        assertEquals(Rol.VIEWER, politica.rolMaximoPara("laura.guardia"));
        assertEquals(Rol.EDITOR, politica.rolMaximoPara("andres.sre"));
        assertEquals(Rol.ADMIN, politica.rolMaximoPara("admin"));
    }

    @Test
    void sinRolPorDefectoLaPoliticaEsInvalida() {
        Properties p = new Properties();
        p.setProperty("excepcion.andres.sre", "Editor");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> PoliticaAccesos.desdeProperties(p));
        assertTrue(e.getMessage().contains("rol.maximo.por.defecto"), e.getMessage());
    }

    @Test
    void unRolInvalidoEnUnaExcepcionHaceFallarLaLectura() {
        Properties p = new Properties();
        p.setProperty("rol.maximo.por.defecto", "Viewer");
        p.setProperty("excepcion.andres.sre", "Root");
        assertThrows(IllegalArgumentException.class, () -> PoliticaAccesos.desdeProperties(p));
    }

    @Test
    void seLeeDesdeUnArchivo(@TempDir Path carpeta) throws IOException {
        Path archivo = carpeta.resolve("politica.properties");
        Files.writeString(archivo, """
                # política de prueba
                rol.maximo.por.defecto=Viewer
                excepcion.andres.sre=Editor
                """, StandardCharsets.UTF_8);

        PoliticaAccesos politica = PoliticaAccesos.desdeArchivo(archivo);

        assertEquals(Rol.EDITOR, politica.rolMaximoPara("andres.sre"));
    }
}
