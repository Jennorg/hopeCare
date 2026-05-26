# Módulo Citas Médicas - Guía para Agentes IA

## Stack Tecnológico
- Java 17+, JavaFX 21, Maven, SQLite
- Dependencias principales: sqlite-jdbc, javafx-controls, javafx-fxml

## Estilo Visual
- Paleta de colores: Teal (`#0d9488`, `#115e59`, `#0f766e`) + Slate (`#f8fafc`, `#64748b`, `#0f172a`)
- Cards con bordes redondeados (12px), sombra sutil, fondo blanco
- Tablas con headers en `#f1f5f9`, filas alternadas
- CSS centralizado: `src/main/resources/com/esperanza/hopecare/main/hopecare.css`
- Botón "Registrar Médico" y "Registrar Paciente" en teal (`#0d9488`)
- Botón "Cerrar Sesión" en teal con subrayado, cambia a `#115e59` en hover

## Convenciones de Código
- No añadir comentarios a menos que se solicite
- Seguir patrones existentes (cada DAO acepta `Connection` para operaciones transaccionales)
- Transacciones explícitas: `conn.setAutoCommit(false)`, commit/rollback en try-catch-finally
- Manejo de excepciones: catch SQLException, e.printStackTrace(), devolver false en DAOs
- Modelos con constructor vacío + constructor con campos + getters/setters

## Estructura del Proyecto

```
hopeCare/
├── pom.xml
├── module-info.java
├── hopecare_auth.db              → Auth (usuarios, roles)
├── hopecare_clinica.db           → Clinica (personas, pacientes, médicos)
├── hopecare_citas.db             → Citas (citas, consultas, horarios)
├── hopecare_facturacion.db       → Facturación
├── hopecare_dashboard.db         → Dashboard (métricas, pacientes registrados)
└── src/main/java/com/esperanza/hopecare/
    ├── HopecareApp.java               → Punto de entrada, inicializa BD y lanza JavaFX
    ├── controller/                     → Todos los controladores JavaFX
    │   ├── LoginController.java        → Login UI
    │   ├── MainController.java         → Layout principal (header + tabs)
    │   ├── CitasController.java        → Tabla de citas + botón "Mi Perfil" para médicos
    │   ├── ConsultaController.java     → Consultas
    │   ├── MedicosController.java      → CRUD médicos (solo ADMIN)
    │   ├── PacientesController.java    → CRUD pacientes (MEDICO no puede agregar/editar)
    │   ├── FacturacionController.java  → Facturación
    │   ├── DashboardController.java    → Dashboard
    │   ├── MedicoFormController.java   → Formulario médico (con modo perfil para médicos)
    │   ├── PacienteFormController.java → Formulario paciente
    │   └── SignupController.java       → Registro
    ├── dao/                            → Data Access Objects
    │   ├── AuthDAO.java
    │   ├── CitaDAO.java, ConsultaDAO.java, HorarioAtencionDAO.java
    │   ├── MedicoDAO.java, PacienteDAO.java, EspecialidadDAO.java
    │   ├── FacturacionDAO.java
    │   └── DashboardDAO.java
    ├── model/                          → Modelos/DTOs
    │   ├── LoginDTO.java, UsuarioModel.java
    │   ├── Cita.java, Consulta.java, HorarioAtencion.java
    │   ├── Medico.java, Paciente.java, Especialidad.java, Persona.java
    │   └── FacturaDTO.java
    ├── service/                        → Lógica de negocio
    │   ├── AuthService.java
    │   └── FacturacionService.java
    ├── util/                           → Utilidades
    │   ├── DatabaseConnection.java     → Conexiones a 5 bases SQLite
    │   ├── InicializarBD.java          → Ejecuta schemas
    │   ├── CargarDatosPrueba.java      → Datos de prueba
    │   ├── SesionManager.java          → Singleton: usuario logueado, rol, isMedico/isAdmin
    │   ├── Hasher.java                 → Hash SHA-256
    │   ├── EventBus.java, NuevaCitaEvent.java, NuevaFacturaEvent.java
    │   └── CargarDashboard.java
    └── resources/
        ├── auth_schema.sql, clinica_schema.sql, citas_schema.sql
        ├── facturacion_schema.sql, dashboard_schema.sql
        ├── com/esperanza/hopecare/view/  → FXML files
        └── css/hopecare.css
```

