# HopeCare - Guía para Agentes IA

## Stack Tecnológico
- Java 11, JavaFX 21, Maven, SQLite
- Dependencias principales: sqlite-jdbc, javafx-controls, javafx-fxml

## Arquitectura
- **Presentación**: JavaFX + FXML, organización por Feature (cada módulo tiene su view/)
- **Negocio**: Services, Presenters (MVP en citas), Facade (medicamentos_lab), EventBus (dashboard)
- **Persistencia**: DAOs con SQLite, conexiones vía `DatabaseConnection.getConnection()`
- **Navegación**: Header superior con Hyperlinks + TabPane oculto (selección por índice)

## Estilo Visual
- Paleta de colores: Teal (`#0d9488`, `#115e59`, `#0f766e`) + Slate (`#f8fafc`, `#64748b`, `#0f172a`)
- Cards con bordes redondeados (12px), sombra sutil, fondo blanco
- Tablas con headers en `#f1f5f9`, filas alternadas
- CSS centralizado: `src/main/resources/com/esperanza/hopecare/main/hopecare.css`

## Convenciones de Código
- No añadir comentarios a menos que se solicite
- Seguir patrones existentes (cada DAO acepta `Connection` para operaciones transaccionales)
- Transacciones explícitas: `conn.setAutoCommit(false)`, commit/rollback en try-catch-finally
- Manejo de excepciones: catch SQLException, e.printStackTrace(), devolver false en DAOs
- Modelos con constructor vacío + constructor con campos + getters/setters

## Paquetes Clave
| Ruta | Propósito |
|------|-----------|
| `modules/citas_consultas/` | Models, DAOs, Presenters (MVP) para citas y consultas |
| `modules/pacientes_medicos/` | Models (Persona, Medico, Paciente, Especialidad) y DAOs para pacientes y médicos (MedicoDAO, PacienteDAO, EspecialidadDAO) |
| `modules/medicamentos_lab/` | Models, DAOs, Services, Facade para farmacia y laboratorio |
| `modules/facturacion/` | Service, DTOs, DAOs para facturación |
| `modules/dashboard/` | Observer pattern, DashboardDAO |
| `common/model/` | Persona (base class con nombre, apellido, documentoIdentidad) |
| `common/db/` | DatabaseConnection, CrearBaseDatos, CargarDatosPrueba |
| `common/events/` | EventBus, NuevaCitaEvent, NuevaFacturaEvent |
| `common/util/` | RoleValidator |
| `citas/view/` | CitasController.java (JavaFX, implementa ICitaView) + citas.fxml |
| `consulta/view/` | ConsultaController.java (standalone) + consulta.fxml |
| `ui/citas/` | CitasPanel.java (Swing, implementa ICitaView) |
| `*/view/` | Java Controllers + FXML files |

## Módulos Completos (Citas/Consultas + Farmacia + Laboratorio)

### Citas (MVP - ICitaView + CitaPresenter)
- **ICP**: `ICitaView.java` - interfaz con mostrarHorariosDisponibles(), getters de selección
- **Presenter**: `CitaPresenter.java` - genera bloques desde HorarioAtencion (intervalo_minutos), filtra ocupados vía CitaDAO, reserva y publica NuevaCitaEvent
- **JavaFX**: `CitasController.java` - dos TableViews (Pacientes y Médicos) con selección por fila + DatePicker + ComboBox. Implementa ICitaView. Carga datos desde `PacienteDAO.listarTodos()` y `MedicoDAO.listarTodos()`
- **Swing**: `CitasPanel.java` - implementación alternativa de ICitaView (usa TextFields)
- **Consola**: `CitaConsoleView.java` - implementación de prueba
- **DAOs**: `CitaDAO.java` (CRUD + filtrar por médico/fecha/estado + `obtenerCitasPorEstadoConNombres()`), `HorarioAtencionDAO.java` (obtener por médico+día), `PacienteDAO.listarTodos()`, `MedicoDAO.listarTodos()` (JOIN con persona y especialidad)

### Consultas (MVP - IConsultaView + ConsultaPresenter)
- **ICP**: `IConsultaView.java` - interfaz con métodos para cargar citas, formulario, solicitar examen (sin recetas - médicos escriben externamente)
- **Presenter**: `ConsultaPresenter.java` - carga citas PROGRAMADA, registra consulta (transacción INSERT consulta + UPDATE cita), solicita examen
- **JavaFX**: `ConsultaController.java` - standalone con ComboBox de citas pendientes (muestra nombres de paciente/médico), TextArea para síntomas/diagnóstico/tratamiento, diálogo modal para seleccionar examen
- **DAO**: `ConsultaDAO.java` - insertarConsultaYActualizarEstado() con commit/rollback explícito

