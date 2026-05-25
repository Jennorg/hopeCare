# Registrar el Dashboard

## 1. DatabaseConnection.java
`src/main/java/com/esperanza/hopecare/util/DatabaseConnection.java`
Registrar la conexión a `hopecare_dashboard.db`.

## 2. HopecareApp.java
`src/main/java/com/esperanza/hopecare/HopecareApp.java`
- En el método `inicializarBasesDatos()`, llamar a `inicializarModulo` con el schema `/dashboard_schema.sql`, tabla de control `paciente`, y el connector `DatabaseConnection::getDashboardConnection`
- Agregar llamado a `verificarYCargarDatosDashboard()`

## 3. main.fxml
`src/main/resources/com/esperanza/hopecare/view/main.fxml`
- En `navLinks`, agregar un hyperlink con `fx:id="linkDashboard"` y `onAction="#navigateToDashboard"`
- En `mainTabPane`, agregar un Tab con `fx:id="tabDashboard"` que haga `fx:include source="dashboard.fxml"`

## 4. MainController.java
`src/main/java/com/esperanza/hopecare/controller/MainController.java`
- Agregar campos `@FXML Tab tabDashboard` y `@FXML Hyperlink linkDashboard`
- En `initialize()`, seleccionar `tabDashboard` por defecto
- Agregar método `navigateToDashboard()` y enlace en `actualizarEnlacesActivos()`