### Flujo de Arquitectura (MVP)

```
[Vista JavaFX] ←→ [Presenter] ←→ [DAO] ←→ [SQLite DB]
   (FXML+Controller)   (lógica)    (persistencia)
```

- **ICitaView / IConsultaView**: Contratos que el Presenter usa para hablar con la Vista
- **CitaPresenter / ConsultaPresenter**: Orquestan la lógica, llaman a los DAOs, avisan a la Vista
- **CitasController / ConsultaController**: Implementan las interfaces View, manejan eventos de UI
- Los Presenters **no conocen JavaFX**: toda la comunicación va por las interfaces View

## Paquetes Clave
| Ruta | Propósito |
|------|-----------|
| `controller/` | Todos los controladores JavaFX (Citas, Consultas, Médicos, Pacientes, Facturación, Dashboard) |
| `dao/` | DAOs para cada módulo (Auth, Cita, Consulta, Medico, Paciente, Facturación, Dashboard) |
| `model/` | Modelos de dominio (Cita, Medico, Paciente, Consulta, FacturaDTO, etc.) |
| `service/` | AuthService (login con roles), FacturacionService |
| `util/` | DatabaseConnection (5 DBs), SesionManager (isAdmin/isMedico/isRecepcionista), Hasher, EventBus |

## Módulos del Sistema

### Login (autenticación con roles)
- **LoginController.java**: Controlador JavaFX que autentica contra `usuario JOIN clinica.persona` vía AuthService
- **AuthDAO.java**: `autenticar(String usuario, String password)` con JOIN a persona y rol
- **LoginDTO.java**: Modelo con idUsuario, nombreUsuario, rol, nombreRol, idPersona
- **SesionManager.java**: Singleton con `isAdmin()`, `isMedico()`, `isRecepcionista()`, `getIdUsuario()`, `getIdPersona()`, `getRol()`
- Roles: ADMIN, RECEPCIONISTA, MEDICO (+ PACIENTE desde registro)
- Usuarios de prueba: `medico/medico123` (Carlos López, MEDICO), `admin/admin123` (ADMIN), `recep/recep123` (RECEPCIONISTA)

### Citas (MVP - ICitaView + CitaPresenter)
- **ICitaView.java**: Interfaz con `mostrarHorariosDisponibles()`, `mostrarCitasExistentes()`, `mostrarDiasDisponibles()` y getters de selección
- **CitaPresenter.java**: Genera bloques desde HorarioAtencion (intervalo_minutos), filtra ocupados vía CitaDAO, reserva y publica NuevaCitaEvent
- **CitasController.java**: Tabla principal de citas existentes + botón "Agendar Nueva Cita" (visible solo para ADMIN/RECEPCIONISTA/PACIENTE, NO para MEDICO) + botón "Mi Perfil" (solo visible para MEDICO) que abre el perfil del médico con solo el nombre editable vía `MedicoFormController.setProfileMode(true)`. El diálogo de nueva cita contiene: buscador de pacientes (con botón "Registrar Paciente"), filtro por especialidad + buscador de médicos (con botón "Registrar Médico"), selector de días disponibles, auto-asignación de fecha y horarios, botón de reserva
- **DAOs**: `CitaDAO.java` (CRUD + `listarTodasConNombres()` + `obtenerCitasPorEstadoConNombres()`), `HorarioAtencionDAO.java` (obtener por médico+día + `obtenerHorariosPorMedico()`)

### Consultas (MVP - IConsultaView + ConsultaPresenter)
- **IConsultaView.java**: Interfaz con inner class `RecetaRequest`
- **ConsultaPresenter.java**: Carga citas PROGRAMADA, registra consulta (transacción INSERT consulta + UPDATE cita)
- **ConsultaController.java**: ComboBox de citas pendientes (con nombres paciente/médico), TextAreas para síntomas/diagnóstico/tratamiento, botón guardar
- **DAO**: `ConsultaDAO.java` - `insertarConsultaYActualizarEstado()` con commit/rollback explícito

