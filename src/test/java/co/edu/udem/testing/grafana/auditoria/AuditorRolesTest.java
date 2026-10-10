package co.edu.udem.testing.grafana.auditoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("AuditorRoles: detecta cuentas con privilegios de más")
class AuditorRolesTest {

    private AuditorRoles auditor;
    private PoliticaAccesos politica;

    @BeforeEach
    void preparar() {
        auditor = new AuditorRoles();
        politica = new PoliticaAccesos(Rol.VIEWER, Map.of("andres.sre", Rol.EDITOR, "admin", Rol.ADMIN));
    }

    @Test
    void sinHallazgosCuandoTodasLasCuentasCumplen() {
        List<Hallazgo> hallazgos = auditor.auditar(List.of(
                new CuentaGrafana("laura.guardia", Rol.VIEWER),
                new CuentaGrafana("andres.sre", Rol.EDITOR),
                new CuentaGrafana("admin", Rol.ADMIN)), politica);
        assertTrue(hallazgos.isEmpty(), hallazgos::toString);
    }

    @Test
    void unRolMenorAlPermitidoNoEsHallazgo() {
        assertTrue(auditor.auditar(List.of(new CuentaGrafana("andres.sre", Rol.VIEWER)), politica).isEmpty());
    }

    @Test
    void reportaUnaCuentaQueSuperaSuRolMaximo() {
        List<Hallazgo> hallazgos = auditor.auditar(List.of(
                new CuentaGrafana("laura.guardia", Rol.EDITOR)), politica);

        assertEquals(List.of(new Hallazgo("laura.guardia", Rol.EDITOR, Rol.VIEWER)), hallazgos);
        assertEquals(Severidad.MEDIA, hallazgos.get(0).severidad());
    }

    @Test
    void unAdminNoAutorizadoEsSeveridadAlta() {
        Hallazgo hallazgo = auditor.auditar(List.of(new CuentaGrafana("laura.guardia", Rol.ADMIN)), politica).get(0);
        assertEquals(Severidad.ALTA, hallazgo.severidad());
    }

    @Test
    void ordenaPrimeroPorSeveridadYLuegoPorLogin() {
        List<Hallazgo> hallazgos = auditor.auditar(List.of(
                new CuentaGrafana("zoe.guardia", Rol.EDITOR),
                new CuentaGrafana("mario.guardia", Rol.ADMIN),
                new CuentaGrafana("ana.guardia", Rol.EDITOR)), politica);

        assertEquals(List.of("mario.guardia", "ana.guardia", "zoe.guardia"),
                hallazgos.stream().map(Hallazgo::login).toList());
    }

    @Test
    void sinCuentasNoHayHallazgos() {
        assertTrue(auditor.auditar(List.of(), politica).isEmpty());
    }
}
