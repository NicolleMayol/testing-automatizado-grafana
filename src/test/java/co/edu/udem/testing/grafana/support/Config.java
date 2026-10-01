package co.edu.udem.testing.grafana.support;

/** Configuración de la suite, tomada de variables de entorno con valores por defecto locales. */
public final class Config {

    public static final String BASE_URL = env("BASE_URL", "http://localhost:3000");
    public static final String USER = env("GRAFANA_USER", "admin");
    public static final String PASSWORD = env("GRAFANA_PASSWORD", "UdemTesting2026");
    public static final boolean HEADLESS = Boolean.parseBoolean(env("HEADLESS", "true"));
    public static final double SLOW_MO = Double.parseDouble(env("SLOW_MO", "0"));

    private Config() {
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
