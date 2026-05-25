# HopeCare - Sistema de Gestión Hospitalaria

HopeCare es una aplicación de escritorio desarrollada en Java con JavaFX y Swing para la gestión de clínicas y hospitales.

## Arquitectura del Proyecto

El proyecto sigue un enfoque híbrido donde la capa de presentación está organizada por características (Features) y los módulos de negocio utilizan diferentes patrones arquitectónicos según su complejidad.

### Capa de Presentación (JavaFX + Swing)

Organizada por **Feature-Based** (Por Característica), donde cada pantalla o módulo visual tiene su propio paquete con el controlador y su archivo FXML. Incluye también implementaciones Swing y consola para el módulo de citas.

### Capa de Negocio (Módulos)

Ubicada en `src/main/java/com/esperanza/hopecare/modules`. Cada módulo implementa una arquitectura distinta:

#### 1. Citas y Consultas (`citas_consultas`)
*   **Patrón**: **MVP (Model-View-Presenter)** con interfaces de vista.
*   **Estructura**: `model/` → `dao/` → `presenter/` → `view/` (interfaces + impl).
*   **Descripción**: Separa la lógica de negocio de la interfaz de usuario mediante las interfaces `ICitaView` e `IConsultaView`.
    *   **CitaPresenter**: Calcula horarios disponibles a partir de `HorarioAtencion` (día, hora_inicio, hora_fin, intervalo_minutos), filtra bloques ocupados y reserva citas. Publica `NuevaCitaEvent` en el `EventBus` al agendar.
    *   **ConsultaPresenter**: Carga citas en estado PROGRAMADA, registra la consulta (inserta en `consulta` y actualiza estado de `cita` a `ATENDIDA` en una transacción) y permite solicitar exámenes de laboratorio y recetar medicamentos con manejo transaccional explícito.
    *   **Implementaciones de vista**: `CitasController` (JavaFX), `CitasPanel` (Swing), `CitaConsoleView` (consola). El `ConsultaController` (JavaFX) es standalone con diálogos integrados para examen/receta.

#### 2. Dashboard (`dashboard`)
*   **Patrón**: **Event-Driven / Observer**.
*   **Descripción**: Se suscribe a `EventBus` para recibir `NuevaCitaEvent` y `NuevaFacturaEvent` y actualizar métricas en tiempo real con `Platform.runLater()`.

#### 3. Facturación (`facturacion`)
*   **Patrón**: **Arquitectura en Capas Tradicional**.
*   **Estructura**: Service → DAO → DTO.
*   **Descripción**: El servicio calcula montos y genera `FacturaDTO` (inmutable) en una transacción que inserta factura y detalles, marcando conceptos como facturados.

#### 4. Medicamentos y Laboratorio (`medicamentos_lab`)
*   **Patrón**: **Facade (Fachada)**.
*   **Descripción**: `GestionClinicaFacade` simplifica la interacción con farmacia y laboratorio, con validación de roles (FARMACIA, LABORATORIO).

#### 5. Pacientes y Médicos (`pacientes_medicos`)
*   **Patrón**: **Capa de Datos Simple (DAO/Model)**.
*   **Descripción**: CRUD básico con herencia desde `Persona` y transacción que guarda en `persona` y luego en `medico`/`paciente`.

## Tecnologías Utilizadas

*   **Java 17+**
*   **JavaFX 21** (Interfaces de usuario modernas)
*   **Swing** (Panel de citas alternativo)
*   **Maven** (Gestión de dependencias y construcción)
*   **SQLite** (Base de datos local embebida)
*   **SLF4J** (Logging opcional)

### Edición de citas (doble clic)

Haciendo doble clic en una fila de la tabla de citas se abre un diálogo que muestra la información completa de la cita y permite editar:
- **Médico**: reasignar a otro médico mediante un ComboBox.
- **Fecha y Horario**: cambiar la fecha (DatePicker) y hora (ComboBox con slots de 30 min).
- **Estado**: modificar entre PROGRAMADA, CANCELADA, ATENDIDA, NO_ASISTIO.
- **Guardar**: persiste los cambios vía `CitaDAO.actualizarCita()` y refresca la tabla.

## Base de Datos

El sistema utiliza SQLite. El archivo de base de datos se genera como `sisgeho.db` en la raíz del proyecto. Para inicializar las tablas, ejecutar el script `schema.sql` sobre la base generada.

La aplicación crea las tablas automáticamente al ejecutar `CrearBaseDatos` e inserta datos de prueba con `CargarDatosPrueba`.

En el diálogo de nueva cita, al seleccionar un médico el `DatePicker` restringe los días seleccionables a aquellos en los que el médico atiende (según la tabla `horario_atencion`). Los días no laborables aparecen deshabilitados visualmente.

### Formato de fechas en SQLite

La columna `fecha_hora` de la tabla `cita` almacena los valores como TEXT en formato `"yyyy-MM-dd HH:mm:ss"`. Esto es crítico para el correcto funcionamiento de las funciones de fecha de SQLite (`DATE()`) y la lectura desde Java. El `CitaDAO` usa `parseFechaHora()` que maneja múltiples formatos (espacio, ISO-8601 con 'T', y epoch millis como respaldo). Al insertar, siempre se escribe en el formato canónico `"yyyy-MM-dd HH:mm:ss"`.

**IMPORTANTE**: Si la base de datos fue creada con una versión anterior del código que usaba `setTimestamp()` (que almacenaba epoch millis), las citas NO se mostrarán en la tabla y se debe resetear la BD borrando `sisgeho.db` para que se regenere con el formato correcto.

##Claves
Para acceder como admin:
admin/admin123
Para acceder como recepcionista:
recep
recep123
Para acceder como medico:
medico
medico123