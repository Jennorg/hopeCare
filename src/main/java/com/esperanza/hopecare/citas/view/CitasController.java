package com.esperanza.hopecare.citas.view;

import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.presenter.CitaPresenter;
import com.esperanza.hopecare.modules.citas_consultas.view.ICitaView;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.EspecialidadDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Especialidad;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Medico;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;
import com.esperanza.hopecare.common.events.EventBus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CitasController implements ICitaView {
    @FXML private TableView<Cita> tvCitas;
    @FXML private Button btnNuevaCita;
    @FXML private TextField txtBuscarCita;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;

    private CitaPresenter presenter;
    private ObservableList<Cita> citasList;
    private FilteredList<Cita> citasFiltradas;

    @FXML
    public void initialize() {
        presenter = new CitaPresenter(this);

        configurarTablaCitas();
        configurarFiltros();
        btnNuevaCita.setOnAction(e -> abrirDialogoNuevaCita());

        presenter.cargarCitasExistentes();
    }

    private void configurarTablaCitas() {
        TableColumn<Cita, Number> colId = (TableColumn<Cita, Number>) tvCitas.getColumns().get(0);
        TableColumn<Cita, String> colPac = (TableColumn<Cita, String>) tvCitas.getColumns().get(1);
        TableColumn<Cita, String> colMed = (TableColumn<Cita, String>) tvCitas.getColumns().get(2);
        TableColumn<Cita, String> colFecha = (TableColumn<Cita, String>) tvCitas.getColumns().get(3);
        TableColumn<Cita, String> colEstado = (TableColumn<Cita, String>) tvCitas.getColumns().get(4);
        TableColumn<Cita, Number> colPrecio = (TableColumn<Cita, Number>) tvCitas.getColumns().get(5);

        colId.setCellValueFactory(new PropertyValueFactory<>("pacienteDocumento"));
        colPac.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colMed.setCellValueFactory(new PropertyValueFactory<>("medicoNombre"));
        colFecha.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    double val = item.doubleValue();
                    setText(val > 0 ? String.format("$%.2f", val) : "—");
                }
            }
        });

        citasList = FXCollections.observableArrayList();
        citasFiltradas = new FilteredList<>(citasList, c -> true);
        tvCitas.setItems(citasFiltradas);

        tvCitas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Cita sel = tvCitas.getSelectionModel().getSelectedItem();
                if (sel != null) abrirDialogoEditarCita(sel);
            }
        });
    }

    private void configurarFiltros() {
        Runnable aplicarFiltro = () -> {
            String texto = txtBuscarCita.getText().toLowerCase().trim();
            LocalDate desde = dpFechaDesde.getValue();
            LocalDate hasta = dpFechaHasta.getValue();

            citasFiltradas.setPredicate(cita -> {
                if (!texto.isEmpty()) {
                    String searchStr = texto.toLowerCase();
                    boolean matchPac = cita.getPacienteNombre() != null &&
                        cita.getPacienteNombre().toLowerCase().contains(searchStr);
                    boolean matchMed = cita.getMedicoNombre() != null &&
                        cita.getMedicoNombre().toLowerCase().contains(searchStr);
                    boolean matchDoc = cita.getPacienteDocumento() != null &&
                        cita.getPacienteDocumento().toLowerCase().contains(searchStr);
                    if (!matchPac && !matchMed && !matchDoc) return false;
                }
                LocalDateTime fh = cita.getFechaHora();
                if (fh != null) {
                    if (desde != null && fh.toLocalDate().isBefore(desde)) return false;
                    if (hasta != null && fh.toLocalDate().isAfter(hasta)) return false;
                }
                return true;
            });
        };

        txtBuscarCita.textProperty().addListener((obs, old, val) -> aplicarFiltro.run());
        dpFechaDesde.valueProperty().addListener((obs, old, val) -> aplicarFiltro.run());
        dpFechaHasta.valueProperty().addListener((obs, old, val) -> aplicarFiltro.run());
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscarCita.clear();
        dpFechaDesde.setValue(null);
        dpFechaHasta.setValue(null);
    }

    private void abrirDialogoNuevaCita() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva Cita Médica");
        dialog.setHeaderText("Complete los datos para agendar una nueva cita");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-border-color: #0d9488; -fx-border-width: 0 0 0 2;");

        PacienteDAO pacienteDAO = new PacienteDAO();
        MedicoDAO medicoDAO = new MedicoDAO();
        EspecialidadDAO espDAO = new EspecialidadDAO();

        ObservableList<Paciente> pacientesList = FXCollections.observableArrayList(pacienteDAO.listarTodos());
        ObservableList<Medico> medicosList = FXCollections.observableArrayList(medicoDAO.listarTodos());

        TextField txtBuscarPac = new TextField();
        txtBuscarPac.setPromptText("Buscar paciente por nombre o cédula...");

        FilteredList<Paciente> pacientesFiltrados = new FilteredList<>(pacientesList, p -> true);
        TableView<Paciente> tvPacientes = new TableView<>();
        tvPacientes.setPrefHeight(150);
        tvPacientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Paciente, String> colPacCedula = new TableColumn<>("Cédula");
        colPacCedula.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        TableColumn<Paciente, String> colPacNombre = new TableColumn<>("Nombre");
        colPacNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre() + " " + cd.getValue().getApellido()));
        tvPacientes.getColumns().addAll(colPacCedula, colPacNombre);
        tvPacientes.setItems(pacientesFiltrados);

        ComboBox<Especialidad> cbEsp = new ComboBox<>();
        cbEsp.setPrefWidth(200);
        List<Especialidad> especialidades = espDAO.listarTodas();
        Especialidad todas = new Especialidad(0, "Todas");
        cbEsp.getItems().add(todas);
        cbEsp.getItems().addAll(especialidades);
        cbEsp.setValue(todas);

        TextField txtBuscarMed = new TextField();
        txtBuscarMed.setPromptText("Buscar médico por nombre...");

        FilteredList<Medico> medicosFiltrados = new FilteredList<>(medicosList, m -> true);
        TableView<Medico> tvMedicos = new TableView<>();
        tvMedicos.setPrefHeight(150);
        tvMedicos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Medico, Number> colMedId = new TableColumn<>("ID");
        colMedId.setCellValueFactory(new PropertyValueFactory<>("idMedico"));
        TableColumn<Medico, String> colMedNombre = new TableColumn<>("Nombre");
        colMedNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre() + " " + cd.getValue().getApellido()));
        TableColumn<Medico, String> colMedEsp = new TableColumn<>("Especialidad");
        colMedEsp.setCellValueFactory(new PropertyValueFactory<>("nombreEspecialidad"));
        TableColumn<Medico, String> colMedReg = new TableColumn<>("Registro");
        colMedReg.setCellValueFactory(new PropertyValueFactory<>("registroMedico"));
        tvMedicos.getColumns().addAll(colMedId, colMedNombre, colMedEsp, colMedReg);
        tvMedicos.setItems(medicosFiltrados);

        final List<Integer> diasValidos = new ArrayList<>();
        DatePicker dpFecha = new DatePicker();
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    return;
                }
                if (!diasValidos.isEmpty() && !diasValidos.contains(date.getDayOfWeek().getValue())) {
                    setDisable(true);
                    setStyle("-fx-text-fill: #bbb;");
                }
            }
        });

        ComboBox<String> cbHorarios = new ComboBox<>();
        cbHorarios.setPrefWidth(200);
        cbHorarios.setDisable(true);

        TextField txtPrecioCita = new TextField();
        txtPrecioCita.setPromptText("0.00");
        txtPrecioCita.setPrefWidth(120);

        Button btnBuscar = new Button("Buscar horarios");
        btnBuscar.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: 600;");
        Button btnReservar = new Button("Reservar cita");
        btnReservar.setStyle("-fx-background-color: #115e59; -fx-text-fill: white; -fx-font-weight: 600;");
        btnReservar.setDisable(true);

        txtBuscarPac.textProperty().addListener((obs, old, val) -> {
            String texto = val.toLowerCase().trim();
            pacientesFiltrados.setPredicate(p -> {
                if (texto.isEmpty()) return true;
                String nc = (p.getNombre() + " " + p.getApellido()).toLowerCase();
                String doc = p.getDocumentoIdentidad() != null ? p.getDocumentoIdentidad().toLowerCase() : "";
                return nc.contains(texto) || doc.contains(texto);
            });
        });

        Runnable filtrarMedicos = () -> {
            String texto = txtBuscarMed.getText().toLowerCase().trim();
            Especialidad esp = cbEsp.getValue();
            medicosFiltrados.setPredicate(m -> {
                boolean coincideNombre = texto.isEmpty() ||
                    (m.getNombre() + " " + m.getApellido()).toLowerCase().contains(texto);
                boolean coincideEsp = esp == null || esp.getIdEspecialidad() == 0 ||
                    m.getIdEspecialidad() == esp.getIdEspecialidad();
                return coincideNombre && coincideEsp;
            });
        };

        cbEsp.setOnAction(e -> filtrarMedicos.run());
        txtBuscarMed.textProperty().addListener((obs, old, val) -> filtrarMedicos.run());

        final int[] idPacSeleccionado = {-1};
        final int[] idMedSeleccionado = {-1};

        tvPacientes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            idPacSeleccionado[0] = sel != null ? sel.getIdPaciente() : -1;
        });

        CitaPresenter dialogPresenter = new CitaPresenter(new ICitaView() {
            @Override public void mostrarCitasExistentes(List<Cita> citas) {}
            @Override public void mostrarHorariosDisponibles(List<LocalTime> bloques) {
                cbHorarios.getItems().clear();
                if (bloques.isEmpty()) {
                    cbHorarios.setDisable(true);
                    btnReservar.setDisable(true);
                    cbHorarios.getItems().add("No hay horarios disponibles");
                } else {
                    for (LocalTime t : bloques) cbHorarios.getItems().add(t.toString());
                    cbHorarios.setDisable(false);
                    btnReservar.setDisable(false);
                }
            }
            @Override public void mostrarDiasDisponibles(List<Integer> diasSemana) {
                diasValidos.clear();
                diasValidos.addAll(diasSemana);
                dpFecha.setValue(null);
                if (!diasSemana.isEmpty()) {
                    LocalDate today = LocalDate.now();
                    for (int i = 0; i < 14; i++) {
                        LocalDate d = today.plusDays(i);
                        if (diasSemana.contains(d.getDayOfWeek().getValue())) {
                            dpFecha.setValue(d);
                            break;
                        }
                    }
                }
            }
            @Override public int getDiaSeleccionado() { return 0; }
            @Override public void mostrarMensajeError(String mensaje) {
                Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
                alert.showAndWait();
            }
            @Override public void mostrarMensajeExito(String mensaje) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
                alert.showAndWait();
                dialog.close();
            }
            @Override public void limpiarCampos() {}
            @Override public int getIdPacienteSeleccionado() { return idPacSeleccionado[0]; }
            @Override public int getIdMedicoSeleccionado() { return idMedSeleccionado[0]; }
            @Override public LocalDate getFechaSeleccionada() { return dpFecha.getValue(); }
            @Override public LocalTime getHoraSeleccionada() {
                String s = cbHorarios.getValue();
                return s != null ? LocalTime.parse(s) : null;
            }
            @Override public double getPrecio() {
                try {
                    return Double.parseDouble(txtPrecioCita.getText().trim().isEmpty() ? "0" : txtPrecioCita.getText().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        });

        tvMedicos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            idMedSeleccionado[0] = sel != null ? sel.getIdMedico() : -1;
            if (sel != null) {
                dialogPresenter.cargarDiasDisponibles(sel.getIdMedico());
            } else {
                diasValidos.clear();
                dpFecha.setValue(null);
            }
        });

        btnBuscar.setOnAction(e -> {
            if (idMedSeleccionado[0] <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Seleccione un médico");
                alert.showAndWait();
                return;
            }
            if (dpFecha.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Seleccione una fecha");
                alert.showAndWait();
                return;
            }
            dialogPresenter.actualizarHorariosDisponibles(idMedSeleccionado[0], dpFecha.getValue());
        });

        btnReservar.setOnAction(e -> {
            dialogPresenter.reservarCita();
        });

        Label lblPacSection = new Label("Seleccionar paciente:");
        lblPacSection.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: 600;");
        VBox pacienteSection = new VBox(5,
            lblPacSection,
            txtBuscarPac,
            tvPacientes
        );

        HBox filtrosMedicos = new HBox(10, cbEsp, txtBuscarMed);
        HBox.setHgrow(txtBuscarMed, javafx.scene.layout.Priority.ALWAYS);
        Label lblMedSection = new Label("Seleccionar médico:");
        lblMedSection.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: 600;");
        VBox medicoSection = new VBox(5,
            lblMedSection,
            filtrosMedicos,
            tvMedicos
        );

        HBox tablesRow = new HBox(20, pacienteSection, medicoSection);
        HBox.setHgrow(pacienteSection, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(medicoSection, javafx.scene.layout.Priority.ALWAYS);

        GridPane horarioGrid = new GridPane();
        horarioGrid.setHgap(10);
        horarioGrid.setVgap(10);
        horarioGrid.add(new Label("Fecha:"), 0, 0);
        horarioGrid.add(dpFecha, 1, 0);
        horarioGrid.add(btnBuscar, 2, 0);
        horarioGrid.add(new Label("Horario:"), 0, 1);
        horarioGrid.add(cbHorarios, 1, 1);
        horarioGrid.add(btnReservar, 2, 1);
        horarioGrid.add(new Label("Costo ($):"), 0, 2);
        horarioGrid.add(txtPrecioCita, 1, 2);

        VBox content = new VBox(15, tablesRow, horarioGrid);
        content.setStyle("-fx-padding: 15;");

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(750);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        dialog.showAndWait();

        presenter.cargarCitasExistentes();
    }

    private void abrirDialogoEditarCita(Cita cita) {
        CitaDAO citaDAO = new CitaDAO();
        MedicoDAO medicoDAO = new MedicoDAO();
        List<Medico> medicos = medicoDAO.listarTodos();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cita #" + cita.getIdCita());
        dialog.setHeaderText("Paciente: " + cita.getPacienteNombre());

        Label lblMedicoActual = new Label("Médico: " + cita.getMedicoNombre());
        Label lblFechaActual = new Label("Fecha/Hora: " + cita.getFechaHora().format(dtf));
        Label lblEstadoActual = new Label("Estado: " + cita.getEstado());

        ComboBox<Medico> cbMedico = new ComboBox<>();
        cbMedico.setPrefWidth(300);
        cbMedico.getItems().addAll(medicos);
        for (Medico m : medicos) {
            if (m.getIdMedico() == cita.getIdMedico()) {
                cbMedico.setValue(m);
                break;
            }
        }

        DatePicker dpFecha = new DatePicker(cita.getFechaHora().toLocalDate());

        ObservableList<String> slots = FXCollections.observableArrayList();
        for (int h = 7; h <= 19; h++) {
            slots.add(String.format("%02d:00", h));
            slots.add(String.format("%02d:30", h));
        }
        ComboBox<String> cbHora = new ComboBox<>(slots);
        cbHora.setValue(cita.getFechaHora().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("PROGRAMADA", "CANCELADA", "ATENDIDA", "NO_ASISTIO");
        cbEstado.setValue(cita.getEstado());

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("0.00");
        txtPrecio.setPrefWidth(150);
        txtPrecio.setDisable(!"ATENDIDA".equals(cita.getEstado()));
        cbEstado.valueProperty().addListener((obs, old, val) ->
            txtPrecio.setDisable(!"ATENDIDA".equals(val))
        );

        Button btnGuardar = new Button("Guardar cambios");

        btnGuardar.setOnAction(e -> {
            Medico medSel = cbMedico.getValue();
            LocalDate nuevaFecha = dpFecha.getValue();
            String horaStr = cbHora.getValue();
            String nuevoEstado = cbEstado.getValue();

            if (medSel == null || nuevaFecha == null || horaStr == null || nuevoEstado == null) {
                mostrarMensajeError("Complete todos los campos.");
                return;
            }

            double precio = 0.0;
            if ("ATENDIDA".equals(nuevoEstado)) {
                try {
                    precio = Double.parseDouble(txtPrecio.getText().trim().isEmpty() ? "0" : txtPrecio.getText().trim());
                    if (precio < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    mostrarMensajeError("Ingrese un costo de consulta válido (número positivo).");
                    return;
                }
            }

            LocalTime nuevaHora = LocalTime.parse(horaStr);
            LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);

            cita.setIdMedico(medSel.getIdMedico());
            cita.setMedicoNombre(medSel.getNombre() + " " + medSel.getApellido());
            cita.setFechaHora(nuevaFechaHora);
            cita.setEstado(nuevoEstado);

            if (citaDAO.actualizarCita(cita)) {
                if ("ATENDIDA".equals(nuevoEstado)) {
                    new ConsultaDAO().insertarSiNoExiste(cita.getIdCita(), precio);
                    EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());
                }
                mostrarMensajeExito("Cita actualizada correctamente.");
                dialog.close();
                presenter.cargarCitasExistentes();
            } else {
                mostrarMensajeError("Error al actualizar la cita.");
            }
        });

        VBox infoSection = new VBox(5,
            new Label("— Información actual —"),
            lblMedicoActual, lblFechaActual, lblEstadoActual
        );
        infoSection.setPadding(new Insets(0, 0, 10, 0));

        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(8);
        editGrid.add(new Label("Nuevo médico:"), 0, 0);
        editGrid.add(cbMedico, 1, 0);
        editGrid.add(new Label("Nueva fecha:"), 0, 1);
        editGrid.add(dpFecha, 1, 1);
        editGrid.add(new Label("Nuevo horario:"), 0, 2);
        editGrid.add(cbHora, 1, 2);
        editGrid.add(new Label("Nuevo estado:"), 0, 3);
        editGrid.add(cbEstado, 1, 3);
        editGrid.add(new Label("Costo consulta ($):"), 0, 4);
        editGrid.add(txtPrecio, 1, 4);

        VBox content = new VBox(12, infoSection, new Label("— Editar —"), editGrid, btnGuardar);
        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        dialog.showAndWait();
    }

    @Override
    public void mostrarCitasExistentes(List<Cita> citas) {
        citasList.setAll(citas);
    }

    @Override
    public void mostrarHorariosDisponibles(List<LocalTime> bloques) {}

    @Override
    public void mostrarDiasDisponibles(List<Integer> diasSemana) {}

    @Override
    public int getDiaSeleccionado() { return -1; }

    @Override
    public void mostrarMensajeError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
            alert.showAndWait();
        });
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
            alert.showAndWait();
        });
    }

    @Override
    public void limpiarCampos() {}

    @Override
    public int getIdPacienteSeleccionado() { return -1; }

    @Override
    public int getIdMedicoSeleccionado() { return -1; }

    @Override
    public LocalDate getFechaSeleccionada() { return null; }

    @Override
    public LocalTime getHoraSeleccionada() { return null; }

    @Override
    public double getPrecio() { return 0.0; }
}
