package co.edu.udem.testing.grafana.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ciclo de vida común de las pruebas de UI:
 * un navegador por clase y un contexto aislado (cookies limpias) por caso.
 * Cada caso deja evidencia: captura, video y traza de Playwright.
 */
public abstract class BaseUiTest {

    private static final Path EVIDENCE = Paths.get("target", "evidence");

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(Config.HEADLESS)
                .setSlowMo(Config.SLOW_MO));
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(Config.BASE_URL)
                .setLocale("en-US")
                .setViewportSize(1280, 800)
                .setRecordVideoDir(EVIDENCE.resolve("videos"))
                .setRecordVideoSize(1280, 800));
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));
        page = context.newPage();
    }

    @AfterEach
    void saveEvidence(TestInfo info) {
        String name = evidenceName(info);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(EVIDENCE.resolve("screenshots").resolve(name + ".png"))
                .setFullPage(true));
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(EVIDENCE.resolve("traces").resolve(name + ".zip")));
        Video video = page.video();
        context.close();
        if (video != null) {
            video.saveAs(EVIDENCE.resolve("videos").resolve(name + ".webm"));
            video.delete();
        }
    }

    /** "TC-02.1 usuario inexistente" -> "TC-02_1". */
    private static String evidenceName(TestInfo info) {
        return info.getDisplayName().split(" ")[0].replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
