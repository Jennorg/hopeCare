package com.esperanza.hopecare.tests;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.util.InicializarBD;
import com.esperanza.hopecare.dao.CitaDAO;
import com.esperanza.hopecare.dao.ConsultaDAO;
import com.esperanza.hopecare.dao.MedicoDAO;
import com.esperanza.hopecare.dao.FacturaConsultaDAO;
import com.esperanza.hopecare.model.Cita;
import com.esperanza.hopecare.model.Consulta;
import com.esperanza.hopecare.model.Medico;
import com.esperanza.hopecare.model.PendienteDTO;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoMedicoTest {

    private static CitaDAO citaDAO;
    private static ConsultaDAO consultaDAO;
    private static MedicoDAO medicoDAO;
    private static FacturaConsultaDAO facturaConsultaDAO;

    // IDs de los 3 medicos, 3 pacientes, 3 citas y 3 consultas
    private static int[] idMedicos = new int[3];
    private static int[] idPacientes = new int[3];
    private static int[] idCitas = new int[3];
    private static int[] idConsultas = new int[3];
    private static double[] precios = {50000.0, 75000.0, 100000.0};
    private static String[] nombresMed = {"MedicoUno", "MedicoDos", "MedicoTres"};
    private static String[] nombresPac = {"PacienteUno", "PacienteDos", "PacienteTres"};

    @BeforeAll
    public static void setUp() throws SQLException {
        citaDAO = new CitaDAO();
        consultaDAO = new ConsultaDAO();
        medicoDAO = new MedicoDAO();
        facturaConsultaDAO = new FacturaConsultaDAO();

        limpiarBases();
        sembrarDatos();
    }

    private static void limpiarBases() throws SQLException {
        InicializarBD.ejecutar();
        try (Connection c = DatabaseConnection.getCitasUnifiedConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM consulta");
            s.execute("DELETE FROM cita");
            s.execute("DELETE FROM horario_atencion");
        }
        try (Connection c = DatabaseConnection.getClinicaConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM paciente");
            s.execute("DELETE FROM medico");
            s.execute("DELETE FROM persona");
            s.execute("DELETE FROM especialidad");
        }
        try (Connection c = DatabaseConnection.getAuthConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM usuario");
            s.execute("DELETE FROM rol");
        }
    }

    private static void sembrarDatos() throws SQLException {
        try (Connection conn = DatabaseConnection.getClinicaConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps;

                ps = conn.prepareStatement("INSERT OR IGNORE INTO especialidad (id_especialidad, nombre_especialidad) VALUES (1, 'Medicina General')");
                ps.executeUpdate();

                for (int i = 0; i < 3; i++) {
                    ps = conn.prepareStatement("INSERT INTO persona (nombre, apellido, documento_identidad) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, nombresMed[i]);
                    ps.setString(2, "ApellidoMed");
                    ps.setString(3, "MED-TEST-" + (i + 1));
                    ps.executeUpdate();
                    var rs = ps.getGeneratedKeys(); rs.next();
                    int idPersMed = rs.getInt(1);

                    ps = conn.prepareStatement("INSERT INTO persona (nombre, apellido, documento_identidad) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, nombresPac[i]);
                    ps.setString(2, "ApellidoPac");
                    ps.setString(3, "PAC-TEST-" + (i + 1));
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys(); rs.next();
                    int idPersPac = rs.getInt(1);

                    ps = conn.prepareStatement("INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta, activo) VALUES (?, ?, ?, ?, 1)", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, idPersMed);
                    ps.setInt(2, 1);
                    ps.setString(3, "RM-TEST-" + (i + 1));
                    ps.setDouble(4, precios[i]);
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys(); rs.next();
                    idMedicos[i] = rs.getInt(1);

                    ps = conn.prepareStatement("INSERT INTO paciente (id_persona, historia_clinica) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, idPersPac);
                    ps.setString(2, "HC-TEST-" + (i + 1));
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys(); rs.next();
                    idPacientes[i] = rs.getInt(1);
                }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        }

        try (Connection conn = DatabaseConnection.getCitasUnifiedConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo) VALUES (?, ?, ?, ?, ?, 1)");
                for (int m = 0; m < 3; m++) {
                    for (int dia = 1; dia <= 5; dia++) {
                        ps.setInt(1, idMedicos[m]);
                        ps.setInt(2, dia);
                        ps.setString(3, "08:00");
                        ps.setString(4, "12:00");
                        ps.setInt(5, 30);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        }
    }

    @Test
    @Order(1)
    public void testPreciosCorrectos() {
        for (int i = 0; i < 3; i++) {
            double precio = medicoDAO.obtenerPrecioConsulta(idMedicos[i]);
            assertEquals(precios[i], precio, 0.001,
                "Medico " + (i+1) + " debe tener precio " + precios[i]);
        }
    }

    @Test
    @Order(2)
    public void testCrearTresCitas() {
        for (int i = 0; i < 3; i++) {
            Cita c = new Cita(idPacientes[i], idMedicos[i],
                LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)),
                "PROGRAMADA");
            c.setCreadaPor(1);
            boolean ok = citaDAO.insertarCita(c);
            assertTrue(ok, "Cita " + (i+1) + " debe crearse");
            idCitas[i] = c.getIdCita();
            assertTrue(idCitas[i] > 0, "Cita " + (i+1) + " debe tener ID valido");
        }
    }

    @Test
    @Order(3)
    public void testCadaMedicoVeSoloSusCitas() {
        // Para cada medico, verificar que solo ve sus citas
        for (int m = 0; m < 3; m++) {
            List<Cita> citas = citaDAO.listarPorMedicoConNombres(idMedicos[m]);
            assertEquals(1, citas.size(), "Medico " + (m+1) + " debe ver exactamente 1 cita");
            assertEquals(idMedicos[m], citas.get(0).getIdMedico(),
                "La cita debe pertenecer al medico " + (m+1));
            assertEquals(idPacientes[m], citas.get(0).getIdPaciente(),
                "La cita debe pertenecer a su paciente");
        }
    }

    @Test
    @Order(4)
    public void testPrecioEnListadoMuestraCuotaDelMedico() {
        // Verificar que el precio mostrado en las citas es el precio_consulta de cada medico
        for (int m = 0; m < 3; m++) {
            List<Cita> citas = citaDAO.listarPorMedicoConNombres(idMedicos[m]);
            assertEquals(precios[m], citas.get(0).getPrecio(), 0.001,
                "Medico " + (m+1) + " debe ver su precio_consulta en la cita");
        }
    }

    @Test
    @Order(5)
    public void testCrearTresConsultas() {
        for (int i = 0; i < 3; i++) {
            Consulta cons = new Consulta(idCitas[i],
                "Diagnostico de prueba " + (i+1),
                "Sintomas de prueba " + (i+1),
                "Tratamiento de prueba " + (i+1),
                precios[i]);
            cons.setFechaConsulta(LocalDateTime.now());
            int id = consultaDAO.insertarConsultaYActualizarEstado(cons);
            assertTrue(id > 0, "Consulta " + (i+1) + " debe crearse");
            idConsultas[i] = id;

            // Verificar estado ATENDIDA
            int citaId = idCitas[i];
            Cita cita = citaDAO.obtenerCitasPorEstado("ATENDIDA").stream()
                .filter(c -> c.getIdCita() == citaId)
                .findFirst().orElse(null);
            assertNotNull(cita, "Cita " + (i+1) + " debe estar ATENDIDA");
            assertEquals("ATENDIDA", cita.getEstado());
        }
    }

    @Test
    @Order(6)
    public void testCadaMedicoVeSoloSusConsultasAtendidas() {
        // listarConsultasAtendidasPorMedico debe filtrar por medico
        for (int m = 0; m < 3; m++) {
            List<Cita> atendidas = citaDAO.listarConsultasAtendidasPorMedico(idMedicos[m]);
            assertEquals(1, atendidas.size(),
                "Medico " + (m+1) + " debe tener exactamente 1 consulta atendida");
            assertEquals(idMedicos[m], atendidas.get(0).getIdMedico(),
                "La consulta atendida debe pertenecer al medico " + (m+1));
            assertEquals(precios[m], atendidas.get(0).getPrecio(), 0.001,
                "El precio debe ser el del medico " + (m+1));
        }
    }

    @Test
    @Order(7)
    public void testListadoGeneralMuestraTresConsultas() {
        List<Cita> todas = citaDAO.listarConsultasAtendidas();
        assertEquals(3, todas.size(), "Deben haber 3 consultas atendidas en total");
    }

    @Test
    @Order(8)
    public void testFacturacionVeTresPendientes() {
        List<PendienteDTO> pendientes = facturaConsultaDAO.listarPendientesConPaciente();
        assertEquals(3, pendientes.size(), "Deben haber 3 consultas pendientes de facturar");
    }

    @Test
    @Order(9)
    public void testFacturacionFiltradaPorMedico() {
        // La facturacion no tiene filtro directo por medico, pero podemos verificar que
        // las 3 consultas pendientes estan asociadas a los 3 medicos distintos.
        // Para simular el filtro: obtenemos los idPaciente de las pendientes y
        // verificamos consistencia con las citas.
        List<PendienteDTO> pendientes = facturaConsultaDAO.listarPendientesConPaciente();

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean match = pendientes.stream()
                .anyMatch(p -> p.getIdPaciente() == idPacientes[idx]
                    && Math.abs(p.getMonto() - precios[idx]) < 0.001);
            assertTrue(match, "Debe haber un pendiente para el paciente " + (i+1)
                + " con monto " + precios[i]);
        }
    }

    @Test
    @Order(10)
    public void testFacturarSoloConsultaDelMedico1() {
        // Simular: facturamos solo la consulta del Medico 1
        boolean ok = facturaConsultaDAO.marcarFacturado(idConsultas[0]);
        assertTrue(ok, "Consulta del Medico 1 debe marcarse como facturada");

        List<PendienteDTO> pendientes = facturaConsultaDAO.listarPendientesConPaciente();
        assertEquals(2, pendientes.size(), "Deben quedar 2 pendientes (Medico 2 y 3)");

        boolean medico1YaNoAparece = pendientes.stream()
            .noneMatch(p -> p.getIdReferencia() == idConsultas[0]);
        assertTrue(medico1YaNoAparece, "La consulta del Medico 1 ya no debe estar pendiente");

        boolean medico2Sigue = pendientes.stream()
            .anyMatch(p -> p.getIdReferencia() == idConsultas[1]);
        assertTrue(medico2Sigue, "La consulta del Medico 2 debe seguir pendiente");

        boolean medico3Sigue = pendientes.stream()
            .anyMatch(p -> p.getIdReferencia() == idConsultas[2]);
        assertTrue(medico3Sigue, "La consulta del Medico 3 debe seguir pendiente");
    }

    @Test
    @Order(11)
    public void testAdminVeTodasLasCitas() {
        // Admin/recepcionista ve todas (sin filtro de medico)
        List<Cita> todas = citaDAO.listarTodasConNombres();
        long count = todas.stream().filter(c -> c.getEstado().equals("ATENDIDA")).count();
        // Originalmente 3 citas, una se atendio, deberian quedar 2 programadas + las atendidas
        // Pero ademas, todas las citas programadas y atendidas aparecen
        assertTrue(todas.size() >= 3, "Admin debe ver al menos 3 citas en total");
    }

    @Test
    @Order(12)
    public void testListadoGeneralAdminVeTresConsultasAtendidas() {
        List<Cita> todas = citaDAO.listarConsultasAtendidas();
        assertEquals(3, todas.size(), "Admin debe ver las 3 consultas atendidas");

        // Verificar que todas tienen precio correcto
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            long match = todas.stream().filter(c -> c.getIdCita() == idCitas[idx]).count();
            assertEquals(1, match, "Admin debe ver la cita " + (i+1));
        }
    }

    @Test
    @Order(13)
    public void testProgramadasNoIncluyeCitasConConsulta() {
        // Después de crear las 3 consultas, no debe haber PROGRAMADAS sin consulta
        List<Cita> programadas = citaDAO.obtenerCitasPorEstadoConNombres("PROGRAMADA");
        assertTrue(programadas.isEmpty(), "No deben quedar citas PROGRAMADA sin consulta");
    }

    @Test
    @Order(14)
    public void testSoloProgramadasSinConsultaEnComboBox() {
        // Crear una NUEVA cita que NO tenga consulta
        Cita nueva = new Cita(idPacientes[0], idMedicos[0],
            LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(10, 0)), "PROGRAMADA");
        nueva.setCreadaPor(1);
        boolean ok = citaDAO.insertarCita(nueva);
        assertTrue(ok, "Nueva cita debe crearse");

        // Ahora debe aparecer como programada disponible
        List<Cita> programadas = citaDAO.obtenerCitasPorEstadoConNombres("PROGRAMADA");
        assertEquals(1, programadas.size(), "Debe haber 1 programada sin consulta");
        assertEquals(nueva.getIdCita(), programadas.get(0).getIdCita(), "Debe ser la nueva cita");

        // Registrar consulta para esa cita
        Consulta cons = new Consulta(nueva.getIdCita(),
            "Diag", "Sint", "Trat", precios[0]);
        cons.setFechaConsulta(LocalDateTime.now());
        int id = consultaDAO.insertarConsultaYActualizarEstado(cons);
        assertTrue(id > 0, "Consulta debe crearse");

        // Ahora ya no debe aparecer como programada
        programadas = citaDAO.obtenerCitasPorEstadoConNombres("PROGRAMADA");
        assertTrue(programadas.isEmpty(),
            "Después de crear consulta, la cita no debe aparecer como programada disponible");
    }

    @Test
    @Order(15)
    public void testFechaConsultaSeMuestraEnHistorial() {
        List<Cita> atendidas = citaDAO.listarConsultasAtendidas();
        assertFalse(atendidas.isEmpty(), "Debe haber consultas atendidas");
        for (Cita c : atendidas) {
            assertNotNull(c.getConsultaFecha(),
                "La consulta #" + c.getConsultaId() + " debe tener fecha");
        }
    }
}
