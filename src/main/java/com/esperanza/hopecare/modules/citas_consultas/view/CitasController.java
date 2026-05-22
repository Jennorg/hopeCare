package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.common.session.SesionManager;
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
import com.esperanza.hopecare.common.db.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.esperanza.hopecare.modules.pacientes_medicos.view.PacienteFormController;
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
    private String rol;
    private int idMedicoLogueado = -1;
    private int idPacienteLogueado = -1;

    @FXML
    public void initialize() {
        presenter = new CitaPresenter(this);

        SesionManager sesion = SesionManager.getInstance();
        rol = sesion.getRol();

        if ("MEDICO".equals(rol)) {
            idMedicoLogueado = new MedicoDAO().obtenerIdMedicoPorIdPersona(sesion.getIdPersona());
        } else if ("PACIENTE".equals(rol)) {
            idPacienteLogueado = new PacienteDAO().obtenerIdPacientePorIdPersona(sesion.getIdPersona());
        }

        configurarTablaCitas();
        configurarFiltros();
        btnNuevaCita.setOnAction(e -> abrirDialogoNuevaCita());

        cargarCitasPorRol();
    }

    private void cargarCitasPorRol() {
        if ("MEDICO".equals(rol) && idMedicoLogueado > 0) {
            List<Cita> citas = new CitaDAO().listarPorMedicoConNombres(idMedicoLogueado);
            mostrarCitasExistentes(citas);
        } else if ("PACIENTE".equals(rol) && idPacienteLogueado > 0) {
            List<Cita> citas = new CitaDAO().listarPorPacienteConNombres(idPacienteLogueado);
            mostrarCitasExistentes(citas);
        } else {
            presenter.cargarCitasExistentes();
        }
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

        final int[] idPacSeleccionado = {-1};

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

        boolean esPaciente = "PACIENTE".equals(rol);
        if (esPaciente) {
            txtBuscarPac.setVisible(false);
            txtBuscarPac.setManaged(false);
            tvPacientes.setVisible(false);
            tvPacientes.setManaged(false);
            idPacSeleccionado[0] = idPacienteLogueado;
        }

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
        txtPrecioCita.setPromptText("Seleccione un médico");
        txtPrecioCita.setPrefWidth(120);
        txtPrecioCita.setEditable(false);
        txtPrecioCita.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #115e59; -fx-font-weight: 600;");

        Button btnBuscar = new Button("Buscar horarios");
        btnBuscar.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: 600;");
        Button btnReservar = new Button("Reservar cita");
        btnReservar.setStyle("-fx-background-color: #115e59; -fx-text-fill: white; -fx-font-weight: 600;");
        btnReservar.setDisable(true);

        if (esPaciente && idPacienteLogueado > 0) {
            for (Paciente p : pacientesList) {
                if (p.getIdPaciente() == idPacienteLogueado) {
                    idPacSeleccionado[0] = idPacienteLogueado;
                    break;
                }
            }
        }

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

        final int[] idMedSeleccionado = {-1};

        if (!esPaciente) {
            tvPacientes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                idPacSeleccionado[0] = sel != null ? sel.getIdPaciente() : -1;
            });
        }

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
                txtPrecioCita.setText(String.valueOf((int) sel.getPrecioConsulta()));
                dialogPresenter.cargarDiasDisponibles(sel.getIdMedico());
            } else {
                txtPrecioCita.clear();
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

        Button btnNuevoPac = new Button("Registrar Paciente");
        btnNuevoPac.getStyleClass().add("button-secondary");
        btnNuevoPac.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 6 12;");
        btnNuevoPac.setOnAction(ev -> {
            try {
                Dialog<ButtonType> pacDialog = new Dialog<>();
                pacDialog.setTitle("Registrar Paciente");
                pacDialog.setHeaderText(null);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/modules/pacientes_medicos/view/paciente_form.fxml"));
                DialogPane dialogPane = loader.load();
                pacDialog.setDialogPane(dialogPane);

                if (dialog.getOwner() != null) {
                    pacDialog.initOwner(dialog.getOwner());
                    double targetHeight = dialog.getOwner().getHeight() * 0.9;
                    dialogPane.setPrefHeight(targetHeight);
                } else if (tvCitas.getScene() != null && tvCitas.getScene().getWindow() != null) {
                    javafx.stage.Window owner = tvCitas.getScene().getWindow();
                    pacDialog.initOwner(owner);
                    double targetHeight = owner.getHeight() * 0.9;
                    dialogPane.setPrefHeight(targetHeight);
                }

                PacienteFormController formController = loader.getController();
                formController.cargarPaciente(new Paciente());

                dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                okButton.getStyleClass().add("button");
                okButton.setText("Guardar");

                Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                cancelButton.getStyleClass().add("button-secondary");
                cancelButton.setText("Cancelar");

                okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!formController.validar()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, formController.getMensajeError());
                        alert.showAndWait();
                        event.consume();
                        return;
                    }

                    Paciente p = formController.obtenerPacienteModificado();
                    if (pacienteDAO.existeDocumento(p.getDocumentoIdentidad(), p.getIdPersona())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ya existe una persona registrada con esta cédula.");
                        alert.showAndWait();
                        event.consume();
                        return;
                    }

                    if (pacienteDAO.existeHistoriaClinica(p.getHistoriaClinica(), p.getIdPaciente())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ya existe un paciente con este número de historia clínica.");
                        alert.showAndWait();
                        event.consume();
                    }
                });

                pacDialog.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        Paciente p = formController.obtenerPacienteModificado();
                        boolean saved = pacienteDAO.insertar(p);

                        if (saved) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Paciente registrado correctamente.");
                            alert.showAndWait();

                            pacientesList.setAll(pacienteDAO.listarTodos());

                            for (Paciente item : tvPacientes.getItems()) {
                                if (item.getDocumentoIdentidad().equals(p.getDocumentoIdentidad())) {
                                    tvPacientes.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo guardar los datos del paciente.");
                            alert.showAndWait();
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar la interfaz del formulario: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button btnNuevoMed = new Button("Registrar Médico");
        btnNuevoMed.getStyleClass().add("button-secondary");
        btnNuevoMed.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 6 12;");
        btnNuevoMed.setOnAction(ev -> {
            Dialog<ButtonType> medDialog = new Dialog<>();
            medDialog.setTitle("Registrar Médico");
            medDialog.setHeaderText("Complete los datos del nuevo médico");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(8);
            grid.setPadding(new Insets(15));

            TextField medNombre = new TextField(); medNombre.setPromptText("Nombre");
            TextField medApellido = new TextField(); medApellido.setPromptText("Apellido");
            TextField medDoc = new TextField(); medDoc.setPromptText("Documento de identidad");
            ComboBox<Especialidad> medEsp = new ComboBox<>();
            medEsp.setPrefWidth(200);
            medEsp.getItems().addAll(espDAO.listarTodas());
            TextField medReg = new TextField(); medReg.setPromptText("Registro médico (CMP)");
            TextField medPrecio = new TextField(); medPrecio.setPromptText("Precio consulta");

            grid.add(new Label("Nombre:"), 0, 0); grid.add(medNombre, 1, 0);
            grid.add(new Label("Apellido:"), 0, 1); grid.add(medApellido, 1, 1);
            grid.add(new Label("Documento:"), 0, 2); grid.add(medDoc, 1, 2);
            grid.add(new Label("Especialidad:"), 0, 3); grid.add(medEsp, 1, 3);
            grid.add(new Label("Registro:"), 0, 4); grid.add(medReg, 1, 4);
            grid.add(new Label("Precio ($):"), 0, 5); grid.add(medPrecio, 1, 5);

            medDialog.getDialogPane().setContent(grid);
            medDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            medDialog.getDialogPane().setPrefWidth(400);

            medDialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) return ButtonType.OK;
                return null;
            });

            medDialog.showAndWait().ifPresent(btn -> {
                String nombre = medNombre.getText().trim();
                String apellido = medApellido.getText().trim();
                String doc = medDoc.getText().trim();
                Especialidad esp = medEsp.getValue();
                String reg = medReg.getText().trim();
                String precioStr = medPrecio.getText().trim();

                if (nombre.isEmpty() || apellido.isEmpty() || doc.isEmpty() || esp == null || reg.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Complete todos los campos obligatorios.");
                    alert.showAndWait();
                    return;
                }
                if (medicoDAO.existeDocumento(doc, -1)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ya existe una persona con este documento.");
                    alert.showAndWait();
                    return;
                }
                if (medicoDAO.existeRegistroMedico(reg, -1)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ya existe un médico con este registro.");
                    alert.showAndWait();
                    return;
                }

                double precio = 0.0;
                try { precio = Double.parseDouble(precioStr); } catch (NumberFormatException ignored) {}

                Medico m = new Medico();
                m.setNombre(nombre);
                m.setApellido(apellido);
                m.setDocumentoIdentidad(doc);
                m.setIdEspecialidad(esp.getIdEspecialidad());
                m.setNombreEspecialidad(esp.getNombre());
                m.setRegistroMedico(reg);
                m.setPrecioConsulta(precio);
                m.setActivo(true);

                if (medicoDAO.insertarMedico(m)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Médico registrado correctamente.");
                    alert.showAndWait();
                    medicosList.setAll(medicoDAO.listarTodos());
                    for (Medico item : tvMedicos.getItems()) {
                        if (item.getDocumentoIdentidad().equals(doc)) {
                            tvMedicos.getSelectionModel().select(item);
                            break;
                        }
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error al registrar el médico.");
                    alert.showAndWait();
                }
            });
        });

        Label lblPacSection = new Label("Seleccionar paciente:");
        lblPacSection.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: 600;");

        HBox HBoxPacSearch = new HBox(10, txtBuscarPac, btnNuevoPac);
        HBox.setHgrow(txtBuscarPac, javafx.scene.layout.Priority.ALWAYS);

        VBox pacienteSection = new VBox(5,
            lblPacSection,
            HBoxPacSearch,
            tvPacientes
        );
        if (esPaciente) {
            pacienteSection.setVisible(false);
            pacienteSection.setManaged(false);
        }

        HBox filtrosMedicos = new HBox(10, cbEsp, txtBuscarMed, btnNuevoMed);
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
        dialog.getDialogPane().setPrefWidth(900);
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
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

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
        txtPrecio.setPromptText("Seleccione médico y estado ATENDIDA");
        txtPrecio.setPrefWidth(150);
        txtPrecio.setEditable(false);
        txtPrecio.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #115e59; -fx-font-weight: 600;");

        Runnable actualizarPrecioEdit = () -> {
            if ("ATENDIDA".equals(cbEstado.getValue()) && cbMedico.getValue() != null) {
                txtPrecio.setText(String.valueOf((int) cbMedico.getValue().getPrecioConsulta()));
            } else {
                txtPrecio.clear();
            }
        };

        cbEstado.valueProperty().addListener((obs, old, val) -> actualizarPrecioEdit.run());
        cbMedico.valueProperty().addListener((obs, old, val) -> actualizarPrecioEdit.run());
        actualizarPrecioEdit.run();

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

        Button btnEliminar = new Button("Eliminar cita");
        btnEliminar.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: 700; -fx-padding: 8 16; -fx-cursor: hand;");
        btnEliminar.setOnAction(ev -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar cita");
            confirm.setHeaderText("¿Está seguro de eliminar la cita #" + cita.getIdCita() + "?");
            confirm.setContentText("Paciente: " + cita.getPacienteNombre() + "\nMédico: " + cita.getMedicoNombre() + "\nFecha: " + cita.getFechaHora().format(dtf) + "\n\nEsta acción no se puede deshacer.");
            confirm.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    if (citaDAO.eliminarCita(cita.getIdCita())) {
                        mostrarMensajeExito("Cita eliminada correctamente.");
                        dialog.close();
                        presenter.cargarCitasExistentes();
                    } else {
                        mostrarMensajeError("Error al eliminar la cita.");
                    }
                }
            });
        });

        HBox buttonRow = new HBox(10, btnGuardar, btnEliminar);

        VBox content = new VBox(12, infoSection, new Label("— Editar —"), editGrid, buttonRow);
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
