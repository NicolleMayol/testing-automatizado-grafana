# language: es
@HU-SRE-02
Característica: Auditoría automática de roles contra la política de guardia
  Como líder del equipo SRE
  Quiero un reporte automático de las cuentas cuyo rol supera lo que permite la política de guardia
  Para corregir privilegios de más antes de que se conviertan en un riesgo o en un hallazgo de auditoría

  Criterios de aceptación:
    CA1. Si todas las cuentas respetan la política, la auditoría queda aprobada y no reporta hallazgos.
    CA2. Una cuenta con un rol mayor al permitido aparece en el reporte con su rol actual y su rol máximo, y la auditoría queda rechazada.
    CA3. Un rol Admin no autorizado se reporta con severidad ALTA; un rol Editor no autorizado, con severidad MEDIA.
    CA4. Las excepciones de la política permiten un rol mayor a cuentas concretas.

  Antecedentes:
    Dada la política de accesos de guardia:
      | cuenta      | rol máximo |
      | por defecto | Viewer     |
      | andres.sre  | Editor     |
      | admin       | Admin      |

  @CA1 @CA4 @api
  Escenario: Todas las cuentas respetan la política
    Dado que la organización tiene estas cuentas:
      | cuenta        | rol    |
      | laura.guardia | Viewer |
      | andres.sre    | Editor |
    Cuando se ejecuta la auditoría de roles
    Entonces la auditoría queda "aprobada"
    Y el reporte no tiene hallazgos

  @CA2 @CA3 @CA4 @api
  Esquema del escenario: Una ingeniera de guardia con privilegios de más
    Dado que la organización tiene estas cuentas:
      | cuenta        | rol    |
      | laura.guardia | <rol>  |
      | andres.sre    | Editor |
    Cuando se ejecuta la auditoría de roles
    Entonces la auditoría queda "rechazada"
    Y el reporte incluye a "laura.guardia" con rol "<rol>", máximo "Viewer" y severidad "<severidad>"
    Y el reporte no incluye a "andres.sre"

    Ejemplos:
      | rol    | severidad |
      | Editor | MEDIA     |
      | Admin  | ALTA      |