### Farmacia (testeable 100%)
- **FXML**: `farmacia.fxml` - 2 cards (Inventario, Entregas Recientes) + form de entrega directa a paciente
- **Controller**: `FarmaciaController.java` - TableViews con CellFactory, ComboBoxes para seleccionar paciente y medicamento, diálogos para agregar/actualizar/eliminar
- **Service**: `InventarioService.java` - agregarMedicamento(), actualizarStock()
- **DAOs**: `EntregaMedicamentoDAO.java` (listarTodas, listarPorPaciente, listarPorMedicamento, eliminar), `MedicamentoDAO.java` (eliminar)
- **Modelo**: `EntregaMedicamento.java` - con campos pacienteNombre, medicamentoNombre para display en tabla
- **Cambios recientes**: Entregas vinculadas directamente a paciente (sin recetas), ComboBoxes con CellFactory para mostrar nombres completos, columna "Receta" muestra SÍ/NO según presente_receta

### Laboratorio (testeable 100%)
- **FXML**: `laboratorio.fxml` - 3 cards (Catálogo Exámenes, Solicitudes, Resultados) + form de registro
- **Controller**: `LaboratorioController.java` - TableViews con CellFactory, filtros por estado, diálogos para agregar/cancelar, nuevo formulario para crear solicitudes con ComboBoxes
- **Service**: `ExamenService.java` - agregarExamen(), listarSolicitudesPorEstado(), listarTodasSolicitudes()
- **DAOs**: `SolicitudExamenDAO.java` - listarTodas(), listarPorEstado(), cancelar()
- **Cambios recientes**: Solicitudes vinculadas directamente a paciente (sin consultas), muestra nombres de pacientes y exámenes en las tablas

### Funcionalidades de Cancelación
- **Farmacia**: Eliminar medicamento del inventario; Cancelar entrega (revierte stock)
- **Laboratorio**: Cancelar solicitud de examen (solo PENDIENTE)
- Todas incluyen diálogo de confirmación antes de ejecutar

## Base de Datos (Schema Completo)

### Schema Actual
- Archivo: `sisgeho.db` en raíz del proyecto
- Script DDL: `src/main/resources/sisgeho_schema.sql` (schema completo con todas las tablas)
- Datos de prueba: `CargarDatosPrueba.java` (5 pacientes, 3 médicos, 5 medicamentos, 5 exámenes, 10 citas)
- Driver: SQLite v3.45.1.0 (xerial sqlite-jdbc)

### Esquema de Tablas
```
USUARIOS Y ROLES:
  - rol (id_rol, nombre_rol, descripcion)
  - persona (id_persona, tipo_persona, nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero, id_usuario)
  - usuario (id_usuario, nombre_usuario, contrasena_hash, id_rol, activo, fecha_creacion)

REGISTRO:
  - especialidad (id_especialidad, nombre_especialidad, descripcion)
  - paciente (id_paciente, id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia, fecha_registro)
  - medico (id_medico, id_persona, id_especialidad, registro_medico, activo)

CITAS Y CONSULTAS:
  - horario_atencion (id_horario, id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo)
  - cita (id_cita, id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion)
  - consulta (id_consulta, id_cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta, facturado)

FARMACIA:
  - medicamento (id_medicamento, nombre_comercial, principio_activo, presentacion, concentracion, precio_unitario, stock_actual, stock_minimo, requiere_receta)
  - entrega_medicamento (id_entrega, id_paciente, id_medicamento, cantidad_entregada, presente_receta, fecha_entrega, entregado_por, facturado)

LABORATORIO:
  - examen_laboratorio (id_examen, nombre_examen, descripcion, precio, tiempo_resultado_horas, resultado_archivo)
  - solicitud_examen (id_solicitud, id_paciente, id_examen, fecha_solicitud, estado, resultado_texto, resultado_archivo, realizado_por, facturado)

FACTURACIÓN:
  - factura (id_factura, id_paciente, fecha_emision, subtotal, impuesto, total, estado_pago, forma_pago)
  - detalle_factura (id_detalle_factura, id_factura, concepto, id_referencia, tipo_referencia, monto)

AUDITORÍA:
  - bitacora_eventos (id_evento, id_usuario, tabla_afectada, id_registro, accion, fecha_hora, datos_antes, datos_despues)
```

### Columnas de Compatibilidad (stock_minimo, facturado, presente_receta)
- `medicamento.stock_minimo`: Mantenido para alertas de stock bajo en DashboardDAO, FarmaciaController, InventarioService
- `entrega_medicamento.facturado`: Mantenido para compatibilidad con módulo de facturación
- `entrega_medicamento.presente_receta`: Indica si el paciente presentó receta al recibir el medicamento
- `solicitud_examen.facturado`: Mantenido para compatibilidad con módulo de facturación

## Inicialización Automática de BD
La app crea y puebla la base de datos automáticamente al iniciar (`HopecareApp.init()`):
1. Verifica si la tabla `persona` existe
2. Si no existe → ejecuta `sisgeho_schema.sql` para crear todas las tablas
3. Si `persona` está vacía → inserta datos de prueba (5 pacientes, 3 médicos, 5 medicamentos, 5 exámenes, 10 citas, 4 usuarios, horarios, etc.)
4. Si ya hay datos → no hace nada

