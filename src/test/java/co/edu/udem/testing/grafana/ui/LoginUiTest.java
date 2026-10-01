package co.edu.udem.testing.grafana.ui;

import co.edu.udem.testing.grafana.pages.LoginPage;
import co.edu.udem.testing.grafana.support.BaseUiTest;
import co.edu.udem.testing.grafana.support.Config;
import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Escenario E1 - Autenticación web en Grafana")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginUiTest extends BaseUiTest {

    private static final Pattern LOGIN_URL = Pattern.compile(".*/login.*");

    @Test
    @Order(1)
    @DisplayName("TC-01 Login con credenciales válidas abre la página de inicio (RF-01)")
    void loginExitoso() {
        new LoginPage(page).open().loginAs(Config.USER, Config.PASSWORD);

        // Resultado esperado 1: sale de /login
        assertThat(page).not().hasURL(LOGIN_URL);

        // Resultado esperado 2: la sesión del navegador es válida en el backend
        APIResponse me = page.request().get("/api/user");
        assertEquals(200, me.status(), "La cookie de sesión debe autorizar /api/user");
        assertTrue(me.text().contains("\"login\":\"" + Config.USER + "\""), "Usuario autenticado incorrecto");
    }

    @ParameterizedTest(name = "TC-02.{index} {0}")
    @Order(2)
    @DisplayName("TC-02 Login con credenciales inválidas muestra 'Login failed' (RF-02)")
    @CsvSource({
            "usuario inexistente,        qa_no_existe,  CualquierClave1",
            "contraseña incorrecta,      admin,         ClaveIncorrecta9"
    })
    void loginFallido(String particion, String user, String pass) {
        LoginPage login = new LoginPage(page).open();
        login.loginAs(user, pass);

        assertThat(login.loginFailedAlert()).isVisible();
        assertThat(page).hasURL(LOGIN_URL);
    }

    @Test
    @Order(3)
    @DisplayName("TC-03 Enviar el formulario vacío muestra validaciones obligatorias (RF-03)")
    void loginCamposVacios() {
        LoginPage login = new LoginPage(page).open();
        login.submitEmpty();

        assertThat(login.fieldError("Email or username is required")).isVisible();
        assertThat(login.fieldError("Password is required")).isVisible();
        assertThat(page).hasURL(LOGIN_URL);
    }
}
