package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("Rol: lectura desde Grafana y jerarquía de privilegios")
class RolTest {

    @ParameterizedTest(name = "\"{0}\" se lee como {1}")
    @CsvSource({"Viewer, VIEWER", "' editor ', EDITOR", "ADMIN, ADMIN", "None, NONE"})
    void leeElRolSinImportarMayusculasNiEspacios(String valor, Rol esperado) {
        assertEquals(esperado, Rol.desdeGrafana(valor));
    }

    @Test
    void rechazaUnRolDesconocidoYDiceCual() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Rol.desdeGrafana("Superuser"));
        assertTrue(e.getMessage().contains("Superuser"), e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaUnRolVacio(String valor) {
        assertThrows(IllegalArgumentException.class, () -> Rol.desdeGrafana(valor));
    }

    @ParameterizedTest(name = "{0} excede {1}: {2}")
    @CsvSource({
            "EDITOR, VIEWER, true",
            "ADMIN,  EDITOR, true",
            "VIEWER, VIEWER, false",
            "VIEWER, EDITOR, false",
            "NONE,   VIEWER, false"})
    void comparaSegunLaJerarquia(Rol actual, Rol maximo, boolean excede) {
        assertEquals(excede, actual.excede(maximo));
    }

    @Test
    void devuelveElNombreQueUsaGrafana() {
        assertEquals("Viewer", Rol.VIEWER.nombreGrafana());
        assertEquals("Admin", Rol.ADMIN.nombreGrafana());
    }
}
