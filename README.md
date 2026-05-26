# HopeCare - Módulo de Gestión de Pacientes y Médicos

HopeCare es un sistema de gestión hospitalaria. Este módulo específico se encarga de la administración de la información base del sistema: Pacientes, Médicos y Especialidades.

## Alcance del Módulo

Este componente permite:
*   **Gestión de Médicos**: Registro, edición y consulta de profesionales de la salud, vinculándolos a sus respectivas especialidades y registros médicos.
*   **Gestión de Pacientes**: Administración de expedientes personales, incluyendo historias clínicas, alergias y contactos de emergencia.
*   **Gestión de Especialidades**: Definición del catálogo de especialidades médicas disponibles en la institución.
*   **Control de Acceso**: Sistema de login y gestión de usuarios vinculados a personas.

## Arquitectura del Proyecto

El proyecto sigue un patrón **MVC (Model-View-Controller)** complementado con una capa de persistencia basada en **DAO (Data Access Object)**.

### Estructura de Paquetes

*   `com.esperanza.hopecare.model`: Clases de entidad (POJOs) que representan los datos. Se utiliza herencia (`Persona` es clase base para `Paciente` y `Medico`).
*   `com.esperanza.hopecare.dao`: Lógica de acceso a datos mediante JDBC y SQLite.
*   `com.esperanza.hopecare.controller`: Controladores de JavaFX que gestionan la interacción del usuario y la lógica de presentación.
*   `com.esperanza.hopecare.view` (Resources): Definiciones de interfaz de usuario en formato FXML.
*   `com.esperanza.hopecare.util`: Clases auxiliares para validación, conexión a base de datos y gestión de sesión.

## Tecnologías Utilizadas

*   **Java 17+**
*   **JavaFX 21**: Framework para la interfaz de usuario.
*   **Maven**: Gestión de dependencias y construcción del proyecto.
*   **SQLite**: Base de datos relacional ligera y embebida.

## Configuración y Ejecución

### Base de Datos
El sistema utiliza SQLite y genera automáticamente el archivo `sisgeho.db` en la raíz del proyecto si no existe.
El script de inicialización se encuentra en `src/main/resources/sisgeho_schema.sql`.

### Datos de Prueba
La aplicación incluye una utilidad `CargarDatosPrueba` que inserta registros iniciales (especialidades, médicos, pacientes y usuarios) para facilitar las pruebas de desarrollo.

### Ejecución
Para ejecutar la aplicación:
```bash
mvn javafx:run
```

## Credenciales por Defecto
Si se cargan los datos de prueba, se puede acceder con:
*   **Usuario**: `admin`
*   **Contraseña**: `admin123`
*   **Usuario**: `medico1`
*   **Contraseña**: `password123`
