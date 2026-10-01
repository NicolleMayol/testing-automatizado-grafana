package co.edu.udem.testing.grafana.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object de la pantalla /login de Grafana.
 * Los selectores son los data-testid públicos de @grafana/e2e-selectors (v11.3.0),
 * más estables que clases CSS o XPath.
 */
public class LoginPage {

    private final Page page;
    private final Locator username;
    private final Locator password;
    private final Locator submit;

    public LoginPage(Page page) {
        this.page = page;
        this.username = page.getByTestId("data-testid Username input field");
        this.password = page.getByTestId("data-testid Password input field");
        this.submit = page.getByTestId("data-testid Login button");
    }

    public LoginPage open() {
        page.navigate("/login");
        return this;
    }

    public void loginAs(String user, String pass) {
        username.fill(user);
        password.fill(pass);
        submit.click();
    }

    public void submitEmpty() {
        submit.click();
    }

    /** Alerta "Login failed" que Grafana muestra ante credenciales inválidas. */
    public Locator loginFailedAlert() {
        return page.getByRole(AriaRole.ALERT)
                .filter(new Locator.FilterOptions().setHasText("Login failed"));
    }

    public Locator fieldError(String text) {
        return page.getByText(text, new Page.GetByTextOptions().setExact(true));
    }
}
