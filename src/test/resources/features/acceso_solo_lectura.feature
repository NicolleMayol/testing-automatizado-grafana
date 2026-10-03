# language: es
@HU-SRE-01
Característica: Acceso de solo lectura para ingenieros de guardia
  Como líder del equipo SRE
  Quiero dar a los ingenieros de guardia acceso de solo lectura a los tableros de Grafana
  Para que consulten el estado de los servicios durante un incidente sin riesgo de modificar los tableros por error

  Criterios de aceptación:
    CA1. Una persona con rol Viewer puede iniciar sesión y abrir el tablero del equipo.
    CA2. La opción de editar el tablero no aparece para el rol Viewer, pero sí para el rol Editor.
    CA3. Si una persona con rol Viewer intenta guardar cambios en el tablero, Grafana los rechaza y el tablero queda igual.
    CA4. Una persona con rol Viewer no puede crear cuentas de usuario.

  Antecedentes:
    Dado que existe el tablero "Guardia SRE - Servicios críticos"

  @CA1 @ui
  Escenario: La ingeniera de guardia consulta el tablero del equipo
    Dado que "laura.guardia" tiene el rol "Viewer"
    Cuando "laura.guardia" inicia sesión en Grafana
    Y abre el tablero "Guardia SRE - Servicios críticos"
    Entonces ve el tablero "Guardia SRE - Servicios críticos"

  @CA2 @ui
  Esquema del escenario: La opción de editar el tablero depende del rol
    Dado que "<usuario>" tiene el rol "<rol>"
    Cuando "<usuario>" inicia sesión en Grafana
    Y abre el tablero "Guardia SRE - Servicios críticos"
    Entonces la opción de editar el tablero está "<opción de editar>"

    Ejemplos:
      | usuario       | rol    | opción de editar |
      | laura.guardia | Viewer | oculta           |
      | andres.sre    | Editor | disponible       |

  @CA3 @api
  Escenario: Grafana rechaza que la ingeniera de guardia modifique el tablero
    Dado que "laura.guardia" tiene el rol "Viewer"
    Cuando "laura.guardia" intenta cambiar el título del tablero "Guardia SRE - Servicios críticos" por "Tablero modificado"
    Entonces Grafana rechaza la acción por falta de permisos
    Y el tablero "Guardia SRE - Servicios críticos" conserva su título

  @CA4 @api
  Escenario: La ingeniera de guardia no puede crear cuentas de usuario
    Dado que "laura.guardia" tiene el rol "Viewer"
    Cuando "laura.guardia" intenta crear la cuenta "intruso.qa"
    Entonces Grafana rechaza la acción por falta de permisos
    Y la cuenta "intruso.qa" no existe
