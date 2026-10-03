package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class CuentaGrafanaTest {

    @Test
    void guardaLoginYRol() {
        CuentaGrafana cuenta = new CuentaGrafana("laura.guardia", Rol.VIEWER);
        assertEquals("laura.guardia", cuenta.login());
        assertEquals(Rol.VIEWER, cuenta.rol());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void exigeUnLogin(String login) {
        assertThrows(IllegalArgumentException.class, () -> new CuentaGrafana(login, Rol.VIEWER));
    }

    @Test
    void exigeUnRol() {
        assertThrows(NullPointerException.class, () -> new CuentaGrafana("laura.guardia", null));
    }
}
