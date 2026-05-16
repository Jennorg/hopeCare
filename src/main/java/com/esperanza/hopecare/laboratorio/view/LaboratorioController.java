package com.esperanza.hopecare.laboratorio.view;

import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.ExamenLaboratorioDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.SolicitudExamenDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.model.ExamenLaboratorio;
import com.esperanza.hopecare.modules.medicamentos_lab.model.SolicitudExamen;
import com.esperanza.hopecare.modules.medicamentos_lab.service.ExamenService;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class LaboratorioController {
    @FXML private Button btnAgregarExamen;
    @FXML private TableView<ExamenLaboratorio> tblExamenes;
    @FXML private TableColumn<ExamenLaboratorio, Integer> colExamenId;
    @FXML private TableColumn<ExamenLaboratorio, String> colExamenNombre;
    @FXML private TableColumn<ExamenLaboratorio, String> colExamenDescripcion;
    @FXML private TableColumn<ExamenLaboratorio, Double> colExamenPrecio;
    @FXML private TableColumn<ExamenLaboratorio, Integer> colExamenTiempo;

    @FXML private ComboBox<String> cbFiltroEstado;
    @FXML private TableView<SolicitudExamen> tblSolicitudes;
    @FXML private TableColumn<SolicitudExamen, Integer> colSolicitudId;
    @FXML private TableColumn<SolicitudExamen, String> colSolicitudPaciente;
    @FXML private TableColumn<SolicitudExamen, String> colSolicitudExamen;
    @FXML private TableColumn<SolicitudExamen, String> colSolicitudFecha;
    @FXML private TableColumn<SolicitudExamen, String> colSolicitudEstado;

    @FXML private TableView<SolicitudExamen> tblResultados;
    @FXML private TableColumn<SolicitudExamen, Integer> colResultadoId;
    @FXML private TableColumn<SolicitudExamen, String> colResultadoPaciente;
    @FXML private TableColumn<SolicitudExamen, String> colResultadoExamen;
    @FXML private TableColumn<SolicitudExamen, String> colResultadoTexto;
    @FXML private TableColumn<SolicitudExamen, String> colResultadoFecha;
    @FXML private TableColumn<SolicitudExamen, String> colResultadoFacturado;

    @FXML private TextField txtIdSolicitud;
    @FXML private TextArea txtResultado;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelarSolicitud;

    @FXML private ComboBox<Paciente> cbNuevoPaciente;
    @FXML private ComboBox<ExamenLaboratorio> cbNuevoExamen;
    @FXML private Button btnCrearSolicitud;

    private GestionClinicaFacade facade;
    private ExamenLaboratorioDAO examenDAO;
    private SolicitudExamenDAO solicitudDAO;
    private PacienteDAO pacienteDAO;
    private ExamenService examenService;

    private ObservableList<ExamenLaboratorio> examenesData = FXCollections.observableArrayList();
    private ObservableList<SolicitudExamen> solicitudesData = FXCollections.observableArrayList();
    private ObservableList<SolicitudExamen> resultadosData = FXCollections.observableArrayList();
    private ObservableList<Paciente> pacientesData = FXCollections.observableArrayList();
    private ObservableList<ExamenLaboratorio> examenesDisponiblesData = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        facade = new GestionClinicaFacade();
        examenDAO = new ExamenLaboratorioDAO();
        solicitudDAO = new SolicitudExamenDAO();
        pacienteDAO = new PacienteDAO();
        examenService = new ExamenService();

        configurarColumnasExamenes();
        configurarColumnasSolicitudes();
        configurarColumnasResultados();
        configurarComboBoxes();

        tblExamenes.setItems(examenesData);
        tblSolicitudes.setItems(solicitudesData);
        tblResultados.setItems(resultadosData);

        cbFiltroEstado.setItems(FXCollections.observableArrayList(
            "TODAS", "PENDIENTE", "COMPLETADO", "CANCELADO"
        ));
        cbFiltroEstado.setValue("PENDIENTE");

        cargarDatos();
    }

    private void configurarColumnasExamenes() {
        colExamenId.setCellValueFactory(new PropertyValueFactory<>("idExamen"));
        colExamenNombre.setCellValueFactory(new PropertyValueFactory<>("nombreExamen"));
        colExamenDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colExamenPrecio.setCellFactory(column -> new TableCell<ExamenLaboratorio, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? String.format("$%.2f", item) : null);
            }
        });
        colExamenTiempo.setCellValueFactory(new PropertyValueFactory<>("tiempoResultadoHoras"));
    }

    private void configurarColumnasSolicitudes() {
        colSolicitudId.setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colSolicitudPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombreCompleto"));
        colSolicitudExamen.setCellValueFactory(new PropertyValueFactory<>("examenNombre"));
        colSolicitudFecha.setCellFactory(column -> new TableCell<SolicitudExamen, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(getTableRow().getItem().getFechaSolicitud() != null ?
                        getTableRow().getItem().getFechaSolicitud().format(dateFormatter) : "");
                }
            }
        });
        colSolicitudEstado.setCellFactory(column -> new TableCell<SolicitudExamen, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    String estado = getTableRow().getItem().getEstado();
                    setText(estado);
                    if ("PENDIENTE".equals(estado)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;");
                    } else if ("COMPLETADO".equals(estado)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
    }

    private void configurarColumnasResultados() {
        colResultadoId.setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colResultadoPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombreCompleto"));
        colResultadoExamen.setCellValueFactory(new PropertyValueFactory<>("examenNombre"));
        colResultadoTexto.setCellValueFactory(new PropertyValueFactory<>("resultadoTexto"));
        colResultadoFecha.setCellFactory(column -> new TableCell<SolicitudExamen, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(getTableRow().getItem().getFechaSolicitud() != null ?
                        getTableRow().getItem().getFechaSolicitud().format(dateFormatter) : "");
                }
            }
        });
        colResultadoFacturado.setCellFactory(column -> new TableCell<SolicitudExamen, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(getTableRow().getItem().isFacturado() ? "SÍ" : "NO");
                    setStyle(getTableRow().getItem().isFacturado() ?
                        "-fx-text-fill: #16a34a;" : "-fx-text-fill: #f59e0b;");
                }
            }
        });
    }

    private void configurarComboBoxes() {
        cbNuevoPaciente.setItems(pacientesData);
        cbNuevoPaciente.setCellFactory(lv -> new ListCell<Paciente>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreCompleto() : null);
            }
        });
        cbNuevoPaciente.setButtonCell(new ListCell<Paciente>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreCompleto() : "Seleccionar paciente...");
            }
        });

        cbNuevoExamen.setItems(examenesDisponiblesData);
        cbNuevoExamen.setCellFactory(lv -> new ListCell<ExamenLaboratorio>() {
            @Override
            protected void updateItem(ExamenLaboratorio item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreExamen() : null);
            }
        });
        cbNuevoExamen.setButtonCell(new ListCell<ExamenLaboratorio>() {
            @Override
            protected void updateItem(ExamenLaboratorio item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreExamen() : "Seleccionar examen...");
            }
        });
    }

    private void cargarDatos() {
        cargarExamenes();
        cargarPacientes();
        cargarSolicitudes();
        cargarResultados();
    }

    private void cargarExamenes() {
        examenesData.clear();
        examenesData.addAll(examenDAO.listarTodos());
        examenesDisponiblesData.clear();
        examenesDisponiblesData.addAll(examenDAO.listarTodos());
    }

    private void cargarPacientes() {
        pacientesData.clear();
        pacientesData.addAll(pacienteDAO.listarTodos());
    }

    private void cargarSolicitudes() {
        String filtro = cbFiltroEstado.getValue();
        solicitudesData.clear();
        if (filtro == null || "TODAS".equals(filtro)) {
            solicitudesData.addAll(solicitudDAO.listarTodas());
        } else {
            solicitudesData.addAll(solicitudDAO.listarPorEstado(filtro));
        }
    }

    private void cargarResultados() {
        resultadosData.clear();
        solicitudDAO.listarPorEstado("COMPLETADO").forEach(resultadosData::add);
    }

    @FXML
    private void filtroEstadoChange() {
        cargarSolicitudes();
    }

    @FXML
    private void btnRegistrarClick() {
        String strIdSolicitud = txtIdSolicitud.getText().trim();
        String resultado = txtResultado.getText().trim();

        if (strIdSolicitud.isEmpty()) {
            mostrarAlerta("Error", "Ingrese el ID de la solicitud", Alert.AlertType.ERROR);
            return;
        }

        if (resultado.isEmpty()) {
            mostrarAlerta("Error", "Ingrese el resultado del examen", Alert.AlertType.ERROR);
            return;
        }

        try {
            int idSolicitud = Integer.parseInt(strIdSolicitud);
            boolean ok = facade.registrarResultadoExamen(idSolicitud, resultado, "COMPLETADO", "LABORATORIO");
            if (ok) {
                mostrarAlerta("Éxito", "Resultado registrado correctamente", Alert.AlertType.INFORMATION);
                cargarDatos();
                txtIdSolicitud.clear();
                txtResultado.clear();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el resultado", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El ID de solicitud debe ser numérico", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void btnCrearSolicitudClick() {
        Paciente paciente = cbNuevoPaciente.getValue();
        ExamenLaboratorio examen = cbNuevoExamen.getValue();

        if (paciente == null || examen == null) {
            mostrarAlerta("Error", "Seleccione paciente y examen", Alert.AlertType.ERROR);
            return;
        }

        boolean ok = examenService.solicitarExamen(paciente.getIdPaciente(), examen.getIdExamen());
        if (ok) {
            mostrarAlerta("Éxito", "Solicitud creada correctamente", Alert.AlertType.INFORMATION);
            cargarSolicitudes();
            cbNuevoPaciente.getSelectionModel().clearSelection();
            cbNuevoExamen.getSelectionModel().clearSelection();
        } else {
            mostrarAlerta("Error", "No se pudo crear la solicitud", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarExamenClick() {
        Dialog<ExamenLaboratorio> dialog = new Dialog<>();
        dialog.setTitle("Agregar Examen de Laboratorio");
        dialog.setHeaderText("Ingrese los datos del nuevo examen");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del examen");
        TextField txtDescripcion = new TextField();
        txtDescripcion.setPromptText("Descripción");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio");
        TextField txtTiempo = new TextField();
        txtTiempo.setText("24");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1); grid.add(txtDescripcion, 1, 1);
        grid.add(new Label("Precio:"), 0, 2); grid.add(txtPrecio, 1, 2);
        grid.add(new Label("Tiempo (hrs):"), 0, 3); grid.add(txtTiempo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                String precioStr = txtPrecio.getText().trim();
                String tiempoStr = txtTiempo.getText().trim();

                if (nombre.isEmpty() || precioStr.isEmpty()) {
                    mostrarAlerta("Error", "Nombre y precio son obligatorios", Alert.AlertType.ERROR);
                    return null;
                }

                try {
                    double precio = Double.parseDouble(precioStr);
                    int tiempo = Integer.parseInt(tiempoStr);
                    ExamenLaboratorio examen = new ExamenLaboratorio();
                    examen.setNombreExamen(nombre);
                    examen.setDescripcion(descripcion);
                    examen.setPrecio(precio);
                    examen.setTiempoResultadoHoras(tiempo);
                    return examen;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Precio y tiempo deben ser numéricos", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(examen -> {
            if (examenService.agregarExamen(examen)) {
                mostrarAlerta("Éxito", "Examen agregado correctamente", Alert.AlertType.INFORMATION);
                cargarExamenes();
            } else {
                mostrarAlerta("Error", "No se pudo agregar el examen", Alert.AlertType.ERROR);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void cancelarSolicitudClick() {
        SolicitudExamen selected = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Advertencia", "Seleccione una solicitud de la tabla", Alert.AlertType.WARNING);
            return;
        }

        if (!"PENDIENTE".equals(selected.getEstado())) {
            mostrarAlerta("Error", "Solo se pueden cancelar solicitudes en estado PENDIENTE", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de cancelar la solicitud #" + selected.getIdSolicitud() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    boolean ok = solicitudDAO.cancelar(selected.getIdSolicitud(), conn);
                    if (ok) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Solicitud cancelada correctamente", Alert.AlertType.INFORMATION);
                        cargarDatos();
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "No se pudo cancelar la solicitud", Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error de base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
}