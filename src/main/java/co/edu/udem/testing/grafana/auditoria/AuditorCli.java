package co.edu.udem.testing.grafana.auditoria;

import java.io.PrintStream;

/** Punto de entrada: audita y devuelve un código de salida útil en un pipeline. */
public final class AuditorCli {

    public static final int APROBADA = 0;
    public static final int ERROR = 1;
    public static final int RECHAZADA = 2;

    private AuditorCli() {
    }

    public static int ejecutar(FuenteCuentas fuente, PoliticaAccesos politica, PrintStream salida) {
        throw new UnsupportedOperationException("pendiente");
    }
}
