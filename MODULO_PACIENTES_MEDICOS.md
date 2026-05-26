# Switch del Módulo de Pacientes y Médicos
## "Aislado" ↔ "Conectado con el resto del sistema"

### ¿Qué es esto?
Este documento explica cómo el módulo de gestión de pacientes y médicos se integra en el sistema HopeCare. Originalmente, este módulo se desarrolló de forma independiente (rama `medics`), pero en el sistema final se conecta con los módulos de Citas, Facturación y Dashboard.

*   **MODO AISLADO:** El módulo funciona SOLO, con una única base de datos (`sisgeho.db`) y paquetes anidados en `modules/`.
*   **MODO CONECTADO:** El módulo usa la base de datos `hopecare_clinica.db`, se comunica con `hopecare_citas.db` para horarios y usa paquetes planos.

---

### Cómo usar esta guía
Cada cambio está explicado como:
- **ARCHIVO:** Ruta del archivo.
- **AISLADO:** Texto en la rama `medics`.
- **CONECTADO:** Texto en la rama `modulosDebug` / actual.
- **QUÉ HACE:** Explicación simple.

---

## Sección 1: DAOs y Modelos (Persistencia)

### 1.1 MedicoDAO.java
**ARCHIVO:** `src/main/java/com/esperanza/hopecare/dao/MedicoDAO.java`

#### 1.1.1 - PACKAGE
- **AISLADO:** `package com.esperanza.hopecare.modules.pacientes_medicos.dao;`
- **CONECTADO:** `package com.esperanza.hopecare.dao;`

#### 1.1.2 - Conexión Principal (`listarTodos`, `existeDocumento`, etc.)
- **AISLADO:** `Connection conn = DatabaseConnection.getConnection();`
- **CONECTADO:** `Connection conn = DatabaseConnection.getClinicaConnection();`
- **QUÉ HACE:** En modo conectado, los datos de médicos viven en la BD específica "clinica".

#### 1.1.3 - Integración con Citas (`insertarMedico`)
**LÍNEA:** 123 (aprox)
- **AISLADO:** (No existía esta parte)
- **CONECTADO:**
```java
if (m.getIdMedico() > 0) {
    try (Connection connCitas = DatabaseConnection.getCitasConnection()) {
        // Inserta horario por defecto de 08:00 a 12:00 (L-D)
        String sqlH = "INSERT INTO horario_atencion ...";
        ...
    }
}
```
- **QUÉ HACE:** Al crear un médico, se le generan horarios automáticamente en la BD de "citas".

---

### 1.2 PacienteDAO.java y EspecialidadDAO.java
#### 1.2.1 - PACKAGE
- **AISLADO:** `package com.esperanza.hopecare.modules.pacientes_medicos.dao;`
- **CONECTADO:** `package com.esperanza.hopecare.dao;`

#### 1.2.2 - CONEXIÓN
- **AISLADO:** `DatabaseConnection.getConnection();`
- **CONECTADO:** `DatabaseConnection.getClinicaConnection();`

---

### 1.3 Modelos (`Medico.java`, `Paciente.java`, `Especialidad.java`, `Persona.java`)
#### 1.3.1 - PACKAGE
- **AISLADO:** `package com.esperanza.hopecare.modules.pacientes_medicos.model;` (o `com.esperanza.hopecare.common.model`)
- **CONECTADO:** `package com.esperanza.hopecare.model;`
- **QUÉ HACE:** Unificación de modelos en un paquete plano.

---

## Sección 2: Controladores y Vistas (UI)

### 2.1 Controladores (`MedicosController`, `PacientesController`, etc.)
**ARCHIVO:** `src/main/java/com/esperanza/hopecare/controller/XXXXController.java`

#### 2.1.1 - PACKAGE
- **AISLADO:** `package com.esperanza.hopecare.modules.pacientes_medicos.view;`
- **CONECTADO:** `package com.esperanza.hopecare.controller;`

#### 2.1.2 - RUTA CSS
- **AISLADO:** `"/com/esperanza/hopecare/main/hopecare.css"`
- **CONECTADO:** `"/com/esperanza/hopecare/css/hopecare.css"`

#### 2.1.3 - Uso de SesionManager
- **QUÉ HACE:** Uso de `SesionManager` para control de permisos (ej: Médicos no pueden editar otros médicos).

---

### 2.2 Archivos FXML
**ARCHIVO:** `src/main/resources/com/esperanza/hopecare/view/*.fxml`

#### 2.2.1 - RUTA EN DISCO
- **AISLADO:** `.../resources/com/esperanza/hopecare/modules/pacientes_medicos/view/`
- **CONECTADO:** `.../resources/com/esperanza/hopecare/view/`

#### 2.2.2 - Atributo fx:controller
- **AISLADO:** `fx:controller="com.esperanza.hopecare.modules.pacientes_medicos.view.MedicosController"`
- **CONECTADO:** `fx:controller="com.esperanza.hopecare.controller.MedicosController"`

---

## Sección 3: Esquemas y Datos (SQL)

### 3.1 clinica_schema.sql
**ARCHIVO:** `src/main/resources/clinica_schema.sql`
- **AISLADO:** Se usaba `sisgeho_schema.sql` (todo junto).
- **CONECTADO:** Se usa `clinica_schema.sql` (solo persona, medico, paciente, especialidad).
- **QUÉ HACE:** Separación de tablas en su propia base de datos.

---

## Sección 4: Inicialización (HopecareApp.java)

**ARCHIVO:** `src/main/java/com/esperanza/hopecare/HopecareApp.java`

### 4.1 Inicialización del Módulo
- **AISLADO:** `inicializarBaseDatos();` (Una sola BD)
- **CONECTADO:**
```java
inicializarModulo("Clinica", "clinica_schema.sql", DatabaseConnection::getClinicaConnection);
verificarYCargarDatosPrueba(); // Carga médicos y pacientes iniciales
```

### 4.2 Carga de Vistas
- **AISLADO:** Rutas largas (`modules/...`).
- **CONECTADO:** Rutas cortas (`/view/...`).

---

## Resumen Rápido para Desarrolladores
1.  Cambia todos los paquetes `modules.pacientes_medicos.xxx` a `dao`, `model` o `controller`.
2.  Cambia `DatabaseConnection.getConnection()` por `DatabaseConnection.getClinicaConnection()`.
3.  Al insertar un médico, inserta también su horario en `getCitasConnection()`.
4.  Mueve los FXML a la carpeta `view/` principal y actualiza sus `fx:controller`.
5.  Si algo falla, borra `hopecare_clinica.db` para que el sistema lo recree.