### Pacientes (soporte para citas)
- **PacienteFormController.java** + `paciente_form.fxml`: Formulario de registro de pacientes, usado desde el diálogo de "Registrar Paciente" en CitasController
- **PacienteDAO.java**: CRUD + `existeDocumento()`, `existeHistoriaClinica()`, `listarTodos()` (JOIN con persona)
- **Paciente.java**: Modelo sin campo `activo`

### Médicos (soporte para citas)
- **MedicoDAO.java**: CRUD + `insertarMedico()` (transacción: crea persona + medico), `existeDocumento()`, `existeRegistroMedico()`, `listarTodos()` (JOIN con persona y especialidad)
- **Medico.java**: Modelo con `nombreEspecialidad` para display
- **EspecialidadDAO.java**: `listarTodas()`

## Bases de Datos (5 archivos SQLite)

| Archivo | Propósito | Schema |
|---------|-----------|--------|
| `hopecare_auth.db` | Usuarios, roles, autenticación | `auth_schema.sql` |
| `hopecare_clinica.db` | Personas, pacientes, médicos, especialidades | `clinica_schema.sql` |
| `hopecare_citas.db` | Citas, consultas, horarios de atención | `citas_schema.sql` |
| `hopecare_facturacion.db` | Facturación | `facturacion_schema.sql` |
| `hopecare_dashboard.db` | Dashboard con métricas y pacientes registrados | `dashboard_schema.sql` |

Driver: SQLite v3.45.1.0 (xerial sqlite-jdbc)

### Visión de las Tablas y sus Relaciones

Base **clinica**:
```
persona (datos personales)
  ├── usuario   (1 a 1: auth.usuario via id_persona)
  ├── medico    (1 a 1: una persona puede ser 0 o 1 médico)
  └── paciente  (1 a 1: una persona puede ser 0 o 1 paciente)
especialidad ── medico (N a 1)
```

Base **citas**:
```
medico (id_medico referenciado desde clinica)
  ├── horario_atencion (1 a N)
  └── cita             (1 a N)
paciente (id_paciente referenciado desde clinica)
  └── cita (1 a N)
cita ── consulta (1 a 0..1)
```

Base **auth**:
```
rol ── usuario (N a 1)
usuario ── persona (N a 1, via id_persona a clinica.persona)
```

### Esquemas

**auth** (`usuario`, `rol`):
```
rol      (id_rol, nombre_rol)
usuario  (id_usuario, nombre_usuario, contrasena_hash, contrasena, id_rol → rol, id_persona, rol, fecha_creacion)
```

**clinica** (`persona`, `especialidad`, `paciente`, `medico`):
```
persona         (id_persona, nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero)
especialidad    (id_especialidad, nombre_especialidad)
paciente        (id_paciente, id_persona → persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia, fecha_registro, activo)
medico          (id_medico, id_persona → persona, id_especialidad → especialidad, registro_medico, precio_consulta, fecha_contratacion, activo)
```

**citas** (`cita`, `consulta`, `horario_atencion`):
```
cita              (id_cita, id_paciente → paciente, id_medico → medico, fecha_hora, estado, motivo, creada_por, fecha_creacion)
consulta          (id_consulta, id_cita → cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta)
horario_atencion  (id_horario, id_medico → medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo)
```

## Inicialización Automática de BD
La app crea y puebla las bases de datos automáticamente al iniciar (`HopecareApp.start()`):
1. Para cada módulo (Auth, Clinica, Citas, Facturacion, Dashboard): verifica si la tabla de control existe
2. Si no existe → ejecuta su schema correspondiente
3. Si las tablas Auth o Clinica están vacías → inserta datos de prueba (3 roles, 3 usuarios, 2 médicos con horarios, 1 paciente)
4. Si el Dashboard está vacío → lo puebla desde Clinica + Citas
Para resetear: borrar los 5 archivos `.db` de la raíz y reiniciar.

## Usuarios de Prueba
| Usuario | Contraseña | Rol | Persona asociada |
|---------|------------|-----|------------------|
| admin | admin123 | ADMIN | Admin Sistema |
| recep | recep123 | RECEPCIONISTA | Recep Sistema |
| medico | medico123 | MEDICO | Carlos López (id_medico=2, Medicina General) |

