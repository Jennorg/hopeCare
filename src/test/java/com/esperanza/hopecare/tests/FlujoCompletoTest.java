package com.esperanza.hopecare.tests;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.util.InicializarBD;
import com.esperanza.hopecare.util.Hasher;
import com.esperanza.hopecare.dao.CitaDAO;
import com.esperanza.hopecare.dao.ConsultaDAO;
import com.esperanza.hopecare.dao.MedicoDAO;
import com.esperanza.hopecare.dao.PacienteDAO;
import com.esperanza.hopecare.dao.FacturaConsultaDAO;
import com.esperanza.hopecare.model.Cita;
import com.esperanza.hopecare.model.Consulta;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoCompletoTest {

    private static CitaDAO citaDAO;
    private static ConsultaDAO consultaDAO;
    private static MedicoDAO medicoDAO;
    private static PacienteDAO pacienteDAO;
    private static FacturaConsultaDAO facturaConsultaDAO;

    private static int idMedico;
    private static int idPaciente;
    private static int idCita;
    private static int idConsulta;

    @BeforeAll
    public static void setUp() throws SQLException {
        citaDAO = new CitaDAO();
        consultaDAO = new ConsultaDAO();
        medicoDAO = new MedicoDAO();
        pacienteDAO = new PacienteDAO();
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
        // Clinica: especialidad, personas, medico, paciente
        try (Connection conn = DatabaseConnection.getClinicaConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps;

                ps = conn.prepareStatement("INSERT OR IGNORE INTO especialidad (id_especialidad, nombre_especialidad) VALUES (1, 'Medicina General')");
                ps.executeUpdate();

                ps = conn.prepareStatement("INSERT INTO persona (nombre, apellido, documento_identidad) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, "Carlos");
                ps.setString(2, "Médico");
                ps.setString(3, "MED-TEST-001");
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys(); rs.next();
                int idPersonaMedico = rs.getInt(1);

                ps = conn.prepareStatement("INSERT INTO persona (nombre, apellido, documento_identidad) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, "Laura");
                ps.setString(2, "Paciente");
                ps.setString(3, "PAC-TEST-001");
                ps.executeUpdate();
                rs = ps.getGeneratedKeys(); rs.next();
                int idPersonaPaciente = rs.getInt(1);

                ps = conn.prepareStatement("INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta, activo) VALUES (?, ?, ?, ?, 1)", Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, idPersonaMedico);
                ps.setInt(2, 1);
                ps.setString(3, "RM-TEST-001");
                ps.setDouble(4, 75000.0);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys(); rs.next();
                idMedico = rs.getInt(1);

                ps = conn.prepareStatement("INSERT INTO paciente (id_persona, historia_clinica) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, idPersonaPaciente);
                ps.setString(2, "HC-TEST-001");
                ps.executeUpdate();
                rs = ps.getGeneratedKeys(); rs.next();
                idPaciente = rs.getInt(1);

                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        }

        // Auth: rol, usuario
        try (Connection conn = DatabaseConnection.getAuthConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps;

                ps = conn.prepareStatement("INSERT OR IGNORE INTO rol (id_rol, nombre_rol) VALUES (1, 'MEDICO')");
                ps.executeUpdate();

                ps = conn.prepareStatement("INSERT OR IGNORE INTO rol (id_rol, nombre_rol) VALUES (2, 'ADMIN')");
                ps.executeUpdate();

                ps = conn.prepareStatement("INSERT OR IGNORE INTO rol (id_rol, nombre_rol) VALUES (3, 'RECEPCIONISTA')");
                ps.executeUpdate();

                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        }

        // Citas: horario_atencion
        try (Connection conn = DatabaseConnection.getCitasUnifiedConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo) VALUES (?, ?, ?, ?, ?, 1)");
                for (int dia = 1; dia <= 5; dia++) {
                    ps.setInt(1, idMedico);
                    ps.setInt(2, dia);
                    ps.setString(3, "08:00");
                    ps.setString(4, "12:00");
                    ps.setInt(5, 30);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        }
    }

    @Test
    @Order(1)
    public void testPrecioMedicoDirecto() {
        double precio = medicoDAO.obtenerPrecioConsulta(idMedico);
        assertEquals(75000.0, precio, 0.001, "El precio_consulta del medico debe ser 75000");
    }

    @Test
    @Order(2)
    public void testCrearCita() {
        Cita cita = new Cita(idPaciente, idMedico,
            LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)),
            "PROGRAMADA");
        cita.setCreadaPor(1);
        boolean ok = citaDAO.insertarCita(cita);
        assertTrue(ok, "La cita debe crearse exitosamente");
        idCita = cita.getIdCita();
        assertTrue(idCita > 0, "La cita debe tener un ID valido");
    }

    @Test
    @Order(3)
    public void testPrecioSeMuestraEnListadoSinConsulta() {
        List<Cita> citas = citaDAO.listarTodasConNombres();
        boolean encontrada = false;
        for (Cita c : citas) {
            if (c.getIdCita() == idCita) {
                encontrada = true;
                assertEquals(75000.0, c.getPrecio(), 0.001,
                    "El precio debe mostrar el precio_consulta del medico (75000) aunque no haya consulta");
                break;
            }
        }
        assertTrue(encontrada, "La cita debe aparecer en el listado");
    }

    @Test
    @Order(4)
    public void testMedicoVeSoloSusCitas() {
        List<Cita> citasMedico = citaDAO.listarPorMedicoConNombres(idMedico);
        boolean todasPertenecientes = citasMedico.stream().allMatch(c -> c.getIdMedico() == idMedico);
        assertTrue(todasPertenecientes, "El medico solo debe ver sus propias citas");

        int otroMedicoFalso = idMedico + 999;
        List<Cita> citasOtro = citaDAO.listarPorMedicoConNombres(otroMedicoFalso);
        assertTrue(citasOtro.isEmpty(), "Un medico inexistente no debe tener citas");
    }

    @Test
    @Order(5)
    public void testCrearConsulta() {
        Consulta consulta = new Consulta(idCita, "Diagnostico de prueba", "Dolor de cabeza", "Reposo", 75000.0);
        consulta.setFechaConsulta(LocalDateTime.now());
        int id = consultaDAO.insertarConsultaYActualizarEstado(consulta);
        assertTrue(id > 0, "La consulta debe crearse exitosamente");
        idConsulta = id;

        Cita citaActualizada = citaDAO.obtenerCitasPorEstado("ATENDIDA").stream()
            .filter(c -> c.getIdCita() == idCita)
            .findFirst().orElse(null);
        assertNotNull(citaActualizada, "La cita debe estar en estado ATENDIDA");
        assertEquals("ATENDIDA", citaActualizada.getEstado());
    }

    @Test
    @Order(6)
    public void testFacturacionDetectaConsultaPendiente() {
        List<PendienteDTO> pendientes = facturaConsultaDAO.listarPendientesConPaciente();
        boolean encontrada = false;
        for (PendienteDTO p : pendientes) {
            if (p.getIdReferencia() == idConsulta) {
                encontrada = true;
                assertEquals(75000.0, p.getMonto(), 0.001,
                    "El monto pendiente debe coincidir con el precio de la consulta");
                assertEquals("CONSULTA", p.getTipoReferencia(),
                    "El tipo de referencia debe ser CONSULTA");
                break;
            }
        }
        assertTrue(encontrada, "La consulta debe aparecer como pendiente de facturacion");
    }

    @Test
    @Order(7)
    public void testMarcarFacturado() {
        boolean ok = facturaConsultaDAO.marcarFacturado(idConsulta);
        assertTrue(ok, "La consulta debe marcarse como facturada");

        List<PendienteDTO> pendientes = facturaConsultaDAO.listarPendientesConPaciente();
        boolean aparece = pendientes.stream().anyMatch(p -> p.getIdReferencia() == idConsulta);
        assertFalse(aparece, "La consulta ya no debe aparecer como pendiente tras facturarse");
    }
}
