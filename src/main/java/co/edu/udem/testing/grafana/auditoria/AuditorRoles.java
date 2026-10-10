package co.edu.udem.testing.grafana.auditoria;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Compara las cuentas reales con la política y devuelve las que tienen privilegios de más. */
public class AuditorRoles {

    private static final Comparator<Hallazgo> ORDEN_REPORTE =
            Comparator.comparing(Hallazgo::severidad).thenComparing(Hallazgo::login);

    public List<Hallazgo> auditar(Collection<CuentaGrafana> cuentas, PoliticaAccesos politica) {
        return cuentas.stream()
                .map(c -> new Hallazgo(c.login(), c.rol(), politica.rolMaximoPara(c.login())))
                .filter(h -> h.rolActual().excede(h.rolMaximo()))
                .sorted(ORDEN_REPORTE)
                .toList();
    }
}
