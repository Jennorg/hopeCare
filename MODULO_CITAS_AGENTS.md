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
└── src/main/java/com/esperanza/hopecare/
    ├── main/
    │   ├── HopecareApp.java          → Punto de entrada, inicializa BD y lanza JavaFX
    │   ├── MainController.java       → Controlador del layout principal (header + tabs)
    │   └── main.fxml                 → Layout principal con TabPane y header
    │
    ├── common/
    │   ├── db/
    │   │   ├── DatabaseConnection.java   → Singleton de conexión SQLite
    │   │   ├── CrearBaseDatos.java       → Ejecuta sisgeho_schema.sql
    │   │   └── CargarDatosPrueba.java    → Inserta datos de prueba
    │   ├── model/Persona.java            → Clase base: nombre, apellido, documento
    │   ├── session/SesionManager.java    → Singleton: usuario logueado, nombre del médico
    │   └── events/NuevaCitaEvent.java    → Evento publicado al agendar cita
    │
    ├── modules/
    │   ├── Auth/            → Login plano (sin roles)
    │   │   ├── model/Usuario.java
    │   │   ├── DAO/AuthDAO.java
    │   │   ├── service/AuthService.java
    │   │   └── view/
    │   │       ├── LoginController.java
    │   │       └── login.fxml
    │   │
    │   ├── pacientes_medicos/   → Modelos y DAOs de soporte
    │   │   ├── model/  (Persona usada via common, Medico, Paciente, Especialidad)
    │   │   ├── dao/    (MedicoDAO, PacienteDAO, EspecialidadDAO)
    │   │   └── view/
    │   │       ├── PacienteFormController.java
    │   │       └── paciente_form.fxml
    │   │
    │   └── citas_consultas/     → Núcleo del módulo
    │       ├── model/  (Cita, Consulta, HorarioAtencion)
    │       ├── dao/    (CitaDAO, ConsultaDAO, HorarioAtencionDAO)
    │       ├── presenter/
    │       │   ├── CitaPresenter.java      → Lógica de agendar citas
    │       │   └── ConsultaPresenter.java  → Lógica de registrar consultas
    │       └── view/
    │           ├── ICitaView.java          → Interfaz que el Presenter usa
    │           ├── IConsultaView.java      → Interfaz que el ConsultaPresenter usa
    │           ├── CitasController.java    → Controlador de agendar citas
    │           ├── citas.fxml
    │           ├── ConsultaController.java → Controlador de consultas
    │           └── consulta.fxml
    │
    └── resources/
        ├── sisgeho_schema.sql
        ├── hopecare.css
        └── ... (fxml resources)
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
| `modules/citas_consultas/` | Models, DAOs, Presenters (MVP) para citas y consultas |
| `modules/pacientes_medicos/` | Models (Persona, Medico, Paciente, Especialidad) y DAOs (MedicoDAO, PacienteDAO, EspecialidadDAO) |
| `modules/Auth/` | Login con autenticación SHA-256 (sin roles, un solo médico) |
| `common/db/` | DatabaseConnection, CrearBaseDatos, CargarDatosPrueba |
| `common/session/` | SesionManager (singleton con nombreUsuario, nombreRol) |

## Módulos del Sistema

### Login (autenticación plana)
- **LoginController.java**: Controlador JavaFX que autentica contra `usuario` + `persona` vía AuthService
- **AuthDAO.java**: `autenticar(String usuario, String password)` con JOIN a persona
- **Usuario.java**: Modelo simple (idUsuario, nombreUsuario, contrasenaHash, idPersona)
- **SesionManager.java**: Singleton que guarda `nombreUsuario` y `nombreRol` (nombre completo del médico)
- Usuario único de prueba: `amedico` / `medico123`

### Citas (MVP - ICitaView + CitaPresenter)
- **ICitaView.java**: Interfaz con `mostrarHorariosDisponibles()`, `mostrarCitasExistentes()`, `mostrarDiasDisponibles()` y getters de selección
- **CitaPresenter.java**: Genera bloques desde HorarioAtencion (intervalo_minutos), filtra ocupados vía CitaDAO, reserva y publica NuevaCitaEvent
- **CitasController.java**: Tabla principal de citas existentes + botón "Agendar Nueva Cita" que abre `Dialog<>` modal. El diálogo contiene: buscador de pacientes (con botón "Registrar Paciente"), filtro por especialidad + buscador de médicos (con botón "Registrar Médico"), selector de días disponibles, auto-asignación de fecha y horarios, botón de reserva
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

## Base de Datos

- Archivo: `sisgeho.db` en raíz del proyecto
- Script DDL: `src/main/resources/sisgeho_schema.sql`
- Driver: SQLite v3.45.1.0 (xerial sqlite-jdbc)
- **No modificar el schema**

### Visión de las Tablas y sus Relaciones

