# Dashboard - Ya implementado

El Dashboard está completamente integrado en el sistema:

- ✅ `DatabaseConnection.java` — conexión `getDashboardConnection()` a `hopecare_dashboard.db`
- ✅ `HopecareApp.java` — `inicializarModulo("Dashboard", ...)` + `verificarYCargarDatosDashboard()`
- ✅ `main.fxml` — Tab `tabDashboard` con `fx:include source="dashboard.fxml"`
- ✅ `MainController.java` — `@FXML Tab tabDashboard`, navegación por defecto al dashboard
- ✅ `DashboardController.java` — controlador con métricas en tiempo real
- ✅ `DashboardDAO.java` — consultas de pacientes, citas, ingresos
- ✅ `CargarDashboard.java` — población inicial de datos desde clinica + citas
- ✅ Suscripción a `NuevaCitaEvent` y `NuevaFacturaEvent` vía EventBus