## Comandos Útiles
```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
$mvn = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"

# Compilar
& $mvn clean compile

# Ejecutar app (inicializa BD automáticamente)
& $mvn clean javafx:run
```

## Navegación Principal
- `main.fxml` usa un `TabPane` oculto con 2 tabs internos: "Agendar Cita" y "Registrar Consulta"
- `MainController.java` maneja la navegación y el cierre de sesión
- Header superior muestra el nombre del médico y un botón "Cerrar Sesión" en teal

## Sistema de Módulos (Java Platform Module System)
- `module-info.java` centraliza las configuraciones de módulos Java
- Paquetes abiertos a `javafx.fxml`: `main`, `modules.Auth.view`, `modules.citas_consultas.view`, `modules.pacientes_medicos.view`
- Paquetes model abiertos a `javafx.base`: `citas_consultas.model`, `pacientes_medicos.model`

## Pendientes Conocidos
- Pruebas unitarias adicionales
- Reportes (JasperReports)
- JAR ejecutable con Maven Assembly Plugin
- Permitir que MEDICO cree citas (actualmente oculto el botón "Nueva Cita")

## Últimos Cambios
- Sistema multi-rol (ADMIN, RECEPCIONISTA, MEDICO, PACIENTE) con permisos por UI
- 5 bases de datos separadas (auth, clinica, citas, facturacion, dashboard)
- Usuario `medico/medico123` asociado a un médico real (Carlos López, id_medico=2) con horarios L-D
- Botón "Mi Perfil" para médicos con solo nombre editable vía `MedicoFormController.setProfileMode(true)`
- Dashboard funcional con métricas en tiempo real vía EventBus
- Facturación integrada
- 27 tests unitarios (FlujoCompleto, FlujoMedico, PersistenciaIntegridad, ValidacionLogica)

---

## Párrafo Didáctico — El Sistema Explicado entre Compas

Mira, el sistema es más sencillo de lo que parece. Todo arranca en `HopecareApp.java`, que es como el botón de encendido: prende las bases de datos (si no existen, las crea solitas con sus schemas y mete datos de prueba), y luego abre la ventana de login. Ahí escribes `medico` / `medico123` y entras como médico, o `admin/admin123` como administrador. Una vez dentro, el `MainController` te muestra el panel principal con dos pestañas: "Agendar Cita" y "Registrar Consulta". La magia está en cómo separamos las cosas: las pantallas (los controladores JavaFX como `CitasController` y `ConsultaController`) solo se encargan de mostrar botones, tablas y formularios, pero no tienen lógica pesada. Esa lógica vive en los **Presenters** (`CitaPresenter` y `ConsultaPresenter`), que son como los gerentes: ellos deciden qué hacer cuando clicks "Reservar" o "Guardar Consulta". ¿Cómo se comunican? A través de interfaces: `ICitaView` e `IConsultaView`, que son como contratos que dicen "el Presenter puede pedirle a la Vista que muestre esto o aquello, pero no sabe ni le importa si es JavaFX, Swing o lo que sea". Los datos los guardan los **DAOs** (`CitaDAO`, `ConsultaDAO`, `MedicoDAO`, `PacienteDAO`, etc.), que son los que realmente hablan con SQLite. Cada DAO recibe una conexión, hace sus consultas SQL y devuelve modelos como `Cita`, `Medico` o `Paciente`. Y ojo al detalle: cuando registras un médico desde el diálogo, `MedicoDAO.insertarMedico()` hace una transacción que crea primero la persona y luego el médico, todo en una sola operación atómica —si falla algo, no se queda a medias. Lo mismo con las consultas: `ConsultaDAO.insertarConsultaYActualizarEstado()` mete la consulta y cambia el estado de la cita a "ATENDIDA" en un solo bloque, con commit y rollback explícito. En resumen: la app es un flujo **login → ver citas → agendar nueva (con registro rápido de paciente/médico) → atender cita → registrar consulta**, todo con una base de datos chiquita de 9 tablas bien relacionadas.