```
persona (datos personales de cualquier persona física)
  ├── usuario   (1 a 1: una persona puede tener 0 o 1 cuenta)
  ├── medico    (1 a 1: una persona puede ser 0 o 1 médico)
  └── paciente  (1 a 1: una persona puede ser 0 o 1 paciente)

especialidad (catálogo de especialidades médicas)
  └── medico   (N a 1: muchos médicos pertenecen a una especialidad)

medico (médicos registrados)
  ├── horario_atencion (1 a N: un médico tiene muchos horarios)
  └── cita             (1 a N: un médico atiende muchas citas)

paciente (pacientes registrados)
  └── cita             (1 a N: un paciente tiene muchas citas)

cita (citas agendadas)
  └── consulta (1 a 0..1: una cita puede tener 0 o 1 consulta asociada)
```

### Esquema (9 tablas)
```
persona           (id_persona, nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero)
usuario           (id_usuario, nombre_usuario, contrasena_hash, id_persona → persona, fecha_creacion)
especialidad      (id_especialidad, nombre_especialidad)
paciente          (id_paciente, id_persona → persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia, fecha_registro)
medico            (id_medico, id_persona → persona, id_especialidad → especialidad, registro_medico, precio_consulta, activo)
horario_atencion  (id_horario, id_medico → medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo)
cita              (id_cita, id_paciente → paciente, id_medico → medico, fecha_hora, estado, motivo, creada_por, fecha_creacion)
consulta          (id_consulta, id_cita → cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta)
```

## Inicialización Automática de BD
La app crea y puebla la base de datos automáticamente al iniciar (`HopecareApp.init()`):
1. Verifica si la tabla `persona` existe
2. Si no existe → ejecuta `sisgeho_schema.sql` para crear todas las tablas
3. Si `persona` está vacía → inserta datos de prueba (1 médico, 5 pacientes, 10 citas, horarios del médico)
4. Si ya hay datos → no hace nada
Para resetear la BD: borrar `sisgeho.db` de la raíz y reiniciar la app.

## Usuario de Prueba
| Usuario | Contraseña | Descripción |
|---------|------------|-------------|
| amedico | medico123 | Médico de prueba (asociado a la persona del primer médico) |

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
- Login con roles (actualmente un solo médico)
- Filtrar citas por médico logueado (actualmente muestra todas)
- Pruebas unitarias JUnit 5
- Reportes (JasperReports)
- JAR ejecutable con Maven Assembly Plugin

## Últimos Cambios
- Rama `modulo-citasMedicas` creada desde `miguel/modulo-facturas`
- Eliminados módulos completos: facturacion, dashboard, medicamentos_lab, test, Swing
- Schema simplificado a 9 tablas (sin rol, sin facturado, sin receta/detalle_receta)
- Login sin roles, credenciales fijas para un médico
- Navegación con solo 2 tabs: Agendar Cita y Registrar Consulta
- Diálogo "Nueva Cita" con 900px de ancho, buscador de pacientes con "Registrar Paciente", filtro especialidad + buscador médicos con "Registrar Médico"
- Botón "Cerrar Sesión" en verde teal en el header

---

## Párrafo Didáctico — El Sistema Explicado entre Compas

Mira, el sistema es más sencillo de lo que parece. Todo arranca en `HopecareApp.java`, que es como el botón de encendido: prende la base de datos (si no existe, la crea solita con el `sisgeho_schema.sql` y mete datos de prueba), y luego abre la ventana de login. Ahí escribes `amedico` / `medico123` y entras. Una vez dentro, el `MainController` te muestra el panel principal con dos pestañas: "Agendar Cita" y "Registrar Consulta". La magia está en cómo separamos las cosas: las pantallas (los controladores JavaFX como `CitasController` y `ConsultaController`) solo se encargan de mostrar botones, tablas y formularios, pero no tienen lógica pesada. Esa lógica vive en los **Presenters** (`CitaPresenter` y `ConsultaPresenter`), que son como los gerentes: ellos deciden qué hacer cuando clicks "Reservar" o "Guardar Consulta". ¿Cómo se comunican? A través de interfaces: `ICitaView` e `IConsultaView`, que son como contratos que dicen "el Presenter puede pedirle a la Vista que muestre esto o aquello, pero no sabe ni le importa si es JavaFX, Swing o lo que sea". Los datos los guardan los **DAOs** (`CitaDAO`, `ConsultaDAO`, `MedicoDAO`, `PacienteDAO`, etc.), que son los que realmente hablan con SQLite. Cada DAO recibe una conexión, hace sus consultas SQL y devuelve modelos como `Cita`, `Medico` o `Paciente`. Y ojo al detalle: cuando registras un médico desde el diálogo, `MedicoDAO.insertarMedico()` hace una transacción que crea primero la persona y luego el médico, todo en una sola operación atómica —si falla algo, no se queda a medias. Lo mismo con las consultas: `ConsultaDAO.insertarConsultaYActualizarEstado()` mete la consulta y cambia el estado de la cita a "ATENDIDA" en un solo bloque, con commit y rollback explícito. En resumen: la app es un flujo **login → ver citas → agendar nueva (con registro rápido de paciente/médico) → atender cita → registrar consulta**, todo con una base de datos chiquita de 9 tablas bien relacionadas.