Para resetear la BD: borrar `sisgeho.db` de la raíz y reiniciar la app.

## Usuarios de Prueba
| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | admin123 | ADMIN |
| recepcion | recepcion123 | RECEPCION |
| farmacia | farmacia123 | FARMACIA |
| laboratorio | laboratorio123 | LABORATORIO |

## Testing
- Archivos de prueba: `src/main/java/com/esperanza/hopecare/test/`
- TestModuloMedicamentosLab.java - pruebas del módulo pharmacy/lab
- TestDatabase.java - pruebas de conexión a BD

## Comandos Útiles
```bash
# Compilar
mvn clean compile

# Ejecutar app (inicializa BD automáticamente)
mvn javafx:run

# Compilar con Maven directo
"C:\Program Files\Zulu\zulu-21\bin\javac" -version
```

## Navegación Principal
La navegación se maneja desde `MainController.java`:
- `main.fxml` usa un `TabPane` oculto con los 6 módulos
- El header superior con `Hyperlink` navega cambiando el índice del TabPane
- Breadcrumb se actualiza dinámicamente

## Sistema de Módulos (Java Platform Module System)
- `module-info.java` centraliza las configuraciones de módulos Java
- Los paquetes `opens` necesarios para JavaFX (`javafx.fxml` para controllers, `javafx.base` para modelos con PropertyValueFactory)
- Paquetes model abiertos a `javafx.base`: `medicamentos_lab.model`, `citas_consultas.model`, `pacientes_medicos.model`, `dashboard.model`, `Auth.model`
- Al agregar nuevos modelos usados en TableView con PropertyValueFactory, agregar `opens` correspondiente en `module-info.java`

## Pendientes Conocidos
- Módulos Dashboard, Registro, Facturación: estructura visual completa pero DAOs/Service pendientes
- Reportes (JasperReports)
- Pruebas unitarias JUnit 5
- JAR ejecutable con Maven Assembly Plugin

## Notas de la Versión Actual
- Schema modificado: tablas `receta` y `detalle_receta` eliminadas
- `entrega_medicamento` ahora referencia directamente `id_paciente` e `id_medicamento` (antes dependía de detalle_receta)
- `solicitud_examen` ahora referencia directamente `id_paciente` (antes dependía de id_consulta)
- Columna `presente_receta` agregada a `entrega_medicamento` para indicar si el paciente presentó receta
- Modelos `EntregaMedicamento` y `SolicitudExamen` ahora incluyen campos de nombres (`pacienteNombreCompleto`, `medicamentoNombre`, `examenNombre`)
- DAOs reescritos con JOINs a persona/medicamento/examen_laboratorio para obtener nombres
- FarmaciaController ahora usa ComboBoxes con CellFactory para seleccionar paciente y medicamento
- Columna "Receta" en tabla de entregas muestra SÍ (verde) / NO (rojo) según presente_receta
- LaboratorioController muestra nombres de pacientes y exámenes, nuevo formulario para crear solicitudes
- IMPORTANTE: Al actualizar, eliminar `sisgeho.db` para recrear con el nuevo esquema

## Estructura de Archivos Modificados
- `main.fxml` / `MainController.java` - Nueva navegación header
- `hopecare.css` - Paleta teal/slate completa
- `farmacia.fxml` / `FarmaciaController.java` - Completo + ComboBoxes con CellFactory + columna Receta SÍ/NO
- `laboratorio.fxml` / `LaboratorioController.java` - Completo + nombres de pacientes + nuevo form crear solicitudes
- `dashboard.fxml` - Estilo visual actualizado (lógica existente)
- `Auth/` - Login/Signup con autenticación SHA-256 y flujo completo
- `module-info.java` - opens para Auth.view
- `sisgeho_schema.sql` - Schema actualizado (entrega_medicamento ahora referencia id_paciente e id_medicamento directamente, eliminadas tablas receta y detalle_receta)
- `EntregaMedicamento.java` - Modelo actualizado con campos pacienteNombre, medicamentoNombre, presente_receta
- `EntregaMedicamentoDAO.java` - DAO reescrito para nuevo esquema con JOIN a persona para obtener nombres
- `SolicitudExamen.java` - Modelo con idPaciente (eliminado idConsulta)
- `SolicitudExamenDAO.java` - DAO reescrito con JOIN a persona/examen para nombres
- `ExamenService.java` - solicitadoExamen(int idPaciente, int idExamen)
- `GestionClinicaFacade.java` - procesarEntregaMedicamento(idPaciente, idMedicamento, cantidad, presenteReceta, rol)
- `ConsultaPresenter.java` / `IConsultaView.java` / `ConsultaController.java` - Sin funcionalidad de recetas
- `Persona.java` - Agregado getNombreCompleto()