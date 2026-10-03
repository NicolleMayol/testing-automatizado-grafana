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
                .filter(c -> c.rol().excede(politica.rolMaximoPara(c.login())))
                .map(c -> new Hallazgo(c.login(), c.rol(), politica.rolMaximoPara(c.login())))
                .sorted(ORDEN_REPORTE)
                .toList();
    }
}
